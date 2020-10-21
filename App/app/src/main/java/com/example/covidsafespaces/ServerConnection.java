package com.example.covidsafespaces;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Looper;
import android.provider.SyncStateContract;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

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
    //private final String serverURL = "http://paeaccenture.pagekite.me/";
    private final String serverURL = "http://98972deb0cc1.eu.ngrok.io/";
    //private final String serverURL = "http://192.168.1.202:5000/";


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
                        Log.i("prueba", e.toString());
                        Log.i("prueba", "Ha fallado la conexión");
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        //Log.i("prueba", response.body().string());
                        Log.i("prueba", "Imagen guardada en el servidor");
                    }
                });
            }
        }).start();
    }

    public void getCapacity(final String capacity, final Listener listener){

        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(serverURL+capacity).get().build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        call.cancel();
                        Log.i("prueba", "Ha fallado la conexión");
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String data = response.body().string();
                        Log.i("prueba", data);
                        try {
                            listener.receiveMessage(new JSONObject(data));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }).start();
    }

}
