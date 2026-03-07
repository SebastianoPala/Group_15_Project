import json
import random
import csv
import sys
import hashlib
import uuid
from datetime import datetime, timedelta
from faker import Faker
from neo4j import GraphDatabase
from pymongo import MongoClient
from bson import ObjectId

# --- CONFIGURATION ---
NEO4J_URI = "bolt://localhost:7687"
NEO4J_USER = "neo4j"
NEO4J_PASSWORD = "00000000"  # Updated password
MONGO_URI = "mongodb://localhost:27017/"
MONGO_DB_NAME = "PlayerHive"

NUM_USERS = 10000            # Number of users to generate
OUTPUT_GAMES = "final_games.json"
OUTPUT_USERS = "final_users.json"

fake = Faker()

# Remove CSV field size limits to prevent errors with large text blocks
maxInt = sys.maxsize
while True:
    try:
        csv.field_size_limit(maxInt)
        break
    except OverflowError:
        maxInt = int(maxInt/10)

# --- UTILITY FUNCTIONS ---
def generate_oid():
    """Simulates a MongoDB ObjectId (24 hex characters)"""
    return uuid.uuid4().hex[:24]

def generate_random_date():
    """Generates a random timestamp between Jan 1, 2023 and today"""
    start_date = datetime(2023, 1, 1)
    end_date = datetime.now()
    delta = end_date - start_date
    random_seconds = random.randrange(int(delta.total_seconds()))
    return start_date + timedelta(seconds=random_seconds)

def generate_recent_timestamp():
    """Generates an ISO timestamp not older than 1 month"""
    end_date = datetime.now()
    start_date = end_date - timedelta(days=30)
    return fake.date_time_between(start_date=start_date, end_date=end_date).isoformat()

# --- DATABASE FUNCTIONS ---
def check_neo4j_connection():
    """Verifies Neo4j credentials before starting the heavy processing."""
    print("Checking Neo4j connection...")
    try:
        driver = GraphDatabase.driver(NEO4J_URI, auth=(NEO4J_USER, NEO4J_PASSWORD))
        driver.verify_connectivity()
        driver.close()
        print(" -> Neo4j authentication successful!\n")
    except Exception as e:
        print(f" -> ERROR: Failed to connect to Neo4j. Check credentials or ensure the server is running.\n{e}")
        sys.exit(1)

def clear_neo4j(session):
    print("   -> Dropping the existing Neo4j database (DETACH DELETE n)...")
    session.run("MATCH (n) DETACH DELETE n")

def manage_neo4j_indexes(session, action="CREATE"):
    if action == "CREATE":
        print("   -> Creating indexes on User.id and Game.id...")
        session.run("CREATE INDEX user_id_idx IF NOT EXISTS FOR (u:User) ON (u.id)")
        session.run("CREATE INDEX game_id_idx IF NOT EXISTS FOR (g:Game) ON (g.id)")
        session.run("CALL db.awaitIndexes()")
    elif action == "DROP":
        print("   -> Dropping temporary indexes...")
        session.run("DROP INDEX user_id_idx IF EXISTS")
        session.run("DROP INDEX game_id_idx IF EXISTS")

def upload_to_mongodb(games_data, users_data):
    """Uploads the finalized in-memory dictionaries directly to MongoDB."""
    print(f"\n8. Connecting to MongoDB ({MONGO_URI})...")
    client = MongoClient(MONGO_URI)
    db = client[MONGO_DB_NAME]
    
    print(f"   -> Dropping existing '{MONGO_DB_NAME}.games' and '{MONGO_DB_NAME}.users' collections...")
    db.games.drop()
    db.users.drop()

    print("   -> Formatting ObjectIds for MongoDB insertion...")
    # MongoDB requires native ObjectId types, not the JSON {"$oid": "..."} string format
    # We create a shallow copy to modify the IDs for Mongo insertion
    mongo_games = []
    for g in games_data:
        g_copy = g.copy()
        g_copy["_id"] = ObjectId(g["_id"]["$oid"])
        mongo_games.append(g_copy)
        
    mongo_users = []
    for u in users_data:
        u_copy = u.copy()
        u_copy["_id"] = ObjectId(u["_id"]["$oid"])
        mongo_users.append(u_copy)

    print("   -> Inserting Game documents into MongoDB...")
    db.games.insert_many(mongo_games)
    
    print("   -> Inserting User documents into MongoDB...")
    db.users.insert_many(mongo_users)
    print("   -> MongoDB upload complete!")
    client.close()


# --- MAIN PIPELINE ---
def main():
    print("=== STARTING MASTER PIPELINE ===\n")
    
    # 0. CHECK CONNECTIONS
    check_neo4j_connection()
    
    # 1. CLEAN GAMES (convert.py logic)
    print("1. Loading and cleaning Games data (from games.json)...")
    try:
        with open('games.json', 'r', encoding='utf-8') as f:
            original_games = json.load(f)
    except FileNotFoundError:
        print("ERROR: 'games.json' not found in the directory.")
        sys.exit(1)
        
    games_list = []
    for app_id, game_data in original_games.items():
        supported_os = [os for os in ["windows", "mac", "linux"] if game_data.get(os)]
        raw_tags = game_data.get("tags")
        flat_genres = []
        if isinstance(raw_tags, dict): 
            flat_genres = list(raw_tags.keys())
        elif isinstance(raw_tags, list) and len(raw_tags) > 0 and isinstance(raw_tags[0], dict):
            for tag_dict in raw_tags: 
                flat_genres.extend(tag_dict.keys())
        elif isinstance(raw_tags, list): 
            flat_genres = raw_tags
        
        games_list.append({
            "_id": {"$oid": generate_oid()},
            "app_id": app_id,
            "name": game_data.get("name", ""),
            "release_date": game_data.get("release_date"),
            "price": game_data.get("price"),
            "detailed_description": game_data.get("detailed_description"),
            "reviews": [],
            "header_image": game_data.get("header_image"),
            "supportedOS": supported_os,
            "achievements": game_data.get("achievements", 0),
            "developers": game_data.get("developers", []),
            "publishers": game_data.get("publishers", []),
            "genres": flat_genres
        })

    # 2. GENERATE USERS
    print(f"\n2. Generating {NUM_USERS} mock Users in memory...")
    users_list = []
    for _ in range(NUM_USERS):
        username = fake.unique.user_name()
        users_list.append({
            "_id": {"$oid": generate_oid()},
            "username": username,
            "password": hashlib.sha256(username.encode('utf-8')).hexdigest(),
            "email": fake.unique.email(),
            "birthdate": fake.date_of_birth(minimum_age=15).isoformat(),
            "role": "USER",
            "friendRequests": [],
            "friends": 0,
            "numGames": 0,
            "hoursPlayed": 0,
            "pfpURL": f"/Playerhive/pfp/{uuid.uuid4().hex}"
        })

    # 3. ASSIGN REVIEWS
    print("\n3. Extracting texts from 'reviews.csv' and generating user reviews...")
    review_texts = []
    try:
        with open('reviews.csv', 'r', encoding='utf-8') as f:
            for row in csv.DictReader(f):
                if 'review_text' in row and row['review_text'].strip():
                    review_texts.append(row['review_text'])
    except FileNotFoundError:
        print("ERROR: 'reviews.csv' not found.")
        sys.exit(1)
        
    for user in users_list:
        M = random.randint(0, 20)
        for _ in range(M):
            game = random.choice(games_list)
            game["reviews"].append({
                "user_id": user['_id']['$oid'],
                "username": user['username'],
                "review_text": random.choice(review_texts),
                "score": round(random.uniform(0.0, 10.0), 1),
                "timestamp": generate_random_date().isoformat()
            })
            
    # Sort reviews chronologically
    for game in games_list:
        game["reviews"].sort(key=lambda x: x["timestamp"])

    # 4. GAME PLAY HISTORY
    print("\n4. Generating play history metrics and [:PLAYED] relationships...")
    played_relationships = []
    for user in users_list:
        M = min(random.randint(0, 15), len(games_list))
        selected_games = random.sample(games_list, M)
        total_hours = 0.0
        
        for game in selected_games:
            hours = round(random.uniform(0.1, 500.0), 1)
            total_hours += hours
            achievements = random.randint(0, game.get("achievements", 0))
            played_relationships.append({
                "user_id": user["_id"]["$oid"], 
                "game_id": game["_id"]["$oid"],
                "hoursPlayed": hours, 
                "achievements": achievements
            })
            
        user["numGames"] = M
        user["hoursPlayed"] = round(total_hours, 1)

    # 5. SOCIAL GRAPH (FRIENDS)
    print("\n5. Generating Social Graph (Friend Requests & Mutual Friendships)...")
    user_map = {u["_id"]["$oid"]: u for u in users_list}
    all_user_ids = list(user_map.keys())
    established_friendships = set()
    pending_requests = set()

    for user in users_list:
        u_id = user["_id"]["$oid"]
        
        valid_cands = [oid for oid in all_user_ids if oid != u_id 
                       and frozenset([u_id, oid]) not in established_friendships 
                       and (u_id, oid) not in pending_requests 
                       and (oid, u_id) not in pending_requests]
        
        if not valid_cands: continue
        M = random.randint(0, min(10, len(valid_cands)))
        if M == 0: continue
            
        N = random.randint(0, M)
        selected_ids = random.sample(valid_cands, M)
        req_targets = selected_ids[:N]
        friend_targets = selected_ids[N:]

        # Create Friend Requests
        for t_id in req_targets:
            user_map[t_id]["friendRequests"].append({
                "user_id": u_id, "username": user["username"],
                "pfpURL": user["pfpURL"], "timestamp": generate_recent_timestamp()
            })
            pending_requests.add((u_id, t_id))

        # Create Mutual Friendships
        for t_id in friend_targets:
            established_friendships.add(frozenset([u_id, t_id]))
            # Increase the 'friends' count for both users in the JSON
            user_map[u_id]["friends"] += 1
            user_map[t_id]["friends"] += 1

    friend_rels = [{"id1": list(pair)[0], "id2": list(pair)[1]} for pair in established_friendships]

    # 6. NEO4J UPLOAD
    print("\n6. Connecting to Neo4j to upload Graph Database...")
    neo4j_games = [{"id": g["_id"]["$oid"], "name": g.get("name",""), "achievements": g.get("achievements",0), "header_image": g.get("header_image","")} for g in games_list]
    neo4j_users = [{"id": u["_id"]["$oid"], "username": u["username"], "pfpURL": u["pfpURL"]} for u in users_list]

    try:
        driver = GraphDatabase.driver(NEO4J_URI, auth=(NEO4J_USER, NEO4J_PASSWORD))
        with driver.session() as session:
            clear_neo4j(session)
            
            print("   -> Inserting Game Nodes...")
            session.run("UNWIND $games AS game CREATE (g:Game {id: game.id, name: game.name, achievements: game.achievements, header_image: game.header_image})", games=neo4j_games)
            
            print("   -> Inserting User Nodes...")
            session.run("UNWIND $users AS user CREATE (u:User {id: user.id, username: user.username, pfpURL: user.pfpURL})", users=neo4j_users)
            
            manage_neo4j_indexes(session, "CREATE")
            
            print(f"   -> Inserting {len(played_relationships)} [:PLAYED] relationships...")
            for i in range(0, len(played_relationships), 10000):
                session.run("UNWIND $rels AS rel MATCH (u:User {id: rel.user_id}) MATCH (g:Game {id: rel.game_id}) CREATE (u)-[:PLAYED {hoursPlayed: rel.hoursPlayed, achievements: rel.achievements}]->(g)", rels=played_relationships[i:i+10000])

            print(f"   -> Inserting {len(friend_rels)} mutual [:FRIENDS_WITH] relationship pairs...")
            for i in range(0, len(friend_rels), 5000):
                session.run("UNWIND $pairs AS pair MATCH (u1:User {id: pair.id1}) MATCH (u2:User {id: pair.id2}) MERGE (u1)-[:FRIENDS_WITH]->(u2) MERGE (u2)-[:FRIENDS_WITH]->(u1)", pairs=friend_rels[i:i+5000])

            manage_neo4j_indexes(session, "DROP")
            
        driver.close()
        print("   -> Neo4j upload completed successfully!")
    except Exception as e:
        print(f"   -> ERROR during Neo4j operations: {e}")

    # 7. SAVE JSON FILES
    print(f"\n7. Saving final in-memory data to '{OUTPUT_GAMES}' and '{OUTPUT_USERS}'...")
    with open(OUTPUT_GAMES, 'w', encoding='utf-8') as f:
        json.dump(games_list, f, indent=4)
    with open(OUTPUT_USERS, 'w', encoding='utf-8') as f:
        json.dump(users_list, f, indent=4)

    # 8. MONGODB UPLOAD
    upload_to_mongodb(games_list, users_list)

    print("\n=== PIPELINE FINISHED SUCCESSFULLY! ===")

if __name__ == "__main__":
    main()
