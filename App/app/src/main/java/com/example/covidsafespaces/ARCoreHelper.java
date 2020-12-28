package com.example.covidsafespaces;

import android.app.Activity;
import android.app.Application;

import java.util.ArrayList;

public class ARCoreHelper {
    private String selectedShape;
    private Activity context;

    public ARCoreHelper(String selectedShape, Activity context){
        this.selectedShape = selectedShape;
        this.context = context;
    }

    public int getImageResource(){
        switch (selectedShape){
            case "habc":
                return R.drawable.habc;
            case "habr":
                return R.drawable.habr;
            case "habl":
                return R.drawable.habl;
            case "habt":
                return R.drawable.habt;
        }
        return 0;
    }

    public String getTextHelp(){
        String text = "";
        switch (selectedShape){
            case "habc":
                return context.getResources().getString(R.string.squareHelp);
            case "habr":
                return context.getResources().getString(R.string.rectangularHelp);
            case "habl":
                return context.getResources().getString(R.string.lHelp);
            case "habt":
                return context.getResources().getString(R.string.tHelp);
        }

        return text;
    }

    public ArrayList<Float> calculateWallSurface(ArrayList<Float> distances, float roomHeight){
        ArrayList wallAreas = new ArrayList();
        for(float distance : distances){
            wallAreas.add(distance*roomHeight);
        }
        if(selectedShape.equals("habc")){
            for(int i=0; i<3; i++){
                wallAreas.add(wallAreas.get(0));
            }
        }else if(selectedShape.equals("habr")){
            wallAreas.add(wallAreas.get(0));
            wallAreas.add(wallAreas.get(1));
        }
        return wallAreas;
    }

    public float calculateSurface(ArrayList<Float> distances){
        float area = (float)0.0;
        switch (selectedShape){
            case "habc":
                area = distances.get(0)*distances.get(0);
                break;
            case "habr":
                area = distances.get(0)*distances.get(1);
                break;
            case "habl":
                area = distances.get(0)*distances.get(1)+distances.get(3)*distances.get(4);
                break;
            case "habt":
                area = distances.get(0)*distances.get(1)+distances.get(3)*distances.get(4);
                break;
        }

        return area;
    }

    public String[] getSelectionItems(){
        String wall = context.getResources().getString(R.string.wall);
        switch (selectedShape){
            case "habc":
                String[] habc = {wall+" 1",wall+" 2",wall+" 3",wall+" 4"};
                return habc;
            case "habr":
                String[] habr = {wall+" 1",wall+" 2",wall+" 3",wall+" 4"};
                return habr;
            case "habl":
                String[] habl = {wall+" 1",wall+" 2",wall+" 3",wall+" 4",wall+" 5",wall+" 6"};
                return habl;
            case "habt":
                String[] habt = {wall+" 1",wall+" 2",wall+" 3",wall+" 4",wall+" 5",wall+" 6",wall+" 7",wall+" 8"};
                return habt;

        }
        return null;
    }
}
