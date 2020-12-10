package com.example.covidsafespaces;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class HelpFragment extends Fragment {
    private TextView text1, text2,text3,text4,text5,text6;
    private View vista;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        vista=inflater.inflate(R.layout.fragment_help, container, false);
        text1= vista.findViewById(R.id.text1_help);
        text1.setText("In this screen we will try to help you and to resolve your doubts about how works our app. " +
                "We have though that the best idea to help the user is to explain each screen in detail:");
        text1.setMovementMethod(new ScrollingMovementMethod());
        text2=vista.findViewById(R.id.text2_help);
        text2.setText("In this screen you will be capable to see, edit or delete your previous measurements. " +
                "If you want to make a new measurement you will have to press the 'new measure button', it will redirect" +
                " you on the AR screen. There you will see some instructions to make you know what you have to do. You will " +
                "have to locate the markers on the corners of the room by clicking on the screen.In it you will be available" +
                "to see the distance between the markers you have placed");
        text2.setMovementMethod(new ScrollingMovementMethod());
        text3=vista.findViewById(R.id.text3_help);
        text3.setText("These distances will be sent to the server to calculate " +
                "the volume of the room. When you have finished these measurements, press the CAPTURE button, you will be directed to this capture screen.");
        text3.setMovementMethod(new ScrollingMovementMethod());
        text4= vista.findViewById(R.id.text4_help);
        text4.setText("As you can see in this screen you will see some instructions of what you have to do in this part. In this part " +
                "you have two options to choose: window detection and people. If you choose window detection you will have to focus on " +
                "each window of the room and press the 'Capture button', when you have captured all the windows of the room press the 'End " +
                "button'. You will have to wait a few seconds and it will appear a message on the screen with the total capacity of the room.");
        text4.setMovementMethod(new ScrollingMovementMethod());
        text5= vista.findViewById(R.id.text5_help);
        text5.setText("If you choose the people option, you will have to take a picture showing  people faces. " +
                "When you have captured this picture it will appear at the screen a message telling you the number of people and warning " +
                "you if someone is not wearing the mask.");
        text5.setMovementMethod(new ScrollingMovementMethod());
        text6= vista.findViewById(R.id.text6_help);
        text6.setText("In this screen you will  be able to see you personal data (username and email) and to change your password if you want. If you deccide to" +
                "change it you will have to write your current password and your new password twice. Once you have finished, you press" +
                " the 'Update Password' button and your password will be updated");
        text6.setMovementMethod(new ScrollingMovementMethod());
        return vista;
    }
    }
