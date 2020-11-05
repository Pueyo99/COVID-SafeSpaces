package com.example.covidsafespaces;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Selection extends AppCompatActivity implements SelectionListener, Listener {

    private Spinner buildings;
    private Adapter buildingAdapter;
    private AlertDialog alertDialog;
    private Spinner rooms;
    private Adapter roomAdapter;
    private String selectedBuilding;
    private String selectedRoom;
    private String username;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        Bundle datos = getIntent().getExtras();
        username = datos.getString("username");

        selectedBuilding = "--Select building--";
        selectedRoom = "--Select room--";

        buildings = findViewById(R.id.buildings);
        buildingAdapter = new Adapter(this, R.layout.item, new ArrayList<String>());
        buildingAdapter.add("--Select building--");
        buildings.setAdapter(buildingAdapter);

        buildings.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedBuilding = ((TextView)view.findViewById(R.id.spinnerItem)).getText().toString();
                if(!selectedBuilding.equals("--Select building--")){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Selection.this, selectedBuilding, Toast.LENGTH_LONG).show();
                            setProgressDialog();
                            new ServerConnection().getBuilding(Selection.this, "room/"+selectedBuilding);
                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        rooms = findViewById(R.id.rooms);
        roomAdapter = new Adapter(this, R.layout.item, new ArrayList<String>());
        roomAdapter.add("--Select room--");
        rooms.setAdapter(roomAdapter);

        rooms.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedRoom = ((TextView) view.findViewById(R.id.spinnerItem)).getText().toString();
                if(!selectedRoom.equals("--Select room--") && !selectedBuilding.equals("--Select building--")){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Selection.this, selectedRoom, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        new ServerConnection().getBuilding(this, "building");
        setProgressDialog();



    }

    @Override
    public void receiveMessage(final JSONArray data, String path) {
        alertDialog.dismiss();
        switch (path.split("/")[0]){
            case "building":
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for(int i=0; i<data.length(); i++){
                            try {
                                buildingAdapter.add(data.getJSONObject(i).getString("BUILDING"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                break;
            case "room":
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        roomAdapter.clear();
                        roomAdapter.add("--Select room--");
                        for(int i=0; i<data.length(); i++){
                            try {
                                roomAdapter.add(data.getJSONObject(i).getString("ROOM"));
                            }catch (JSONException e){
                                e.printStackTrace();
                            }
                        }
                    }
                });
                break;
        }
    }

    @Override
    public void receiveMessage(JSONObject data) {
        try {
            String capacity = data.getString("CAPACITY");
            showCapacity(capacity);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getCapacity(View v){
        String path = "capacity/"+selectedBuilding+"/"+selectedRoom;
        new ServerConnection().getCapacity(this,path);

    }

    public void showCapacity(String capacity){
        LayoutInflater inflater = LayoutInflater.from(this);
        final View v = inflater.inflate(R.layout.show_capacity, null,false);

        ((TextView)v.findViewById(R.id.capacidad)).setText("This room can accommodate "+capacity+" people");

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

    public void newMeasure(View v){
        TextView newBuilding = findViewById(R.id.newBuilding);
        TextView newRoom = findViewById(R.id.newRoom);
        Intent i = new Intent(this, Main.class);
        i.putExtra("username",username);
        startActivity(i);
    }

    public void setProgressDialog() {

        LayoutInflater inflater = LayoutInflater.from(Selection.this);
        View v = inflater.inflate(R.layout.alert_dialog, null, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setView(v);

        alertDialog = builder.create();
        alertDialog.show();
        Window window = alertDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(alertDialog.getWindow().getAttributes());
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            alertDialog.getWindow().setAttributes(layoutParams);
        }
    }


}