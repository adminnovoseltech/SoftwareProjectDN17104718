package com.novoseltech.handymano.views.professional;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.novoseltech.handymano.views.professional.job.JobsList;
import com.novoseltech.handymano.views.professional.project.ProjectsActivity;

public class  HomeActivityProfessional extends AppCompatActivity {

    private static final String TAG = "LOG: ";
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    DrawerLayout drawerLayout;

    Functions functions;

    TextView tv_UserName;
    ShapeableImageView profileImage;

    LinearLayout homeNavLayout;
    LinearLayout messageNavLayout;
    LinearLayout jobsNavLayout;
    LinearLayout projectsNavLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_professional);
        drawerLayout = findViewById(R.id.drawer_layout);

        tv_UserName = drawerLayout.findViewById(R.id.text_UserName_Professional);
        //tv_UserName.setText(userData.get("username").toString());

        //Profile image listener on navigation drawer
        profileImage = drawerLayout.findViewById(R.id.profilePictureProfessional);

        FirebaseUser user = mAuth.getCurrentUser();

        if(mAuth.getCurrentUser().getPhotoUrl() != null){
            Glide.with(getApplicationContext())
                    .load(user.getPhotoUrl())
                    .into(profileImage);
        }else{
            Log.d(TAG, "Profile image not found. Loading default image.");
        }

        /***********************************************
         * Navigation drawer listeners
         *************************************************/

        //Profile image click lister
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivityProfessional.this, ProfessionalProfileActivity.class);
                startActivity(intent);

            }
        });

        //Linear layout listener for projects page fragment
        projectsNavLayout = drawerLayout.findViewById(R.id.projectsNavigation);
        projectsNavLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivityProfessional.this, ProjectsActivity.class);
                startActivity(intent);
            }
        });

        jobsNavLayout = drawerLayout.findViewById(R.id.jobsNavigation);
        jobsNavLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivityProfessional.this, JobsList.class);
                startActivity(intent);
            }
        });


        /*************************************************************
         * end of navigation drawer listeners
         *************************************************************/
    }

    public void ClickMenu(View view) {
        openDrawer(drawerLayout);
    }

    public static void openDrawer(DrawerLayout drawerLayout) {
        //Open drawer layout
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public void ClickHome(View view){
        //recreate the activity

        finish();
        startActivity(getIntent());
    }

    public void ClickLogOut(View view) {
        logout();
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