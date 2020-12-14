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
import android.widget.ImageButton;
import android.widget.ImageView;
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

public class Main extends AppCompatActivity implements Listener {
    Context context;
    private static final int MAX_PREVIEW_WIDTH = 1920;
    private static final int MAX_PREVIEW_HEIGHT = 1080;
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    //private static final int REQUEST_STORAGE_PERMISSION = 2;
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

    private int imagesSended;
    private int imagesReceived;
    private boolean endPressed;

    private AlertDialog alertDialog;
    private ArrayList<Float> areas;
    private int areaIndex;
    private String path;
    private String username;
    private String building;
    private String room;
    private String selectedShape;
    private ARCoreHelper helper;
    private int people=0;
    private ImageButton captureButton;
    private ImageButton endButton;
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
            if (path=="people"){
                setProgressDialog();
            }
            if(path=="window"){
                imagesSended++;
            }
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
        showHelp();
        Bundle datos = getIntent().getExtras();
        if(datos != null){
            username = datos.getString("username");
            building = datos.getString("building");
            room = datos.getString("room");
            areas = (ArrayList<Float>) datos.getSerializable("areas");
            selectedShape = datos.getString("selectedShape");
            helper = new ARCoreHelper(selectedShape, Main.this);
        }

        path = "window";
        areaIndex = 0;

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

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
                    new ServerConnection().getCapacity(Main.this, username,building,room);
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


    public void setResultDialog(String warning, String message){
        LayoutInflater inflater = LayoutInflater.from(Main.this);
        View v = inflater.inflate(R.layout.result_dialog, null, false);

        ((TextView)v.findViewById(R.id.people)).setText(message);
        ((TextView)v.findViewById(R.id.warning)).setText(warning);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setView(v);

        AlertDialog resultDialog = builder.create();
        Window window = resultDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(resultDialog.getWindow().getAttributes());
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            resultDialog.getWindow().setAttributes(layoutParams);
        }
        resultDialog.show();
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
                    alertDialog.dismiss();
                    people = data.getInt("Número de personas");
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
                    int capacity = data.getInt("CAPACITY");
                    Double windowSurface = data.getDouble("WINDOWSURFACE");
                    int ventilationCapacity = (int) Math.floor(windowSurface/0.125);
                    Intent results = new Intent(context, Results2.class);
                    results.putExtra("capacity",capacity);
                    results.putExtra("windowSurface", windowSurface);
                    results.putExtra("people",people);
                    results.putExtra("username",username);
                    startActivity(results);
                    finish();
                    //showCapacity(capacity,windowSurface);
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
        moveTaskToBack(true);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.home:
                Intent home = new Intent(this, MainActivity.class);
                home.putExtra("username",username);
                startActivity(home);
                finish();
                break;
            case R.id.help:
                showHelp();
                break;
            case R.id.map:
                showMap();
                break;
            case R.id.options:
                setSingleChoiceDialog();
                break;
            case R.id.windowSelection:
                setWindowSelectionDialog();
                break;
        }

        return true;
    }

    private void showMap(){
        LayoutInflater inflater = LayoutInflater.from(this);
        View v = inflater.inflate(R.layout.map_dialog, null, false);
        ((ImageView)v.findViewById(R.id.mapImage)).setImageResource(helper.getImageResource());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setView(v);

        AlertDialog resultDialog = builder.create();
        Window window = resultDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(resultDialog.getWindow().getAttributes());
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            resultDialog.getWindow().setAttributes(layoutParams);
        }
        resultDialog.show();
    }

    private void setWindowSelectionDialog(){
        String[] choices = helper.getSelectionItems();

        //AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        new AlertDialog.Builder(this).setTitle("Select option").setSingleChoiceItems(choices, areaIndex, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                areaIndex=which;
            }
        }).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();


    }

    private void setSingleChoiceDialog(){
        String[] choices = {"Window","People"};
        int checked =  path=="window"?0:1;
        //AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        new AlertDialog.Builder(this).setTitle("Select option").setSingleChoiceItems(choices, checked, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        path = "window";
                        break;
                    case 1:
                        path="people";
                        break;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Main.this, path, Toast.LENGTH_LONG).show();

                    }
                });
            }
        }).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();


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

    public void showHelp(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.help_dialogwindow);
        final TextView text =(TextView) dialog.findViewById(R.id.textwindow);
        text.setText("INSTRUCTIONS\n\nYou have two functions available :\n-Window detection\nPeople \n"+
                "Now you are in Window Detection, if you want to change it, press then menu button and select the function you want\n\n" +
                "WINDOW DETECTION:\n\n1.Take pictures of each window of the room\n2.When you have captured all the windows available, " +
                "press the 'end' button and wait the response of the server\n\nPEOPLE:\n\nYou have to take a photo from an angle that shows all the " +
                "separate people and their respective faces.");
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
