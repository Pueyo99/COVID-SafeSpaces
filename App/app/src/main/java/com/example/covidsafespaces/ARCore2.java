package com.example.covidsafespaces;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.CameraConfig;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static java.lang.Math.pow;

public class ARCore2 extends AppCompatActivity implements Scene.OnUpdateListener{

    private static final double MIN_OPENGL_VERSION = 3.0;
    private static final String TAG = ARCore.class.getSimpleName();

    private Toolbar mToolbar;

    private String username;
    private String building;
    private String room;
    private boolean edit;

    //private final SnackbarHelper messageSnackbarHelper = new SnackbarHelper();
    //private final float[] modelMatrix = new float[16];
    private float[] modelMatrix = new float[16];
    private float[] modelMatrixAnt = new float[16];
    private ArrayList<float[]> poses = new ArrayList<>();
    int nAnchors = 0;
    float distance;
    float finaldistance;

    private ArrayList<Float> distances = new ArrayList<>();
    private boolean updateDistance;
    private int rate=0;

    private ArFragment arFragment;
    private AnchorNode currentAnchorNode;
    private TextView tvDistance;
    ModelRenderable cubeRenderable;
    private Anchor currentAnchor = null;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context =this;
        if (!checkIsSupportedDeviceOrFinish(this)) {
            Toast.makeText(getApplicationContext(), "Device not supported", Toast.LENGTH_LONG).show();
        }

        setContentView(R.layout.activity_a_r_core);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        tvDistance = findViewById(R.id.tvDistance);

        Bundle extras = getIntent().getExtras();
        if(extras!=null){
            username = extras.getString("username");
            building = extras.getString("building");
            room = extras.getString("room");
            edit = extras.getBoolean("edit");
            if(extras.containsKey("distances")){
                distances = (ArrayList<Float>) extras.getSerializable("distances");
                if(distances.size()>0){
                    tvDistance.setText("Distance: " + distances.get(0) + " m.");
                }
            }else{
                showHelp();
            }
        }



        initAR();
    }

    public void initAR() {

        /*try {
            Session mSession = new Session(this);
            Config mConfig = new Config(mSession);
            mConfig.setPlaneFindingMode(Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL);
            mConfig.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
            arFragment.getArSceneView().setupSession(mSession);
        } catch (Exception ex){
            ex.printStackTrace();
        }
        
         */


        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        initModel();

        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            //Toast.makeText(this, "Anchor creado", Toast.LENGTH_LONG).show();
            if (cubeRenderable == null)
                return;

            // Creating Anchor.
            Anchor anchor = hitResult.createAnchor();
            AnchorNode anchorNode = new AnchorNode(anchor);
            anchorNode.setParent(arFragment.getArSceneView().getScene());



            //clearAnchor();

            currentAnchor = anchor;
            currentAnchorNode = anchorNode;
            nAnchors++;
            updateDistance=true;


            TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
            node.setRenderable(cubeRenderable);
            node.setParent(anchorNode);
            arFragment.getArSceneView().getScene().addOnUpdateListener(this);
            arFragment.getArSceneView().getScene().addChild(anchorNode);
            node.select();


        });
    }

    public boolean checkIsSupportedDeviceOrFinish(final Activity activity) {

        String openGlVersionString =
                ((ActivityManager) Objects.requireNonNull(activity.getSystemService(Context.ACTIVITY_SERVICE)))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show();
            activity.finish();
            return false;
        }
        return true;
    }

    private void initModel() {
        MaterialFactory.makeTransparentWithColor(this, new Color(android.graphics.Color.BLUE))
                .thenAccept(
                        material -> {
                            Vector3 vector3 = new Vector3(0.033f, 0.033f, 0.033f);
                            cubeRenderable = ShapeFactory.makeCube(vector3, Vector3.zero(), material);
                            cubeRenderable.setShadowCaster(false);
                            cubeRenderable.setShadowReceiver(false);
                        });
    }

    private void clearAnchor() {
        currentAnchor = null;


        if (currentAnchorNode != null) {
            arFragment.getArSceneView().getScene().removeChild(currentAnchorNode);
            currentAnchorNode.getAnchor().detach();
            currentAnchorNode.setParent(null);
            currentAnchorNode = null;
        }
    }


    @Override
    public void onUpdate(FrameTime frameTime) {
        Frame frame = arFragment.getArSceneView().getArFrame();
        Log.d("API123", "onUpdateframe... current anchor node " + (currentAnchorNode == null));

        if (currentAnchorNode != null) {
            Pose objectPose = currentAnchor.getPose();
            //Pose cameraPose = frame.getCamera().getPose();
            objectPose.toMatrix(modelMatrix, 0);

            if (nAnchors == 1) {
                //1rst Anchor --- Save current modelMatrix as modelMatrixAnt
                //System.arraycopy(modelMatrix, 0, modelMatrixAnt, 0, 16);
                if(updateDistance){
                    System.arraycopy(modelMatrix, 0, modelMatrixAnt, 0, 16);
                    //poses.add(modelMatrix);
                    /*Toast.makeText(this, "x: "+modelMatrix[13]+"\ny: "+modelMatrix[14]
                    +"\nz: "+modelMatrix[15], Toast.LENGTH_LONG).show();

                     */
                    updateDistance = false;
                }
                //aux++;
            } else if (nAnchors == 2) {
                //2nd Anchor --- Calculate+Show distance on screen
                if(updateDistance){
                    //poses.add(modelMatrix);
                    /*Toast.makeText(this, "x: "+modelMatrix[13]+"\ny: "+modelMatrix[14]
                            +"\nz: "+modelMatrix[15], Toast.LENGTH_LONG).show();

                     */
                    updateDistance = false;
                    //distance = distance2Points(poses.get(1), poses.get(0));
                    distance = distance2Points(modelMatrix,modelMatrixAnt);
                    showDistance(distance);
                    distances.add(distance);
                    System.arraycopy(modelMatrix, 0, modelMatrixAnt, 0, 16);
                    nAnchors = 0;



                    /*modelMatrix = new float[16];
                    modelMatrixAnt = new float[16];
                    clearAnchor();

                     */
                    /*getIntent().putExtra("distances",distances);
                    recreate();

                     */




                }

                //distance = 10;
                /*String distanceString = String.valueOf(distance);
                tvDistance.setText("Distance: " + distanceString + " m.");
                //aux=0;
                System.arraycopy(modelMatrix, 0, modelMatrixAnt, 0, 16);

                 */
            }

            /*

            float dx = objectPose.tx() - cameraPose.tx();
            float dy = objectPose.ty() - cameraPose.ty();
            float dz = objectPose.tz() - cameraPose.tz();

            ///Compute the straight-line distance.
            float distanceMeters = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
            tvDistance.setText("Distance from camera: " + distanceMeters + " metres");
            */

            /*float[] distance_vector = currentAnchor.getPose().inverse()
                    .compose(cameraPose).getTranslation();
            float totalDistanceSquared = 0;
            for (int i = 0; i < 3; ++i)
                totalDistanceSquared += distance_vector[i] * distance_vector[i];*/
        }
    }

    private void showDistance(float distance){
        finaldistance = distance;
        tvDistance.setText("Distance: " + distance + " m.");
    }

    public void onButtonClick(View v) {
        //Toast.makeText(this, "1: " + distances.get(0) + "\n2: " + distances.get(1), Toast.LENGTH_LONG).show();
        ArrayList<Float> areas = new ArrayList<>();
        for (Float distance : distances) {
            areas.add((float) (distance * 3.0));
        }
        Intent i = new Intent(this, Main.class);
        i.putExtra("username", username);
        i.putExtra("building",building);
        i.putExtra("room",room);
        i.putExtra("areas", areas);
        startActivity(i);
        if(!edit){
            new ServerConnection().insertCapacity(username,building,room,
                    (int) Math.floor((distances.get(0)*distances.get(1))/4));
        }else{
            new ServerConnection().editCapacity(username,building,room,
                    (int) Math.floor((distances.get(0)*distances.get(1))/4));
        }


    }

    //Calculate distancia between 2 points on same frame
    private float distance2Points(float[] array1, float[] array2) {

        /*Toast.makeText(this, "x: "+array1[13]+"\ny: "+array1[14]
                +"\nz: "+array1[15], Toast.LENGTH_LONG).show();
        Toast.makeText(this, "x: "+array2[13]+"\ny: "+array2[14]
                +"\nz: "+array2[15], Toast.LENGTH_LONG).show();

         */

        float distx1 = array1[13];
        float disty1 = array1[14];
        float distz1 = array1[15];
        float distx2 = array2[13];
        float disty2 = array2[14];
        float distz2 = array2[15];

        return (float) Math.sqrt(pow(distx1 - distx2, 2) + pow(disty1 - disty2, 2) + pow(distz1 - distz2, 2));
    }

    //Compare Matrix
    static boolean areSame(float A[], float B[]) {
        int i;
        for (i = 0; i < 16; i++)
            if (A[i] != B[i])
                return false;
        return true;
    }

    private void showHelp(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.help_dialogwindow);
        final TextView text = dialog.findViewById(R.id.textwindow);
        text.setText("STEPS TO FOLLOW\n\nYou will have two markers available to place in two corners of your ground\n" +
                "1.Place two markers on two corners of the room to mesure the width of the room. You will see on ths screen the distance between them\n" +
                "2.Press the + button, placed on the menu bar, and repeat the steps explained before to measure the other wall.\nHaving all these measures we " +
                "proceed to calculate area of the ground");
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.help:
                showHelp();
                break;
            case R.id.add:
                getIntent().putExtra("distances",distances);
                recreate();
                break;
            case R.id.restart:

                break;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}