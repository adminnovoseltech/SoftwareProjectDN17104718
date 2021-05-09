package com.novoseltech.handymano.views.professional;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.novoseltech.handymano.R;
import com.novoseltech.handymano.fragments.PasswordChangeDialog;
import com.novoseltech.handymano.fragments.PasswordConfirmationDialog;

public class PrivacySettingActivity extends AppCompatActivity {

    Switch sw_email;
    Switch sw_phone;
    LinearLayout ll_passwordChange;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_setting);

        sw_email = findViewById(R.id.sw_email);
        sw_phone = findViewById(R.id.sw_phone);
        ll_passwordChange = findViewById(R.id.ll_passwordChange);



        fStore.collection("user")
                .document(user.getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    sw_email.setChecked(documentSnapshot.getBoolean("email_visible"));
                    sw_phone.setChecked(documentSnapshot.getBoolean("phone_visible"));

                }
            }
        });

        sw_email.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                fStore.collection("user")
                        .document(user.getUid())
                        .update("email_visible", isChecked)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                            }
                        });
            }
        });

        sw_phone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                fStore.collection("user")
                        .document(user.getUid())
                        .update("phone_visible", isChecked)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                            }
                        });
            }
        });

        ll_passwordChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });



    }

    public void showDialog(){
        PasswordChangeDialog exampleDialog = new PasswordChangeDialog();
        exampleDialog.show(getSupportFragmentManager(), "example dialog");
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }




}