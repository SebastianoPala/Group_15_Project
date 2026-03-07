import json
import random
from neo4j import GraphDatabase

# Neo4j Connection Settings
URI = "bolt://localhost:7687"
USER = "neo4j"
PASSWORD = "00000000"

def clear_database(tx):
    tx.run("MATCH (n) DETACH DELETE n")

def insert_games(tx, games_data):
    tx.run("""
        UNWIND $games AS game
        CREATE (g:Game {
            id: game.id,
            name: game.name,
            achievements: game.achievements,
            header_image: game.header_image
        })
    """, games=games_data)

def insert_users(tx, users_data):
    tx.run("""
        UNWIND $users AS user
        CREATE (u:User {
            id: user.id,
            username: user.username,
            pfpURL: user.pfpURL
        })
    """, users=users_data)

def manage_indexes(session, action="CREATE"):
    """Creates or drops indexes on User.id and Game.id."""
    if action == "CREATE":
        print("Creating indexes on User.id and Game.id...")
        session.run("CREATE INDEX user_id_idx IF NOT EXISTS FOR (u:User) ON (u.id)")
        session.run("CREATE INDEX game_id_idx IF NOT EXISTS FOR (g:Game) ON (g.id)")
        # Crucial: Wait for the background index build to finish before continuing
        session.run("CALL db.awaitIndexes()")
        print("Indexes are online.")
    elif action == "DROP":
        print("Dropping indexes as requested...")
        session.run("DROP INDEX user_id_idx IF EXISTS")
        session.run("DROP INDEX game_id_idx IF EXISTS")
        print("Indexes dropped.")

def create_played_relationships(tx, relationships):
    tx.run("""
        UNWIND $rels AS rel
        MATCH (u:User {id: rel.user_id})
        MATCH (g:Game {id: rel.game_id})
        CREATE (u)-[:PLAYED {
            hoursPlayed: rel.hoursPlayed, 
            achievements: rel.achievements
        }]->(g)
    """, rels=relationships)

def main():
    print("1. Loading JSON files...")
    
    try:
        with open("PlayerHive.games.json", "r", encoding="utf-8") as f:
            raw_games = json.load(f)
            clean_games = [{"id": g["_id"]["$oid"], "name": g.get("name", ""), "achievements": g.get("achievements", 0), "header_image": g.get("header_image", "")} for g in raw_games]
    except Exception as e:
        print(f"Error loading games: {e}") 
        return

    try:
        with open("PlayerHive.users.json", "r", encoding="utf-8") as f:
            raw_users = json.load(f)
            clean_users = [{"id": u["_id"]["$oid"], "username": u.get("username", ""), "pfpURL": u.get("pfpURL", "")} for u in raw_users]
    except Exception as e:
        print(f"Error loading users: {e}") 
        return

    print("2. Generating play history data locally...")
    relationships_batch = []
    
    for u_idx, user in enumerate(raw_users):
        user_id = user["_id"]["$oid"]
        M = min(random.randint(0, 15), len(raw_games))
        selected_games = random.sample(raw_games, M)
        
        total_hours = 0.0
        for game in selected_games:
            game_id = game["_id"]["$oid"]
            hours = round(random.uniform(0.1, 500.0), 1)
            total_hours += hours
            achievements = random.randint(0, game.get("achievements", 0))
            
            relationships_batch.append({
                "user_id": user_id, "game_id": game_id, 
                "hoursPlayed": hours, "achievements": achievements
            })
            
        user["numGames"] = M
        user["hoursPlayed"] = round(total_hours, 1)

    print(f"   -> Prepared {len(relationships_batch)} relationships.")

    # Save updated users
    with open("updated_PlayerHive.users.json", "w", encoding="utf-8") as f:
        json.dump(raw_users, f, indent=4)
    print("   -> Saved updated user JSON.")

    print("\n3. Connecting to Neo4j to execute database operations...")
    try:
        driver = GraphDatabase.driver(URI, auth=(USER, PASSWORD))
        with driver.session() as session:
            print("   -> Dropping existing database...")
            session.execute_write(clear_database)
            
            print("   -> Inserting Game and User nodes...")
            session.execute_write(insert_games, clean_games)
            session.execute_write(insert_users, clean_users)
            
            # Add Index
            manage_indexes(session, action="CREATE")
            
            print("   -> Inserting relationships in chunks of 10,000...")
            chunk_size = 10000
            for i in range(0, len(relationships_batch), chunk_size):
                chunk = relationships_batch[i:i + chunk_size]
                session.execute_write(create_played_relationships, chunk)
                print(f"      - Inserted {min(i + chunk_size, len(relationships_batch))} / {len(relationships_batch)}", end='\r')
            print("\n   -> All relationships inserted successfully.")
            
            # Remove Index
            manage_indexes(session, action="DROP")
            
        driver.close()
        print("\nAll done! Neo4j database is fully populated and indexed cleaned.")
        
    except Exception as e:
        print(f"\nAn error occurred with Neo4j: {e}")

if __name__ == "__main__":
    main()
