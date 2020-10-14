package com.example.covidsafespaces;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Looper;
import android.provider.SyncStateContract;
import android.util.Base64;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ServerConnection {
    private final String serverURL = "http://192.168.1.202:5000/";


    public void postImage(final byte[] image, final String filename, final int rotation){
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();

                JSONObject data = new JSONObject();
                try {
                    data.put("image",Base64.encodeToString(image, Base64.DEFAULT));
                    data.put("filename", filename);
                    data.put("rotation", rotation);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                RequestBody body = RequestBody.create(data.toString(),MediaType.parse("application/json"));
                Request request = new Request.Builder().url(serverURL+"image").post(body).build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        call.cancel();
                        Log.i("prueba", "Ha fallado la conexión");
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        Log.i("prueba", response.body().string());
                    }
                });
            }
        }).start();
    }

    public void getCapacity(final String capacity, final Listener listener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;

                try {
                    URL url = new URL(serverURL);
                    connection = (HttpURLConnection) new URL(serverURL+capacity).openConnection();
                    connection.connect();

                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuffer buffer = new StringBuffer();
                    String line = "";

                    while ((line = reader.readLine()) != null){
                        buffer.append(line);
                    }

                    reader.close();
                    connection.disconnect();
                    listener.receiveMessage(new JSONObject(buffer.toString()));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

}
