import json

def process_steam_data(input_filename, output_filename):
    # 1. Open and load the original JSON file
    with open(input_filename, 'r', encoding='utf-8') as infile:
        original_data = json.load(infile)

    transformed_data = []

    # 2. Loop through every game in the dictionary
    for app_id, game_data in original_data.items():
        
        # 3. Figure out the supported OS list
        supported_os = []
        if game_data.get("windows"):
            supported_os.append("windows")
        if game_data.get("mac"):
            supported_os.append("mac")
        if game_data.get("linux"):
            supported_os.append("linux")

        # 4. Process and flatten the tags into genres
        raw_tags = game_data.get("tags")
        flat_genres = []
        
        if isinstance(raw_tags, dict):
            # Extract keys if it's a dictionary: {"Action": 150} -> ["Action"]
            flat_genres = list(raw_tags.keys())
        elif isinstance(raw_tags, list) and len(raw_tags) > 0 and isinstance(raw_tags[0], dict):
            # Extract keys if it's a list of dictionaries
            for tag_dict in raw_tags:
                flat_genres.extend(tag_dict.keys())
        elif isinstance(raw_tags, list):
            # Fallback just in case it's already a flat list
            flat_genres = raw_tags

        # 5. Build the new document (Categories removed, tags mapped to genres)
        new_doc = {
            "app_id": app_id,
            "name": game_data.get("name"),
            "release_date": game_data.get("release_date"),
            "price": game_data.get("price"),
            "detailed_description": game_data.get("detailed_description"),
            "reviews":[],
            "header_image": game_data.get("header_image"),
            "supportedOS": supported_os,
            "achievements": game_data.get("achievements"),
            "developers": game_data.get("developers"),
            "publishers": game_data.get("publishers"),
            "genres": flat_genres
        }
        
        # Add to our new array
        transformed_data.append(new_doc)

    # 6. Save the array of documents to a new JSON file
    with open(output_filename, 'w', encoding='utf-8') as outfile:
        json.dump(transformed_data, outfile, indent=4)
        
    print(f"Successfully processed and flattened {len(transformed_data)} games in a single pass!")

if __name__ == "__main__":
    # Replace these with your actual file names!
    input_file = 'games.json'
    output_file = 'cleaned_games.json'
    
    # Run the unified function
    process_steam_data(input_file, output_file)
