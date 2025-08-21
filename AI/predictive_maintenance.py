import os
import logging
import pickle
from datetime import datetime
import pandas as pd
import requests
from sklearn.linear_model import LogisticRegression
from sklearn.preprocessing import StandardScaler
from flask import Flask, request, jsonify

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)

# Model and scaler file paths
MODEL_PATH = 'maintenance_model.pkl'
SCALER_PATH = 'scaler.pkl'

# API endpoints
MAINTENANCE_API = 'http://localhost:8080/api/data/all-maintenance-records'
INCIDENT_API = 'http://localhost:8080/api/data/incidents'
NOTIFICATION_API = 'http://localhost:8080/api/data/notifications'

def fetch_data():
    """Fetch maintenance and incident data from Spring Boot APIs."""
    try:
        m_resp = requests.get(MAINTENANCE_API)
        m_resp.raise_for_status()
        maintenance_data = m_resp.json()
        logger.info(f"[fetch_data] Fetched maintenance data: {len(maintenance_data)} records")

        i_resp = requests.get(INCIDENT_API)
        i_resp.raise_for_status()
        incident_data = i_resp.json()
        logger.info(f"[fetch_data] Fetched incident data: {len(incident_data)} records")
        return maintenance_data, incident_data
    except requests.RequestException as e:
        logger.error(f"[fetch_data] Error fetching data: {e}", exc_info=True)
        return [], []

def preprocess_data(maintenance_data, incident_data):
    """Preprocess for model training."""
    try:
        df = pd.DataFrame(maintenance_data)
        if df.empty:
            raise ValueError("Maintenance data is empty.")
        
        logger.info(f"[preprocess_data] DataFrame columns: {list(df.columns)}")
        
        required_columns = ['equipmentId', 'startDate', 'endDate']
        missing_columns = [col for col in required_columns if col not in df.columns]
        if missing_columns:
            raise ValueError(f"Missing required columns: {missing_columns}")

        df['start_date'] = pd.to_datetime(df['startDate'])
        df['end_date'] = pd.to_datetime(df['endDate'], errors='coerce')

        idx = df.groupby('equipmentId')['start_date'].idxmax()
        recent = df.loc[idx].copy()

        recent['days_since_last'] = (datetime.now() - recent['start_date']).dt.days
        recent['duration'] = (recent['end_date'] - recent['start_date']).dt.days.fillna(0)

        if incident_data:
            incident_df = pd.DataFrame(incident_data)
            logger.info(f"[preprocess_data] Incident DataFrame columns: {list(incident_df.columns)}")
            if 'busId' in incident_df.columns:
                counts = incident_df.groupby('busId').size().rename('incident_count').reset_index()
                recent = recent.merge(counts, left_on='equipmentId', right_on='busId', how='left')
                recent['incident_count'] = recent['incident_count'].fillna(0)
            else:
                recent['incident_count'] = 0
        else:
            recent['incident_count'] = 0

        recent['needs_maintenance'] = ((recent['days_since_last'] > 5) | (recent['incident_count'] > 2)).astype(int)

        features = ['days_since_last', 'duration', 'incident_count']
        X = recent[features].fillna(0)
        y = recent['needs_maintenance']

        return X, y
    except Exception as e:
        logger.error(f"[preprocess_data] Error: {str(e)}", exc_info=True)
        raise

def train_model(X, y):
    """Train and persist model + scaler."""
    try:
        scaler = StandardScaler()
        X_scaled = scaler.fit_transform(X)

        model = LogisticRegression(random_state=42)
        model.fit(X_scaled, y)

        with open(MODEL_PATH, 'wb') as f:
            pickle.dump(model, f)
        with open(SCALER_PATH, 'wb') as f:
            pickle.dump(scaler, f)

        logger.info("[train_model] Model and scaler trained and saved.")
        return model, scaler
    except Exception as e:
        logger.error(f"[train_model] Error: {str(e)}", exc_info=True)
        raise

def load_model():
    """Load existing model/scaler or train new ones."""
    try:
        if os.path.exists(MODEL_PATH) and os.path.exists(SCALER_PATH):
            with open(MODEL_PATH, 'rb') as f:
                model = pickle.load(f)
            with open(SCALER_PATH, 'rb') as f:
                scaler = pickle.load(f)
            logger.info("[load_model] Loaded model and scaler from disk.")
        else:
            maintenance_data, incident_data = fetch_data()
            if not maintenance_data:
                raise RuntimeError("No data available to train model.")
            X, y = preprocess_data(maintenance_data, incident_data)
            model, scaler = train_model(X, y)
        return model, scaler
    except Exception as e:
        logger.error(f"[load_model] Error: {str(e)}", exc_info=True)
        raise

def prepare_input(equipment_id):
    """Fetch latest maintenance for a single equipment and build feature vector."""
    url = f"{MAINTENANCE_API}?equipmentId={equipment_id}"
    logger.info(f"[prepare_input] GET {url}")
    try:
        resp = requests.get(url)
        resp.raise_for_status()
        records = resp.json()
        logger.info(f"[prepare_input] Records for equipmentId {equipment_id}: {records}")

        # Check if response contains an error (e.g., from Spring Boot 404)
        if isinstance(records, list) and len(records) == 1 and 'error' in records[0]:
            error_message = records[0]['error']
            logger.info(f"[prepare_input] Error from Spring Boot: {error_message}")
            return None, error_message

        if not records:
            logger.info(f"[prepare_input] No records found for equipmentId {equipment_id}")
            return None, f"No maintenance records found for equipmentId {equipment_id}"

        df = pd.DataFrame(records)
        logger.info(f"[prepare_input] DataFrame columns: {list(df.columns)}")
        
        required_columns = ['equipmentId', 'startDate', 'endDate']
        missing_columns = [col for col in required_columns if col not in df.columns]
        if missing_columns:
            logger.error(f"[prepare_input] Missing required columns: {missing_columns}")
            return None, f"Invalid data format: missing columns {missing_columns}"

        df['start_date'] = pd.to_datetime(df['startDate'])
        df['end_date'] = pd.to_datetime(df['endDate'], errors='coerce')

        latest = df.loc[df['start_date'].idxmax()]
        days_since = (datetime.now() - latest['start_date']).days
        duration = (latest['end_date'] - latest['start_date']).days if pd.notnull(latest['end_date']) else 0
        incident_count = 0

        return [days_since, duration, incident_count], None
    except requests.RequestException as e:
        logger.error(f"[prepare_input] Error fetching for {equipment_id}: {e}", exc_info=True)
        return None, f"Error fetching data for equipmentId {equipment_id}: {str(e)}"
    except Exception as e:
        logger.error(f"[prepare_input] Error processing records for {equipment_id}: {str(e)}", exc_info=True)
        return None, f"Error processing data for equipmentId {equipment_id}: {str(e)}"

@app.route('/predict', methods=['POST'])
def predict():
    """Predict maintenance need and return probability."""
    raw = request.data.decode('utf-8')
    logger.info(f"[predict] Raw body: {raw}")

    data = request.get_json(silent=True)
    logger.info(f"[predict] Parsed JSON: {data}")

    if not isinstance(data, dict):
        logger.error(f"[predict] Invalid JSON payload: {raw}")
        return jsonify({'error': 'Invalid JSON payload', 'raw': raw}), 400

    equipment_id = data.get('equipmentId')
    if equipment_id is None:
        logger.error(f"[predict] equipmentId missing, received keys: {list(data.keys()) if data else []}")
        return jsonify({
            'error': 'equipmentId is required',
            'received_keys': list(data.keys()) if data else [],
            'raw': raw
        }), 400

    try:
        model, scaler = load_model()
        features, error = prepare_input(equipment_id)
        if features is None:
            logger.error(f"[predict] Failed to prepare input data for equipmentId: {equipment_id}: {error}")
            return jsonify({'error': error}), 404 if 'No maintenance records found' in error else 500

        X = scaler.transform([features])
        pred = model.predict(X)[0]
        prob = model.predict_proba(X)[0][1]

        result = {
            'equipmentId': str(equipment_id),
            'needsMaintenance': bool(pred),
            'probability': float(prob)
        }

        if pred == 1:
            notification = {
                'equipmentId': str(equipment_id),
                'message': f"Equipment {equipment_id} requires maintenance.",
                'timestamp': datetime.now().isoformat()
            }
            try:
                resp = requests.post(NOTIFICATION_API, json=notification)
                resp.raise_for_status()
                logger.info(f"[predict] Notification sent for equipmentId: {equipment_id}")
            except requests.RequestException as e:
                logger.error(f"[predict] Notification failed: {e}", exc_info=True)

        logger.info(f"[predict] Response: {result}")
        return jsonify(result)
    except Exception as e:
        logger.error(f"[predict] Prediction error: {str(e)}", exc_info=True)
        return jsonify({'error': 'Internal server error', 'detail': str(e)}), 500

if __name__ == '__main__':
    try:
        load_model()
    except Exception as e:
        logger.exception(f"[__main__] Initialization error: {e}")
        raise

    try:
        from gunicorn.app.base import BaseApplication

        class FlaskApplication(BaseApplication):
            def __init__(self, app, options=None):
                self.application = app
                self.options = options or {}
                super().__init__()

            def load_config(self):
                for k, v in self.options.items():
                    self.cfg.set(k, v)

            def load(self):
                return self.application

        FlaskApplication(app, {'bind': '0.0.0.0:5000', 'workers': 4, 'timeout': 120}).run()
    except ImportError:
        app.run(host='0.0.0.0', port=5000, threaded=True)