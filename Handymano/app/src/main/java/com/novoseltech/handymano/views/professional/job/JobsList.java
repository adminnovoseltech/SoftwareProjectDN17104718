package com.novoseltech.handymano.views.professional.job;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.novoseltech.handymano.MainActivity;
import com.novoseltech.handymano.R;
import com.novoseltech.handymano.adapter.JobsAdapter;
import com.novoseltech.handymano.views.message.MessageMenu;
import com.novoseltech.handymano.views.professional.HomeActivityProfessional;
import com.novoseltech.handymano.views.professional.ProfessionalProfileActivity;
import com.novoseltech.handymano.views.professional.feedback.FeedbackList;
import com.novoseltech.handymano.views.professional.project.ProjectsActivity;

import java.util.ArrayList;
import java.util.List;

public class JobsList extends AppCompatActivity {

    //Layout components
    private DrawerLayout drawerLayout;
    private TextView tv_drawerUsername;
    private CircularImageView profileImage;
    private RecyclerView rv_regularJobList;

    //Firebase components
    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();

    //Objects
    private List<String> usersAL = new ArrayList<>();
    private List<String> jobsAL = new ArrayList<>();
    private RecyclerView.Adapter adapter;
    private GeoPoint tradeGP;
    private String tradeRadius;
    private String tradeCategory;
    private static final String TAG = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jobs_list);

        drawerLayout = findViewById(R.id.drawer_layout_professional);
        tv_drawerUsername = drawerLayout.findViewById(R.id.text_UserName_Professional);
        profileImage = drawerLayout.findViewById(R.id.civ_profilePictureProfessional);
        rv_regularJobList = findViewById(R.id.rv_regularJobList);

        if(mAuth.getCurrentUser().getPhotoUrl() != null){
            Glide.with(getApplicationContext())
                    .load(user.getPhotoUrl())
                    .into(profileImage);
        }else{
            Log.d("TAG", "Profile image not found. Loading default image.");
        }

        //Query
        fStore.collection("user").document(user.getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    tradeGP = documentSnapshot.getGeoPoint("location");
                    tradeRadius = documentSnapshot.getString("radius");
                    tradeCategory = documentSnapshot.getString("category");
                    tv_drawerUsername.setText(documentSnapshot.getString("username"));
                }
            }
        });

        fStore.collection("user").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if(document.getString("accountType").equals("Standard")){
                                    usersAL.add(document.getId());
                                    String docId = document.getId();
                                    fStore.collection("user")
                                            .document(docId)
                                            .collection("jobs")
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    for(QueryDocumentSnapshot documentSnapshot : task.getResult()){
                                                        GeoPoint jobGP = documentSnapshot.getGeoPoint("location");
                                                        String jobCategory = documentSnapshot.getString("category");

                                                        if(documentSnapshot.getString("status").equals("Active") && (distanceBetweenTwoCoordinates(jobGP.getLatitude(), jobGP.getLongitude(),
                                                                 tradeGP.getLatitude(), tradeGP.getLongitude()) <= Double.parseDouble(tradeRadius) || tradeRadius.equals("0")) && jobCategory.equals(tradeCategory)){
                                                            jobsAL.add(docId + ',' + documentSnapshot.getId());
                                                        }

                                                    }
                                                }
                                            });
                                }
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }

                });





        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                adapter = new JobsAdapter(jobsAL, getApplicationContext());

                rv_regularJobList.setHasFixedSize(true);
                rv_regularJobList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                rv_regularJobList.setAdapter(adapter);


            }
        }, 1000);
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void ClickMenu(View view) {
        openDrawer(drawerLayout);
    }

    public void ClickProfile(View view) {
        Intent intent = new Intent(JobsList.this, ProfessionalProfileActivity.class);
        finish();
        startActivity(intent);
    }

    public void ClickProjects(View view){
        //recreate the activity

        Intent intent = new Intent(JobsList.this, ProjectsActivity.class);
        finish();
        startActivity(intent);
    }

    public void ClickJobs(View view){

        finish();
        startActivity(getIntent());

    }

    public void ClickMessages(View view){
        Intent intent = new Intent(JobsList.this, MessageMenu.class);
        intent.putExtra("USER_TYPE", "Professional");
        finish();
        startActivity(intent);
    }


    public static void openDrawer(DrawerLayout drawerLayout) {
        //Open drawer layout
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public void ClickLogOut(View view) {
        logout();
    }

    public void ClickHome(View view){
        Intent intent = new Intent(JobsList.this, HomeActivityProfessional.class);
        finish();
        startActivity(intent);
    }

    public void ClickFeedback(View view){
        Intent intent = new Intent(JobsList.this, FeedbackList.class);
        finish();
        startActivity(intent);
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
                Intent intent = new Intent(JobsList.this, MainActivity.class);
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

    public double distanceBetweenTwoCoordinates(double lat1, double lon1, double lat2, double lon2){
        //https://www.movable-type.co.uk/scripts/latlong.html
        //Used 'haversine' formula to calculate the distance between two coordinates

        int earthRadius = 6371;

        double dLat = degreesToRadians(lat2 - lat1);
        double dLon = degreesToRadians(lon2 - lon1);

        lat1 = degreesToRadians(lat1);
        lat2 = degreesToRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.sin(dLon / 2) * Math.sin(dLon / 2)
                * Math.cos(lat1) * Math.cos(lat2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadius * c;
    }

    public double degreesToRadians(double degrees){
        return degrees * (Math.PI / 180);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        //Toast.makeText(getApplicationContext(), "Works", Toast.LENGTH_SHORT).show();
    }
}