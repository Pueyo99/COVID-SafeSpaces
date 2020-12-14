package com.example.covidsafespaces;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.covidsafespaces.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HomeFragment extends Fragment implements Listener,SelectionListener{
    private View view;

    private Context context;
    private Activity activity;

    private Spinner buildings;
    private Adapter buildingAdapter;
    private AlertDialog alertDialog;
    private Spinner rooms;
    private Adapter roomAdapter;
    private ArrayList<String> shapes;
    private String selectedBuilding;
    private String selectedRoom;
    private String username;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = getActivity();
        context = activity.getApplicationContext();
        username = getArguments().getString("username");

        selectedBuilding = getResources().getString(R.string.selectBuilding);
        selectedRoom = getResources().getString(R.string.selectRoom);

        shapes = new ArrayList<>();


    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_home, container, false);

        buildings = view.findViewById(R.id.buildings);
        buildingAdapter = new Adapter(activity, R.layout.item, new ArrayList<String>());
        buildingAdapter.add(getResources().getString(R.string.selectBuilding));
        buildings.setAdapter(buildingAdapter);

        buildings.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedBuilding = ((TextView)view.findViewById(R.id.spinnerItem)).getText().toString();
                if(!selectedBuilding.equals(getResources().getString(R.string.selectBuilding))){
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, selectedBuilding, Toast.LENGTH_LONG).show();
                            setProgressDialog();
                        }
                    });
                    new ServerConnection().getRoom(HomeFragment.this,selectedBuilding, username);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        rooms = view.findViewById(R.id.rooms);
        roomAdapter = new Adapter(activity, R.layout.item, new ArrayList<String>());
        roomAdapter.add(getResources().getString(R.string.selectRoom));
        rooms.setAdapter(roomAdapter);

        rooms.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedRoom = ((TextView) view.findViewById(R.id.spinnerItem)).getText().toString();
                if(!selectedRoom.equals(getResources().getString(R.string.selectRoom)) && !selectedBuilding.equals(getResources().getString(R.string.selectBuilding))){
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String shape = shapes.get(roomAdapter.getItemIndex(selectedRoom)-1);
                            Toast.makeText(context, selectedRoom+"-"+shape, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        view.findViewById(R.id.getCapacityButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCapacity(v);
            }
        });

        view.findViewById(R.id.editMeasureButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editMeasure(v);
            }
        });

        view.findViewById(R.id.deleteMeasureButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteMeasure(v);
            }
        });

        view.findViewById(R.id.newMeasureButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newMeasure(v);
            }
        });

        new ServerConnection().getBuilding(this, username);
        setProgressDialog();

        return view;
    }


    public void setProgressDialog() {

        LayoutInflater inflater = LayoutInflater.from(activity);
        View v = inflater.inflate(R.layout.alert_dialog, null, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(false);
        builder.setView(v);

        alertDialog = builder.create();

        Window window = alertDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(alertDialog.getWindow().getAttributes());
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            alertDialog.getWindow().setAttributes(layoutParams);
        }
        alertDialog.show();
    }

    public void getCapacity(View v){
        new ServerConnection().getCapacity(this,username, selectedBuilding,selectedRoom);

    }

    public void deleteMeasure(View v){
        new AlertDialog.Builder(activity).setTitle(getResources().getString(R.string.deleteMeasure))
                .setMessage(getResources().getString(R.string.alertDeleteMeasure))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new ServerConnection().deleteMeasure(HomeFragment.this, username,selectedBuilding,selectedRoom);
                    }
                }).setNegativeButton(android.R.string.cancel,null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public void newMeasure(View v){
        String newBuilding = ((TextView)view.findViewById(R.id.newBuilding)).getText().toString().trim();
        String newRoom = ((TextView)view.findViewById(R.id.newRoom)).getText().toString().trim();
        if(newBuilding.equals("") || newRoom.equals("")){
            TextView newMeasureWarning = view.findViewById(R.id.newMeasureWarning);
            newMeasureWarning.setText(getResources().getString(R.string.fieldsFilled));
            newMeasureWarning.setVisibility(View.VISIBLE);
        }else{
            Intent i = new Intent(context, RoomShapeSelection.class);
            i.putExtra("username",username);
            i.putExtra("building", newBuilding);
            i.putExtra("room", newRoom);
            i.putExtra("edit", false);
            startActivity(i);
        }

    }

    public void editMeasure(View v){
        if(selectedBuilding == getResources().getString(R.string.selectBuilding)){
            Toast.makeText(context, "Select a building", Toast.LENGTH_LONG).show();
            return;
        }

        if(selectedRoom == getResources().getString(R.string.selectRoom)){
            Toast.makeText(context, "Select a room", Toast.LENGTH_LONG).show();
            return;
        }

        String shape = shapes.get(roomAdapter.getItemIndex(selectedRoom)-1);

        Intent i = new Intent(context, ARCore2.class);
        i.putExtra("username", username);
        i.putExtra("building", selectedBuilding);
        i.putExtra("room", selectedRoom);
        i.putExtra("edit", true);
        i.putExtra("selectedShape", shape);
        startActivity(i);
    }

    @Override
    public void receiveMessage(final JSONArray data, final String path) {
        alertDialog.dismiss();
        switch (path){
            case "building":
                activity.runOnUiThread(new Runnable() {
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
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        roomAdapter.clear();
                        shapes.clear();
                        roomAdapter.add(getResources().getString(R.string.selectRoom));
                        for(int i=0; i<data.length(); i++){
                            try {
                                shapes.add(data.getJSONObject(i).getString("SHAPE"));
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
                    int capacity = data.getInt("CAPACITY");
                    Double windowSurface = data.getDouble("WINDOWSURFACE");
                    int ventilationCapacity = (int) Math.floor(windowSurface/0.125);
                    Intent results = new Intent(context, Results2.class);
                    results.putExtra("capacity",capacity);
                    results.putExtra("windowSurface", windowSurface);
                    results.putExtra("username",username);
                    startActivity(results);
                    //showCapacity(String.valueOf(capacity), windowSurface);
                    break;
                case "delete":
                    switch (data.getString("delete")){
                        case "successful":
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    shapes.remove(roomAdapter.getItemIndex(selectedRoom)-1);
                                    roomAdapter.deleteItem(selectedRoom);
                                    selectedRoom=getResources().getString(R.string.selectRoom);
                                    if(roomAdapter.getSize() == 1){
                                        buildingAdapter.deleteItem(selectedBuilding);
                                        selectedBuilding=getResources().getString(R.string.selectBuilding);
                                    }
                                    rooms.setSelection(roomAdapter.getItemIndex(selectedRoom));
                                    buildings.setSelection(buildingAdapter.getItemIndex(selectedBuilding));
                                }
                            });
                            break;
                        case "unsuccessful":
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "Measure not deleted",Toast.LENGTH_LONG).show();
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

    public void showCapacity(String capacity, Double windowSurface){
        LayoutInflater inflater = LayoutInflater.from(activity);
        final View v = inflater.inflate(R.layout.show_capacity, null,false);

        String str = "This room can accommodate "+capacity+" people and has "+windowSurface+ " m2 of ventilation";
        str += ", enough for "+ (int) Math.floor(windowSurface/0.125)+" people";
        ((TextView)v.findViewById(R.id.capacidad)).setText(str);

        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;

        final PopupWindow popupWindow = new PopupWindow(v,width,height,true);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                popupWindow.showAtLocation(v, Gravity.CENTER, 0 ,0);
            }
        });

    }
}