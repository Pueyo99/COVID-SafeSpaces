package com.example.covidsafespaces;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.covidsafespaces.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

public class ProfileFragment extends Fragment implements Listener{
    private final String REGEXP = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\-\\._$@$!%*?&])([A-Za-z\\d$@$!%*?&]|[^ ]){8,15}$";

    private View view;

    private Context context;
    private Activity activity;

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
    private int level=0;
    private TextView  uppercaseText,lowercaseText,numberText, specialCharacterText,level_security;
    private CheckBox uppercaseCheck, lowercaseCheck, numberCheck, specialCharacterCheck;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = getActivity();
        context = activity.getApplicationContext();

        mUsername = getArguments().getString("username");

    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);

        username = view.findViewById(R.id.username);
        username.setText(mUsername);
        mail = view.findViewById(R.id.mail);
        currentPassword = view.findViewById(R.id.currentPassword);
        wrongCurrentPassword = view.findViewById(R.id.wrongCurrentPassword);
        password1 = view.findViewById(R.id.password1);
        password2 = view.findViewById(R.id.password2);
        wrongPassword = view.findViewById(R.id.wrongNewPassword);
        uppercaseText= view.findViewById(R.id.uppercaseText);
        lowercaseText=view.findViewById(R.id.lowercaseText);
        numberText=view.findViewById(R.id.numberText);
        specialCharacterText=view.findViewById(R.id.specialCharacterText);
        uppercaseCheck=view.findViewById(R.id.uppercaseCheck);
        lowercaseCheck=view.findViewById(R.id.lowercaseCheck);
        numberCheck=view.findViewById(R.id.numberCheck);
        specialCharacterCheck=view.findViewById(R.id.specialCharacterCheck);
        level_security=view.findViewById(R.id.level_sec);
        level_security.setVisibility(View.INVISIBLE);

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

        password1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                level=0;
                if(password1.getText().toString().matches(".*[a-z].*")){
                    lowercaseText.setTextColor(Color.parseColor("#008000"));
                    lowercaseCheck.setChecked(true);
                    level++;
                }else{
                    lowercaseText.setTextColor(Color.parseColor("#000000"));
                    lowercaseCheck.setChecked(false);
                }
                if(password1.getText().toString().matches(".*[A-Z].*")) {
                    uppercaseText.setTextColor(Color.parseColor("#008000"));
                    uppercaseCheck.setChecked(true);
                    level++;
                }else{
                    uppercaseText.setTextColor(Color.parseColor("#000000"));
                    uppercaseCheck.setChecked(false);
                }
                if(password1.getText().toString().matches(".*\\d.*")) {
                    numberText.setTextColor(Color.parseColor("#008000"));
                    numberCheck.setChecked(true);
                    level++;
                }else{
                    numberText.setTextColor(Color.parseColor("#000000"));
                    numberCheck.setChecked(false);
                }
                if(password1.getText().toString().matches(".*[\\-\\._$@$!%*?&]")) {
                    specialCharacterText.setTextColor(Color.parseColor("#008000"));
                    specialCharacterCheck.setChecked(true);
                    level++;
                }else{
                    specialCharacterText.setTextColor(Color.parseColor("#000000"));
                    specialCharacterCheck.setChecked(false);
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

        view.findViewById(R.id.deleteAccountButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAccount(v);
            }
        });

        view.findViewById(R.id.changePasswordButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.findViewById(R.id.changePasswordLayout).setVisibility(View.VISIBLE);
            }
        });

        view.findViewById(R.id.updatePasswordButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePassword(v);
            }
        });

        getProfileInfo();

        return view;

    }

    private void getProfileInfo(){
        new ServerConnection().getProfileInfo(this, mUsername);
    }

    @Override
    public void receiveMessage(JSONObject data) {
        try {
            switch (data.getString("function")){
                case "profile":
                    mMail=data.getString("mail");
                    mPassword=data.getString("password");
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mail.setText(mMail);
                        }
                    });
                    break;
                case "delete":
                    switch (data.getString("delete")){
                        case "unsuccessful":
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "Account not deleted", Toast.LENGTH_LONG).show();
                                }
                            });
                            break;
                        case "successful":
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    startActivity(new Intent(context,Login.class));
                                    activity.finish();
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

    public void updatePassword(View v){
        if(currentPassword.getText().toString().equals(mPassword)){
            if(password1.getText().toString().equals(password2.getText().toString())){
                if(Pattern.matches(REGEXP,password1.getText().toString())){
                    mPassword = password1.getText().toString();
                    new ServerConnection().updatePassword(mUsername, mPassword);
                    view.findViewById(R.id.changePasswordLayout).setVisibility(View.GONE);
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

    public void deleteAccount(View v){
        new AlertDialog.Builder(activity).setTitle(getResources().getString(R.string.deleteAccount))
                .setMessage(getResources().getString(R.string.alertDeleteMessage))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new ServerConnection().deleteAccount(ProfileFragment.this, mUsername);
                    }
                }).setNegativeButton(android.R.string.cancel,null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}