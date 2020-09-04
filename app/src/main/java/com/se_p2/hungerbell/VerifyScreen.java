package com.se_p2.hungerbell;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.se_p2.hungerbell.Model.User;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


public class VerifyScreen extends AppCompatActivity {
    EditText otpEditText;
    Button verifyButton,resendButton,backButton;
    private String mVerificationId,name,password,phone;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;
    DatabaseReference user_table;
    FirebaseDatabase database;
    ProgressDialog myDialog;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_screen);
        //Intialize edit text and buttons
        otpEditText=(EditText)findViewById(R.id.otpEditText);
        verifyButton=(Button)findViewById(R.id.verifyButton);
        resendButton=(Button)findViewById(R.id.resendOtp);
        backButton=(Button)findViewById(R.id.backButton);


        //Intiate Firebase
        database= FirebaseDatabase.getInstance();
        user_table=database.getReference("User");
        mAuth = FirebaseAuth.getInstance();

        //Get data from sign up page
        final Bundle signUpDetails=getIntent().getExtras();
        assert signUpDetails != null;
        phone = signUpDetails.getString("PHONE");
        name = signUpDetails.getString("NAME");
        password = signUpDetails.getString("PASSWORD");

        //Send OTP
        sendVerificationCode(phone);

        //Verify button click Listener
        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = otpEditText.getText().toString();
                if (code.isEmpty() || code.length() < 6) {
                    otpEditText.setError("Enter valid code");
                    otpEditText.requestFocus();
                    return;
                }
                //verifying the code entered manually
                verifyVerificationCode(code);
            }
        });

        //Resend Button ClickListener
        resendButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                myDialog=new ProgressDialog(VerifyScreen.this);
                myDialog.setMessage("Please wait...");
                myDialog.show();
                Toast.makeText(VerifyScreen.this,"OTP Resent !",Toast.LENGTH_LONG).show();
                resendVerificationCode(phone, mResendToken);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent(VerifyScreen.this,SignUpScreen.class);
                startActivity(intent);
            }
        });

    }

    //Send OTP to provided phone number
    private void sendVerificationCode(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + phoneNumber,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks);
    }

    //Resend OTP to provided Button
    private void resendVerificationCode(String phoneNumber, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
        myDialog.dismiss();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks,
                forceResendingToken);
    }

    //the callback to detect the verification status
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

            //Getting the code sent by SMS
            String code = phoneAuthCredential.getSmsCode();

            //sometime the code is not detected automatically
            //in this case the code will be null
            //so user has to manually enter the code
            if (code != null) {
                otpEditText.setText(code);
                //verifying the code
                verifyVerificationCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(VerifyScreen.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);

            //storing the verification id that is sent to the user
            mVerificationId = s;
            mResendToken = forceResendingToken;
        }
    };


    private void verifyVerificationCode(String code) {
        //creating the credential
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        //signing the user
        signInWithPhoneAuthCredential(credential);
    }

    //Verify if OTP is correct
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(VerifyScreen.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //verification successful we will start the profile activity
                            User user=new User(name,password);
                            user_table.child(phone).setValue(user);
                            Toast.makeText(VerifyScreen.this,"Account Created !",Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(VerifyScreen.this, signInScreen.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                otpEditText.setError("Wrong OTP !");
                                otpEditText.requestFocus();
                            }
                        }
                    }
                });
    }
}