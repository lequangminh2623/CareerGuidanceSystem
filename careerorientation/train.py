import pandas as pd
import joblib
from sklearn.ensemble import RandomForestClassifier
from preprocessing import Preprocessor

# 1. Load dataset
df = pd.read_csv("dataset/student-scores-6k.csv")

# 2. Split features / labels
X = df.drop(columns=["id", "first_name", "last_name", "email", "career_aspiration"])
y = df["career_aspiration"]

# 3. Preprocess
preprocessor = Preprocessor()
X_processed = preprocessor.fit_transform(X)  # scale thang 10 + Z-score

# 4. Train model trên toàn bộ dữ liệu
rf = RandomForestClassifier(n_estimators=200, random_state=42)
rf.fit(X_processed, y)

# 5. Save model + preprocessor
joblib.dump(rf, "models/random_forest.pkl")
joblib.dump(preprocessor, "models/preprocessor.pkl")

print("✅ Training completed. Model and Preprocessor saved in /models")