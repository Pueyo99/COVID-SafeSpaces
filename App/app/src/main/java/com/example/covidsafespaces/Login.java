package com.example.covidsafespaces;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Login extends AppCompatActivity {

    Button entrar;
    Button registrar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prueba);

        TextView usuario = findViewById(R.id.usertext);
        TextView password = findViewById(R.id.password);
        registrar = (Button)findViewById(R.id.registrar);
        entrar = (Button)findViewById(R.id.entrar);

        entrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent entrar = new Intent(Login.this, Main.class);
                startActivity(entrar);
            }
        });

        registrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registrar = new Intent(Login.this, Register.class);
                startActivity(registrar);
            }
        });
    }
}