package com.novoseltech.handymano.views.standard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.novoseltech.handymano.MainActivity;
import com.novoseltech.handymano.R;
import com.novoseltech.handymano.views.message.ChatActivity;
import com.novoseltech.handymano.views.message.MessageMenu;
import com.novoseltech.handymano.views.standard.feedback.FeedbackActivity;
import com.novoseltech.handymano.views.standard.job.JobsActivity;
import com.novoseltech.handymano.views.standard.job.StandardJobViewActivity;
import com.novoseltech.handymano.views.standard.project.ProjectList;
import com.novoseltech.handymano.views.standard.project.ViewProject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 @author Denis Novosel
 @student_id 17104718
 @email x17104718@student.ncirl.ie
 @github https://github.com/adminnovoseltech/SoftwareProjectDN17104718
 @class ViewProfessionalActivity.java
 **/

public class ViewProfessionalActivity extends AppCompatActivity{

    //Layout components
    private DrawerLayout drawerLayout;
    private TextView tv_tradeName;
    private TextView tv_tradeCategory;
    private TextView tv_tradeExperience;
    private TextView tv_tradeLastProject;
    private TextView tv_viewTradeAllProjects;
    private TextView tv_tradeAddress;
    private TextView tv_tradePhoneNo;
    private TextView tv_tradeEmailAddress;
    private ImageView iv_messageTrade;
    private CircularImageView iv_tradeProfileImageView;
    private LinearLayout ll_tradePhoneNo;
    private LinearLayout ll_tradeEmailAddress;
    private SupportMapFragment mapFragment;
    private Button btn_viewTradeFeedback;
    private RatingBar rb_tradeRating;
    private CircularImageView profileImage;
    private TextView tv_UserName;

    //Firebase objects
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();

    //Variables
    private String UID = user.getUid();
    private String tradeName;
    private String tradeCategory;
    private String tradeExperience;
    private String lastProject;
    private double tradeLatitude;
    private double tradeLongitude;
    private GeoPoint tradeGeopoint;
    private int oneStarCount = 0;
    private int twoStarCount = 0;
    private int threeStarCount = 0;
    private int fourStarCount = 0;
    private int fiveStarCount = 0;
    private double totalRating = 0.0;
    private String TRADE_UID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_professional);
        drawerLayout = findViewById(R.id.drawer_layout_standard);

        TRADE_UID = getIntent().getStringExtra("USER_ID");

        profileImage = drawerLayout.findViewById(R.id.civ_profilePictureStandard);
        iv_tradeProfileImageView = findViewById(R.id.iv_tradeProfileImageView);

        if(user.getPhotoUrl() != null){
            Glide.with(getApplicationContext())
                    .load(user.getPhotoUrl())
                    .into(profileImage);
        }

        ll_tradePhoneNo = findViewById(R.id.ll_tradePhone);
        ll_tradePhoneNo.setVisibility(View.GONE);
        ll_tradeEmailAddress = findViewById(R.id.ll_tradeEmail);
        ll_tradeEmailAddress.setVisibility(View.GONE);
        tv_tradePhoneNo = findViewById(R.id.tv_tradePhoneNo);
        tv_tradeEmailAddress = findViewById(R.id.tv_tradeEmail);

        tv_UserName = drawerLayout.findViewById(R.id.text_UserName_Standard);
        fStore.collection("user")
                .document(user.getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    tv_UserName.setText(documentSnapshot.getString("username"));
                }
            }
        });

        StorageReference storageReference = FirebaseStorage.getInstance()
                .getReference().child("images")
                .child(TRADE_UID)
                .child("profile_image_" + TRADE_UID + ".jpeg");

        storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {

                if(task.isSuccessful()){
                    Log.d("DOWNLOAD URL", task.getResult().toString());

                    Glide.with(getApplicationContext())
                            .load(task.getResult().toString())
                            .into(iv_tradeProfileImageView);
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Glide.with(getApplicationContext())
                        .load(R.drawable.ic_profile_512)
                        .into(iv_tradeProfileImageView);
            }
        });




        Handler handler = new Handler();

        tv_tradeAddress = findViewById(R.id.tv_tradeAddress);
        rb_tradeRating = findViewById(R.id.rb_tradeRating);


        /**
         *
         * RATING STATISTICS RETRIEVAL
         *
         * **/

        fStore.collection("rating")
                .document(TRADE_UID)
                .collection("feedback")
                .whereEqualTo("stars", 5)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(DocumentSnapshot documentSnapshot : task.getResult()){
                        fiveStarCount++;
                    }
                }else{
                    Log.d("LOG", "Error getting documents");
                }
            }
        });

        fStore.collection("rating")
                .document(TRADE_UID)
                .collection("feedback")
                .whereEqualTo("stars", 4)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(DocumentSnapshot documentSnapshot : task.getResult()){
                        fourStarCount++;
                    }
                }else{
                    Log.d("LOG", "Error getting documents");
                }
            }
        });

        fStore.collection("rating")
                .document(TRADE_UID)
                .collection("feedback")
                .whereEqualTo("stars", 3)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(DocumentSnapshot documentSnapshot : task.getResult()){
                        threeStarCount++;
                    }
                }else{
                    Log.d("LOG", "Error getting documents");
                }
            }
        });

        fStore.collection("rating")
                .document(TRADE_UID)
                .collection("feedback")
                .whereEqualTo("stars", 2)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(DocumentSnapshot documentSnapshot : task.getResult()){
                        twoStarCount++;
                    }
                }else{
                    Log.d("LOG", "Error getting documents");
                }
            }
        });

        fStore.collection("rating")
                .document(TRADE_UID)
                .collection("feedback")
                .whereEqualTo("stars", 1)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(DocumentSnapshot documentSnapshot : task.getResult()){
                        oneStarCount++;
                    }
                }else{
                    Log.d("LOG", "Error getting documents");
                }
            }
        });


        fStore.collection("user").document(TRADE_UID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    tradeName = documentSnapshot.getString("username");
                    tradeCategory = documentSnapshot.getString("category");
                    tradeExperience = documentSnapshot.getString("experience");
                    tradeGeopoint = documentSnapshot.getGeoPoint("location");
                    tradeLatitude = tradeGeopoint.getLatitude();
                    tradeLongitude = tradeGeopoint.getLongitude();

                    if(documentSnapshot.getBoolean("phone_visible")){
                        ll_tradePhoneNo.setVisibility(View.VISIBLE);
                        tv_tradePhoneNo.setText(documentSnapshot.getString("phoneNo"));
                    }

                    if(documentSnapshot.getBoolean("email_visible")){
                        ll_tradeEmailAddress.setVisibility(View.VISIBLE);
                        tv_tradeEmailAddress.setText(documentSnapshot.getString("email"));
                    }

                    Log.d("LAT", String.valueOf(tradeLatitude));
                    Log.d("LON", String.valueOf(tradeLongitude));
                    String address = getCompleteAddressString(tradeLatitude, tradeLongitude);
                    tv_tradeAddress.setText(address);
                }
            }
        });

        Query query = fStore.collection("user").document(TRADE_UID).
                collection("projects").orderBy("creation_date", Query.Direction.DESCENDING)
                .limit(1);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    QuerySnapshot querySnapshot = task.getResult();
                    List<DocumentSnapshot> documentSnapshot = querySnapshot.getDocuments();
                    if(documentSnapshot.isEmpty()){
                        lastProject = "User has no projects created";
                    }else{
                        lastProject = documentSnapshot.get(0).getId();
                    }



                }else{
                    lastProject = task.getException().getLocalizedMessage();
                }
            }
        });



        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                tv_tradeName = findViewById(R.id.tv_tradeName);
                tv_tradeCategory = findViewById(R.id.tv_tradeCategory);
                tv_tradeExperience = findViewById(R.id.tv_tradeExperience);
                tv_tradeLastProject = findViewById(R.id.tv_tradeLastProject);
                tv_viewTradeAllProjects = findViewById(R.id.tv_tradeViewAllProjects);

                //Rating
                btn_viewTradeFeedback = findViewById(R.id.btn_viewTradeFeedback);
                //rating calculation
                int totalRates = oneStarCount + twoStarCount + threeStarCount + fourStarCount + fiveStarCount;
                double totalScore = oneStarCount + (twoStarCount * 2) + (threeStarCount * 3) + (fourStarCount * 4) + (fiveStarCount * 5);
                totalRating = totalScore / totalRates;

                Log.d("TAG", "Start of log");
                Log.d("TOTAL RATES 4", String.valueOf(fourStarCount));
                Log.d("TOTAL SCORE", String.valueOf(totalScore));

                rb_tradeRating.setRating((float) totalRating);


                iv_messageTrade = findViewById(R.id.iv_messageTrade);

                tv_tradeName.setText(tradeName);
                tv_tradeCategory.setText(tradeCategory);
                tv_tradeExperience.setText(tradeExperience + " of experience");
                tv_tradeLastProject.setText("Last project: " + lastProject);

                if(tv_tradeLastProject.getText().toString().equals("Last project: User has no projects created")){
                    tv_viewTradeAllProjects.setVisibility(View.GONE);
                }


                btn_viewTradeFeedback.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ViewProfessionalActivity.this, FeedbackActivity.class);
                        intent.putExtra("USER_ID", TRADE_UID);
                        intent.putExtra("TRADE_USERNAME", tradeName);
                        startActivity(intent);
                    }
                });

                tv_viewTradeAllProjects.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent  = new Intent(getApplicationContext(), ProjectList.class);
                        intent.putExtra("USER_ID", TRADE_UID);
                        startActivity(intent);
                    }
                });

                tv_tradeLastProject.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(!tv_tradeLastProject.getText().toString().equals("Last project: User has no projects created")){
                            Intent intent  = new Intent(getApplicationContext(), ViewProject.class);
                            intent.putExtra("USER_ID", TRADE_UID);
                            intent.putExtra("PROJECT_ID", lastProject);
                            startActivity(intent);
                        }

                    }
                });

                iv_messageTrade.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ViewProfessionalActivity.this, ChatActivity.class);
                        intent.putExtra("MODE", "PROFILE_VISIT");
                        intent.putExtra("TRADE_NAME", tradeName);
                        intent.putExtra("TRADE_ID", TRADE_UID);
                        startActivity(intent);
                    }
                });



                mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.trade_map_frag);

                mapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {

                        LatLng point = new LatLng(tradeLatitude, tradeLongitude);
                        addMarkerToTheMap(point, tradeName);
                    }
                });

            }
        }, 1000);

    }

    public void addMarkerToTheMap(LatLng point, String pinText){

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                googleMap.clear();



                //Create marker
                MarkerOptions options = new MarkerOptions().position(point)
                        .title(pinText);
                //Zoom to location
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 10));



                //Add marker
                googleMap.addMarker(options);
            }
        });
    }

    public String getAddressFromLatLng(double lat, double lng){
        Geocoder geocoder;
        List<Address> addresses;
        String address = null;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(lat, lng, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();


        } catch (IOException e) {
            e.printStackTrace();
        }

        return address;

    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.d("My Current", strReturnedAddress.toString());
            } else {
                Log.d("My Current", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("My Current", "Cannot get Address!");
        }
        return strAdd;
    }


    public void logout(){
        //Close app
        //Initialize alert dialog
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
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
                Intent intent = new Intent(ViewProfessionalActivity.this, MainActivity.class);
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

    public void ClickMenu(View view) {
        openDrawer(drawerLayout);
    }

    public static void openDrawer(DrawerLayout drawerLayout) {
        //Open drawer layout
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public static void closeDrawer(DrawerLayout drawerLayout) {
        //Close drawer layout
        //Check condition
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            //When drawer is open
            //Close drawer
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    public void ClickJobs(View view){
        //recreate the activity

        Intent intent = new Intent(ViewProfessionalActivity.this, JobsActivity.class);
        finish();
        startActivity(intent);
    }

    public void ClickLogOut(View view) {
        logout();
    }

    public void ClickHome(View view) {
        Intent intent = new Intent(ViewProfessionalActivity.this, HomeActivityStandard.class);
        finish();
        startActivity(intent);
    }

    public void ClickProfile(View view) {
        Intent intent = new Intent(ViewProfessionalActivity.this, StandardProfileActivity.class);
        finish();
        startActivity(intent);
    }

    public void ClickMessages(View view) {
        Intent intent = new Intent(ViewProfessionalActivity.this, MessageMenu.class);
        intent.putExtra("USER_TYPE", "Standard");
        finish();
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
}