package com.novoseltech.handymano;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.novoseltech.handymano.views.standard.HomeActivityStandard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserRegistrationActivity extends AppCompatActivity {

    //Layout components
    private EditText etUsername;
    private EditText etEmail;
    private EditText etPhoneNo;
    private EditText etPassword;

    //Firebase components
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    //Variables
    private String UID;
    private boolean usernameMatches = false;
    private boolean phoneNoMatches = false;
    private Functions appFunctions = new Functions();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);
        
        etUsername = findViewById(R.id.registerUsername_input);
        etEmail = findViewById(R.id.registerEmail_input);
        etPhoneNo = findViewById(R.id.registerPhoneNo_input);
        etPassword = findViewById(R.id.registerPassword_input);

    }

    public void ClickRegisterUser(View view) {
        fStore.collection("user")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot documentSnapshot : task.getResult()){

                                if(documentSnapshot.getString("username").equals(etUsername.getText().toString().trim())){
                                    usernameMatches = true;
                                    Log.d("DEBUG", "Username matches");
                                }else{
                                    usernameMatches = false;
                                }

                                if(documentSnapshot.getString("phoneNo").equals(etPhoneNo.getText().toString().trim())){
                                    phoneNoMatches = true;
                                    Log.d("DEBUG", "Phone number matches");
                                }else{
                                    phoneNoMatches = false;
                                }
                            }
                        }
                    }
                });

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                registerUser();
            }
        }, 2000);


    }

    public void ClickCancelUserRegistration(View view){
        Intent intent = new Intent(UserRegistrationActivity.this, RegistrationChoiceActivity.class);
        startActivity(intent);
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
        }else if(usernameMatches){
            etUsername.setError("Username already exists. Please specify unique username");
            etUsername.requestFocus();
            return;
        }else if(appFunctions.containsOffensiveWord(etUsername.getText().toString())){
            etUsername.setError("Username contains offensive word. Please change the username to something appropriate");
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
        }else if(appFunctions.containsOffensiveWord(etEmail.getText().toString())){
            etEmail.setError("Email contains offensive word. Please change the username to something appropriate");
            etEmail.requestFocus();
            return;
        }else if(phoneNo.isEmpty()){
            etPhoneNo.setError("Phone number is required");
            etPhoneNo.requestFocus();
            return;
        }else if(phoneNoMatches){
            etPhoneNo.setError("This phone number is already used by another user. Please specify another phone number.");
            etPhoneNo.requestFocus();
            return;
        }else if(password.isEmpty()){
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }else if(!isValidPassword(password)){
            etPassword.setError("Password must at least contain 1 digit, 1 uppercase, 1 lowercase and at least 8 characters in length");
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
                    DocumentReference docReferenceChat = fStore.collection("chat").document(UID);
                    Map<String, Object> user = new HashMap<>();
                    Map<String, Object> chatMap = new HashMap<>();
                    user.put("username", username);
                    user.put("email", email);
                    user.put("phoneNo", phoneNo);
                    user.put("accountType", "Standard");

                    List<String> chatList = new ArrayList<>();
                    chatMap.put("recipients", chatList);

                    docReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            System.out.println("onSuccess: user profile " + UID + "created!");
                        }
                    });

                    docReferenceChat.set(chatMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            System.out.println("Chat list created");
                        }
                    });

                    Intent intent = new Intent(UserRegistrationActivity.this, HomeActivityStandard.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    finish();
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

    public boolean isValidPassword(final String password){
        //https://androidfreetutorial.wordpress.com/2018/01/04/regular-expression-for-password-field-in-android/
        Pattern pattern;
        Matcher matcher;

        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}$";

        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
}