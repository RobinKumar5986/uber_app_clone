package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;


import com.example.myapplication.Maps.CustomerMapActivity;
import com.example.myapplication.Maps.DriverMapActivity;
import com.example.myapplication.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;


public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    DatabaseReference databaseReference;
    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //--------Initializing The Firebase Instances-----------//
        try {
            mAuth = FirebaseAuth.getInstance();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        //------------------------Login In The Account------------------------------//
        binding.btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.txtEmail.getText() + "";
                String password = binding.txtPassword.getText() + "";
                ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setTitle("Login...");
                progressDialog.setMessage("Please Waite While We are Loading Your Details...");
                if (!email.isEmpty() && !password.isEmpty()) {
                    progressDialog.show();
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if (task.isSuccessful()) {
                                        databaseReference=FirebaseDatabase.getInstance().getReference().child("Users");
                                        databaseReference.child(mAuth.getCurrentUser().getUid()).
                                                addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if(snapshot.exists()){
                                                            String idDriver=snapshot.child("isDriver").getValue(String.class);
                                                            Toast.makeText(MainActivity.this, idDriver, Toast.LENGTH_SHORT).show();
                                                            //Intent Passing
                                                            progressDialog.dismiss();
                                                            if(Integer.parseInt(Objects.requireNonNull(idDriver))==1) {
                                                                Intent intent = new Intent(MainActivity.this, DriverMapActivity.class);
                                                                startActivity(intent);
                                                                finish();
                                                            }else{
                                                                Intent intent = new Intent(MainActivity.this, CustomerMapActivity.class);
                                                                startActivity(intent);
                                                                finish();
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                        Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    } else {
                                        progressDialog.dismiss();
                                    }
                                }
                            });

                } else {
                    Toast.makeText(MainActivity.this, "Enter Credentials", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //---------For Creating a New Account-----------//
        binding.txtClickSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //-----------Checking if the User is already LoggedIn------//
        if (mAuth.getCurrentUser() != null) {

            databaseReference=FirebaseDatabase.getInstance().getReference().child("Users");
            databaseReference.child(mAuth.getCurrentUser().getUid()).
                    addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                String idDriver=snapshot.child("isDriver").getValue(String.class);
                                Toast.makeText(MainActivity.this, idDriver, Toast.LENGTH_SHORT).show();
                                //Intent Passing
                                if(Integer.parseInt(Objects.requireNonNull(idDriver))==1) {
                                    Intent intent = new Intent(MainActivity.this, DriverMapActivity.class);
                                    startActivity(intent);
                                    finish();
                                }else{
                                    Intent intent = new Intent(MainActivity.this, CustomerMapActivity.class);
                                    startActivity(intent);
                                    finish();
                                }

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }

    }
}