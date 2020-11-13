package com.example.covidsafespaces;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ChangePassword extends AppCompatActivity {
    private TextView password1;
    private TextView password2;
    private TextView msg;
    private Button updateButton;
    private boolean password1Visible = false;
    private boolean password2Visible = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        msg= findViewById(R.id.msg);
        password1= findViewById(R.id.newPassword1);
        password2= findViewById(R.id.newPassword2);
        updateButton= findViewById(R.id.buttonUpdate);
        password1.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;
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
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(password1.getText().toString().trim().equals(password2.getText().toString().trim())){
                    String reply ="Your password has been updated";
                    Toast.makeText(getApplicationContext(),reply,Toast.LENGTH_LONG).show();
                    Intent i = new Intent(ChangePassword.this, Drawer.class);
                    startActivity(i);
                }else{
                    msg.setVisibility(View.VISIBLE);
                }
            }
        });
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
}