package com.se_p2.hungerbell;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.se_p2.hungerbell.Model.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class signInScreen extends AppCompatActivity implements View.OnClickListener {
    EditText phoneNumber,password;
    Button loginButton,forgotPasswordButton,signUpButton;
    DatabaseReference user_table;
    FirebaseDatabase database;
    ProgressDialog myDialog;
    User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_screen);

        phoneNumber=(EditText)findViewById(R.id.phoneNumberEditText);
        password=(EditText)findViewById(R.id.passswordEditText);
        loginButton=(Button)findViewById(R.id.logInButton);
        forgotPasswordButton=(Button)findViewById(R.id.forgotPasswordButton);
        signUpButton=(Button)findViewById(R.id.signUpOnLogInButton);

        loginButton.setOnClickListener(this);
        forgotPasswordButton.setOnClickListener(this);
        signUpButton.setOnClickListener(this);

        //Intiate Firebase
        database=FirebaseDatabase.getInstance();
        user_table=database.getReference("User");
    }

    @Override
    public void onClick(View view) {
        myDialog=new ProgressDialog(signInScreen.this);
        myDialog.setMessage("Please wait...");
        myDialog.show();
        switch (view.getId()){
            case R.id.logInButton:
                login();
                break;
            case R.id.forgotPasswordButton:
                Toast.makeText(this,"Forgot Password Button",Toast.LENGTH_LONG).show();
                break;
            case R.id.signUpOnLogInButton:
                openSignUpScreen();
                break;
        }
    }

    private void openSignUpScreen() {
        Intent intent=new Intent(signInScreen.this,SignUpScreen.class);
        startActivity(intent);
    }

    private void login() {
        user_table.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(TextUtils.isEmpty(phoneNumber.getText().toString()) || phoneNumber.getText().toString().length()<10) {
                    myDialog.dismiss();
                    phoneNumber.setError("Enter a valid number !");
                    phoneNumber.requestFocus();
                } else {
                    if (dataSnapshot.child(phoneNumber.getText().toString()).exists()) {
                        myDialog.dismiss();
                        //Get User Information
                        user = dataSnapshot.child(phoneNumber.getText().toString()).getValue(User.class);
                        if(TextUtils.isEmpty(password.getText().toString())){
                            password.setError("Password cannot be empty");
                            password.requestFocus();
                        }else {
                            if (user != null && user.getPassword().equals(password.getText().toString())) {
                                Toast.makeText(signInScreen.this, "Sign In Successfully", Toast.LENGTH_SHORT).show();
                                Intent intent=new Intent(signInScreen.this,HomeScreen.class);
                                startActivity(intent);
                            } else {
                                password.setError("Wrong Password !");
                                password.requestFocus();
                            }
                        }
                    } else {
                        myDialog.dismiss();
                        Toast.makeText(signInScreen.this, "User name does not exist !", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}