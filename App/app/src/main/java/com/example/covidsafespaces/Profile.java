package com.example.covidsafespaces;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

public class Profile extends AppCompatActivity implements Listener{

    private final String PASSWORDTEXT = "La contraseña debe conterner una minúscula, una mayúscula, un número y un carácter especial";
    private final String REGEXP = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\-\\._$@$!%*?&])([A-Za-z\\d$@$!%*?&]|[^ ]){8,15}$";

    private String mUsername;
    private String mMail;
    private String mPassword;
    private TextView username;
    private TextView mail;
    private TextView currentPassword;
    private TextView wrongCurrentPassword;
    private TextView password1;
    private TextView password2;
    private TextView wrongPassword;
    private boolean passwordVisibleC;
    private boolean passwordVisible1;
    private boolean passwordVisible2;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Bundle datos = getIntent().getExtras();
        mUsername = datos.getString("username");

        username = findViewById(R.id.username);
        username.setText(mUsername);
        mail = findViewById(R.id.mail);
        currentPassword = findViewById(R.id.currentPassword);
        wrongCurrentPassword = findViewById(R.id.wrongCurrentPassword);
        password1 = findViewById(R.id.password1);
        password2 = findViewById(R.id.password2);
        wrongPassword = findViewById(R.id.wrongNewPassword);

        currentPassword.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (currentPassword.getRight() - currentPassword.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        showHidePassword((TextView)v, "C");
                        return true;
                    }
                }
                return false;
            }
        });

        password1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (password1.getRight() - password1.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        showHidePassword((TextView)v, "1");
                        return true;
                    }
                }
                return false;
            }
        });

        password2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (password2.getRight() - password2.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        showHidePassword((TextView)v, "2");
                        return true;
                    }
                }
                return false;
            }
        });

        getProfileInfo();

    }

    public void changePassword(View v){
        findViewById(R.id.changePasswordLayout).setVisibility(View.VISIBLE);
    }

    public void updatePassword(View v){
        if(currentPassword.getText().toString().equals(mPassword)){
            if(password1.getText().toString().equals(password2.getText().toString())){
                if(Pattern.matches(REGEXP,password1.getText().toString())){
                    mPassword = password1.getText().toString();
                    new ServerConnection().updatePassword(mUsername, mPassword);
                    findViewById(R.id.changePasswordLayout).setVisibility(View.GONE);
                    currentPassword.setText("");
                    password1.setText("");
                    password2.setText("");
                    wrongCurrentPassword.setVisibility(View.GONE);
                    wrongPassword.setVisibility(View.GONE);
                }else{
                    wrongPassword.setText(PASSWORDTEXT);
                    wrongPassword.setVisibility(View.VISIBLE);
                    wrongCurrentPassword.setVisibility(View.GONE);
                }
            } else {
                wrongCurrentPassword.setVisibility(View.GONE);
                wrongPassword.setText("Passwords do not match");
                wrongPassword.setVisibility(View.VISIBLE);
            }
        }else {
            wrongPassword.setVisibility(View.GONE);
            wrongCurrentPassword.setVisibility(View.VISIBLE);
        }
    }

    private void getProfileInfo(){
        new ServerConnection().getProfileInfo(this, mUsername);
    }


    @Override
    public void receiveMessage(JSONObject data) {
        try {
            mMail=data.getString("mail");
            mPassword=data.getString("password");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mail.setText(mMail);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showHidePassword(TextView password, String passwordTag){
        boolean visible = false;
        switch (passwordTag){
            case "C":
                visible = passwordVisibleC;
                passwordVisibleC = !passwordVisibleC;
                break;
            case "1":
                visible = passwordVisible1;
                passwordVisible1 = !passwordVisible1;
                break;
            case "2":
                visible = passwordVisible2;
                passwordVisible2 = !passwordVisible2;
                break;
        }

        if(visible){
            password.setTransformationMethod(PasswordTransformationMethod.getInstance());
            password.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_visible, 0);
        } else{
            password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            password.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_novisible, 0);
        }
    }
}