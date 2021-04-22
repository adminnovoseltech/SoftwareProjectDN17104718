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
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.novoseltech.handymano.MainActivity;
import com.novoseltech.handymano.R;
import com.novoseltech.handymano.views.message.ChatActivity;
import com.novoseltech.handymano.views.message.MessageMenu;
import com.novoseltech.handymano.views.standard.job.JobsActivity;
import com.novoseltech.handymano.views.standard.job.StandardJobViewActivity;
import com.novoseltech.handymano.views.standard.project.ProjectList;
import com.novoseltech.handymano.views.standard.project.ViewProject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ViewProfessionalActivity extends AppCompatActivity{

    //Navigation drawer
    DrawerLayout drawerLayout;

    TextView tv_tradeName;
    TextView tv_tradeCategory;
    TextView tv_tradeExperience;
    TextView tv_tradeLastProject;
    TextView tv_viewTradeAllProjects;
    ImageView iv_messageTrade;

    TextView tv_tradeAddress;

    //Firebase objects
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    String UID = user.getUid();


    String tradeName;
    String tradeCategory;
    String tradeExperience;
    String lastProject;
    double tradeLatitude;
    double tradeLongitude;
    GeoPoint tradeGeopoint;

    SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_professional);
        drawerLayout = findViewById(R.id.drawer_layout_standard);

        TextView tv_UserName = drawerLayout.findViewById(R.id.text_UserName_Standard);
        ShapeableImageView profileImage = drawerLayout.findViewById(R.id.profilePicture);
        if(user.getPhotoUrl() != null){
            Glide.with(getApplicationContext())
                    .load(user.getPhotoUrl())
                    .into(profileImage);
        }

        String user_id = getIntent().getStringExtra("USER_ID");


        Handler handler = new Handler();

        tv_tradeAddress = findViewById(R.id.tv_tradeAddress);



        fStore.collection("user").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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

                    Log.d("LAT", String.valueOf(tradeLatitude));
                    Log.d("LON", String.valueOf(tradeLongitude));
                    //String address = getAddressFromLatLng(tradeLatitude, tradeLongitude);
                    String address = getCompleteAddressString(tradeLatitude, tradeLongitude);
                    tv_tradeAddress.setText(address);
                }
            }
        });

        Query query = fStore.collection("user").document(user_id).
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



                iv_messageTrade = findViewById(R.id.iv_messageTrade);

                tv_tradeName.setText(tradeName);
                tv_tradeCategory.setText(tradeCategory);
                tv_tradeExperience.setText(tradeExperience);
                tv_tradeLastProject.setText("Last project: " + lastProject);



                tv_viewTradeAllProjects.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent  = new Intent(getApplicationContext(), ProjectList.class);
                        intent.putExtra("USER_ID", user_id);
                        startActivity(intent);
                    }
                });

                tv_tradeLastProject.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent  = new Intent(getApplicationContext(), ViewProject.class);
                        intent.putExtra("USER_ID", user_id);
                        intent.putExtra("PROJECT_ID", lastProject);
                        startActivity(intent);
                    }
                });

                iv_messageTrade.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ViewProfessionalActivity.this, ChatActivity.class);
                        intent.putExtra("MODE", "PROFILE_VISIT");
                        intent.putExtra("TRADE_NAME", tradeName);
                        intent.putExtra("TRADE_ID", user_id);
                        startActivity(intent);
                    }
                });



                mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.trade_map_frag);

                mapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {

                        LatLng point = new LatLng(tradeLatitude, tradeLongitude);
                        //googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 7));
                        addMarkerToTheMap(point, tradeName);
                    }
                });

            }
        }, 400);

        //String address = getAddressFromLatLng(tradeLatitude, tradeLongitude);
        //Log.d("LAT", String.valueOf(tradeLatitude));
        //Log.d("LON", String.valueOf(tradeLongitude));


       // tv_tradeAddress.setText(address);



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
        startActivity(intent);
    }

    public void ClickLogOut(View view) {
        logout();
    }

    public void ClickHome(View view) {
        Intent intent = new Intent(ViewProfessionalActivity.this, HomeActivityStandard.class);
        startActivity(intent);
    }

    public void ClickProfile(View view) {
        Intent intent = new Intent(ViewProfessionalActivity.this, StandardProfileActivity.class);
        startActivity(intent);
    }

    public void ClickMessages(View view) {
        Intent intent = new Intent(ViewProfessionalActivity.this, MessageMenu.class);
        intent.putExtra("USER_TYPE", "Standard");
        startActivity(intent);
    }


}