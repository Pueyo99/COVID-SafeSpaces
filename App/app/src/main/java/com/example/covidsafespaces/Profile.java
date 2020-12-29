package com.example.covidsafespaces;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

public class Profile extends AppCompatActivity implements Listener, NavigationView.OnNavigationItemSelectedListener {

    private final String PASSWORDTEXT = "Password must contain an upper case, a lower case, a number and a special character";
    private final String REGEXP = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\-\\._$@$!%*?&])([A-Za-z\\d$@$!%*?&]|[^ ]){8,15}$";

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private NavigationView mNavigationView;

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

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mDrawerLayout = findViewById(R.id.drawer_layout);

        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open,
                R.string.drawe_close);
        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);
        mActionBarDrawerToggle.syncState();

        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        Bundle datos = getIntent().getExtras();
        //mUsername = datos.getString("username");

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
                    wrongPassword.setText(getResources().getString(R.string.passwordSecurity));
                    wrongPassword.setVisibility(View.VISIBLE);
                    wrongCurrentPassword.setVisibility(View.GONE);
                }
            } else {
                wrongCurrentPassword.setVisibility(View.GONE);
                wrongPassword.setText(getResources().getString(R.string.passwordDifferent));
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

    public void deleteAccount(View v){
        new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.deleteAccount))
                .setMessage(getResources().getString(R.string.alertDeleteMessage))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new ServerConnection().deleteAccount(Profile.this, mUsername);
                    }
                }).setNegativeButton(android.R.string.cancel,null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    @Override
    public void receiveMessage(JSONObject data) {
        try {
            switch (data.getString("function")){
                case "profile":
                    mMail=data.getString("mail");
                    mPassword=data.getString("password");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mail.setText(mMail);
                        }
                    });
                    break;
                case "delete":
                    switch (data.getString("delete")){
                        case "unsuccessful":
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(Profile.this, "Account not deleted", Toast.LENGTH_LONG).show();
                                }
                            });
                            break;
                        case "successful":
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    startActivity(new Intent(Profile.this,Login.class));
                                    finish();
                                }
                            });
                            break;
                    }
                    break;


            }
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

    @Override
    public void onBackPressed() {
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else{
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_home:
                mDrawerLayout.closeDrawer(GravityCompat.START);
                Intent home = new Intent(Profile.this, Selection.class);
                home.putExtra("username", mUsername);
                startActivity(home);
                break;
            case R.id.nav_profile:
                mDrawerLayout.closeDrawer(GravityCompat.START);
                break;
            case R.id.nav_contact:

                break;
            case R.id.nav_help:

                break;
            case R.id.nav_logout:
                startActivity(new Intent(this, Login.class));
                finish();
                break;
            case R.id.nav_exit:
                mDrawerLayout.closeDrawer(GravityCompat.START);
                moveTaskToBack(true);
                break;
        }

        return true;
    }
}