package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.myapplication.databinding.ActivitySignUpBinding;
import com.example.myapplication.firebaseDatasetter.accountDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;


public class SignUpActivity extends AppCompatActivity {
    ActivitySignUpBinding binding;
    protected FirebaseAuth mAuth;
    protected DatabaseReference firebaseDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //------------Initializing The Firebase Instances------//
        try {
            mAuth = FirebaseAuth.getInstance();
            firebaseDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        //--------------------------------------------------//
        //--------Creating new User and Saving the User details in the database--------//
        binding.btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!binding.btnDriver.isChecked() && !binding.btnCustomer.isChecked()) {
                    Toast.makeText(SignUpActivity.this, "Select Driver or Customer", Toast.LENGTH_SHORT).show();
                } else {
                    String userName = binding.txtUsername.getText() + "";
                    String email = binding.txtEmail.getText() + "";
                    String password = binding.txtPassword.getText() + "";
                    if (!userName.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
                        ProgressDialog progressDialog = new ProgressDialog(SignUpActivity.this);
                        progressDialog.setTitle("Creating the Account");
                        progressDialog.setMessage("please be patient while we are creating your account...");
                        progressDialog.show();
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        progressDialog.dismiss();
                                        if (task.isSuccessful()) {
                                            accountDetails ac = new accountDetails();
                                            ac.setUserName(userName);
                                            ac.setEmail(email);
                                            ac.setPassword(password);
                                            if (binding.btnDriver.isChecked()) ac.setIsDriver("1");
                                            else ac.setIsDriver("0");

                                            firebaseDatabase.child(mAuth.getCurrentUser().getUid()).setValue(ac);
//                                            Toast.makeText(SignUpActivity.this, ac.getIsDriver(), Toast.LENGTH_SHORT).show();
//                                            Intent intent = new Intent(SignUpActivity.this, MapActivity.class);
//                                            intent.putExtra("isDriver", ac.getIsDriver());
//                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(SignUpActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    } else {
                        Toast.makeText(SignUpActivity.this, "Fill the Credentials...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        {
        //-----------------------------------------------------------------------------//
//        //----------Checking if the user is currently Login------------//
//
//        if (mAuth.getCurrentUser() != null) {
//            firebaseDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
//            firebaseDatabase.child(mAuth.getCurrentUser().getUid())
//                    .addListenerForSingleValueEvent(new ValueEventListener() {
//
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            if (snapshot.exists()) {
//                                String isDriver = snapshot.child("isDriver").getValue(String.class);
////                                Toast.makeText(SignUpActivity.this, isDriver, Toast.LENGTH_SHORT).show();
//                                Intent intent=new Intent(SignUpActivity.this,MapActivity.class);
//                                intent.putExtra("isDriver",isDriver);
//                                startActivity(intent);
//                               finish();
//                            } else {
//                                Toast.makeText(SignUpActivity.this, "Snap not exists", Toast.LENGTH_SHORT).show();
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//
//                        }
//                    });
//        }
//        //------------------------------------------------------------//
        }
        //--------------Already Have Account-----------------//
        binding.txtAlreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(SignUpActivity.this,MainActivity.class);
                finish();
            }
        });
        //--------------------------------------------------//
    }

}