import json
from neo4j import GraphDatabase

# Neo4j Connection Settings
URI = "bolt://localhost:7687"  # Update with your Neo4j URI
USER = "neo4j"                 # Update with your Neo4j username
PASSWORD = "00000000"          # Update with your Neo4j password

def clear_database(tx):
    """Deletes all nodes and relationships in the current database."""
    tx.run("MATCH (n) DETACH DELETE n")

def insert_games(tx, games_data):
    """Uses UNWIND to batch create Game nodes."""
    query = """
    UNWIND $games AS game
    CREATE (g:Game {
        id: game.id,
        name: game.name,
        achievements: game.achievements,
        header_image: game.header_image
    })
    """
    tx.run(query, games=games_data)

def insert_users(tx, users_data):
    """Uses UNWIND to batch create User nodes."""
    query = """
    UNWIND $users AS user
    CREATE (u:User {
        id: user.id,
        username: user.username,
        pfpURL: user.pfpURL
    })
    """
    tx.run(query, users=users_data)

def main():
    print("Loading JSON files...")
    
    # 1. Load and parse the Games data
    try:
        with open("PlayerHive.games.json", "r", encoding="utf-8") as f:
            raw_games = json.load(f)
            
        # Extract only the requested fields and flatten the MongoDB _id
        clean_games = []
        for g in raw_games:
            clean_games.append({
                "id": g["_id"]["$oid"],
                "name": g.get("name", "Unknown Game"),
                "achievements": g.get("achievements", 0),
                "header_image": g.get("header_image", "")
            })
    except FileNotFoundError:
        print("Error: 'PlayerHive.games.json' not found.")
        return

    # 2. Load and parse the Users data
    try:
        with open("PlayerHive.users.json", "r", encoding="utf-8") as f:
            raw_users = json.load(f)
            
        # Extract only the requested fields and flatten the MongoDB _id
        clean_users = []
        for u in raw_users:
            clean_users.append({
                "id": u["_id"]["$oid"],
                "username": u.get("username", "Unknown"),
                "pfpURL": u.get("pfpURL", "")
            })
    except FileNotFoundError:
        print("Error: 'PlayerHive.users.json' not found.")
        return

    print(f"Prepared {len(clean_games)} games and {len(clean_users)} users for insertion.")
    print("Connecting to Neo4j...")

    # 3. Connect to Neo4j and execute transactions
    try:
        driver = GraphDatabase.driver(URI, auth=(USER, PASSWORD))
        with driver.session() as session:
            print("Dropping existing database (DETACH DELETE)...")
            session.execute_write(clear_database)
            
            print("Inserting Game nodes...")
            session.execute_write(insert_games, clean_games)
            
            print("Inserting User nodes...")
            session.execute_write(insert_users, clean_users)
            
        driver.close()
        print("Successfully populated the Neo4j database!")
        
    except Exception as e:
        print(f"An error occurred with Neo4j: {e}")

if __name__ == "__main__":
    main()
