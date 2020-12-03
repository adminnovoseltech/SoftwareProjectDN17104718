package com.novoseltech.myhandyman;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserRegistrationActivity extends AppCompatActivity {

    Functions func = new Functions();

    //Initializing objects
    EditText etUsername;
    EditText etEmail;
    EditText etPhoneNo;
    EditText etPassword;

    //Firebase objects
    private FirebaseAuth mAuth;
    private FirebaseFirestore fStore;
    private String UID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);
        
        etUsername = findViewById(R.id.registerUsername_input);
        etEmail = findViewById(R.id.registerEmail_input);
        etPhoneNo = findViewById(R.id.registerPhoneNo_input);
        etPassword = findViewById(R.id.registerPassword_input);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();


    }

    public void ClickRegisterUser(View view) {
        registerUser();

    }

    public void ClickCancelUserRegistration(View view){
        func.redirectActivity(this, RegistrationChoiceActivity.class);
    }
    
    public void registerUser(){
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phoneNo = etPhoneNo.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if(username.isEmpty()){
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return;
        }else if(email.isEmpty()){
            etEmail.setError("Email address is required");
            etEmail.requestFocus();
            return;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            etEmail.setError("Please enter valid email address");
            etEmail.requestFocus();
            return;
        }else if(phoneNo.isEmpty()){
            etPhoneNo.setError("Phone number is required");
            etPhoneNo.requestFocus();
            return;
        }else if(password.isEmpty()){
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }else if(password.length() < 6){
            etPassword.setError("Minimum password length is 6 characters");
            etPassword.requestFocus();
            return;
        }
        
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(), "User registered successfully", Toast.LENGTH_SHORT).show();
                    UID = mAuth.getCurrentUser().getUid();
                    DocumentReference docReference = fStore.collection("user").document(UID);
                    Map<String, Object> user = new HashMap<>();
                    user.put("username", username);
                    user.put("email", email);
                    user.put("phoneNo", phoneNo);
                    user.put("accountType", "Standard");

                    docReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            System.out.println("onSuccess: user profile " + UID + "created!");
                        }
                    });
                    Intent intent = new Intent(UserRegistrationActivity.this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }else{
                    if(task.getException() instanceof FirebaseAuthUserCollisionException){
                        Toast.makeText(getApplicationContext(), "Email already registered", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}