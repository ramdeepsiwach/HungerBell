package com.se_p2.hungerbell;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.se_p2.hungerbell.Common.Common;
import com.se_p2.hungerbell.Model.User;

import androidx.appcompat.app.AppCompatActivity;

public class WelcomeScreen extends AppCompatActivity {

    Button getStartedButton;
    User users;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user!= null) {

            Toast.makeText(this,user.getUid().toString(),Toast.LENGTH_SHORT).show();

            //startActivity(new Intent(this, HomeActivity.class));
            finish();
        }
        setContentView(R.layout.activity_welcome_screen);
        getStartedButton=findViewById(R.id.getStartedButton);
        getStartedButton.setOnClickListener(view -> {

            Intent intent =new Intent(WelcomeScreen.this,signInScreen.class);
            startActivity(intent);
        });
    }
}