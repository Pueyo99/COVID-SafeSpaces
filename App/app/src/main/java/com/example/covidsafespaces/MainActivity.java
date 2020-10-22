package com.example.covidsafespaces;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements Listener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void send(View v){
        TextView ubication = findViewById(R.id.ubication);
        TextView surface = findViewById(R.id.surface);
        TextView height = findViewById(R.id.height);
        TextView people = findViewById(R.id.people);
        TextView ventilation = findViewById(R.id.ventilation);


        new ServerConnection().getCapacity(surface.getText().toString(), this);
        Log.i("received", "conexion creada");
    }

    @Override
    public void receiveMessage(JSONObject data) {
        try {
            String capacity = data.getString("max_cap");
            showCapacity(capacity);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void showCapacity(String capacity){
        LayoutInflater inflater = LayoutInflater.from(this);
        final View v = inflater.inflate(R.layout.show_capacity, null,false);

        ((TextView )v.findViewById(R.id.capacidad)).setText("This room can accommodate "+capacity+" people");

        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;

        final PopupWindow popupWindow = new PopupWindow(v,width,height,true);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                popupWindow.showAtLocation(v, Gravity.CENTER, 0 ,0);
            }
        });

    }

}