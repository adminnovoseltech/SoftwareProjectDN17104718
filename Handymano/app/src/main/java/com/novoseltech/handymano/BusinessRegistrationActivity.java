package com.novoseltech.handymano;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.novoseltech.handymano.fragments.AddressSelect;
import com.novoseltech.handymano.views.professional.HomeActivityProfessional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 @author Denis Novosel
 @student_id 17104718
 @email x17104718@student.ncirl.ie
 @github https://github.com/adminnovoseltech/SoftwareProjectDN17104718
 @class BusinessRegistrationActivity.java
 **/

public class BusinessRegistrationActivity extends AppCompatActivity {

    //Layout components
    private EditText etBusinessUsername;
    private EditText etBusinessEmail;
    private EditText etBusinessPhoneNo;
    private EditText etBusinessPassword;
    private TextView or4;
    private Button btn_register;
    private Button btn_cancel;
    private Button btn_chooseLocation;
    private FrameLayout mapFrame;
    private ConstraintLayout cl_regFormBusiness;
    private ConstraintLayout.LayoutParams btc;

    //Firebase components
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    //Variables
    private String UID;
    private String businessCategory="N/A";
    private String businessExperience="N/A";
    private boolean usernameMatches = false;
    private boolean phoneNoMatches = false;
    private double latitude;
    private double longitude;
    private String radius;
    private AddressSelect af = new AddressSelect();
    private Functions appFunctions = new Functions();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_registration);

        etBusinessUsername = findViewById(R.id.registerBusinessUsername_input);
        etBusinessEmail = findViewById(R.id.registerBusinessEmail_input);
        etBusinessPhoneNo = findViewById(R.id.registerBusinessPhoneNo_input);
        etBusinessPassword = findViewById(R.id.registerBusinessPassword_input);
        or4 = findViewById(R.id.textView_or4);
        btn_register = findViewById(R.id.btn_businessRegister);
        btn_cancel = findViewById(R.id.btn_businessRegisterCancel);
        btn_chooseLocation = findViewById(R.id.btn_chooseLocation);
        mapFrame = findViewById(R.id.frame_location);
        mapFrame.setVisibility(View.GONE);
        cl_regFormBusiness = findViewById(R.id.cl_regFormBusiness);
        btn_register.setVisibility(View.GONE);
        btn_chooseLocation.setVisibility(View.VISIBLE);
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

                cl_regFormBusiness.setVisibility(View.GONE);
                mapFrame.setVisibility(View.VISIBLE);
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                fStore.collection("user")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful()){
                                    for(QueryDocumentSnapshot documentSnapshot : task.getResult()){

                                        if(documentSnapshot.getString("username").equals(etBusinessUsername.getText().toString().trim())){
                                            usernameMatches = true;
                                        }else{
                                            usernameMatches = false;
                                        }

                                        if(documentSnapshot.getString("phoneNo").equals(etBusinessPhoneNo.getText().toString().trim())){
                                            phoneNoMatches = true;
                                        }else{
                                            phoneNoMatches = false;
                                        }
                                    }
                                }
                            }
                        });

                //Invoke registration method
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        registerBusinessUser();
                    }
                }, 2000);

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
                    businessExperience = "0-1 years";
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

    public void ClickCancelBusinessRegistration(View view){
        mapFrame.setVisibility(View.GONE);
        finish();
        Intent intent = new Intent(BusinessRegistrationActivity.this, RegistrationChoiceActivity.class);
        startActivity(intent);
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
        }else if(usernameMatches){
            etBusinessUsername.setError("Username already exists. Please specify unique username");
            etBusinessUsername.requestFocus();
            return;
        }else if(appFunctions.containsOffensiveWord(etBusinessUsername.getText().toString())){
            etBusinessUsername.setError("Username contains offensive word. Please change the username to something appropriate");
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
        }else if(appFunctions.containsOffensiveWord(etBusinessEmail.getText().toString())){
            etBusinessEmail.setError("Email contains offensive word. Please change the username to something appropriate");
            etBusinessEmail.requestFocus();
            return;
        }else if(phoneNo.isEmpty()){
            etBusinessPhoneNo.setError("Phone number is required");
            etBusinessPhoneNo.requestFocus();
            return;
        }else if(phoneNoMatches){
            etBusinessPhoneNo.setError("This phone number is already used by another user. Please specify another phone number.");
            etBusinessPhoneNo.requestFocus();
            return;
        }
        else if(password.isEmpty()){
            etBusinessPassword.setError("Password field cannot be empty.");
            etBusinessPassword.requestFocus();
            return;
        }else if(!isValidPassword(password)){
            etBusinessPassword.setError("Password must at least contain 1 digit, 1 uppercase, 1 lowercase and at least 8 characters in length");
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
                    Toast.makeText(getApplicationContext(), "Successfully registered", Toast.LENGTH_SHORT).show();
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
                    user.put("email_visible", false);
                    user.put("phone_visible", false);

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
                                                    docReferenceFeedback.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Log.d("DOC DELETED", "");
                                                        }
                                                    });
                                                }
                                            });

                                }
                            });

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

    public void setLocationData(String lat, String lon, String rad){
        latitude = Double.parseDouble(lat);
        longitude = Double.parseDouble(lon);
        radius = rad;
        btn_register.setVisibility(View.VISIBLE);
    }
}