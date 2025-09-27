# file: server.py
# This server provides two APIs to run AI analysis against data using the API - ``/analyze``,
# and to receive encrypted data using the API - ``/upload``.

import os
import base64
from flask import Flask, request, jsonify
from fake_ai_model import analyze_content

app = Flask(__name__)

# --- Configuration ---
# Create a directory to store uploaded files.
UPLOAD_FOLDER = 'uploaded_files'
if not os.path.exists(UPLOAD_FOLDER):
    os.makedirs(UPLOAD_FOLDER)

# --- API Endpoints ---

@app.route('/analyze', methods=['POST'])
def analyze_file():
    """
    AI analysis endpoint.
    Receives file content as a Base64 encoded string, decodes it,
    and returns an AI-generated analysis.
    """
    print("--- Received request for /analyze ---")
    data = request.json
    if not data or 'file_content_b64' not in data:
        return jsonify({"error": "Missing file_content_b64"}), 400

    try:
        # Decode the Base64 string to bytes, then to a UTF-8 string.
        file_content_bytes = base64.b64decode(data['file_content_b64'])
        file_content_str = file_content_bytes.decode('utf-8', errors='ignore')

        # Get analysis from our fake AI model.
        analysis_result = analyze_content(file_content_str)
        
        print("--- Analysis successful. Sending back results. ---")
        return jsonify(analysis_result)

    except Exception as e:
        print(f"Error during analysis: {e}")
        return jsonify({"error": str(e)}), 500


@app.route('/upload', methods=['POST'])
def upload_file():
    """
    File upload endpoint.
    Receives the encrypted file (Base64), a filename, and AI metadata.
    It saves the encrypted file to the server.
    """
    print("--- Received request for /upload ---")
    data = request.json
    if not data or 'encrypted_file_b64' not in data or 'filename' not in data:
        return jsonify({"error": "Missing required fields"}), 400

    try:
        filename = data['filename']
        encrypted_file_b64 = data['encrypted_file_b64']
        ai_metadata = data.get('ai_metadata', {}) # .get is safer

        # Define the path to save the encrypted file.
        # We add '.enc' to denote it's an encrypted file.
        save_path = os.path.join(UPLOAD_FOLDER, f"{filename}.enc")

        # Decode the Base64 string and save the encrypted bytes to a file.
        with open(save_path, "wb") as f:
            f.write(base64.b64decode(encrypted_file_b64))

        print(f"--- Encrypted file '{filename}.enc' saved successfully. ---")
        print(f"--- Associated AI Metadata: {ai_metadata} ---")
        
        return jsonify({
            "status": "success",
            "message": f"File '{filename}' uploaded securely.",
            "saved_path": save_path
        })

    except Exception as e:
        print(f"Error during upload: {e}")
        return jsonify({"error": str(e)}), 500


# --- Main execution ---
if __name__ == '__main__':
    # Run the app. Use host='0.0.0.0' to make it accessible from your local network (e.g., your Android device).
    app.run(host='0.0.0.0', port=5000, debug=True)
