package com.example.covidsafespaces;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TermsOfUse extends AppCompatActivity {
    private TextView accept;
    private Button acceptButton;
    private CheckBox check;
    private String username;

    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_termsofuse);

        Bundle datos = getIntent().getExtras();
        username = datos.getString("username");

        acceptButton = findViewById(R.id.Accept);
        accept = findViewById(R.id.Text);
        check = (CheckBox) findViewById(R.id.chec);

        accept.setText("PRIVACY POLICY \n\n" +"This Privacy Policy establishes the terms in which COVD_SAFE_SPACES uses and protects the information that is provided by its users when using its website. This company is committed to the security of its users' data. When we ask you to fill in the fields of personal information with which you can be identified, we do so ensuring that it will only be used in accordance with the terms of this document. However, this Privacy Policy may change over time or be updated, so we recommend and emphasize that you continually review this page to ensure that you agree with said changes.\n\n"+
                "INFORMATION THAT IS COLLECTED\n\n"+"Our website may collect personal information such as: Name, contact information such as your email address and demographic information. Likewise, when necessary, specific information may be required to process an order or make a delivery or billing.\n\n"+
                "USE OF THE INFORMATION COLLECTED\n\n"+"Our website uses the information in order to provide the best possible service, particularly to keep a record of users, orders if applicable, and improve our products and services. It is possible that emails will be sent periodically through our site with special offers, new products and other advertising information that we consider relevant to you or that may provide you with some benefit, these emails will be sent to the address you provide and may be canceled anytime.\n"+
                "COVID_SAFE_SPACES is highly committed to fulfilling the commitment to keep your information secure. We use the most advanced systems and constantly update them to ensure that there is no unauthorized access.\n\n"+
                "CONTROL OF YOUR PERSONAL INFORMATION\n\n"+"At any time you can restrict the collection or use of personal information that is provided to our website. Each time you are asked to fill in a form, such as user registration, you can mark or unmark the option to receive information by email. In case you have marked the option to receive our newsletter or advertising, you can cancel it at any time.\n" +
                "This company will not sell, transfer or distribute the personal information that is collected without your consent, unless required by a judge with a court order.\n" +
                " It reserves the right to change the terms of this Privacy Policy at any time.\n");
        accept.setMovementMethod(new ScrollingMovementMethod());
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(check.isChecked()==true){
                    Intent i = new Intent(TermsOfUse.this, Main.class);
                    i.putExtra("username", username);
                    startActivity(i);
                }else{
                    String reply ="You must accept the terms uf use and conditions";
                    Toast.makeText(getApplicationContext(),reply,Toast.LENGTH_SHORT).show();
                }



            }
        });

    }
}

