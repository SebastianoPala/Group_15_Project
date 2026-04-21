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
    # MODIFICA: Rimosso .isoformat() per mantenere l'oggetto datetime puro per PyMongo
    return fake.date_time_between(start_date=start_date, end_date=end_date)

def format_to_localdate(date_raw):
    """Parses various date formats and converts them to YYYY-MM-DD for Java LocalDate."""
    if not date_raw:
        return None
    date_str = str(date_raw).strip()
    # Common formats found in game datasets
    formats = [
        "%Y-%m-%d", "%b %d, %Y", "%d %b, %Y", "%B %d, %Y", "%d %B, %Y", 
        "%Y/%m/%d", "%m/%d/%Y", "%d/%m/%Y"
    ]
    for fmt in formats:
        try:
            return datetime.strptime(date_str, fmt).strftime("%Y-%m-%d")
        except ValueError:
            continue
    # If it's just a year
    if len(date_str) == 4 and date_str.isdigit():
        return f"{date_str}-01-01"
    return date_str # Fallback se la data è già in un formato non riconosciuto

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
        # FIX: L'indice ora viene creato sulla proprietà 'id' per User
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
            
        if random.random() < 0.70:
            discount = 0
        else:
            discount = random.choice(range(0, 95, 5))
        
        games_list.append({
            "_id": {"$oid": generate_oid()},
            "name": game_data.get("name", ""),
            "release_date": format_to_localdate(game_data.get("release_date")),
            "price": game_data.get("price"),
            "discount": discount,
            "description": game_data.get("detailed_description"),
            "reviews": [],
            "image": game_data.get("header_image"),
            "supportedOS": supported_os,
            "achievements": game_data.get("achievements", 0),
            "developers": game_data.get("developers", []),
            "publishers": game_data.get("publishers", []),
            "genres": flat_genres
        })
    print(f"   -> Successfully loaded and cleaned {len(games_list)} games.")

    # 2. GENERATE USERS
    print(f"\n2. Generating {NUM_USERS} mock Users in memory...")
    users_list = []
    for i in range(NUM_USERS):
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
        if (i + 1) % 1000 == 0 or (i + 1) == NUM_USERS:
            print(f"   -> Generated {i + 1}/{NUM_USERS} users...", end='\r')
    print()

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
        
    for i, user in enumerate(users_list):
        M = random.randint(0, 20)
        for _ in range(M):
            game = random.choice(games_list)
            game["reviews"].append({
                "user_id": user['_id']['$oid'],
                "username": user['username'],
                "review_text": random.choice(review_texts),
                "score": round(random.uniform(0.0, 10.0), 1),
                # MODIFICA: Rimosso .isoformat() per lasciare l'oggetto datetime puro per PyMongo
                "timestamp": generate_random_date()
            })
            
        if (i + 1) % 1000 == 0 or (i + 1) == len(users_list):
            print(f"   -> Assigned reviews for {i + 1}/{len(users_list)} users...", end='\r')
    print()
            
    print("   -> Sorting reviews chronologically and calculating sumScore and countScore...")
    for i, game in enumerate(games_list):
        game["reviews"].sort(key=lambda x: x["timestamp"])
        
        if game["reviews"]:
            game["sumScore"] = round(sum(r["score"] for r in game["reviews"]), 1)
            game["countScore"] = len(game["reviews"])
        else:
            game["sumScore"] = 0.0
            game["countScore"] = 0
            
        if (i + 1) % 1000 == 0 or (i + 1) == len(games_list):
            print(f"   -> Processed scores for {i + 1}/{len(games_list)} games...", end='\r')
    print()

    # 4. GAME PLAY HISTORY
    print("\n4. Generating play history metrics and [:PLAYED] relationships...")
    played_relationships = []
    game_playtime_stats = {g["_id"]["$oid"]: {"total_hours": 0.0, "user_count": 0} for g in games_list}
    
    for i, user in enumerate(users_list):
        M = min(random.randint(0, 15), len(games_list))
        selected_games = random.sample(games_list, M)
        total_hours = 0.0
        
        for game in selected_games:
            hours = round(random.uniform(0.1, 500.0), 1)
            total_hours += hours
            achievements = random.randint(0, game.get("achievements", 0))
            
            game_id_str = game["_id"]["$oid"]
            played_relationships.append({
                "user_id": user["_id"]["$oid"], 
                "game_id": game_id_str, 
                "hoursPlayed": hours, 
                "achievements": achievements
            })
            
            game_playtime_stats[game_id_str]["total_hours"] += hours
            game_playtime_stats[game_id_str]["user_count"] += 1
            
        user["numGames"] = M
        user["hoursPlayed"] = round(total_hours, 1)
        
        if (i + 1) % 1000 == 0 or (i + 1) == len(users_list):
            print(f"   -> Generated play history for {i + 1}/{len(users_list)} users...", end='\r')
    print()

    print("   -> Calculating totalHoursPlayed and numPlayers for games...")
    for i, game in enumerate(games_list):
        stats = game_playtime_stats[game["_id"]["$oid"]]
        if stats["user_count"] > 0:
            game["totalHoursPlayed"] = round(stats["total_hours"], 1)
            game["numPlayers"] = stats["user_count"]
        else:
            game["totalHoursPlayed"] = 0.0
            game["numPlayers"] = 0
            
        if (i + 1) % 1000 == 0 or (i + 1) == len(games_list):
            print(f"   -> Processed playtime stats for {i + 1}/{len(games_list)} games...", end='\r')
    print()

    # 5. SOCIAL GRAPH (FRIENDS)
    print("\n5. Generating Social Graph (Friend Requests & Mutual Friendships)...")
    user_map = {u["_id"]["$oid"]: u for u in users_list}
    all_user_ids = list(user_map.keys())
    established_friendships = set()
    pending_requests = set()

    for i, user in enumerate(users_list):
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

        for t_id in req_targets:
            user_map[t_id]["friendRequests"].append({
                "user_id": u_id, "username": user["username"],
                "pfpURL": user["pfpURL"], "timestamp": generate_recent_timestamp()
            })
            pending_requests.add((u_id, t_id))

        for t_id in friend_targets:
            established_friendships.add(frozenset([u_id, t_id]))
            user_map[u_id]["friends"] += 1
            user_map[t_id]["friends"] += 1

        if (i + 1) % 1000 == 0 or (i + 1) == len(users_list):
            print(f"   -> Processed social graph for {i + 1}/{len(users_list)} users...", end='\r')
    print()

    friend_rels = [{"id1": list(pair)[0], "id2": list(pair)[1]} for pair in established_friendships]

    # 6. NEO4J UPLOAD
    print("\n6. Connecting to Neo4j to upload Graph Database...")
    neo4j_games = [{"id": g["_id"]["$oid"], "name": g.get("name",""), "achievements": g.get("achievements",0), "image": g.get("image","")} for g in games_list]
    
    # FIX: Rinominata la chiave da "user_id" a "id" in fase di generazione per Neo4j
    neo4j_users = [{"id": u["_id"]["$oid"], "username": u["username"], "pfpURL": u["pfpURL"]} for u in users_list]

    try:
        driver = GraphDatabase.driver(NEO4J_URI, auth=(NEO4J_USER, NEO4J_PASSWORD))
        with driver.session() as session:
            clear_neo4j(session)
            
            print("   -> Inserting Game Nodes...")
            session.run("UNWIND $games AS game CREATE (g:Game {id: game.id, name: game.name, achievements: game.achievements, image: game.image})", games=neo4j_games)
            
            print("   -> Inserting User Nodes...")
            # FIX: La query Cypher ora salva "id: user.id" al posto di "user_id: user.user_id"
            session.run("UNWIND $users AS user CREATE (u:User {id: user.id, username: user.username, pfpURL: user.pfpURL})", users=neo4j_users)
            
            manage_neo4j_indexes(session, "CREATE")
            
            print(f"   -> Inserting {len(played_relationships)} [:PLAYED] relationships...")
            for i in range(0, len(played_relationships), 10000):
                # FIX: MATCH cerca il campo 'id' nell'utente (u:User {id: rel.user_id})
                session.run("UNWIND $rels AS rel MATCH (u:User {id: rel.user_id}) MATCH (g:Game {id: rel.game_id}) CREATE (u)-[:PLAYED {hoursPlayed: rel.hoursPlayed, achievements: rel.achievements}]->(g)", rels=played_relationships[i:i+10000])

            print(f"   -> Inserting {len(friend_rels)} mutual [:FRIENDS_WITH] relationship pairs...")
            for i in range(0, len(friend_rels), 5000):
                # FIX: MATCH cerca il campo 'id' nell'utente (u1:User {id: pair.id1})
                session.run("UNWIND $pairs AS pair MATCH (u1:User {id: pair.id1}) MATCH (u2:User {id: pair.id2}) MERGE (u1)-[:FRIENDS_WITH]->(u2) MERGE (u2)-[:FRIENDS_WITH]->(u1)", pairs=friend_rels[i:i+5000])

            manage_neo4j_indexes(session, "DROP")
            
        driver.close()
        print("   -> Neo4j upload completed successfully!")
    except Exception as e:
        print(f"   -> ERROR during Neo4j operations: {e}")

    # 7. SAVE JSON FILES
    print(f"\n7. Saving final in-memory data to '{OUTPUT_GAMES}' and '{OUTPUT_USERS}'...")
    # MODIFICA: Aggiunto default=str in modo che il .json venga salvato regolarmente senza crashare a causa del formato datetime
    with open(OUTPUT_GAMES, 'w', encoding='utf-8') as f:
        json.dump(games_list, f, indent=4, default=str)
    with open(OUTPUT_USERS, 'w', encoding='utf-8') as f:
        json.dump(users_list, f, indent=4, default=str)

    # 8. MONGODB UPLOAD
    upload_to_mongodb(games_list, users_list)

    print("\n=== PIPELINE FINISHED SUCCESSFULLY! ===")

if __name__ == "__main__":
    main()
