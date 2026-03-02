import sys
import os
sys.path.append(os.path.join(os.path.dirname(__file__), 'faker-master'))
import json
import bcrypt
from faker import Faker

def generate_users(n):
    # Initialize Faker with English locale
    fake = Faker('en_US') 
    users = []

    for i in range(n):
        # 1. Generate a random plain text password
        plain_password = fake.password(length=12)
        
        # 2. Hash the password using bcrypt (includes automatic salting)
        hashed_password = bcrypt.hashpw(plain_password.encode('utf-8'), bcrypt.gensalt()).decode('utf-8')

        # 3. Build the user document
        user = {
            "username": fake.user_name(),
            "password": hashed_password,
            "email": fake.email(),
            "role": "USER",
            # Convert dates to ISO 8601 string format
            "birthDate": fake.date_of_birth(minimum_age=18, maximum_age=90).isoformat(),
            "creationDate": fake.date_time_this_decade().isoformat()
        }
        users.append(user)

        # 4. Print progress on the same line
        # \r moves the cursor back to the beginning of the line
        # flush=True forces the terminal to update immediately
        print(f"\rProgress: {i + 1}/{n} users generated...", end="", flush=True)

    # Print a new line once the loop is complete so the next message doesn't overwrite the progress
    print() 
    return users

def main():
    # Ask the user for the number N of documents to create
    try:
        n = int(input("Enter the number of users (N) to generate: "))
        if n <= 0:
            print("The number must be greater than zero.")
            return
    except ValueError:
        print("Error: Please enter a valid integer.")
        return

    print(f"Starting generation of {n} users. This might take a moment due to password hashing...")
    
    # Generate the mock data
    users_data = generate_users(n)

    # Write the data to users.json
    print("Saving to 'users.json'...")
    with open('users.json', 'w', encoding='utf-8') as f:
        # indent=4 makes the JSON file nicely formatted and readable
        json.dump(users_data, f, indent=4, ensure_ascii=False)

    print(f"Success! The file 'users.json' is ready.")

if __name__ == "__main__":
    main()
