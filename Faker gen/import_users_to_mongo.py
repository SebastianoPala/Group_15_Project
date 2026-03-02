import json
import sys
from pymongo import MongoClient
from pymongo.errors import ConnectionFailure, ServerSelectionTimeoutError

def main():
    # 1. Load the JSON data
    print("Loading data from 'users.json'...")
    try:
        with open('users.json', 'r', encoding='utf-8') as f:
            users_data = json.load(f)
    except FileNotFoundError:
        print("Error: 'users.json' not found. Please run the generation script first.")
        sys.exit(1) # Terminate the script

    # 2. Connect to MongoDB
    print("Connecting to MongoDB...")
    try:
        # serverSelectionTimeoutMS=5000 ensures the script fails after 5 seconds if MongoDB is down
        client = MongoClient("mongodb://localhost:27017/", serverSelectionTimeoutMS=5000)
        
        # The MongoClient constructor doesn't actually connect right away. 
        # We run an 'admin' command to force a connection and verify it works.
        client.admin.command('ping')
        print("Successfully connected to MongoDB!")
        
    except (ConnectionFailure, ServerSelectionTimeoutError) as e:
        print(f"Fatal Error: Could not connect to MongoDB. Make sure the database is running.\nDetails: {e}")
        sys.exit(1) # Terminate the script on connection failure

    # 3. Select Database and Collection
    # Note: "PlayerHive" and "Users" will be created automatically upon insertion.
    db = client["PlayerHive"]
    collection = db["Users"]

    # 4. Insert the data
    print(f"Inserting {len(users_data)} documents into the 'Users' collection of 'PlayerHive' DB...")
    try:
        # insert_many is highly efficient for inserting a list of documents
        result = collection.insert_many(users_data)
        print(f"Success! Inserted {len(result.inserted_ids)} documents.")
    except Exception as e:
        print(f"An error occurred during insertion: {e}")

if __name__ == "__main__":
    main()
