package com.novoseltech.myhandyman;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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

public class BusinessRegistrationActivity extends AppCompatActivity {

    Functions func = new Functions();
    //Initializing objects
    EditText etBusinessUsername;
    EditText etBusinessEmail;
    EditText etBusinessPhoneNo;
    EditText etBusinessPassword;

    //Firebase objects
    private FirebaseAuth mAuth;
    private FirebaseFirestore fStore;
    private String UID;
    private String businessCategory="N/A";
    private String businessExperience="N/A";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_registration);

        etBusinessUsername = findViewById(R.id.registerBusinessUsername_input);
        etBusinessEmail = findViewById(R.id.registerBusinessEmail_input);
        etBusinessPhoneNo = findViewById(R.id.registerBusinessPhoneNo_input);
        etBusinessPassword = findViewById(R.id.registerBusinessPassword_input);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();



        //Services category - creating the dropdown
        final String[] SERVICES_CATEGORY = new String[] {
                "Builder",
                "Carpenter",
                "Plumber",
                "Electrician",
                "Metal worker"
        };

        final ArrayAdapter<String> adapterSC = new ArrayAdapter<>(this, R.layout.services_category_layout, R.id.tv_1, SERVICES_CATEGORY);

        final AutoCompleteTextView dropdown_SC = findViewById(R.id.servicesCategory_dropdown);
        dropdown_SC.setAdapter(adapterSC);
        dropdown_SC.setInputType(0);

        dropdown_SC.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(dropdown_SC.getEditableText().toString().equals("Builder")){
                    businessCategory = "Builder";
                }else if(dropdown_SC.getEditableText().toString().equals("Carpenter")){
                    businessCategory = "Carpenter";
                }else if(dropdown_SC.getEditableText().toString().equals("Plumber")){
                    businessCategory = "Plumber";
                }else if(dropdown_SC.getEditableText().toString().equals("Electrician")){
                    businessCategory = "Electrician";
                }else if(dropdown_SC.getEditableText().toString().equals("Metal worker")){
                    businessCategory = "Metal worker";
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });



        //End of Services category dropdown

        //Years of experience - creating the dropdown
        final String[] YEARS_OF_EXPERIENCE = new String[] {
                "0-1 years",
                "1-3 years",
                "3-5 years",
                "5-10 years",
                "10+ years"
        };

        final ArrayAdapter<String> adapterYOE = new ArrayAdapter<>(this, R.layout.years_of_experience_layout, R.id.tv_2, YEARS_OF_EXPERIENCE);

        final AutoCompleteTextView dropdownYOE = findViewById(R.id.yearsOfExperience_dropdown);
        dropdownYOE.setAdapter(adapterYOE);
        dropdownYOE.setInputType(0);

        dropdownYOE.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(dropdownYOE.getEditableText().toString().equals("0-1 years")){
                    businessExperience = "Builder";
                }else if(dropdownYOE.getEditableText().toString().equals("1-3 years")){
                    businessExperience = "1-3 years";
                }else if(dropdownYOE.getEditableText().toString().equals("3-5 years")){
                    businessExperience = "3-5 years";
                }else if(dropdownYOE.getEditableText().toString().equals("5-10 years")){
                    businessExperience = "5-10 years";
                }else if(dropdownYOE.getEditableText().toString().equals("10+ years")){
                    businessExperience = "10+ years";
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //End of Years of experience dropdown


    }

    public void ClickRegisterBusiness(View view){
        registerBusinessUser();
    }

    public void ClickCancelBusinessRegistration(View view){
        func.redirectActivity(this, RegistrationChoiceActivity.class);
    }

    public void registerBusinessUser(){
        String username = etBusinessUsername.getText().toString().trim();
        String email = etBusinessEmail.getText().toString().trim();
        String phoneNo = etBusinessPhoneNo.getText().toString().trim();
        String password = etBusinessPassword.getText().toString().trim();

        if(username.isEmpty()){
            etBusinessUsername.setError("Username is required");
            etBusinessUsername.requestFocus();
            return;
        }else if(email.isEmpty()){
            etBusinessEmail.setError("Email address is required");
            etBusinessEmail.requestFocus();
            return;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            etBusinessEmail.setError("Please enter valid email address");
            etBusinessEmail.requestFocus();
            return;
        }else if(phoneNo.isEmpty()){
            etBusinessPhoneNo.setError("Phone number is required");
            etBusinessPhoneNo.requestFocus();
            return;
        }else if(password.isEmpty()){
            etBusinessPassword.setError("Password is required");
            etBusinessPassword.requestFocus();
            return;
        }else if(password.length() < 6){
            etBusinessPassword.setError("Minimum password length is 6 characters");
            etBusinessPassword.requestFocus();
            return;
        }else if(businessExperience.equals("N/A")){
            //dropdownYOE.setError("Please select experience level");
            //dropdownYOE.requestFocus();
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
                    user.put("accountType", "Business");
                    user.put("category", businessCategory);
                    user.put("experience", businessExperience);

                    docReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            System.out.println("onSuccess: user profile " + UID + "created!");
                        }
                    });
                    Intent intent = new Intent(BusinessRegistrationActivity.this, HomeActivity.class);
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