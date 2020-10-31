package com.se_p2.hungerbell;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.se_p2.hungerbell.Common.Common;
import com.se_p2.hungerbell.Model.User;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.firebase.ui.auth.AuthUI;

import androidx.fragment.app.FragmentTransaction;
import dmax.dialog.SpotsDialog;

import static android.R.color.background_dark;

public class WelcomeScreen extends AppCompatActivity {

    private static final int APP_REQUEST_CODE=7171;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;
    private AlertDialog dialog;
    private DatabaseReference userRef;
    private List<AuthUI.IdpConfig> providers;

    private Place placeSelected;
    private AutocompleteSupportFragment places_fragment;
    private PlacesClient placesClient;
    private List<Place.Field> placesFields= Arrays.asList(Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {

        Places.initialize(this,getString(R.string.google_maps_key));
        placesClient=Places.createClient(this);

        providers= Collections.singletonList(new AuthUI.IdpConfig.PhoneBuilder().build());
        userRef= FirebaseDatabase.getInstance().getReference(Common.USER_REF);
        firebaseAuth=FirebaseAuth.getInstance();
        dialog=new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        listener=firebaseAuthLocal->{
            FirebaseUser user=firebaseAuthLocal.getCurrentUser();
            if(user!=null){
                //Check user from firebase
                checkServerUserFromFirebase(user);
            }else{
                phoneLogin();
            }
        };
    }

    private void checkServerUserFromFirebase(FirebaseUser user) {
        dialog.show();userRef.child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            User user=snapshot.getValue(User.class);
                                goToHomeActivity(user);
                        }else{
                            //User not exist
                            dialog.dismiss();
                            showRegisterDialog(user);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        dialog.dismiss();
                        Toast.makeText(WelcomeScreen.this,""+error.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showRegisterDialog(FirebaseUser user) {
        AlertDialog.Builder builder=new AlertDialog.Builder(this,R.style.ThemeWithCorners);

        builder.setTitle("Register");
        builder.setMessage("Please fill information");
        View itemView= LayoutInflater.from(this).inflate(R.layout.layout_register,null);
        EditText edt_name=itemView.findViewById(R.id.edt_name);
        EditText edt_phone=itemView.findViewById(R.id.edt_phone);
        TextView txt_address=itemView.findViewById(R.id.txt_address_detail);

        places_fragment= (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.places_autocomplete_fragment);
        places_fragment.setPlaceFields(placesFields);
        places_fragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                placeSelected=place;
                txt_address.setText(place.getAddress());
            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(WelcomeScreen.this, ""+status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        //Set data
        edt_phone.setText(user.getPhoneNumber());


        builder.setCancelable(false)
                .setNegativeButton("CANCEL", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    startActivity(new Intent(WelcomeScreen.this,MainActivity.class));
                })
                .setPositiveButton("REGISTER", (dialogInterface, i) -> {
                    if(placeSelected!=null) {

                        if (TextUtils.isEmpty(edt_name.getText().toString())) {
                            edt_name.setError("Enter your name");
                            edt_name.requestFocus();
                            return;
                        }
                        User cUser = new User();
                        cUser.setUid(user.getUid());
                        cUser.setName(edt_name.getText().toString());
                        cUser.setPhone(edt_phone.getText().toString());
                        cUser.setAddress(txt_address.getText().toString());
                        cUser.setLat(placeSelected.getLatLng().latitude);
                        cUser.setLng(placeSelected.getLatLng().longitude);
                        dialog.show();

                        userRef.child(cUser.getUid())
                                .setValue(cUser)
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        dialog.dismiss();
                                        Toast.makeText(WelcomeScreen.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnCompleteListener(task -> {
                            dialog.dismiss();
                            Toast.makeText(WelcomeScreen.this, "Register successful ", Toast.LENGTH_SHORT).show();
                            goToHomeActivity(cUser);
                        });
                    }else{
                        Toast.makeText(this, "Please select address", Toast.LENGTH_SHORT).show();
                    }
                });

        builder.setView(itemView);

        AlertDialog registerDialog=builder.create();
        dialog.setOnDismissListener(dialogInterface -> {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.remove(places_fragment);
            fragmentTransaction.commit();
        });
        registerDialog.show();

        Button btnPositive = registerDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button btnNegative = registerDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btnPositive.getLayoutParams();
        layoutParams.weight = 10;
        btnPositive.setLayoutParams(layoutParams);
        btnNegative.setLayoutParams(layoutParams);
    }

    private void goToHomeActivity(User currentUser) {
        dialog.dismiss();
        FirebaseInstanceId.getInstance()
                .getInstanceId()
                .addOnFailureListener(e -> {
                    Toast.makeText(this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    Common.currentUser=currentUser;
                    startActivity(new Intent(WelcomeScreen.this,HomeActivity.class));
                    finish();
                }).addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                Common.currentUser=currentUser;
                Common.updateToken(WelcomeScreen.this,task.getResult().getToken());
                startActivity(new Intent(WelcomeScreen.this,HomeActivity.class));
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==APP_REQUEST_CODE){
            IdpResponse response=IdpResponse.fromResultIntent(data);
            if(resultCode==RESULT_OK){
                FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
            }else{
                Toast.makeText(this,"Failed to sign in",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void phoneLogin() {
        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),APP_REQUEST_CODE);
    }


    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {
        if(listener !=null)
            firebaseAuth.removeAuthStateListener(listener);
        
        super.onStop();
    }

}