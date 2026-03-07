import json
import random
from datetime import datetime, timedelta
from faker import Faker
from neo4j import GraphDatabase
from neo4j.exceptions import ServiceUnavailable

# --- Configuration ---
NEO4J_URI = "bolt://localhost:7687"
NEO4J_USER = "neo4j"
NEO4J_PASSWORD = "00000000"  # Update with your password
INPUT_JSON = "updated_PlayerHive.users.json"
OUTPUT_JSON = "final_users.json"
INDEX_NAME = "user_id_social_idx"

fake = Faker()

def generate_timestamp():
    """Generates an ISO timestamp not older than 1 month."""
    end_date = datetime.now()
    start_date = end_date - timedelta(days=30)
    return fake.date_time_between(start_date=start_date, end_date=end_date).isoformat()

# --- Neo4j Functions ---
class Neo4jSocialManager:
    def __init__(self, uri, user, password):
        try:
            self.driver = GraphDatabase.driver(uri, auth=(user, password))
        except ServiceUnavailable as e:
            print(f"Error connecting to Neo4j: {e}")
            exit(1)

    def close(self):
        self.driver.close()

    def create_index(self):
        print("Creating index on :User(id)...")
        with self.driver.session() as session:
            session.run(f"CREATE INDEX {INDEX_NAME} IF NOT EXISTS FOR (u:User) ON (u.id)")
            session.run("CALL db.awaitIndexes()")
            print("Index is online.")

    def drop_index(self):
        print("Dropping index on :User(id)...")
        with self.driver.session() as session:
            session.run(f"DROP INDEX {INDEX_NAME} IF EXISTS")
            print("Index dropped.")

    def insert_friendships(self, friendships_list):
        if not friendships_list:
            print("No friendships to insert into Neo4j.")
            return

        print(f"\nInserting {len(friendships_list)} mutual friendship pairs into Neo4j...")
        
        batch_size = 5000
        total = len(friendships_list)
        
        # EXPLICIT TWO-WAY RELATIONSHIP
        query = """
        UNWIND $pairs AS pair
        MATCH (u1:User {id: pair.id1})
        MATCH (u2:User {id: pair.id2})
        MERGE (u1)-[:FRIENDS_WITH]->(u2)
        MERGE (u2)-[:FRIENDS_WITH]->(u1)
        """

        with self.driver.session() as session:
            for i in range(0, total, batch_size):
                batch = friendships_list[i : i + batch_size]
                session.run(query, pairs=batch)
                print(f"   Inserted batch {min(i + batch_size, total)}/{total}", end='\r')
        print("\nNeo4j insertion complete.")

# --- Main Logic ---
def main():
    print(f"Loading {INPUT_JSON}...")
    try:
        with open(INPUT_JSON, "r", encoding="utf-8") as f:
            users = json.load(f)
    except FileNotFoundError:
        print(f"Error: {INPUT_JSON} not found.")
        return

    user_map_by_id = {u["_id"]["$oid"]: u for u in users}
    
    # Trackers
    established_friendships = set()
    pending_requests = set()

    for u in users:
        u["friendRequests"] = []

    neo4j_social_manager = Neo4jSocialManager(NEO4J_URI, NEO4J_USER, NEO4J_PASSWORD)
    neo4j_social_manager.create_index()

    print("\nStarting social data generation locally...")
    
    total_users = len(users)
    friendships_created_count = 0
    requests_sent_count = 0
    all_user_ids = list(user_map_by_id.keys())

    for i, current_user in enumerate(users):
        u_id = current_user["_id"]["$oid"]
        
        if i % 100 == 0 or i == total_users - 1:
            print(f"   Processed Users: {i+1}/{total_users} | Friendship pairs queued: {friendships_created_count}", end='\r')

        valid_candidates = []
        for other_id in all_user_ids:
            if other_id == u_id:
                continue
            pair = frozenset([u_id, other_id])
            if pair in established_friendships:
                continue
            if (u_id, other_id) in pending_requests or (other_id, u_id) in pending_requests:
                continue
            
            valid_candidates.append(other_id)

        num_valid = len(valid_candidates)
        if num_valid == 0:
            continue

        M = random.randint(0, min(5, num_valid))
        if M == 0:
            continue
            
        N = random.randint(0, M)
        
        selected_ids = random.sample(valid_candidates, M)
        request_targets = selected_ids[:N]
        friend_targets = selected_ids[N:]

        # Handle Requests
        for target_id in request_targets:
            target_user_dict = user_map_by_id[target_id]
            request_subdoc = {
                "user_id": u_id,
                "username": current_user["username"],
                "pfpURL": current_user["pfpURL"],
                "timestamp": generate_timestamp()
            }
            target_user_dict["friendRequests"].append(request_subdoc)
            pending_requests.add((u_id, target_id))
            requests_sent_count += 1

        # Handle Friendships
        for target_id in friend_targets:
            established_friendships.add(frozenset([u_id, target_id]))
            friendships_created_count += 1

    print(f"\n\nSocial generation complete.")
    print(f"   -> queued {len(established_friendships)} unique friendship pairs.")
    print(f"   -> added {requests_sent_count} friend requests to JSON.")

    print(f"\nSaving {OUTPUT_JSON}...")
    with open(OUTPUT_JSON, "w", encoding="utf-8") as f:
        json.dump(users, f, indent=4)

    neo4j_rels = []
    for pair in established_friendships:
        ids = list(pair)
        neo4j_rels.append({"id1": ids[0], "id2": ids[1]})
    
    neo4j_social_manager.insert_friendships(neo4j_rels)

    neo4j_social_manager.drop_index()
    neo4j_social_manager.close()

    print("\nAll done.")

if __name__ == "__main__":
    main()
