package com.example.covidsafespaces;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.regex.Pattern;

public class Register extends AppCompatActivity {

    private final String PASSWORDTEXT = "Your password is not accomplishing enough conditions ";
    private final String REGEXP = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\-\\._$@$!%*?&])([A-Za-z\\d$@$!%*?&]|[^ ]){8,15}$";
    private TextView username;
    private TextView mail;
    private TextView password1;
    private TextView password2;
    private TextView alertText;
    private Button registerButton;
    private boolean password1Visible = false;
    private boolean password2Visible = false;
    private int level=0;
    private TextView  u_case,l_case,number, s_c,level_security;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        username = findViewById(R.id.registerUsername);
        mail = findViewById(R.id.registerMail);
        password1 = findViewById(R.id.registerPassword);
        password2 = findViewById(R.id.registerpassword2);
        alertText = findViewById(R.id.wrongPassword);
        registerButton = findViewById(R.id.registerButton);
        u_case= findViewById(R.id.c_uppercase);
        l_case=findViewById(R.id.c_lowercase);
        number=findViewById(R.id.c_number);
        s_c=findViewById(R.id.c_special_character);
        level_security=findViewById(R.id.level_sec);
        level_security.setVisibility(View.INVISIBLE);
        password1.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (password1.getRight() - password1.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        showHidePassword1();
                        return true;
                    }
                }

                return false;
            }
        });

        password2.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (password2.getRight() - password2.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        showHidePassword2();
                        return true;
                    }
                }
                return false;
            }
        });
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });


    }

    private void register(){
        if(password1.getText().toString().trim().equals(password2.getText().toString().trim())){
            if(Pattern.matches(REGEXP, password1.getText().toString().trim())){
                new ServerConnection().register(username.getText().toString().trim(), mail.getText().toString().trim(),
                        password1.getText().toString().trim());
                onBackPressed();
                /*Intent i = new Intent(this, Login.class);
                //i.putExtra("username",username.getText().toString());
                startActivity(i);
                finish();

                 */

                //Toast.makeText(this, "Verification email sended", Toast.LENGTH_LONG).show();

            }else{
                alertText.setText(getResources().getString(R.string.passwordSecurity));
                alertText.setVisibility(View.VISIBLE);
            }

        } else{
            password1.setText("");
            password2.setText("");
            alertText.setText(getResources().getString(R.string.passwordDifferent));
            alertText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        level=0;
        if(password1.getText().toString().matches(".*[a-z].*")){
            l_case.setTextColor(Color.parseColor("#008000"));
            level++;
        }else{
            l_case.setTextColor(Color.parseColor("#000000"));
        }
        if(password1.getText().toString().matches(".*[A-Z].*")) {
            u_case.setTextColor(Color.parseColor("#008000"));
            level++;
        }else{
            u_case.setTextColor(Color.parseColor("#000000"));
        }
        if(password1.getText().toString().matches(".*\\d.*")) {
            number.setTextColor(Color.parseColor("#008000"));
            level++;
        }else{
            number.setTextColor(Color.parseColor("#000000"));
        }
        if(password1.getText().toString().matches(".*[\\-\\._$@$!%*?&]")) {
            s_c.setTextColor(Color.parseColor("#008000"));
            level++;
        }else{
            s_c.setTextColor(Color.parseColor("#000000"));
        }
        switch(level){
            case 0:
                level_security.setVisibility(View.INVISIBLE);
                break;
            case 1:
                level_security.setVisibility(View.VISIBLE);
                level_security.setText("Low");
                level_security.setTextColor(Color.parseColor("#FF0000"));
                break;
            case 2:
                level_security.setVisibility(View.VISIBLE);
                level_security.setText("Medium");
                level_security.setTextColor(Color.parseColor("#FFFF00"));
                break;
            case 3:
                level_security.setVisibility(View.VISIBLE);
                level_security.setText("High");
                level_security.setTextColor(Color.parseColor("#98c201"));
                break;
            case 4:
                level_security.setVisibility(View.VISIBLE);
                level_security.setText("Very high");
                level_security.setTextColor(Color.parseColor("#008000"));
                break;
            default:
        }
        return super.onKeyUp(keyCode, event);
    }

    private void showHidePassword1(){
        if(password1Visible){
            password1.setTransformationMethod(PasswordTransformationMethod.getInstance());

            password1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_visible, 0);
            password1Visible = false;
        } else{
            password1.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            password1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_novisible, 0);
            password1Visible = true;
        }
    }

    private void showHidePassword2(){
        if(password2Visible){
            password2.setTransformationMethod(PasswordTransformationMethod.getInstance());

            password2.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_visible, 0);
            password2Visible = false;
        } else{
            password2.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            password2.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_novisible, 0);
            password2Visible = true;
        }
    }

    public void showMessage(){
        LayoutInflater inflater = LayoutInflater.from(this);
        final View v = inflater.inflate(R.layout.show_capacity, null,false);

        ((TextView)v.findViewById(R.id.capacidad)).setText("The passwords entered are not the same");

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