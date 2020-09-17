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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class SignUpScreen extends AppCompatActivity implements View.OnClickListener {
    EditText phoneNumberEditText,userNameEditText,signUpPasswordEditText,signUpAddressEditText;
    Button signUpButton,logInButton;
    ProgressDialog myDialog = new ProgressDialog(this);

    DatabaseReference user_table;
    FirebaseDatabase database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_screen);
        phoneNumberEditText=findViewById(R.id.signUpPhoneNumberEditText);
        userNameEditText=findViewById(R.id.usernameEditText);
        signUpPasswordEditText=findViewById(R.id.signUpPassswordEditText);
        signUpAddressEditText=findViewById(R.id.addressEditText);

        signUpButton=findViewById(R.id.signUpButton);
        logInButton=findViewById(R.id.logInOnSignUpPageButton);

        signUpButton.setOnClickListener(this);
        logInButton.setOnClickListener(this);

        //Intiate Firebase
        database=FirebaseDatabase.getInstance();
        user_table=database.getReference("User");

    }

    @Override
    public void onClick(View view) {
        myDialog.setMessage("Please wait...");
        myDialog.show();
        switch (view.getId()){
            case R.id.signUpButton:
                signUpMethod();
                break;
            case R.id.logInOnSignUpPageButton:
                Intent intent = new Intent(SignUpScreen.this, signInScreen.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                break;
        }
    }

    private void signUpMethod() {
        final String phoneNumber = phoneNumberEditText.getText().toString();
        final String password = signUpPasswordEditText.getText().toString();
        final String name = userNameEditText.getText().toString();
        final String address=signUpAddressEditText.getText().toString();
        if (TextUtils.isEmpty(phoneNumber) || phoneNumber.length() < 10) {
            myDialog.dismiss();
            phoneNumberEditText.setError("Phone number cannot be empty or less than 10 digits !");
            phoneNumberEditText.requestFocus();
        } else if (TextUtils.isEmpty(name)) {
            myDialog.dismiss();
            userNameEditText.setError("User Name cannot be empty !");
            userNameEditText.requestFocus();
        } else if (TextUtils.isEmpty(password) || password.length() < 5) {
            myDialog.dismiss();
            signUpPasswordEditText.setError("Password cannot be empty or less than 5 digits !");
            signUpPasswordEditText.requestFocus();
        } else {
            user_table.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //check if phone number already exists
                    if (dataSnapshot.child(phoneNumber).exists()) {
                        myDialog.dismiss();
                        Toast.makeText(SignUpScreen.this, "Phone Number already registered", Toast.LENGTH_SHORT).show();
                    } else {
                        myDialog.dismiss();
                        Intent intent = new Intent(SignUpScreen.this, VerifyScreen.class);
                        Bundle signUPDetails = new Bundle();
                        signUPDetails.putString("PHONE", phoneNumber);
                        signUPDetails.putString("NAME", name);
                        signUPDetails.putString("PASSWORD", password);
                        signUPDetails.putString("Address",address);
                        intent.putExtras(signUPDetails);
                        startActivity(intent);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }
}