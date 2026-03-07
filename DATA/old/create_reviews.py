import json
import random
import csv
import sys
from datetime import datetime, timedelta

# Remove CSV field size limits to prevent errors on massive text blocks
maxInt = sys.maxsize
while True:
    try:
        csv.field_size_limit(maxInt)
        break
    except OverflowError:
        maxInt = int(maxInt/10)

def generate_random_date():
    """Generates a random timestamp between Jan 1, 2023 and today."""
    start_date = datetime(2023, 1, 1)
    end_date = datetime.now()
    delta = end_date - start_date
    random_seconds = random.randrange(int(delta.total_seconds()))
    return start_date + timedelta(seconds=random_seconds)

def embed_reviews_in_games():
    print("Loading data files...")
    
    try:
        with open('cleaned_games.json', 'r', encoding='utf-8') as f:
            games = json.load(f)
    except FileNotFoundError:
        print("Error: 'cleaned_games.json' not found.")
        return

    try:
        with open('PlayerHive.users.json', 'r', encoding='utf-8') as f:
            users = json.load(f)
    except FileNotFoundError:
        print("Error: 'PlayerHive.users.json' not found.")
        return

    review_texts = []
    try:
        with open('reviews.csv', 'r', encoding='utf-8') as f:
            reader = csv.DictReader(f)
            for row in reader:
                if 'review_text' in row and row['review_text'].strip():
                    review_texts.append(row['review_text'])
    except FileNotFoundError:
        print("Error: 'reviews.csv' not found.")
        return

    if not review_texts:
        print("Error: No reviews found in the CSV.")
        return

    print("Data loaded! Embedding mock reviews into games...")
    total_users = len(users)

    # 1. Process users and assign reviews
    for index, user in enumerate(users):
        print(f"Processing user {index + 1} of {total_users}...", end='\r')
        
        user_id = user['_id']['$oid']
        username = user['username']
        M = random.randint(0, 20)
        
        for _ in range(M):
            review_text = random.choice(review_texts)
            game = random.choice(games)
            score = round(random.uniform(0.0, 10.0), 1)
            timestamp = generate_random_date().isoformat()
            
            review_doc = {
                "user_id": user_id,
                "username": username,
                "review_text": review_text,
                "score": score,
                "timestamp": timestamp
            }
            
            if "reviews" not in game:
                game["reviews"] = []
            
            game["reviews"].append(review_doc)

    print(f"\nSuccessfully processed all {total_users} users.")
    print("Sorting reviews chronologically for each game...")

    # 2. Sort the reviews in each game by timestamp (oldest first)
    for game in games:
        if "reviews" in game:
            game["reviews"].sort(key=lambda x: x["timestamp"])

    # 3. Save the updated games list
    output_filename = 'updated_games_with_reviews.json'
    print("Saving data to file... This might take a moment.")
    with open(output_filename, 'w', encoding='utf-8') as f:
        json.dump(games, f, indent=4)
        
    print(f"Done! Updated games saved to '{output_filename}'.")

if __name__ == "__main__":
    embed_reviews_in_games()
