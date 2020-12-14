package com.example.covidsafespaces;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class RoomShapeSelection extends AppCompatActivity {
    private String username, building, room;
    private boolean edit;

    private int currentChecked, numberOfSelected;
    private ArrayList<CheckBox> checkBoxes;
    private String[] names = {"habc","habr","habl","habt"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_shape_selection);

        Bundle data = getIntent().getExtras();
        username = data.getString("username");
        building = data.getString("building");
        room = data.getString("room");
        edit = data.getBoolean("edit");

        currentChecked = 0;
        numberOfSelected=1;

        checkBoxes = new ArrayList<>();

        checkBoxes.add(findViewById(R.id.ccheck));
        checkBoxes.add(findViewById(R.id.rcheck));
        checkBoxes.add(findViewById(R.id.lcheck));
        checkBoxes.add(findViewById(R.id.tcheck));

        for (CheckBox c : checkBoxes){
            c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    checkedChangeListener(buttonView,isChecked);
                }
            });
        }
    }

    public void measure(View v){
        Intent i = new Intent(this,ARCore2.class);
        i.putExtra("username", username);
        i.putExtra("building", building);
        i.putExtra("room",room);
        i.putExtra("selectedShape", names[currentChecked]);
        i.putExtra("edit", edit);
        startActivity(i);
    }

    public void checkedChangeListener(CompoundButton buttonView, boolean isChecked){
        if(isChecked){
            numberOfSelected++;
            currentChecked = checkBoxes.indexOf(buttonView);
            if(numberOfSelected>1){
                for(int i=0; i<checkBoxes.size(); i++){
                    if(i!=currentChecked){
                        checkBoxes.get(i).setChecked(false);
                    }
                }
            }
        }else{
            checkBoxes.get(currentChecked).setChecked(true);
        }
    }
}