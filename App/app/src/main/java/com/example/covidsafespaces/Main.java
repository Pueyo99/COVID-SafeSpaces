package com.example.covidsafespaces;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
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
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
//prueba
public class Main extends AppCompatActivity{

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
    private Button captureButton;
    private String mCameraId;
    private AutoFitTextureView mTextureView;
    private int mSensorOrientation;
    private CameraCaptureSession mCaptureSession;
    private CameraCaptureSession mPreviewCaptureSession;
    private CameraCaptureSession.CaptureCallback mPreviewCaptureCallback = new
            CameraCaptureSession.CaptureCallback() {
                private void process(CaptureResult captureResult){
                    switch (mCaptureState){
                        case STATE_PREVIEW:
                            //Do nothing
                            break;
                        case STATE_WAIT_LOCK:
                            mCaptureState = STATE_PREVIEW;
                            Integer afState = captureResult.get(CaptureResult.CONTROL_AF_STATE);
                            if(afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED || afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED){
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
    private class ImageSaver implements Runnable{
        private final Image mImage;

        public ImageSaver(Image image){
            mImage = image;
        }

        @Override
        public void run() {
            ByteBuffer byteBuffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(bytes);
            new ServerConnection().postImage(bytes, createFileName(), mDisplayRotation);
            mImage.close();
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
            openCamera(width,height);      //when TextureView is available we open the camera
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            configureTransform(width,height);
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
            mCameraDevice=null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            mCameraOpenCloseLock.release();
            camera.close();
            mCameraDevice=null;
            finish();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //createImageFolder();
        addOrientationListener();

        mTextureView = (AutoFitTextureView) findViewById(R.id.texture);

        captureButton = findViewById(R.id.captureButton);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lockFocus();
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


    /*private void setUpCameraOutputs(int width, int height){
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

                Point displaySize = new Point();
                getWindowManager().getDefaultDisplay().getSize(displaySize);
                int rotatedPreviewWidth = width;
                int rotatedPreviewHeight = height;
                int maxPreviewWidth = displaySize.x;
                int maxPreviewHeight = displaySize.y;

                if(maxPreviewWidth > MAX_PREVIEW_WIDTH){
                    maxPreviewWidth = MAX_PREVIEW_WIDTH;
                }
                if(maxPreviewHeight > MAX_PREVIEW_HEIGHT){
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT;
                }



                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),  //we get all the possible choices of the preview
                        rotatedPreviewWidth, rotatedPreviewHeight,maxPreviewWidth,maxPreviewHeight,largest);

                mImageSize = largest;
                mImageReader = ImageReader.newInstance(mImageSize.getWidth(),mImageSize.getHeight(),
                        ImageFormat.JPEG,2);
                mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);

                mCameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e){
            Toast.makeText(Main.this,"Camera2 API not supported on this device", Toast.LENGTH_LONG).show();
        }
    }

     */

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
}
