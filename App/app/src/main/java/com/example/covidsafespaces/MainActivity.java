package com.example.covidsafespaces;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

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
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import data.DBAccess;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private final int REQUEST_GPS_LOCATION = 3;

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private NavigationView mNavigationView;

    private FusedLocationProviderClient mFusedLocationClient;
    private String countryName;

    private String currentView;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle data = getIntent().getExtras();
        username = data.getString("username");

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

        currentView = "";
        onNavigationItemSelected(mNavigationView.getMenu().findItem(R.id.nav_home));


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
            /*if(getSupportFragmentManager().getBackStackEntryCount()>0){
                getSupportFragmentManager().popBackStackImmediate();
            }else {
                moveTaskToBack(true);
            }

             */
            moveTaskToBack(true);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_home:
                if(!currentView.equals(getResources().getString(R.string.home))){
                    Fragment homeFragment = new HomeFragment();
                    Bundle homeData = new Bundle();
                    homeData.putString("username",username);
                    homeFragment.setArguments(homeData);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,homeFragment).
                            addToBackStack(null).commit();
                    currentView = getResources().getString(R.string.home);
                    mToolbar.setTitle(getResources().getString(R.string.home));
                }
                mDrawerLayout.closeDrawer(GravityCompat.START);
                break;
            case R.id.nav_profile:
                if(!currentView.equals(getResources().getString(R.string.profile))){
                    Fragment profileFragment = new ProfileFragment();
                    Bundle profileData = new Bundle();
                    profileData.putString("username",username);
                    profileFragment.setArguments(profileData);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,profileFragment).
                            addToBackStack(null).commit();
                    currentView = getResources().getString(R.string.profile);
                    mToolbar.setTitle(getResources().getString(R.string.profile));
                }
                mDrawerLayout.closeDrawer(GravityCompat.START);
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
            LocationManager lm = (LocationManager) MainActivity.this.getSystemService(Context.LOCATION_SERVICE);
            return lm.isLocationEnabled();
        } else {
// This is Deprecated in API 28
            int mode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
            return  (mode != Settings.Secure.LOCATION_MODE_OFF);

        }
    }

    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

        // Setting Dialog Title
        alertDialog.setTitle(getResources().getString(R.string.settinsUbicationTitle));

        // Setting Dialog Message
        alertDialog.setMessage(getResources().getString(R.string.settingsUbicationMessage));

        // On pressing the Settings button.
        alertDialog.setPositiveButton(getResources().getString(R.string.settings), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                MainActivity.this.startActivity(intent);
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
                        Toast.makeText(MainActivity.this, "Security distance: "+distance, Toast.LENGTH_LONG).show();
                    }
                });


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void requestUbicationPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
            new AlertDialog.Builder(MainActivity.this).setMessage("R string request permission")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this,
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
            new AlertDialog.Builder(MainActivity.this).setMessage("R string request permission")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this,
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
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "ERROR: Camera permissions not granted",
                        Toast.LENGTH_LONG).show();
                finishAffinity();
                System.exit(0);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

        if (requestCode == REQUEST_GPS_LOCATION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "ERROR: Ubication permissions not granted",
                        Toast.LENGTH_LONG).show();
                finishAffinity();
                System.exit(0);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}