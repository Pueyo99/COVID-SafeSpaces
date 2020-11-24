package com.example.covidsafespaces;

import data.DBAccess;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Main extends AppCompatActivity implements Listener,NavigationView.OnNavigationItemSelectedListener {
    Context context;
    private static final int MAX_PREVIEW_WIDTH = 1920;
    private static final int MAX_PREVIEW_HEIGHT = 1080;
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    //private static final int REQUEST_STORAGE_PERMISSION = 2;
    private final int REQUEST_GPS_LOCATION = 3;
    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAIT_LOCK = 1;

    /*private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

     */


    /*private File mImageFolder;
    private String mImageFileName;

     */

    private int mCaptureState = STATE_PREVIEW;

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private NavigationView mNavigationView;

    private int imagesSended;
    private int imagesReceived;
    private boolean endPressed;

    private AlertDialog alertDialog;
    private FusedLocationProviderClient mFusedLocationClient;
    private ArrayList<Float> areas;
    private int areaIndex;
    private String path;
    private String username;
    private String building;
    private String room;
    private String countryName;
    private Button captureButton;
    private Button endButton;
    private String mCameraId;
    private AutoFitTextureView mTextureView;
    private int mSensorOrientation;
    private CameraCaptureSession mCaptureSession;
    private CameraCaptureSession mPreviewCaptureSession;
    private CameraCaptureSession.CaptureCallback mPreviewCaptureCallback = new
            CameraCaptureSession.CaptureCallback() {
                private void process(CaptureResult captureResult) {
                    switch (mCaptureState) {
                        case STATE_PREVIEW:
                            //Do nothing
                            break;
                        case STATE_WAIT_LOCK:
                            mCaptureState = STATE_PREVIEW;
                            Integer afState = captureResult.get(CaptureResult.CONTROL_AF_STATE);
                            if (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED || afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
                                startStillCaptureRequest();
                            }
                            break;
                    }
                }

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    process(result);
                    mPreviewCaptureSession = session;
                }
            };
    private CameraDevice mCameraDevice;
    private Size mPreviewSize;
    private Size mImageSize;
    private int mDisplayRotation;
    private HandlerThread mBackgroundThread; //To avoid long time tasks running on the UI thread
    private Handler mBackgroundHandler;
    private ImageReader mImageReader;
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new
            ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    mBackgroundHandler.post(new ImageSaver(reader.acquireLatestImage()));
                }
            };

    private class ImageSaver implements Runnable {
        private final Image mImage;

        public ImageSaver(Image image) {
            mImage = image;
        }

        @Override
        public void run() {
            ByteBuffer byteBuffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(bytes);
            new ServerConnection().postImage(bytes, createFileName(), mDisplayRotation, username,path,
                    building,room,areas.get(areaIndex),Main.this);
            mImage.close();
            imagesSended++;
            Toast.makeText(Main.this, "Sended: "+imagesSended,Toast.LENGTH_LONG).show();
            /*FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(mImageFileName);
                fileOutputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mImage.close();
                if(fileOutputStream != null){
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }*/
        }
    }

    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CaptureRequest mPreviewRequest;
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera(width, height);      //when TextureView is available we open the camera
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraOpenCloseLock.release();
            mCameraDevice = camera;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            mCameraOpenCloseLock.release();
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            mCameraOpenCloseLock.release();
            camera.close();
            mCameraDevice = null;
            finish();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        context =this;
        new Help_dialogWindow(context);
        Bundle datos = getIntent().getExtras();
        if(datos != null){
            username = datos.getString("username");
            building = datos.getString("building");
            room = datos.getString("room");
            areas = (ArrayList<Float>) datos.getSerializable("areas");
        }

        path = "window";
        areaIndex = 0;

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mDrawerLayout = findViewById(R.id.drawer_layout);

        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open,
                R.string.drawe_close);
        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);
        mActionBarDrawerToggle.syncState();

        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        //createImageFolder();
        addOrientationListener();
        mTextureView = (AutoFitTextureView) findViewById(R.id.texture);

        captureButton = findViewById(R.id.captureButton);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lockFocus();
                Toast.makeText(getApplicationContext(), "Image captured", Toast.LENGTH_SHORT).show();
            }
        });

        endButton = findViewById(R.id.endButton);
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imagesSended==imagesReceived){
                    setProgressDialog();
                    new ServerConnection().getCapacity(Main.this,username,building,room);
                }else{
                    setProgressDialog();
                    endPressed=true;
                }


                /*setProgressDialog();
                new ServerConnection().get("275.0", Main.this);

                 */
            }
        });

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

    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Main.this);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing the Settings button.
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                Main.this.startActivity(intent);
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

    public boolean isLocationEnabled()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
// This is new method provided in API 28
            LocationManager lm = (LocationManager) Main.this.getSystemService(Context.LOCATION_SERVICE);
            return lm.isLocationEnabled();
        } else {
// This is Deprecated in API 28
            int mode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
            return  (mode != Settings.Secure.LOCATION_MODE_OFF);

        }
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
                Toast.makeText(Main.this, "Security distance: "+distance, Toast.LENGTH_LONG).show();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setResultDialog(String warning, String message){
        LayoutInflater inflater = LayoutInflater.from(Main.this);
        View v = inflater.inflate(R.layout.result_dialog, null, false);

        ((TextView)v.findViewById(R.id.people)).setText(message);
        ((TextView)v.findViewById(R.id.warning)).setText(warning);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setView(v);

        AlertDialog resultDialog = builder.create();
        resultDialog.show();
        Window window = resultDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(resultDialog.getWindow().getAttributes());
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            resultDialog.getWindow().setAttributes(layoutParams);
        }
    }

    public void setProgressDialog() {

        LayoutInflater inflater = LayoutInflater.from(Main.this);
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
    public void receiveMessage(final JSONObject data) {
        try {
            switch (data.getString("function")){
                case "post":
                    //final String percentage = data.getString("Window percentage");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Main.this,data.toString(), Toast.LENGTH_LONG).show();
                        }
                    });

                    break;
                case "window":
                    imagesReceived++;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Main.this,"Received: "+imagesReceived,Toast.LENGTH_LONG).show();
                            Toast.makeText(Main.this,data.toString(), Toast.LENGTH_LONG).show();
                        }
                    });
                    if(imagesReceived==imagesSended && endPressed){
                        new ServerConnection().getCapacity(Main.this, username,building,room);
                    }
                    break;
                case "people":
                    String warning = "People: "+data.getString("Número de personas");
                    String message = data.getString("Message");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setResultDialog(warning,message);
                        }
                    });
                    break;
                case "get":
                    alertDialog.dismiss();
                    String capacity = data.getString("CAPACITY");
                    Double windowSurface = data.getDouble("WINDOWSURFACE");
                    showCapacity(capacity,windowSurface);
                    break;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void showCapacity(String capacity,Double windowSurface){
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

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestUbicationPermission();
            return;
        }
        if(!isLocationEnabled()){
            showSettingsAlert();
            return;
        }

        mBackgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                getLocation();
            }
        });

        if(mTextureView.isAvailable()){
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());  //If it's available, we open the camera
        }else{
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener); //If it's not available, we pass a listener in order to
        }                                                                    //get notified when the camera is available
    }

    @Override
    protected void onPause() {
        stopBackgroundThread();
        closeCamera();
        super.onPause();

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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_home:
                mDrawerLayout.closeDrawer(GravityCompat.START);
                Intent home = new Intent(this, Selection.class);
                home.putExtra("username", username);
                startActivity(home);
                break;
            case R.id.nav_picture:
                mDrawerLayout.closeDrawer(GravityCompat.START);
                Intent picture = new Intent(this, Main.class);
                picture.putExtra("username", username);
                startActivity(picture);
                break;
            case R.id.nav_AR:
                mDrawerLayout.closeDrawer(GravityCompat.START);
                Intent arcore = new Intent(this, ARCore.class);
                startActivity(arcore);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.window:
                path = "window";
                if(item.isChecked()){
                    item.setChecked(false);
                }else{
                    item.setChecked(true);
                }
                break;
            case R.id.people:
                path="people";
                if(item.isChecked()){
                    item.setChecked(false);
                }else{
                    item.setChecked(true);
                }
                break;

            case R.id.big:
                areaIndex = 0;
                if(item.isChecked()){
                    item.setChecked(false);
                }else{
                    item.setChecked(true);
                }
                break;
            case R.id.small:
                areaIndex = 1;
                if(item.isChecked()){
                    item.setChecked(false);
                }else{
                    item.setChecked(true);
                }
                break;
        }

        return true;
    }

    private void addOrientationListener()
    {
        OrientationEventListener listener=new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL)
        {
            public void onOrientationChanged(int orientation) {
                if (orientation >= 330 || orientation < 30) {
                    mDisplayRotation = Surface.ROTATION_0;
                } else if (orientation >= 60 && orientation < 120) {
                    mDisplayRotation = Surface.ROTATION_270;
                } else if (orientation >= 150 && orientation < 210) {
                    mDisplayRotation = Surface.ROTATION_180;
                } else if (orientation >= 240 && orientation < 300) {
                    mDisplayRotation = Surface.ROTATION_90;
                }
            }
        };

        if(listener.canDetectOrientation())
            listener.enable();
    }


    private void openCamera(int width, int height){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        != PackageManager.PERMISSION_GRANTED){
            requestCameraPermission();
            return;
        }
        setUpCameraOutputs(width,height);
        configureTransform(width,height);
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try{
            if(!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)){
                throw new RuntimeException("Time out waiting to lock camera opening");
            }
            manager.openCamera(mCameraId,mStateCallback,mBackgroundHandler);
        }catch (CameraAccessException e){
            e.printStackTrace();
        }catch (InterruptedException e){
            throw new RuntimeException("Interrupted while trying to lock camera opening", e);
        }
    }

    private void closeCamera(){
        try{
            mCameraOpenCloseLock.acquire();
            if(mCaptureSession != null){
                mCaptureSession.close();
                mCaptureSession=null;
            }
            if(mCameraDevice!=null){
                mCameraDevice.close();
                mCameraDevice=null;
                Log.i("prueba", "Camera closed");
            }
            if(mImageReader != null){
                mImageReader.close();
                mImageReader=null;
            }
        } catch (InterruptedException e){
            throw new RuntimeException("Interrupted while trying to lock camera closing",e);
        } finally {
            mCameraOpenCloseLock.release();

        }
    }
    
    private void setUpCameraOutputs(int width, int height){
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try{
            for(String cameraId : manager.getCameraIdList()){
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                //We don't want to use front camera
                Integer front = characteristics.get(CameraCharacteristics.LENS_FACING);
                if(front != null && front == CameraCharacteristics.LENS_FACING_FRONT){
                    continue;  //we skip the front camera loop, we only do the setup for the rear camera
                }

                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if(map == null){
                    continue;
                }
                Size largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), //we get all the possible choices from an Image
                        new CompareSizesByArea());
                //mImageSize = largest;
                mImageReader = ImageReader.newInstance(largest.getWidth(),largest.getHeight(),
                        ImageFormat.JPEG,2);
                mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);

                mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                boolean swappedDimensions = false;
                switch (mDisplayRotation){
                    case Surface.ROTATION_0:
                    case Surface.ROTATION_180:
                        if(mSensorOrientation == 90 || mSensorOrientation == 270){
                            swappedDimensions = true;
                        }
                        break;
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        if(mSensorOrientation == 0 || mSensorOrientation == 180){
                            swappedDimensions = true;
                        }
                }

                Point displaySize = new Point();
                getWindowManager().getDefaultDisplay().getSize(displaySize);
                int rotatedPreviewWidth = width;
                int rotatedPreviewHeight = height;
                int maxPreviewWidth = displaySize.x;
                int maxPreviewHeight = displaySize.y;

                if(swappedDimensions){
                    rotatedPreviewWidth = height;
                    rotatedPreviewHeight = width;
                    maxPreviewWidth = displaySize.y;
                    maxPreviewHeight = displaySize.x;
                }

                if(maxPreviewWidth > MAX_PREVIEW_WIDTH){
                    maxPreviewWidth = MAX_PREVIEW_WIDTH;
                }
                if(maxPreviewHeight > MAX_PREVIEW_HEIGHT){
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT;
                }

                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),  //we get all the possible choices of the preview
                        rotatedPreviewWidth, rotatedPreviewHeight,maxPreviewWidth,maxPreviewHeight,largest);

                if(mDisplayRotation == Surface.ROTATION_90 || mDisplayRotation == Surface.ROTATION_270){
                    mTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                } else {
                    mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
                }


                mCameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e){
            Toast.makeText(Main.this,"Camera2 API not supported on this device", Toast.LENGTH_LONG).show();
        }
    }



    private void createCameraPreviewSession(){
        try{
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;

            texture.setDefaultBufferSize(mPreviewSize.getWidth(),mPreviewSize.getHeight());

            Surface surface = new Surface(texture);

            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);

            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            if(mCameraDevice == null){
                                return;
                            }

                            mPreviewCaptureSession = session;
                            //mCaptureSession = session;
                            try{
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                mPreviewCaptureSession.setRepeatingRequest(mPreviewRequest,null,mBackgroundHandler);
                            } catch (CameraAccessException e){
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                        }
                    }, null);
        } catch (CameraAccessException e){
            e.printStackTrace();
        }
    }

    private void startStillCaptureRequest(){
        try {
            //mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            //mPreviewRequestBuilder.addTarget(mImageReader.getSurface());
            final CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            //captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(mDisplayRotation));
            CameraCaptureSession.CaptureCallback stillCaptureCallback = new
                    CameraCaptureSession.CaptureCallback() {
                        @Override
                        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
                            super.onCaptureStarted(session, request, timestamp, frameNumber);
                            /*try {
                                createImageFileName();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                             */


                        }
                    };

            mPreviewCaptureSession.capture(captureBuilder.build(),stillCaptureCallback, null);
            mPreviewCaptureSession.capture(mPreviewRequestBuilder.build(),stillCaptureCallback, null);  //Handler null because stillCaptureCallback already is executed
        } catch (CameraAccessException e) {                                                                     //from the background thread
            e.printStackTrace();
        }
    }

/*
    private int getOrientation(int rotation){
        return (mSensorOrientation+ORIENTATIONS.get(rotation)+270)%360;
    }

 */




    private void lockFocus(){

        /*if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED){
            requestStoragePermission();
            return;
        }

         */
        mCaptureState = STATE_WAIT_LOCK;
        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);
        try {
            mPreviewCaptureSession.capture(mPreviewRequestBuilder.build(),mPreviewCaptureCallback,mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void requestCameraPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CAMERA)){
            new AlertDialog.Builder(Main.this).setMessage("R string request permission")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(Main.this,
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
                Toast.makeText(Main.this, "ERROR: Camera permissions not granted",
                        Toast.LENGTH_LONG).show();
            }
        }else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

        if(requestCode == REQUEST_GPS_LOCATION){
            if(grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(Main.this, "ERROR: Ubication permissions not granted",
                        Toast.LENGTH_LONG).show();
            }
        }else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

        /*if(requestCode == REQUEST_STORAGE_PERMISSION){
            if(grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(Main.this, "ERROR: Storage writing permissions not granted",
                        Toast.LENGTH_LONG).show();
            }
        }else{
            super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        }

         */
    }

    private void startBackgroundThread(){
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread(){
        mBackgroundThread.quitSafely();
        try{
            mBackgroundThread.join();
            mBackgroundThread=null;
            mBackgroundHandler=null;
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth, int textureViewHeight,
                                          int maxWidth, int maxHeight, Size aspectRatio){
        List<Size> bigEnough = new ArrayList<>();
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for(Size option : choices){
            if(option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                option.getHeight() == option.getWidth()*h/w){
                if(option.getWidth() >= textureViewWidth && option.getHeight() >= textureViewHeight){
                    bigEnough.add(option);
                }else {
                    notBigEnough.add(option);
                }
            }
        }

        if(bigEnough.size() > 0){
            return Collections.min(bigEnough, new CompareSizesByArea());
        }else if(notBigEnough.size() > 0){
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            Log.e("Camera2", "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    private void configureTransform(int viewWidth, int viewHeight){
        if (mTextureView == null || mPreviewSize == null){
            return;
        }
        //int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int rotation = mDisplayRotation;
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0,0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0,0, mPreviewSize.getHeight(),mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if(Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation){
            bufferRect.offset(centerX - bufferRect.centerX(), centerY-bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max((float) viewHeight/mPreviewSize.getHeight(), (float) viewWidth /mPreviewSize.getWidth());
            matrix.postScale(scale,scale, centerX, centerY);
            matrix.postRotate(90*(rotation-2), centerX,centerY);
        } else if(Surface.ROTATION_180 == rotation){
            matrix.postRotate(180, centerX,centerY);
        }
        mTextureView.setTransform(matrix);
    }

    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth()*lhs.getHeight()-
                    (long) rhs.getWidth()*rhs.getHeight());
        }
    }

    private void requestUbicationPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
            new AlertDialog.Builder(Main.this).setMessage("R string request permission")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(Main.this,
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

    /*
    private void createImageFolder(){
        File imageFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        mImageFolder = new File(imageFile, "CovidSafeSpaces");
        if(!mImageFolder.exists()){
            mImageFolder.mkdirs();
        }
    }

     */

    /*
    private File createImageFileName() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd HHmmss").format(new Date());
        String prepend = "IMAGE_" + timestamp + "_";
        File imageFile = new File(mImageFolder, prepend+".jpeg");
        mImageFileName = imageFile.getAbsolutePath();
        return imageFile;
    }

     */

    private String createFileName(){
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    }


    /*
    private void requestStoragePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            new AlertDialog.Builder(Main.this).setMessage("R string request permission")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(Main.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_STORAGE_PERMISSION);
                        }
                    }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).create();
        } else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
        }
    }

     */
    public class Help_dialogWindow {
        public Help_dialogWindow(Context context){
            final Dialog dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setContentView(R.layout.help_dialogwindow);
            final TextView text =(TextView) dialog.findViewById(R.id.textwindow);
            text.setText("INSTRUCTIONS\n\nYou have three functions available :\n-Window detection\nPeople \n"+
                    "Now you are in Window Detection, if you want to change it press then button up to the right and select the function you want\n\n" +
                    "WINDOW DETECTION:\n\n1.Take pictures of each window of the room\n2.When you have captured all the windows available, " +
                    "press the button 'end' and wait the response of the server\n\nPEOPLE:\n\nYou have to take a photo from an angle that shows all the " +
                    "separate people and their respective faces." +
                    "we can see all the faces of each person and");
            text.setMovementMethod(new ScrollingMovementMethod());
            Button ok = (Button) dialog.findViewById(R.id.okbutton);
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            dialog.show();

        }
    }
}
