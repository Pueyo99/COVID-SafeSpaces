package com.example.covidsafespaces;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class Adapter  extends ArrayAdapter<String> {

    private Context context;
    private ArrayList<String> items;
    private LayoutInflater inflater;
    private String currItemVal = null;


    public Adapter(Context context, int resource, ArrayList<String> items ) {
        super(context, resource, items);
        this.context = context;
        this.items = items;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public String getItem(int position){
        return items.get(position);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position,convertView,parent);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position,convertView,parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent){
        View item = inflater.inflate(R.layout.item, parent, false);
        currItemVal = null;
        currItemVal = (String) items.get(position);
        TextView label = item.findViewById(R.id.spinnerItem);
        label.setText(currItemVal);
        return item;
    }
}
