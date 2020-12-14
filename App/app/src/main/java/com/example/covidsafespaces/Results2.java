package com.example.covidsafespaces;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
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
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

public class Results2 extends AppCompatActivity {

    private CheckBox mask;
    private String username;
    private double windowSurface;
    private int capacity, ventilationCapacity, people;
    private SeekBar seekBar;
    private TextView progress;
    private BarChart results;
    private ArrayList<BarEntry> capacidades;

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
                capacidades.set(2,new BarEntry(2,ventilationCapacity));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        BarDataSet barDataSet = new BarDataSet(capacidades, "People, Capacity, Ventilation");
                        //Porque no cambia el colo
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

        mask=findViewById(R.id.mask_check);
        mask.setChecked(true);

        capacidades = new ArrayList<>();
        capacidades.add(new BarEntry(0, people));
        capacidades.add(new BarEntry(1, capacity));
        capacidades.add(new BarEntry(2, ventilationCapacity));

        BarDataSet barDataSet = new BarDataSet(capacidades, "People, Capacity, Ventilation");
        //Porque no cambia el colo
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
        YAxis yAxis = results.getAxisLeft();
        YAxis yAxis1 = results.getAxisRight();
        yAxis1.setStartAtZero(true);
        yAxis.setStartAtZero(true);
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
                break;
        }

        return true;
    }
}