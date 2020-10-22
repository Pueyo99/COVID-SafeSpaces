package com.example.covidsafespaces;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class Login extends AppCompatActivity implements Listener{

    private Button enter;
    private Button register;
    private TextView username;
    private TextView password;
    private boolean passwordVisible = false;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prueba);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        register = (Button)findViewById(R.id.register);
        enter = (Button)findViewById(R.id.enter);

        password.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (password.getRight() - password.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        showHidePassword();
                        return true;
                    }
                }
                return false;
            }
        });

        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ServerConnection().get(username.getText().toString().trim(), Login.this);
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent register = new Intent(Login.this, Register.class);
                startActivity(register);
            }
        });
    }

    private void showHidePassword(){
        if(passwordVisible){
            password.setTransformationMethod(PasswordTransformationMethod.getInstance());
            passwordVisible = false;
        } else{
            password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            passwordVisible = true;
        }
    }

    @Override
    public void receiveMessage(JSONObject data) {
        try {
            String receivedPassword = data.getString("password");
            if(password.getText().toString().trim().equals(receivedPassword)){
                Intent i = new Intent(this, Main.class);
                startActivity(i);
            } else{
                showMessage();
                username.setText("");
                password.setText("");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void showMessage(){
        LayoutInflater inflater = LayoutInflater.from(this);
        final View v = inflater.inflate(R.layout.show_capacity, null,false);

        ((TextView)v.findViewById(R.id.capacidad)).setText("Contraseña incorrecta");

        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;

        final PopupWindow popupWindow = new PopupWindow(v,width,height,true);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                popupWindow.showAtLocation(v, Gravity.CENTER, 0 ,0);
            }
        });

    }
}