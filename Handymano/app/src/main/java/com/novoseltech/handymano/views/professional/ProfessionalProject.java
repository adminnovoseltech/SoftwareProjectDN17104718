package com.novoseltech.handymano.views.professional;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.novoseltech.handymano.R;

public class ProfessionalProject extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professional_project);

        String PROJECT_ID = getIntent().getStringExtra("PROJECT_ID");

        //Firebase objects
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        //Layout objects
        TextView tv_projectTitle = findViewById(R.id.tv_projectTitle);
        TextView tv_projectDescription = findViewById(R.id.tv_projectDescription);

        tv_projectTitle.setText(PROJECT_ID);




    }
}