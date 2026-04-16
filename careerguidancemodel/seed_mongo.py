import os
import pandas as pd
from pymongo import MongoClient
from dotenv import load_dotenv

load_dotenv()

def seed_database():
    user = os.getenv("MONGO_ROOT_USER", "mongodb")
    password = os.getenv("MONGO_ROOT_PASSWORD", "mongodb")

    host = "mongodb"
    port = 27017
    
    uri = f"mongodb://{user}:{password}@{host}:{port}/?authSource=admin"
    
    try:
        client = MongoClient(uri)
        db = client["career_guidance"]

        # Store Admission Scores
        print("--- Seeding admission scores ---")
        csv_path = "./diem_chuan.csv"
        
        if os.path.exists(csv_path):
            df = pd.read_csv(csv_path)
            df = df.fillna("") # Replace NaNs with empty strings
            
            records = df.to_dict(orient="records")
            
            # Drops collection before inserting to prevent duplicate records
            db.admission_scores.drop()
            if records:
                db.admission_scores.insert_many(records)
                print(f"✓ Success: Inserted {len(records)} admission scores.")
            else:
                print("! Warning: CSV file is empty.")
        else:
            print(f"✘ Error: File not found at {os.path.abspath(csv_path)}")
            
    except Exception as e:
        print(f"✘ Error: Could not connect to MongoDB: {str(e)}")

if __name__ == "__main__":
    seed_database()
