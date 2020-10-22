package com.example.covidsafespaces;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Register extends AppCompatActivity {
    Button registernusr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        TextView  usrregister = findViewById(R.id.usrregister);
        TextView password = findViewById(R.id.userpassword);
        TextView password2 = findViewById(R.id.userpassword2);



        registernusr = (Button)findViewById(R.id.registerbutton);

        registernusr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent entrar = new Intent(Register.this, Main.class);
                startActivity(entrar);
            }
        });


    }
}