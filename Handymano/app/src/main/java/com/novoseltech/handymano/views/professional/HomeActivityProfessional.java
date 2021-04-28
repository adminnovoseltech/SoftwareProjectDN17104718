package com.novoseltech.handymano.views.professional;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.novoseltech.handymano.Functions;
import com.novoseltech.handymano.MainActivity;
import com.novoseltech.handymano.R;
import com.novoseltech.handymano.views.message.MessageMenu;
import com.novoseltech.handymano.views.professional.job.JobsList;
import com.novoseltech.handymano.views.professional.project.ProjectsActivity;

import java.util.HashMap;
import java.util.Map;

public class  HomeActivityProfessional extends AppCompatActivity {

    private static final String TAG = "LOG: ";
    FirebaseAuth mAuth = FirebaseAuth.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_professional);

        FirebaseUser user = mAuth.getCurrentUser();


        Map<String, Object> userHash;

        if(getIntent().hasExtra("USER_MAP")){
            userHash = (HashMap<String, Object>) getIntent().getSerializableExtra("USER_MAP");
        }else{
            userHash = new HashMap<>();
        }

        TextView tv_tradeHomeName = findViewById(R.id.tv_tradeHomeName);
        tv_tradeHomeName.setText((String)userHash.get("username"));

        TextView tv_tradeHomeCategory = findViewById(R.id.tv_tradeHomeCategory);
        tv_tradeHomeCategory.setText((String)userHash.get("category"));

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
                //functions.redirectActivity(HomeActivityProfessional.this, MainActivity.class);

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