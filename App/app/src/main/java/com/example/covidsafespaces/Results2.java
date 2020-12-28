package com.example.covidsafespaces;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Collections;

public class Results2 extends AppCompatActivity {

    private ImageView maskImage;
    private String username, building,room;
    private double windowSurface;
    private int capacity, ventilationCapacity, people;
    private boolean wearingMask;
    private SeekBar seekBar;
    private TextView progress;
    private BarChart results;
    private ArrayList<BarEntry> capacidades;
    private ArrayList<Integer> values;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results2);

        Bundle extras = getIntent().getExtras();
        capacity = extras.getInt("capacity");
        windowSurface = extras.getDouble("windowSurface");
        ventilationCapacity = (int) Math.floor(windowSurface/0.125);
        people = extras.containsKey("people")?extras.getInt("people"):0;
        username = extras.getString("username");
        building = extras.getString("building");
        room = extras.getString("room");

        if(extras.containsKey("mask")){
            wearingMask = extras.getBoolean("mask");
            maskImage = findViewById(R.id.maskImage);
            if(wearingMask){
                maskImage.setImageResource(R.drawable.green_tick);
            }else {
                maskImage.setImageResource(R.drawable.red_cross);
            }
        }else{
            findViewById(R.id.maskLayout).setVisibility(View.GONE);
        }

        results = findViewById(R.id.Results);
        seekBar = findViewById(R.id.seekBar);
        progress = findViewById(R.id.progress);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Results2.this.progress.setText(progress+"%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Double currentWindowSurface = ((seekBar.getProgress())*windowSurface)/100;
                int ventilationCapacity = (int) Math.floor(currentWindowSurface/0.125);
                values.set(2,ventilationCapacity);
                capacidades.set(2,new BarEntry(2,ventilationCapacity));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        BarDataSet barDataSet = new BarDataSet(capacidades, "People, Capacity, Ventilation");
                        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                        barDataSet.setValueTextColor(android.R.color.black);
                        barDataSet.setValueTextSize(16f);
                        BarData barData = new BarData(barDataSet);
                        results.clear();
                        drawGraph(barData);
                    }
                });
            }
        });

        values = new ArrayList<>();
        values.add(people);
        values.add(capacity);
        values.add(ventilationCapacity);

        capacidades = new ArrayList<>();
        capacidades.add(new BarEntry(0, people));
        capacidades.add(new BarEntry(1, capacity));
        capacidades.add(new BarEntry(2, ventilationCapacity));

        BarDataSet barDataSet = new BarDataSet(capacidades, "People, Capacity, Ventilation");
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        barDataSet.setValueTextColor(android.R.color.black);
        barDataSet.setValueTextSize(16f);
        BarData barData = new BarData(barDataSet);
        drawGraph(barData);
    }

    private void drawGraph(BarData barData){
        //El texto del eje X
        ArrayList<String> labelsNames = new ArrayList<>();
        labelsNames.add("People");
        labelsNames.add("Capacity");
        labelsNames.add("Ventilation");

        results.setFitBars(true);
        results.setData(barData);
        results.getDescription().setText(" ");
        results.animateY(2000);
        Legend legend = results.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setForm(Legend.LegendForm.CIRCLE);
        //Eje X
        XAxis xAxis = results.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labelsNames));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(labelsNames.size());
        xAxis.setLabelRotationAngle(0);

        //Eje y
        results.getAxisLeft().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) Math.floor(value));
            }
        });
        results.getAxisRight().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) Math.floor(value));
            }
        });

        int max = Collections.max(values);
        //Toast.makeText(this,max,Toast.LENGTH_LONG).show();
        YAxis yAxis = results.getAxisLeft();
        YAxis yAxis1 = results.getAxisRight();
        yAxis.setAxisMinimum(0f);
        yAxis1.setAxisMinimum(0f);
        if(max==1){
            yAxis.setLabelCount(0,true);
            yAxis1.setLabelCount(0,true);
            yAxis.setAxisMaximum(1f);
            yAxis1.setAxisMaximum(1f);
        }else {
            yAxis.setAxisMaximum((float)max);
            yAxis1.setAxisMaximum((float)max);
            yAxis.setLabelCount(max);
            yAxis1.setLabelCount(max);
        }
        /*yAxis1.setStartAtZero(true);
        yAxis.setStartAtZero(true);

         */

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.results_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.home:
                Intent i = new Intent(Results2.this, MainActivity.class);
                i.putExtra("username",username);
                startActivity(i);
                finish();
                break;
            case R.id.people:
                Intent people = new Intent(Results2.this,Main.class);
                people.putExtra("username",username);
                people.putExtra("building",building);
                people.putExtra("room",room);
                people.putExtra("onlyPeople", true);
                startActivity(people);
                break;
        }

        return true;
    }
}