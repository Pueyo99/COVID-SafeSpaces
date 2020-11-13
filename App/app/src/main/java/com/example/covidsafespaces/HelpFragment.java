package com.example.covidsafespaces;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class HelpFragment extends Fragment {
    private TextView text1, text2;
    private View vista;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        vista=inflater.inflate(R.layout.fragment_help, container, false);
        text1= vista.findViewById(R.id.text1);
        text1.setText("1. You have 4 markers availables to locate on the corners of the room. Do it as in the image bellow:");
        text2= vista.findViewById(R.id.text2);
        text2.setText("2. You will see the measures taken on the screen\n3. Take a picture of each winddow of the room\n4." +
                "You will see that each wall is identify with a letter");

        return vista;
    }
    }
