import json
import hashlib
import uuid
from faker import Faker

def generate_mock_data():
    fake = Faker()
    users = []
    
    # Generate 5000 user documents
    for _ in range(10000):
        username = fake.unique.user_name()
        
        # Hashing the username with 1 iteration (SHA-256)
        password_hash = hashlib.sha256(username.encode('utf-8')).hexdigest()
        
        user_doc = {
            "username": username,
            "password": password_hash,
            "email": fake.unique.email(),
            "birthdate": fake.date_of_birth(minimum_age=15).isoformat(),
            "role": "USER",
            "friendRequests": [],
            "friends":0,
            "numGames": 0,
            "hoursPlayed": 0,
            "pfpURL": f"/Playerhive/pfp/{uuid.uuid4().hex}"
        }
        users.append(user_doc)

    # Write the data to a JSON file
    with open("users.json", "w", encoding="utf-8") as f:
        json.dump(users, f, indent=4)

    print("Successfully generated users.json with 10000 user documents.")

if __name__ == "__main__":
    generate_mock_data()
