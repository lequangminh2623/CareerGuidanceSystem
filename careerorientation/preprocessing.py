import pandas as pd
from sklearn.preprocessing import StandardScaler

num_columns = [
    "absence_days",
    "weekly_self_study_hours",
    "math_score",
    "history_score",
    "physics_score",
    "chemistry_score",
    "biology_score",
    "english_score",
    "geography_score"
]

score_columns = [
    "math_score",
    "history_score",
    "physics_score",
    "chemistry_score",
    "biology_score",
    "english_score",
    "geography_score"
]

class Preprocessor:
    def __init__(self):
        self.scaler = StandardScaler()
        self._is_fitted = False

    def fit(self, df: pd.DataFrame):
        df = self._convert_bool(df)
        df = self._scale_scores_to_10(df)  # chỉ scale thang 10 lúc train
        self.scaler.fit(df[num_columns])
        self._is_fitted = True
        return self

    def fit_transform(self, df: pd.DataFrame) -> pd.DataFrame:
        self.fit(df)
        df = self._convert_bool(df)
        df = self._scale_scores_to_10(df)
        df[num_columns] = self.scaler.transform(df[num_columns])
        return df

    def transform(self, df: pd.DataFrame) -> pd.DataFrame:
        if not self._is_fitted:
            raise ValueError("Preprocessor chưa fit. Phải gọi fit() trước khi transform().")
        df = self._convert_bool(df)
        # Không scale thang 10 nữa, dữ liệu đã chuẩn thang 10 từ API
        df[num_columns] = self.scaler.transform(df[num_columns])
        return df

    def _convert_bool(self, df: pd.DataFrame) -> pd.DataFrame:
        df_processed = df.copy()
        if "gender" in df_processed.columns:
            df_processed["gender"] = df_processed["gender"].map({"male": 1, "female": 0})
        for col in ["part_time_job", "extracurricular_activities"]:
            if col in df_processed.columns:
                df_processed[col] = df_processed[col].astype(int)
        return df_processed

    def _scale_scores_to_10(self, df: pd.DataFrame) -> pd.DataFrame:
        df_scaled = df.copy()
        for col in score_columns:
            if col in df_scaled.columns:
                df_scaled[col] = df_scaled[col] / 10.0
        return df_scaled
