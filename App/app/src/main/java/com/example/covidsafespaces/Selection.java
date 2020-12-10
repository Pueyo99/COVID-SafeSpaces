package com.example.covidsafespaces;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import data.DBAccess;

public class Selection extends AppCompatActivity implements SelectionListener, Listener, NavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private final int REQUEST_GPS_LOCATION = 3;

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private NavigationView mNavigationView;

    private FusedLocationProviderClient mFusedLocationClient;
    private String countryName;

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

        countryName = null;

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mDrawerLayout = findViewById(R.id.drawer_layout);

        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open,
                R.string.drawe_close);
        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);
        mActionBarDrawerToggle.syncState();

        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        selectedBuilding = getResources().getString(R.string.selectBuilding);
        selectedRoom = getResources().getString(R.string.selectRoom);

        buildings = findViewById(R.id.buildings);
        buildingAdapter = new Adapter(this, R.layout.item, new ArrayList<String>());
        buildingAdapter.add(getResources().getString(R.string.selectBuilding));
        buildings.setAdapter(buildingAdapter);

        buildings.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedBuilding = ((TextView)view.findViewById(R.id.spinnerItem)).getText().toString();
                if(!selectedBuilding.equals(getResources().getString(R.string.selectBuilding))){
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
        roomAdapter.add(getResources().getString(R.string.selectRoom));
        rooms.setAdapter(roomAdapter);

        rooms.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedRoom = ((TextView) view.findViewById(R.id.spinnerItem)).getText().toString();
                if(!selectedRoom.equals(getResources().getString(R.string.selectRoom)) && !selectedBuilding.equals(getResources().getString(R.string.selectBuilding))){
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
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestUbicationPermission();
            return;
        }

        if(countryName == null){
            if(!isLocationEnabled()){
                showSettingsAlert();
                return;
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    getLocation();
                }
            }).start();
        }


        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            requestCameraPermission();
            return;
        }
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
                        roomAdapter.add(getResources().getString(R.string.selectRoom));
                        for(int i=0; i<data.length(); i++){
                            try {
                                roomAdapter.add(data.getJSONObject(i).getString("ROOM"));
                            }catch (JSONException e){
                                e.printStackTrace();
                            }
                        }
                        rooms.setSelection(roomAdapter.getItemIndex(getResources().getString(R.string.selectRoom)));
                    }
                });
                break;
        }
    }

    @Override
    public void receiveMessage(JSONObject data) {
        try {
            switch (data.getString("function")){
                case "get":
                    String capacity = data.getString("CAPACITY");
                    Double windowSurface = data.getDouble("WINDOWSURFACE");
                    showCapacity(capacity, windowSurface);
                    break;
                case "delete":
                    switch (data.getString("delete")){
                        case "successful":
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    roomAdapter.deleteItem(selectedRoom);
                                    if(roomAdapter.getSize() == 1){
                                        buildingAdapter.deleteItem(selectedBuilding);
                                    }
                                }
                            });
                            break;
                        case "unsuccessful":
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(Selection.this, "Measure not deleted",Toast.LENGTH_LONG).show();
                                }
                            });
                            break;
                    }
                    break;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*public void getCapacity(View v){
        new ServerConnection().getCapacity(this,username, selectedBuilding,selectedRoom);

    }

     */

    public void deleteMeasure(View v){
        new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.deleteMeasure))
                .setMessage(getResources().getString(R.string.alertDeleteMeasure))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new ServerConnection().deleteMeasure(Selection.this, username,selectedBuilding,selectedRoom);
                    }
                }).setNegativeButton(android.R.string.cancel,null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public void showCapacity(String capacity, Double windowSurface){
        LayoutInflater inflater = LayoutInflater.from(this);
        final View v = inflater.inflate(R.layout.show_capacity, null,false);

        String str = "This room can accommodate "+capacity+" people and has "+windowSurface+ " m2 of ventilation";
        str += ", enough for "+ (int) Math.floor(windowSurface/0.125)+" people";
        ((TextView)v.findViewById(R.id.capacidad)).setText(str);

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
        String newBuilding = ((TextView)findViewById(R.id.newBuilding)).getText().toString().trim();
        String newRoom = ((TextView)findViewById(R.id.newRoom)).getText().toString().trim();
        if(newBuilding.equals("") || newRoom.equals("")){
            TextView newMeasureWarning = findViewById(R.id.newMeasureWarning);
            newMeasureWarning.setText(getResources().getString(R.string.fieldsFilled));
            newMeasureWarning.setVisibility(View.VISIBLE);
        }else{
            Intent i = new Intent(this, ARCore2.class);
            i.putExtra("username",username);
            i.putExtra("building", newBuilding);
            i.putExtra("room", newRoom);
            i.putExtra("edit", false);
            startActivity(i);
        }

    }

    public void editMeasure(View v){
        if(selectedBuilding == getResources().getString(R.string.selectBuilding)){
            Toast.makeText(this, "Select a building", Toast.LENGTH_LONG).show();
            return;
        }

        if(selectedRoom == getResources().getString(R.string.selectRoom)){
            Toast.makeText(this, "Select a room", Toast.LENGTH_LONG).show();
            return;
        }

        Intent i = new Intent(this, ARCore2.class);
        i.putExtra("username", username);
        i.putExtra("building", selectedBuilding);
        i.putExtra("room", selectedRoom);
        i.putExtra("edit", true);
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
                mDrawerLayout.closeDrawer(GravityCompat.START);
                break;
            case R.id.nav_profile:
                //Toast.makeText(this, "Navigate to profile", Toast.LENGTH_LONG).show();
                mDrawerLayout.closeDrawer(GravityCompat.START);
                Intent profile = new Intent(this, Profile.class);
                profile.putExtra("username", username);
                startActivity(profile);
                break;
            case R.id.nav_contact:

                break;
            case R.id.nav_help:

                break;
            case R.id.nav_logout:
                startActivity(new Intent(this, Login.class));
                finish();
                break;
            case R.id.nav_exit:
                mDrawerLayout.closeDrawer(GravityCompat.START);
                moveTaskToBack(true);
                break;
        }

        return true;
    }

    public boolean isLocationEnabled()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
// This is new method provided in API 28
            LocationManager lm = (LocationManager) Selection.this.getSystemService(Context.LOCATION_SERVICE);
            return lm.isLocationEnabled();
        } else {
// This is Deprecated in API 28
            int mode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
            return  (mode != Settings.Secure.LOCATION_MODE_OFF);

        }
    }

    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Selection.this);

        // Setting Dialog Title
        alertDialog.setTitle(getResources().getString(R.string.settinsUbicationTitle));

        // Setting Dialog Message
        alertDialog.setMessage(getResources().getString(R.string.settingsUbicationMessage));

        // On pressing the Settings button.
        alertDialog.setPositiveButton(getResources().getString(R.string.settings), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                Selection.this.startActivity(intent);
            }
        });

        // On pressing the cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
            }
        });

        // Showing Alert Message
        alertDialog.create().show();
    }

    public void getLocation() {

        /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestUbicationPermission();
            return;
        }

         */

        /*if(!isLocationEnabled()){
            showSettingsAlert();
            return;
        }

         */

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                Log.i("prueba", "Ha habido exito");
                if(location != null){
                    getCountryName(location.getLatitude(), location.getLongitude());
                }
            }
        });
    }

    public void getCountryName(double latitude, double longitude){
        Log.i("prueba", "He entrado a pillar el nombre");
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try{
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            Address result;

            if (addresses != null && !addresses.isEmpty()){
                countryName = addresses.get(0).getCountryCode();
                //countryName = addresses.get(0).getCountryName();
                //Toast.makeText(Main.this, "Your country is\n"+countryName,Toast.LENGTH_LONG).show();
                Log.i("prueba", countryName);
                Log.i("prueba", "Latitud: "+latitude+"\nLongitud: "+longitude);
                DBAccess database = new DBAccess(this);
                database.open();
                double distance = database.getDistance(countryName);
                database.close();
                Log.i("prueba", String.valueOf(distance));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Selection.this, "Security distance: "+distance, Toast.LENGTH_LONG).show();
                    }
                });


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void requestUbicationPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
            new AlertDialog.Builder(Selection.this).setMessage("R string request permission")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(Selection.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_GPS_LOCATION);
                        }
                    }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).create();
        } else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_GPS_LOCATION);
        }
    }

    private void requestCameraPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CAMERA)){
            new AlertDialog.Builder(Selection.this).setMessage("R string request permission")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(Selection.this,
                                    new String[]{Manifest.permission.CAMERA},REQUEST_CAMERA_PERMISSION);
                        }
                    }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).create();
        } else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CAMERA_PERMISSION){
            if(grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(Selection.this, "ERROR: Camera permissions not granted",
                        Toast.LENGTH_LONG).show();
                        finishAffinity();
                        System.exit(0);
            }
        }else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

        if(requestCode == REQUEST_GPS_LOCATION){
            if(grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(Selection.this, "ERROR: Ubication permissions not granted",
                        Toast.LENGTH_LONG).show();
                    finishAffinity();
                    System.exit(0);
            }
        }else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }
}