package com.example.covidsafespaces;

import android.util.Base64;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ServerConnection {

    private final String serverURL = "https://147.83.50.15:8999/";
    //private final String serverURL = "http://147.83.50.15:8999/";
    //private final String serverURL = "https://192.168.1.202:5000/";
    //private final String serverURL = "https://192.168.43.201:5000/";

    public void postImage(final byte[] image, final String filename, final int rotation, final String username, final String path,
                          final String building,final String room,final float area,final Listener listener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient.Builder().hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                }).readTimeout(180, TimeUnit.SECONDS).build();

                JSONObject data = new JSONObject();
                try {
                    data.put("image",Base64.encodeToString(image, Base64.DEFAULT));
                    data.put("filename", filename);
                    data.put("rotation", rotation);
                    data.put("username", username);
                    data.put("building",building);
                    data.put("room",room);
                    data.put("area", area);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                RequestBody body = RequestBody.create(data.toString(),MediaType.parse("application/json"));
                Request request = new Request.Builder().url(serverURL+path).post(body).build();

                Log.i("prueba", path);

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        call.cancel();
                        e.printStackTrace();
                        Log.i("prueba", "Ha fallado la conexión");
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        try {
                            JSONObject data = new JSONObject(response.body().string());
                            Log.i("prueba", data.toString());
                            data.put("function", path);
                            listener.receiveMessage(data);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //Log.i("prueba", response.body().string());
                        Log.i("prueba", "Imagen guardada en el servidor");
                    }
                });
            }
        }).start();
    }

    public void get(final String data, final Listener listener){

        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient.Builder().hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                }).build();
                Request request = new Request.Builder().url(serverURL+data).get().build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        call.cancel();
                        e.printStackTrace();
                        Log.i("prueba", "Ha fallado la conexión");
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String body = response.body().string();
                        Log.i("prueba", body);
                        try {
                            JSONObject data = new JSONObject(body);
                            data.put("function", "get");
                            listener.receiveMessage(data);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }).start();

    }

    public void login(final Listener listener, final String username){
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient.Builder().hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                }).build();

                HttpUrl.Builder urlBuilder = HttpUrl.parse(serverURL+"login").newBuilder();
                urlBuilder.addQueryParameter("username", username);

                Request request = new Request.Builder().url(urlBuilder.build()).get().build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        call.cancel();
                        e.printStackTrace();
                        Log.i("prueba", "Ha fallado la conexión");
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String body = response.body().string();
                        Log.i("prueba", body);
                        try {
                            JSONObject data = new JSONObject(body);
                            data.put("function", "login");
                            listener.receiveMessage(data);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }).start();
    }

    public void recover(final Listener listener,final String username){
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient.Builder().hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                }).build();

                HttpUrl.Builder urlBuilder = HttpUrl.parse(serverURL+"recover").newBuilder();
                urlBuilder.addQueryParameter("username", username);

                Request request = new Request.Builder().url(urlBuilder.build()).get().build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        call.cancel();
                        e.printStackTrace();
                        Log.i("prueba", "Ha fallado la conexión");
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String body = response.body().string();
                        Log.i("prueba", body);
                        try {
                            JSONObject data = new JSONObject(body);
                            data.put("function", "recover");
                            listener.receiveMessage(data);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }).start();
    }

    public void register(final String username, final String mail, final String password){
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient.Builder().hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                }).build();

                JSONObject data = new JSONObject();
                try {
                    data.put("username",username);
                    data.put("mail", mail);
                    data.put("password", password);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                RequestBody body = RequestBody.create(data.toString(),MediaType.parse("application/json"));
                Request request = new Request.Builder().url(serverURL+"register").post(body).build();

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
                        Log.i("prueba", "Registro realizado con éxito");
                    }
                });
            }
        }).start();
    }

    public void deleteAccount(final Listener listener, final String username){
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient.Builder().hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                }).build();

                HttpUrl.Builder urlBuilder = HttpUrl.parse(serverURL+"delete/user").newBuilder();
                urlBuilder.addQueryParameter("username", username);

                Request request = new Request.Builder().url(urlBuilder.build()).get().build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        call.cancel();
                        e.printStackTrace();
                        Log.i("prueba", "Ha fallado la conexión");
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String body = response.body().string();
                        Log.i("prueba", body);
                        try {
                            JSONObject data = new JSONObject(body);
                            data.put("function", "delete");
                            listener.receiveMessage(data);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }).start();
    }

    public void deleteMeasure(final Listener listener, final String username, final String building, final String room){
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient.Builder().hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                }).build();

                HttpUrl.Builder urlBuilder = HttpUrl.parse(serverURL+"delete/measure").newBuilder();
                urlBuilder.addQueryParameter("username", username);
                urlBuilder.addQueryParameter("building", building);
                urlBuilder.addQueryParameter("room", room);

                Request request = new Request.Builder().url(urlBuilder.build()).get().build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        call.cancel();
                        e.printStackTrace();
                        Log.i("prueba", "Ha fallado la conexión");
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String body = response.body().string();
                        Log.i("prueba", body);
                        try {
                            JSONObject data = new JSONObject(body);
                            data.put("function", "delete");
                            listener.receiveMessage(data);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }).start();
    }

    public void getBuilding(final SelectionListener listener, final String username){

        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient.Builder().hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                }).build();

                HttpUrl.Builder urlBuilder = HttpUrl.parse(serverURL+"building").newBuilder();
                urlBuilder.addQueryParameter("username", username);

                Request request = new Request.Builder().url(urlBuilder.build()).get().build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        call.cancel();
                        e.printStackTrace();
                        Log.i("prueba", "Ha fallado la conexión");
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String body = response.body().string();
                        Log.i("prueba", body);
                        try {
                            JSONArray data = new JSONArray(body);
                            listener.receiveMessage(data, "building");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }).start();
    }

    public void getRoom(final SelectionListener listener, final String building, final String username){
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient.Builder().hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                }).build();

                HttpUrl.Builder urlBuilder = HttpUrl.parse(serverURL+"room").newBuilder();
                urlBuilder.addQueryParameter("username", username);
                urlBuilder.addQueryParameter("building",building);

                Request request = new Request.Builder().url(urlBuilder.build()).get().build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        call.cancel();
                        e.printStackTrace();
                        Log.i("prueba", "Ha fallado la conexión");
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String body = response.body().string();
                        Log.i("prueba", body);
                        try {
                            JSONArray data = new JSONArray(body);
                            listener.receiveMessage(data, "room");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }).start();
    }

    public void getCapacity (final Listener listener, final String username, final String building, final String room){

        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient.Builder().hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                }).build();

                HttpUrl.Builder urlBuilder = HttpUrl.parse(serverURL+"capacity").newBuilder();
                urlBuilder.addQueryParameter("username", username);
                urlBuilder.addQueryParameter("building",building);
                urlBuilder.addQueryParameter("room", room);

                Request request = new Request.Builder().url(urlBuilder.build()).get().build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        call.cancel();
                        e.printStackTrace();
                        Log.i("prueba", "Ha fallado la conexión");
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String body = response.body().string();
                        Log.i("prueba", body);
                        try {
                            JSONObject data = new JSONObject(body);
                            data.put("function", "get");
                            listener.receiveMessage(data);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }).start();
    }

    public void insertCapacity(final String username, final String building, final String room,
                               final int capacity,final String shape){
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient.Builder().hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                }).build();

                JSONObject data = new JSONObject();
                try {
                    data.put("username",username);
                    data.put("building", building);
                    data.put("room", room);
                    data.put("capacity", capacity);
                    data.put("shape", shape);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                RequestBody body = RequestBody.create(data.toString(),MediaType.parse("application/json"));
                Request request = new Request.Builder().url(serverURL+"insert").post(body).build();

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
                        Log.i("prueba", "Medida insertada con éxito");
                    }
                });
            }
        }).start();
    }

    public void editCapacity(final String username, final String building, final String room,
                               final int capacity){
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient.Builder().hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                }).build();

                JSONObject data = new JSONObject();
                try {
                    data.put("username",username);
                    data.put("building", building);
                    data.put("room", room);
                    data.put("capacity", capacity);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                RequestBody body = RequestBody.create(data.toString(),MediaType.parse("application/json"));
                Request request = new Request.Builder().url(serverURL+"update/measure").post(body).build();

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
                        Log.i("prueba", "Medida actualizada con éxito");
                    }
                });
            }
        }).start();
    }

    public void getProfileInfo(final Listener listener, final String username){
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient.Builder().hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                }).build();

                HttpUrl.Builder urlBuilder = HttpUrl.parse(serverURL+"profile").newBuilder();
                urlBuilder.addQueryParameter("username", username);

                Request request = new Request.Builder().url(urlBuilder.build()).get().build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        call.cancel();
                        e.printStackTrace();
                        Log.i("prueba", "Ha fallado la conexión");
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String body = response.body().string();
                        Log.i("prueba", body);
                        try {
                            JSONObject data = new JSONObject(body);
                            data.put("function","profile");
                            listener.receiveMessage(data);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }).start();
    }

    public void updatePassword(final String username, final String password){
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient.Builder().hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                }).build();

                JSONObject data = new JSONObject();
                try {
                    data.put("username", username);
                    data.put("password", password);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                RequestBody body = RequestBody.create(data.toString(), MediaType.parse("application/json"));
                Request request = new Request.Builder().url(serverURL + "update").post(body).build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        call.cancel();
                        e.printStackTrace();
                        Log.i("prueba", "Ha fallado la conexión");
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        Log.i("prueba", "Contraseña actualizada");
                    }
                });
            }
        }).start();
    }




}
