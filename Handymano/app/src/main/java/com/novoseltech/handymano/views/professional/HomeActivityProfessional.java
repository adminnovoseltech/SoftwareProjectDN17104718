package com.novoseltech.handymano.views.professional;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.novoseltech.handymano.MainActivity;
import com.novoseltech.handymano.R;
import com.novoseltech.handymano.views.message.MessageMenu;
import com.novoseltech.handymano.views.professional.feedback.FeedbackList;
import com.novoseltech.handymano.views.professional.job.JobsList;
import com.novoseltech.handymano.views.professional.project.ProjectsActivity;

/**
 @author Denis Novosel
 @student_id 17104718
 @email x17104718@student.ncirl.ie
 @github https://github.com/adminnovoseltech/SoftwareProjectDN17104718
 @class HomeActivityProfessional.java
**/

public class  HomeActivityProfessional extends AppCompatActivity {

    //Layout components
    private TextView tv_tradeHomeName;
    private TextView tv_tradeHomeCategory;
    private CardView cv_tradeHomeProject;
    private CardView cv_tradeHomeJob;
    private CardView cv_tradeHomeMessage;
    private CardView cv_tradeHomeFeedback;
    private Button btn_tradePrivacy;
    private Button btn_tradeHomeLogout;
    private CircularImageView circularImageView;

    //Firebase components
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();

    //Variables
    private static final String TAG = HomeActivityProfessional.class.getSimpleName();
    private String USERNAME = "";
    private String CATEGORY = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_professional);

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
                tv_tradeHomeName = findViewById(R.id.tv_tradeHomeName);
                tv_tradeHomeName.setText(USERNAME);

                tv_tradeHomeCategory = findViewById(R.id.tv_tradeHomeCategory);
                tv_tradeHomeCategory.setText(CATEGORY);

                circularImageView = findViewById(R.id.civ_tradeHomeProfileImage);
                if(user.getPhotoUrl() != null){
                    Glide.with(getApplicationContext())
                            .load(user.getPhotoUrl())
                            .into(circularImageView);
                }else{
                    Log.d(TAG, "Profile image not found. Loading default image.");
                }

                circularImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(HomeActivityProfessional.this, ProfessionalProfileActivity.class);
                        finish();
                        startActivity(intent);
                    }
                });

                cv_tradeHomeProject = findViewById(R.id.cv_tradeHomeProject);
                cv_tradeHomeProject.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(HomeActivityProfessional.this, ProjectsActivity.class);
                        finish();
                        startActivity(intent);
                    }
                });

                cv_tradeHomeJob = findViewById(R.id.cv_tradeHomeJob);
                cv_tradeHomeJob.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(HomeActivityProfessional.this, JobsList.class);
                        finish();
                        startActivity(intent);
                    }
                });

                cv_tradeHomeMessage = findViewById(R.id.cv_tradeHomeMessage);
                cv_tradeHomeMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(HomeActivityProfessional.this, MessageMenu.class);
                        intent.putExtra("USER_TYPE", "Professional");
                        finish();
                        startActivity(intent);
                    }
                });

                cv_tradeHomeFeedback = findViewById(R.id.cv_tradeHomeFeedback);
                cv_tradeHomeFeedback.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(HomeActivityProfessional.this, FeedbackList.class);
                        finish();
                        startActivity(intent);
                    }
                });

                btn_tradePrivacy = findViewById(R.id.btn_tradePrivacy);
                btn_tradePrivacy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(HomeActivityProfessional.this, PrivacySettingActivity.class);
                        startActivity(intent);
                    }
                });

                btn_tradeHomeLogout = findViewById(R.id.btn_tradeHomeLogout);
                btn_tradeHomeLogout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        logout();
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

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //Set title
        builder.setTitle("Close application");
        //Set message
        builder.setMessage("Do you want to close the application ?");
        //Yes button
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                finishAndRemoveTask();

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