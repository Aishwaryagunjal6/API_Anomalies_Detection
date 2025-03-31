import pandas as pd
import joblib
from sklearn.ensemble import IsolationForest

# Load dataset
df = pd.read_csv("updated_network_performance_dataset.csv")

# Select relevant features (modify as per dataset structure)
X = df[['response_time', 'latency', 'error_rate']]

# Train isolation forest for anomaly detection
model = IsolationForest(n_estimators=100, contamination=0.05, random_state=42)
model.fit(X)

# Save the model
joblib.dump(model, "model.pkl")
print("Model trained and saved!")
