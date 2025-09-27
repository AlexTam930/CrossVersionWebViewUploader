// file: AiProcessor.java
package com.alextam.uploadimage;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.io.IOException;

/**
* This file is one of the logics that is reponsible to interace with Python back end.
* 
**/
public class AiProcessor {

    private static final String TAG = "AiProcessor";
    // IMPORTANT: Replace with your computer's IP address on the local network.
    private static final String BASE_URL = "http://192.168.1.100:5000"; 
    private static final String ANALYZE_URL = BASE_URL + "/analyze";
    private static final String UPLOAD_URL = BASE_URL + "/upload";

    private static final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public interface AiProcessorCallback {
        void onSuccess(String message);
        void onFailure(String errorMessage);
    }

    public static void processAndUploadFile(Context context, Uri fileUri, String fileName, AiProcessorCallback callback) {
        new Thread(() -> {
            try {
                // Step 1: Read file content into a byte array from Uri.
                byte[] fileBytes = readFileBytes(context, fileUri);
                if (fileBytes == null) {
                    postResult(() -> callback.onFailure("Failed to read file."));
                    return;
                }

                // Step 2: Send file content to AI for analysis.
                analyzeFile(fileBytes, (aiMetadata) -> {
                    if (aiMetadata == null) {
                        postResult(() -> callback.onFailure("AI analysis failed."));
                        return;
                    }
                    
                    try {
                        // Step 3: Encrypt the file locally.
                        Log.d(TAG, "Encrypting file...");
                        byte[] encryptedBytes = CryptoUtil.encrypt(fileBytes);
                        Log.d(TAG, "Encryption successful.");
                        
                        // Step 4: Upload the encrypted file and AI metadata.
                        uploadFile(encryptedBytes, fileName, aiMetadata, callback);

                    } catch (Exception e) {
                        Log.e(TAG, "Encryption failed.", e);
                        postResult(() -> callback.onFailure("File encryption failed: " + e.getMessage()));
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "File processing failed.", e);
                postResult(() -> callback.onFailure("File processing failed: " + e.getMessage()));
            }
        }).start();
    }

    private static byte[] readFileBytes(Context context, Uri uri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private static void analyzeFile(byte[] fileBytes, AnalysisCallback callback) {
        try {
            Log.d(TAG, "Sending file for AI analysis...");
            String fileContentB64 = Base64.encodeToString(fileBytes, Base64.NO_WRAP);
            JSONObject json = new JSONObject();
            json.put("file_content_b64", fileContentB64);

            RequestBody body = RequestBody.create(json.toString(), JSON);
            Request request = new Request.Builder().url(ANALYZE_URL).post(body).build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "AI analysis request failed.", e);
                    callback.onComplete(null);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            String responseBody = response.body().string();
                            Log.d(TAG, "AI analysis successful. Response: " + responseBody);
                            callback.onComplete(new JSONObject(responseBody));
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to parse AI response.", e);
                            callback.onComplete(null);
                        }
                    } else {
                        Log.e(TAG, "AI analysis failed with code: " + response.code());
                        callback.onComplete(null);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Failed to build analysis request.", e);
            callback.onComplete(null);
        }
    }

    private static void uploadFile(byte[] encryptedBytes, String fileName, JSONObject aiMetadata, AiProcessorCallback callback) {
        try {
            Log.d(TAG, "Uploading encrypted file...");
            String encryptedFileB64 = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP);
            JSONObject json = new JSONObject();
            json.put("filename", fileName);
            json.put("encrypted_file_b64", encryptedFileB64);
            json.put("ai_metadata", aiMetadata);

            RequestBody body = RequestBody.create(json.toString(), JSON);
            Request request = new Request.Builder().url(UPLOAD_URL).post(body).build();

            client.newCall(request).enqueue(new Callback() {
                 @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Upload request failed.", e);
                    postResult(() -> callback.onFailure("Upload failed: " + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Upload successful.");
                        postResult(() -> callback.onSuccess("File uploaded securely with AI analysis."));
                    } else {
                        Log.e(TAG, "Upload failed with code: " + response.code());
                        postResult(() -> callback.onFailure("Upload failed with server error: " + response.code()));
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Failed to build upload request.", e);
            postResult(() -> callback.onFailure("Upload failed: " + e.getMessage()));
        }
    }
    
    private interface AnalysisCallback {
        void onComplete(JSONObject aiMetadata);
    }
    
    private static void postResult(Runnable action) {
        new Handler(Looper.getMainLooper()).post(action);
    }
}
