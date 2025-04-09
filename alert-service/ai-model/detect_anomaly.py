# -*- coding: utf-8 -*-
import sys
import pandas as pd
import numpy as np
import joblib
from sklearn.impute import SimpleImputer
from sklearn.preprocessing import StandardScaler, OneHotEncoder
from sklearn.compose import ColumnTransformer
from sklearn.pipeline import Pipeline
from sklearn.ensemble import IsolationForest

# --------------------------
# Preprocessing & Model Training Logic
# --------------------------

def load_and_preprocess_data(filepath):
    # Load dataset
    df = pd.read_csv(filepath)
    
    # Handle missing values
    num_cols = df.select_dtypes(include=['int64', 'float64']).columns
    imputer = SimpleImputer(strategy="median")
    df[num_cols] = imputer.fit_transform(df[num_cols])
    
    # Remove outliers for key features
    def remove_outliers(df, column, factor=1.5):
        Q1 = df[column].quantile(0.25)
        Q3 = df[column].quantile(0.75)
        IQR = Q3 - Q1
        lower_bound = Q1 - factor * IQR
        upper_bound = Q3 + factor * IQR
        return df[(df[column] >= lower_bound) & (df[column] <= upper_bound)]
    
    df = remove_outliers(df, "request_response_time_ms")
    
    # Feature engineering
    df["time_of_day"] = df["failure_patterns_time_of_day"].apply(
        lambda hour: "Morning" if 5 <= hour < 12 else
                     "Afternoon" if 12 <= hour < 17 else
                     "Evening" if 17 <= hour < 21 else "Night"
    )
    df["load_per_connection"] = df["server_load_pct"] / df["num_concurrent_connections"]
    
    return df

# Define preprocessing pipeline
numerical_features = [
    "request_response_time_ms", "ttfb_ms", "connection_establishment_time_ms",
    "packet_loss_rate_pct", "network_jitter_ms", "bandwidth_utilization_pct",
    "num_concurrent_connections", "server_response_time_ms", "request_distribution_pct",
    "server_health_metric", "server_load_pct", "queue_length", "processing_time_ms",
    "error_rate_per_server", "api_availability_pct", "error_rates_by_endpoint",
    "recovery_time_sec", "num_timeouts", "circuit_breaker_activation_freq",
    "retry_success_rate_pct", "load_per_connection"
]
categorical_features = ["time_of_day"]

preprocessor = ColumnTransformer(
    transformers=[
        ('num', StandardScaler(), numerical_features),
        ('cat', OneHotEncoder(), categorical_features)
    ]
)

# Full pipeline including preprocessing and model
full_pipeline = Pipeline([
    ('preprocessor', preprocessor),
    ('model', IsolationForest(contamination=0.01, random_state=42))
])

# --------------------------
# Prediction Logic
# --------------------------

if __name__ == "__main__":
    # If training mode (example: python script.py --train)
    if "--train" in sys.argv:
        # Load and preprocess data
        df = load_and_preprocess_data("network_performance_dataset.csv")
        
        # Train model
        full_pipeline.fit(df)
        
        # Save pipeline (includes preprocessing and model)
        joblib.dump(full_pipeline, "trained_pipeline.pkl")
        print("Model trained and saved successfully.")
    
    # Prediction mode (example: python script.py <response_time> <latency> ...)
    else:
        # Load saved pipeline
        try:
            pipeline = joblib.load("trained_pipeline.pkl")
        except FileNotFoundError:
            print("Error: Model not found. Train first with --train")
            sys.exit(1)
        
        # Validate input (ensure all 20 numerical features are provided)
        if len(sys.argv) != len(numerical_features) + 1:
            print(f"Error: Expected {len(numerical_features)} numerical features")
            sys.exit(1)
        
        # Parse input features
        input_features = [float(arg) for arg in sys.argv[1:]]
        input_df = pd.DataFrame([input_features], columns=numerical_features)
        
        # Predict
        prediction = pipeline.predict(input_df)
        
        print("anomaly" if prediction[0] == -1 else "normal")
