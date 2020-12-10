package com.example.covidsafespaces;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class ContactFragment extends Fragment {
    private View vista;
    private Button email,phone, instagram;
    private String Email;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        vista=inflater.inflate(R.layout.fragment_contact, container, false);
        email=vista.findViewById(R.id.contact_email);
        Email="pae_safe_spaces@accenture.com";
        email.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                sendEmail(Email);
            }
        });
        phone=vista.findViewById(R.id.contact_phone);
        phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_DIAL);
                i.setData(Uri.parse("tel:93255678"));
                startActivity(i);
            }
        });
        instagram=vista.findViewById(R.id.contact_instagram);
        instagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("http://instagram.com/_u/safespaces20");
                Intent insta= new Intent(Intent.ACTION_VIEW,uri);
                insta.setPackage("com.instagram.android");
                try{
                    startActivity(insta);
                }catch(ActivityNotFoundException e){
                    startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(("http://instagram.com/_u/safespaces20"))));
                }
            }
        });
        return vista;
    }
    private void sendEmail(String recipient){
        Intent email = new Intent(Intent.ACTION_SEND);
        email.setData(Uri.parse("mailto:"));
        email.setType("text/plain");
        email.putExtra(Intent.EXTRA_EMAIL,new String[] {recipient});
        try{
            startActivity(Intent.createChooser(email,"Choose an email client"));
        }catch(Exception e){
            //Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }
}
