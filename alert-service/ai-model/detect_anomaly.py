import sys
import joblib
import numpy as np

# Load trained model
model = joblib.load("model.pkl")

# Ensure correct number of inputs
if len(sys.argv) != 4:
    print("Error: Expected 3 arguments (response_time, latency, error_rate)")
    sys.exit(1)

# Get features from command-line args
response_time = float(sys.argv[1])
latency = float(sys.argv[2])
error_rate = float(sys.argv[3])

# Predict if it's an anomaly
prediction = model.predict(np.array([[response_time, latency, error_rate]]))

if prediction[0] == -1:
    print("anomaly")
else:
    print("normal")
