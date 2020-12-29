package com.example.covidsafespaces;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
        setContentView(R.layout.activity_login);

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
                //new ServerConnection().login(Login.this, username.getText().toString().trim());
                Intent intent = new Intent(Login.this,Drawer.class);
                startActivity(intent);
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

            password.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_visible, 0);
            passwordVisible = false;
        } else{
            password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            password.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_novisible, 0);
            passwordVisible = true;
        }
    }

    @Override
    public void receiveMessage(JSONObject data) {
        try {
            switch (data.getString("function")){
                case "login":
                    if(data.has("password")){
                        String receivedPassword = data.getString("password");
                        if(password.getText().toString().trim().equals(receivedPassword)){
                            //Intent i = new Intent(this, Main.class);
                            Intent i = new Intent(this, MainActivity.class);
                            i.putExtra("username", username.getText().toString());
                            startActivity(i);
                            finish();
                        } else{
                            //showMessage();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ((TextView) findViewById(R.id.wrongPassword)).setVisibility(View.VISIBLE);
                                    ((TextView) findViewById(R.id.wrongUsername)).setVisibility(View.GONE);
                                    password.setText("");
                                }
                            });

                        }
                    } else{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((TextView) findViewById(R.id.wrongUsername)).setVisibility(View.VISIBLE);
                                ((TextView) findViewById(R.id.wrongPassword)).setVisibility(View.GONE);
                            }
                        });
                    }
                    break;
                case "recover":
                    if(data.has("error")){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((TextView) findViewById(R.id.wrongUsername)).setVisibility(View.VISIBLE);
                                ((TextView) findViewById(R.id.wrongPassword)).setVisibility(View.GONE);
                            }
                        });
                    } else{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String str = "Password sended to email";
                                Toast.makeText(Login.this, str, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    break;


            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*
    @Override
    public void receiveMessage(JSONObject data) {
        try {
            String receivedPassword = data.getString("password");
            if(password.getText().toString().trim().equals(receivedPassword)){
                //Intent i = new Intent(this, Main.class);
                Intent i = new Intent(this, Selection.class);
                i.putExtra("username", username.getText().toString());
                startActivity(i);
                finish();
            } else{
                //showMessage();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView) findViewById(R.id.wrongPassword)).setVisibility(View.VISIBLE);
                        ((TextView) findViewById(R.id.wrongUsername)).setVisibility(View.GONE);
                        password.setText("");
                    }
                });

            }
        } catch (JSONException e) {
            //If json doesn't have password key, username is wrong
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((TextView) findViewById(R.id.wrongUsername)).setVisibility(View.VISIBLE);
                    ((TextView) findViewById(R.id.wrongPassword)).setVisibility(View.GONE);
                }
            });

        }
    }

     */

    public void recoverPassword(View v){
        new ServerConnection().recover(this, username.getText().toString().trim());
    }


}