import json
import random
from neo4j import GraphDatabase

# Neo4j Connection Settings
URI = "bolt://localhost:7687"
USER = "neo4j"
PASSWORD = "00000000"

def create_played_relationships(tx, relationships):
    """Uses UNWIND to batch create the [:PLAYED] relationships."""
    query = """
    UNWIND $rels AS rel
    MATCH (u:User {id: rel.user_id})
    MATCH (g:Game {id: rel.game_id})
    CREATE (u)-[:PLAYED {
        hoursPlayed: rel.hoursPlayed, 
        achievements: rel.achievements
    }]->(g)
    """
    tx.run(query, rels=relationships)

def main():
    print("Loading JSON files...")
    
    # 1. Load the Data
    try:
        with open("PlayerHive.games.json", "r", encoding="utf-8") as f:
            games = json.load(f)
    except FileNotFoundError:
        print("Error: 'PlayerHive.games.json' not found.")
        return

    try:
        with open("PlayerHive.users.json", "r", encoding="utf-8") as f:
            users = json.load(f)
    except FileNotFoundError:
        print("Error: 'PlayerHive.users.json' not found.")
        return

    print("Data loaded! Generating play history and updating users...")
    
    relationships_batch = []
    total_users = len(users)

    # 2. Process each user
    for index, user in enumerate(users):
        print(f"Processing user {index + 1} of {total_users}...", end='\r')
        
        user_id = user["_id"]["$oid"]
        
        # Random number of games M between 0 and 15
        M = random.randint(0, 15)
        
        # Select M unique games to avoid duplicate relationships
        # random.sample throws an error if M > len(games), so we safeguard it
        M = min(M, len(games))
        selected_games = random.sample(games, M)
        
        total_hours_played = 0.0
        
        for game in selected_games:
            game_id = game["_id"]["$oid"]
            
            # Generate random hours (e.g., up to 500 hours) and round to 1 decimal
            hours = round(random.uniform(0.1, 500.0), 1)
            total_hours_played += hours
            
            # Generate random achievements unlocked based on the game's max
            max_achievements = game.get("achievements", 0)
            achievements_unlocked = random.randint(0, max_achievements)
            
            # Add to our batch list for Neo4j
            relationships_batch.append({
                "user_id": user_id,
                "game_id": game_id,
                "hoursPlayed": hours,
                "achievements": achievements_unlocked
            })
            
        # 3. Update the user's JSON fields
        user["numGames"] = M
        user["hoursPlayed"] = round(total_hours_played, 1)

    print(f"\nSuccessfully generated {len(relationships_batch)} unique relationships.")

    # 4. Save the updated users back to JSON
    output_filename = "updated_PlayerHive.users.json"
    print(f"Saving updated users to '{output_filename}'...")
    with open(output_filename, "w", encoding="utf-8") as f:
        json.dump(users, f, indent=4)

    # 5. Push the relationships to Neo4j
    print("Connecting to Neo4j to insert relationships...")
    try:
        driver = GraphDatabase.driver(URI, auth=(USER, PASSWORD))
        with driver.session() as session:
            session.execute_write(create_played_relationships, relationships_batch)
            
        driver.close()
        print("Successfully created all relationships in Neo4j!")
        
    except Exception as e:
        print(f"An error occurred with Neo4j: {e}")

if __name__ == "__main__":
    main()
