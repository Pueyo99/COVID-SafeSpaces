package com.example.covidsafespaces;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Selection extends AppCompatActivity implements SelectionListener, Listener, NavigationView.OnNavigationItemSelectedListener {

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private NavigationView mNavigationView;

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

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mDrawerLayout = findViewById(R.id.drawer_layout);

        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open,
                R.string.drawe_close);
        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);
        mActionBarDrawerToggle.syncState();

        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

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
                        }
                    });
                    new ServerConnection().getRoom(Selection.this,selectedBuilding, username);
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

        new ServerConnection().getBuilding(this, username);
        setProgressDialog();
    }

    @Override
    public void onBackPressed() {
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else{
            moveTaskToBack(true);
        }
    }

    @Override
    public void receiveMessage(final JSONArray data, final String path) {
        alertDialog.dismiss();
        switch (path){
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
        new ServerConnection().getCapacity(this,username, selectedBuilding,selectedRoom);

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


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_home:

                break;
            case R.id.nav_picture:

                break;
            case R.id.nav_AR:

                break;
            case R.id.nav_profile:
                //Toast.makeText(this, "Navigate to profile", Toast.LENGTH_LONG).show();
                mDrawerLayout.closeDrawer(GravityCompat.START);
                Intent i = new Intent(this, Profile.class);
                i.putExtra("username", username);
                startActivity(i);
                break;
            case R.id.nav_contact:

                break;
            case R.id.nav_help:

                break;
            case R.id.nav_logout:
                startActivity(new Intent(this, Login.class));
                break;
            case R.id.nav_exit:
                moveTaskToBack(true);
                break;
        }

        return true;
    }
}