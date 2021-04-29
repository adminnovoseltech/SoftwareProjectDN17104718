package com.novoseltech.handymano.views.professional;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.novoseltech.handymano.Functions;
import com.novoseltech.handymano.MainActivity;
import com.novoseltech.handymano.R;
import com.novoseltech.handymano.views.message.MessageMenu;
import com.novoseltech.handymano.views.professional.feedback.FeedbackList;
import com.novoseltech.handymano.views.professional.job.JobsList;
import com.novoseltech.handymano.views.professional.project.ProjectsActivity;
import com.novoseltech.handymano.views.standard.HomeActivityStandard;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class  HomeActivityProfessional extends AppCompatActivity {

    private static final String TAG = "LOG: ";
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    String USERNAME = "";
    String CATEGORY = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_professional);

        FirebaseUser user = mAuth.getCurrentUser();

        fStore = FirebaseFirestore.getInstance();
        fStore.collection("user").document(mAuth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot documentSnapshot = task.getResult();
                            USERNAME = documentSnapshot.getString("username");
                            CATEGORY = documentSnapshot.getString("category");

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Data retrieval failed", Toast.LENGTH_SHORT).show();
                    }
                });

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                TextView tv_tradeHomeName = findViewById(R.id.tv_tradeHomeName);
                tv_tradeHomeName.setText(USERNAME);

                TextView tv_tradeHomeCategory = findViewById(R.id.tv_tradeHomeCategory);
                tv_tradeHomeCategory.setText(CATEGORY);

                ImageView iv_tradeHomeProfileImage = findViewById(R.id.iv_tradeHomeProfileImage);
                if(user.getPhotoUrl() != null){
                    Glide.with(getApplicationContext())
                            .load(user.getPhotoUrl())
                            .into(iv_tradeHomeProfileImage);
                }else{
                    Log.d(TAG, "Profile image not found. Loading default image.");
                }

                CardView cv_tradeHomeProject = findViewById(R.id.cv_tradeHomeProject);
                cv_tradeHomeProject.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(HomeActivityProfessional.this, ProjectsActivity.class);
                        finish();
                        startActivity(intent);
                    }
                });

                CardView cv_tradeHomeJob = findViewById(R.id.cv_tradeHomeJob);
                cv_tradeHomeJob.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(HomeActivityProfessional.this, JobsList.class);
                        finish();
                        startActivity(intent);
                    }
                });

                CardView cv_tradeHomeMessage = findViewById(R.id.cv_tradeHomeMessage);
                cv_tradeHomeMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(HomeActivityProfessional.this, MessageMenu.class);
                        intent.putExtra("USER_TYPE", "Professional");
                        finish();
                        startActivity(intent);
                    }
                });

                CardView cv_tradeHomeFeedback = findViewById(R.id.cv_tradeHomeFeedback);
                cv_tradeHomeFeedback.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(HomeActivityProfessional.this, FeedbackList.class);
                        finish();
                        startActivity(intent);
                    }
                });

                Button btn_tradeHomeProfile = findViewById(R.id.btn_tradeHomeProfile);
                btn_tradeHomeProfile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(HomeActivityProfessional.this, ProfessionalProfileActivity.class);
                        finish();
                        startActivity(intent);
                    }
                });
            }
        }, 500);

    }


    public void logout(){
        //Close app
        //Initialize alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //Set title
        builder.setTitle("Log out");
        //Set message
        builder.setMessage("Are you sure you want to log out ?");
        //Yes button
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                FirebaseAuth.getInstance().signOut();
                finish();
                Intent intent = new Intent(HomeActivityProfessional.this, MainActivity.class);
                startActivity(intent);

            }
        });

        //No button
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Dismiss dialog
                dialogInterface.dismiss();
            }
        });
        //Show dialog
        builder.show();
    }
}