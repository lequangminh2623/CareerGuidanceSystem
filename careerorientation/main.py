from fastapi import FastAPI
from pydantic import BaseModel
import pandas as pd
import joblib

# Load model & preprocessor
rf = joblib.load("models/random_forest.pkl")
preprocessor = joblib.load("models/preprocessor.pkl")

app = FastAPI()

# Input schema
class StudentInput(BaseModel):
    gender: bool
    part_time_job: bool
    absence_days: int
    extracurricular_activities: bool
    weekly_self_study_hours: float
    math_score: float
    history_score: float
    physics_score: float
    chemistry_score: float
    biology_score: float
    english_score: float
    geography_score: float

@app.post("/orientate")
def orientate(input_data: StudentInput):
    # Convert input -> DataFrame
    print("Received Input Data:", input_data)

    df = pd.DataFrame([input_data.model_dump()])

    # Preprocess
    X_processed = preprocessor.transform(df)
    # Predict class
    prediction = rf.predict(X_processed)[0]

    return {
        "career_orientation": prediction,
    }