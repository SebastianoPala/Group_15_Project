import json
import csv
import sys
from pymongo import MongoClient, UpdateOne

def elabora_dati_salva_ram():
    print("\n--- CONFIGURAZIONE INIZIALE ---")
    file_json = input("Inserisci il nome del file JSON dei giochi (es. games.json): ").strip()
    file_csv = input("Inserisci il nome del file CSV delle recensioni (es. reviews.csv): ").strip()
    
    uri_mongo = "mongodb://localhost:27017/"
    db_name = "PlayerHive"
    collection_name = "games"

    vettore_originale = [
        "name", "release_date", "required_age", "price", "dlc_count", 
        "detailed_description", "about_the_game", "short_description", 
        "reviews", "header_image", "website", "support_url", 
        "support_email", "windows", "mac", "linux", "metacritic_score", 
        "metacritic_url", "achievements", "recommendations", "notes", 
        "supported_languages", "full_audio_languages", "packages", 
        "developers", "publishers", "categories", "genres", 
        "screenshots", "movies", "user_score", "score_rank", 
        "positive", "negative", "estimated_owners", "average_playtime_forever", 
        "average_playtime_2weeks", "median_playtime_forever", 
        "median_playtime_2weeks", "discount", "peak_ccu", "tags"
    ]

    vettore_nuovo = [
        "name", "release_date", "required_age", "price", "", 
        "description", "", "", 
        "", "header_image", "website", "", 
        "", "", "", "", "", 
        "", "achievements", "", "", 
        "supported_languages", "full_audio_languages", "", 
        "developers", "publishers", "categories", "genres", 
        "", "", "", "", 
        "", "", "", "", 
        "", "", 
        "", "", "", ""
    ]

    # Limite di 10 Megabyte convertito in byte
    LIMITE_DIMENSIONE_BYTE = 10 * 1024 * 1024 

    print("\n--- INIZIO PROCESSO ---")
    
    client = MongoClient(uri_mongo)
    db = client[db_name]
    collection = db[collection_name]

    # ==========================================
    # FASE 1: Carica la base dei giochi e calcola le dimensioni
    # ==========================================
    print(f"1. Creazione base giochi da '{file_json}' su MongoDB...")
    giochi_batch = []
    
    # Dizionario per tracciare il peso in byte di ogni documento-gioco
    peso_giochi = {} 
    id_validi = set()
    
    try:
        with open(file_json, 'r', encoding='utf-8') as f:
            dati_json = json.load(f)
    except FileNotFoundError:
        print(f"ERRORE CRITICO: Il file '{file_json}' non esiste.")
        return
        
    for original_appid, game_data in dati_json.items():
        clean_game = {
            "_id": str(original_appid),
            "appid": str(original_appid),
            "reviews": []
        }
        
        # Filtra i campi
        for orig, nuovo in zip(vettore_originale, vettore_nuovo):
            if nuovo != "" and orig in game_data:
                clean_game[nuovo] = game_data[orig]
                
        giochi_batch.append(clean_game)
        app_id_str = str(original_appid)
        id_validi.add(app_id_str)
        
        # Calcola il peso iniziale in byte del documento (trasformato in stringa)
        peso_iniziale = len(json.dumps(clean_game).encode('utf-8'))
        peso_giochi[app_id_str] = peso_iniziale

    # Inserisce i giochi (ignora gli errori se i giochi sono già presenti)
    if giochi_batch:
        try:
            collection.insert_many(giochi_batch, ordered=False)
        except Exception:
            pass
    
    print("   -> Struttura giochi creata sul database.")
    
    # Puliamo la memoria
    del dati_json
    del giochi_batch

    # ==========================================
    # FASE 2: Streaming CSV e aggiunta recensioni (con limite 10MB)
    # ==========================================
    print(f"\n2. Lettura in streaming del CSV '{file_csv}' e caricamento recensioni...")
    batch_size = 10000
    operazioni_bulk = []
    
    contatore_inserite = 0
    contatore_scartate_per_spazio = 0
    giochi_saturi = set() # Set veloce per i giochi che hanno raggiunto i 10MB

    try:
        with open(file_csv, mode='r', encoding='utf-8-sig') as f_csv:
            reader = csv.DictReader(f_csv)
            colonna_id = 'app_id' if 'app_id' in reader.fieldnames else 'appid'

            for row in reader:
                app_id = str(row.get(colonna_id, '')).strip().strip('"').strip("'")
                testo = row.get('review_text', '')

                # Se l'ID è valido, c'è del testo, e IL GIOCO NON È GIA' PIENO
                if app_id in id_validi and app_id not in giochi_saturi and testo:
                    recensione_obj = {'message': testo}
                    
                    # Stimiamo il peso della recensione (+ qualche byte per le virgole e struttura BSON)
                    peso_recensione = len(json.dumps(recensione_obj).encode('utf-8')) + 10
                    
                    # Controllo del limite dei 10 MB
                    if peso_giochi[app_id] + peso_recensione > LIMITE_DIMENSIONE_BYTE:
                        giochi_saturi.add(app_id) # Marchia il gioco come saturo
                        contatore_scartate_per_spazio += 1
                        continue # Salta questa recensione e passa alla riga successiva
                    
                    # Se c'è spazio, aggiorniamo il peso e prepariamo l'inserimento
                    peso_giochi[app_id] += peso_recensione
                    operazioni_bulk.append(UpdateOne(
                        {'_id': app_id},
                        {'$push': {'reviews': recensione_obj}}
                    ))
                    contatore_inserite += 1

                elif app_id in giochi_saturi:
                    contatore_scartate_per_spazio += 1

                # Invia il blocco a MongoDB ogni 10.000 recensioni per salvare RAM
                if len(operazioni_bulk) >= batch_size:
                    collection.bulk_write(operazioni_bulk, ordered=False)
                    operazioni_bulk.clear()
                    print(f"   -> Progresso: {contatore_inserite} recensioni caricate... ({len(giochi_saturi)} giochi hanno raggiunto il limite di spazio)")

            # Carica le ultime recensioni rimaste in canna
            if operazioni_bulk:
                collection.bulk_write(operazioni_bulk, ordered=False)
                
    except FileNotFoundError:
        print(f"ERRORE CRITICO: Il file '{file_csv}' non esiste.")
        return
            
    print("\n--- RISULTATI FINALI ---")
    print(f"Recensioni agganciate con successo: {contatore_inserite}")
    print(f"Recensioni ignorate per limite dei 10MB: {contatore_scartate_per_spazio}")
    print(f"Giochi che hanno saturato lo spazio disponibile: {len(giochi_saturi)}")
    print("--- PROCESSO COMPLETATO ---")

if __name__ == "__main__":
    elabora_dati_salva_ram()
