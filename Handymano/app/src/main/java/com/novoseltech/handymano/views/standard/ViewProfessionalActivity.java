package com.novoseltech.handymano.views.standard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.novoseltech.handymano.R;
import com.novoseltech.handymano.views.standard.project.ProjectList;
import com.novoseltech.handymano.views.standard.project.ViewProject;

import java.util.List;

public class ViewProfessionalActivity extends AppCompatActivity{

    TextView tv_tradeName;
    TextView tv_tradeCategory;
    TextView tv_tradeExperience;
    TextView tv_tradeLastProject;
    TextView tv_viewTradeAllProjects;

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

        String user_id = getIntent().getStringExtra("USER_ID");


        Handler handler = new Handler();



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




}