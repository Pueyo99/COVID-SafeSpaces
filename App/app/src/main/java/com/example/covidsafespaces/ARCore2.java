package com.example.covidsafespaces;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.graphics.Canvas;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.CameraConfig;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.ar.sceneform.ux.TransformationSystem;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static java.lang.Math.pow;

public class ARCore2 extends AppCompatActivity implements Scene.OnUpdateListener{

    private static final double MIN_OPENGL_VERSION = 3.0;
    private static final String TAG = ARCore2.class.getSimpleName();

    private Toolbar mToolbar;

    private ARCoreHelper helper;

    private String username;
    private String building;
    private String room;
    private String selectedShape;
    private float roomHeight;
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
    private AnchorNode pastAnchorNode;
    private TextView tvDistance;
    ModelRenderable cubeRenderable;
    private Anchor currentAnchor = null;
    Context context;
    private Canvas canvas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context =this;
        if (!checkIsSupportedDeviceOrFinish(this)) {
            Toast.makeText(getApplicationContext(), "Device not supported", Toast.LENGTH_LONG).show();
        }

        roomHeight = (float) 3.0;

        setContentView(R.layout.activity_a_r_core);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        tvDistance = findViewById(R.id.tvDistance);

        Bundle extras = getIntent().getExtras();
        if(extras!=null){
            username = extras.getString("username");
            building = extras.getString("building");
            room = extras.getString("room");
            selectedShape = extras.getString("selectedShape");
            helper = new ARCoreHelper(selectedShape, ARCore2.this);
            edit = extras.getBoolean("edit");
            if(extras.containsKey("distances")){
                distances = (ArrayList<Float>) extras.getSerializable("distances");
                if(distances.size()>0){
                    tvDistance.setText("Distance: " + distances.get(distances.size()-1) + " m.");
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
        MaterialFactory.makeTransparentWithColor(this, new Color(android.graphics.Color.rgb(51,0,51)))
                .thenAccept(
                        material -> {
                            Vector3 vector3 = new Vector3(0.033f, 0.033f, 0.033f);
                            cubeRenderable = ShapeFactory.makeCube(vector3, Vector3.zero(), material);
                            cubeRenderable.setShadowCaster(false);
                            cubeRenderable.setShadowReceiver(false);
                        });
    }

    public void drawLine(AnchorNode node1, AnchorNode node2, float distance) {
        //Draw a line between two AnchorNodes
        Log.d(TAG,"drawLine");
        Vector3 point1, point2;
        point1 = node1.getWorldPosition();
        point2 = node2.getWorldPosition();


        //First, find the vector extending between the two points and define a look rotation
        //in terms of this Vector.
        final Vector3 difference = Vector3.subtract(point1, point2);

        final Vector3 directionFromTopToBottom = difference.normalized();
        final Quaternion rotationFromAToB =
                Quaternion.lookRotation(directionFromTopToBottom, Vector3.up());
        MaterialFactory.makeOpaqueWithColor(getApplicationContext(), new Color(153, 0, 153))
                .thenAccept(
                        material -> {
                            /* Then, create a rectangular prism, using ShapeFactory.makeCube() and use the difference vector
                                   to extend to the necessary length.  */
                            Log.d(TAG,"drawLine insie .thenAccept");
                            ModelRenderable model = ShapeFactory.makeCube(
                                    new Vector3(.005f, .005f, difference.length()),
                                    Vector3.zero(), material);
                            /* Last, set the world rotation of the node to the rotation calculated earlier and set the world position to
                                   the midpoint between the given points . */
                            Anchor lineAnchor = node2.getAnchor();
                            Node nodeForLine = new Node();
                            nodeForLine.setParent(node1);
                            nodeForLine.setRenderable(model);
                            nodeForLine.setWorldPosition(Vector3.add(point1, point2).scaled(.5f));
                            nodeForLine.setWorldRotation(rotationFromAToB);



                        }
                );

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
                    pastAnchorNode = currentAnchorNode;
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
                    drawLine(currentAnchorNode, pastAnchorNode, distance);



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
        ArrayList<Float>  wallSurfaces = helper.calculateWallSurface(distances,roomHeight);
        Float surface = helper.calculateSurface(distances);

        //showWallSurfaces(wallSurfaces,surface);

        /*ArrayList<Float> areas = new ArrayList<>();
        for (Float distance : distances) {
            areas.add((float) (distance * roomHeight));
        }
        */


        Intent i = new Intent(this, Main.class);
        i.putExtra("username", username);
        i.putExtra("building",building);
        i.putExtra("room",room);
        i.putExtra("areas", wallSurfaces);
        i.putExtra("selectedShape",selectedShape);
        startActivity(i);
        if(!edit){
            new ServerConnection().insertCapacity(username,building,room,
                    (int) Math.floor(surface/4),selectedShape);
        }else{
            new ServerConnection().editCapacity(username,building,room,
                    (int) Math.floor(surface/4));
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
        text.setText(helper.getTextHelp());
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

    private void showMeasures(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.help_dialogwindow);
        final TextView text = dialog.findViewById(R.id.textwindow);
        String str = "Room height: "+roomHeight;
        for(int i=0; i<distances.size(); i++){
            str += "\nDistance "+(i+1)+": "+distances.get(i);
        }
        text.setText(str);
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

    private void showWallSurfaces(ArrayList<Float> wallSurfaces,float surface){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.help_dialogwindow);
        final TextView text = dialog.findViewById(R.id.textwindow);
        String str = "Room height: "+roomHeight;
        for(int i=0; i<wallSurfaces.size(); i++){
            str += "\nDistance "+(i+1)+": "+wallSurfaces.get(i);
        }
        str+="\nRoom surface: "+surface;
        text.setText(str);
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

    private void selectHeight(){
        final Dialog dialog = new Dialog(this);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.height_dialog);

        Window window = dialog.getWindow();
        if(window != null){
            window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(layoutParams);
        }

        dialog.findViewById(R.id.okbutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roomHeight = Float.parseFloat(((TextView)dialog.findViewById(R.id.height)).getText().toString());
                dialog.dismiss();
            }
        });


        dialog.show();
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
            case R.id.back:
                if(distances.size()>0){
                    distances.remove(distances.size()-1);
                }
                getIntent().putExtra("distances",distances);
                recreate();
                break;
            case R.id.restart:
                distances.clear();
                recreate();
                break;
            case R.id.showMeasures:
                showMeasures();
                break;
            case R.id.height:
                selectHeight();
                break;
            case R.id.map:
                showMap();
                break;
        }

        return true;
    }

}