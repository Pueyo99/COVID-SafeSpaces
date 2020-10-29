package com.example.tape2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.se.omapi.Session;
import android.widget.Toast;
import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Pose;
import com.google.ar.core.Trackable;
import com.google.ar.core.Camera;
import com.google.ar.core.examples.java.common.helpers.CameraPermissionHelper;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;

import java.util.Collections;


public class MainActivity extends AppCompatActivity {
    public final Anchor anchor;
    private static final Object INSTALL_REQUESTED = null;
    private Session session;

    public MainActivity(Anchor anchor) {
        this.anchor = anchor;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    // Set to true ensures requestInstall() triggers installation if necessary.
    private boolean mUserRequestedInstall = true;

    @Override
    protected void onResume() {
        super.onResume();

        // ARCore requires camera permission to operate.
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            CameraPermissionHelper.requestCameraPermission(this);
            return;
        }
        // Make sure Google Play Services for AR is installed and up to date.
        try {
            Session mSession;
            if (mSession == null) {
                switch (ArCoreApk.getInstance().requestInstall(this, mUserRequestedInstall)) {
                    case INSTALLED:
                        // Success, create the AR session.
                        mSession = new Session(this);
                        break;
                    case INSTALL_REQUESTED:
                        // Ensures next invocation of requestInstall() will either return
                        // INSTALLED or throw an exception.
                        mUserRequestedInstall = false;
                        return;
                }
            }
        } catch (UnavailableUserDeclinedInstallationException e) {

            // Display an appropriate message to the user and return gracefully.
            Toast.makeText(this, "TODO: handle exception " + e, Toast.LENGTH_LONG)
                    .show();
            return;
        }


    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                    .show();
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(this);
            }
            finish();
        }

    }

    private static class startAnchor {

        public final Anchor anchor;
        public float[] color;
        public final Trackable trackable;

        public startAnchor(Anchor a, float[] color4f, Trackable trackable) {
            this.anchor = a;
            this.color = color4f;
            this.trackable = trackable;
        }
    }

    Frame frame = session.update();
    Camera camera = frame.getCamera();
    Camera start     = camera;
    //
    startAnchor = session.createAnchor(HitResult.getHitPose());
    Pose startPose = startAnchor.getPose();     //startAnchor ha de ser classe Camera
    Pose endPose = HitResult.getHitPose();

    // Clean up the anchor
    session.removeAnchors(Collections.singleton(startAnchor));
    startAnchor = null;

    // Compute the difference vector between the two hit locations.
    float dx = startPose.tx() - endPose.tx();
    float dy = startPose.ty() - endPose.ty();
    float dz = startPose.tz() - endPose.tz();

    // Compute the straight-line distance.
    float distanceMeters = (float) Math.sqrt(dx*dx + dy*dy + dz*dz);
    /*
    //distance between two points
    Pose pose0 = // first HitResult's Anchor
    Pose pose1 = // second HitResult's Anchor

    double distanceM = Math.sqrt(Math.pow((pose0.tx() - pose1.tx()), 2) +
            Math.pow((pose0.ty() - pose1.ty()), 2) +
            Math.pow((pose0.tz() - pose1.tz()), 2));

    double distanceCm = ((int)(distanceM * 1000))/10.0f;
    */


}