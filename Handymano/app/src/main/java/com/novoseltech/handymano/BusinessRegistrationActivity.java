package com.novoseltech.handymano;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.novoseltech.handymano.fragments.AddressSelect;
import com.novoseltech.handymano.views.professional.HomeActivityProfessional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BusinessRegistrationActivity extends AppCompatActivity {

    Functions func = new Functions();
    //Initializing objects
    EditText etBusinessUsername;
    EditText etBusinessEmail;
    EditText etBusinessPhoneNo;
    EditText etBusinessPassword;
    TextView or4;
    TextView or5;

    Button btn_register;
    Button btn_cancel;
    Button btn_saveLocation;
    Button btn_chooseLocation;

    //Firebase objects
    private FirebaseAuth mAuth;
    private FirebaseFirestore fStore;
    private String UID;
    private String businessCategory="N/A";
    private String businessExperience="N/A";

    //Location
    FrameLayout mapFrame;

    //Location data
    //String latitude;
    //String longitude;
    double latitude;
    double longitude;
    String radius;


    //Layout
    ConstraintLayout.LayoutParams btc;









    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_registration);

        etBusinessUsername = findViewById(R.id.registerBusinessUsername_input);
        etBusinessEmail = findViewById(R.id.registerBusinessEmail_input);
        etBusinessPhoneNo = findViewById(R.id.registerBusinessPhoneNo_input);
        etBusinessPassword = findViewById(R.id.registerBusinessPassword_input);
        or4 = findViewById(R.id.textView_or4);
        or5 = findViewById(R.id.textView_or5);

        btn_register = findViewById(R.id.btn_businessRegister);
        btn_cancel = findViewById(R.id.btn_businessRegisterCancel);
        btn_saveLocation = findViewById(R.id.btn_saveLocation);
        btn_chooseLocation = findViewById(R.id.btn_chooseLocation);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        mapFrame = findViewById(R.id.frame_location);
        mapFrame.setVisibility(View.GONE);

        btn_register.setVisibility(View.GONE);
        btn_saveLocation.setVisibility(View.GONE);
        btn_chooseLocation.setVisibility(View.VISIBLE);

        or5.setVisibility(View.GONE);

        AddressSelect af = new AddressSelect();

        //Layout params
         btc = (ConstraintLayout.LayoutParams) btn_cancel.getLayoutParams();


        btn_chooseLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("mode", "NewReg");
                af.setArguments(bundle);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_location, af)
                        .commit();

                btn_register.setVisibility(View.GONE);
                btn_cancel.setVisibility(View.GONE);
                or4.setVisibility(View.GONE);
                or5.setVisibility(View.GONE);
                btn_chooseLocation.setVisibility(View.GONE);

                btn_saveLocation.setVisibility(View.VISIBLE);
                mapFrame.setVisibility(View.VISIBLE);
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Invoke registration method
                registerBusinessUser();
            }
        });

        btn_saveLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                latitude = Double.parseDouble(af.getLocationData(0));
                longitude = Double.parseDouble(af.getLocationData(1));
                radius = af.getLocationData(2);



                if(((latitude == 0.0) && (longitude == 0.0)) || (radius == null)){
                    if(radius == null){
                        Toast.makeText(getApplicationContext(), "Radius cannot be empty", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getApplicationContext(), "Location needs to be set", Toast.LENGTH_SHORT).show();
                    }
                }else{

                    //lp.setMargins(lp.leftMargin, 180, lp.rightMargin, lp.bottomMargin);

                    btc.setMargins(btc.leftMargin, 2500, btc.rightMargin, btc.bottomMargin);
                    btn_register.setVisibility(View.VISIBLE);

                    btn_chooseLocation.setVisibility(View.VISIBLE);

                    btn_cancel.setVisibility(View.VISIBLE);
                    or4.setVisibility(View.VISIBLE);
                    or5.setVisibility(View.VISIBLE);

                    btn_saveLocation.setVisibility(View.GONE);
                    mapFrame.setVisibility(View.GONE);

                    //Toast.makeText(getApplicationContext(), radius, Toast.LENGTH_SHORT).show();
                }


            }
        });



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
        //Register user temporarily disabled
        //registerBusinessUser();
        //mapFrame.setVisibility(View.VISIBLE);


    }

    public void ClickCancelBusinessRegistration(View view){
        mapFrame.setVisibility(View.GONE);
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
                    DocumentReference docReferenceChat = fStore.collection("chat").document(UID);
                    DocumentReference docReferenceRating = fStore.collection("rating").document(UID);
                    DocumentReference docReferenceFeedback = fStore.collection("rating")
                            .document(UID).collection("feedback").document(UID);
                    Map<String, Object> user = new HashMap<>();
                    Map<String, Object> chatMap = new HashMap<>();

                    GeoPoint gp = new GeoPoint(latitude, longitude);
                    user.put("username", username);
                    user.put("email", email);
                    user.put("phoneNo", phoneNo);
                    user.put("accountType", "Professional");
                    user.put("category", businessCategory);
                    user.put("experience", businessExperience);
                    user.put("location", gp);
                    user.put("radius", radius);

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

                    docReferenceRating.set(new HashMap<String, Object>())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    docReferenceFeedback.set(new HashMap<String, Object>())
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                }
                                            });

                                }
                            });

                    /*docReferenceRating.collection("feedback").document(UID).set(new HashMap<String, Object>())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    *//*docReferenceRating.collection("feedback").document(UID).delete()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Log.d("TAG", "Document successfully deleted");
                                                    }
                                                }
                                            });*//*
                                }
                            });*/



                    Intent intent = new Intent(BusinessRegistrationActivity.this, HomeActivityProfessional.class);
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



}