package com.novoseltech.handymano.views.professional.job;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.novoseltech.handymano.R;
import com.novoseltech.handymano.adapter.SliderAdapter;
import com.novoseltech.handymano.model.SliderItem;
import com.novoseltech.handymano.views.message.ChatActivity;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.IndicatorView.draw.controller.DrawController;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import java.util.List;
import java.util.Locale;

public class ViewJob extends AppCompatActivity {

    //Layout components
    private SliderView sliderView;
    private Button btnMessageAdvertiser;
    private TextView tv_proJobTitle;
    private TextView tv_tradeJobDescription;
    private TextView tv_tradePostedBy;
    private TextView tv_jobAddress;

    //Firebase components
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();

    //Objects
    private String JOB_ID;
    private String USER_ID;
    private String USER_NAME;
    private String jobCreationDate = "";
    long imageCount = 0;
    private SliderAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_job);

        JOB_ID = getIntent().getStringExtra("JOB_ID");
        USER_ID = getIntent().getStringExtra("USER_ID");

        tv_proJobTitle = findViewById(R.id.tv_professionalJobTitle);
        tv_proJobTitle.setText(JOB_ID);
        tv_tradeJobDescription = findViewById(R.id.tv_tradeJobDescription);
        tv_tradePostedBy = findViewById(R.id.tv_postedBy);
        tv_jobAddress = findViewById(R.id.tv_jobAddressTradeView);
        btnMessageAdvertiser = findViewById(R.id.btn_messageAdvertiser);
        sliderView = findViewById(R.id.is_tradeJobImagesSlider);
        adapter = new SliderAdapter(getApplicationContext());
        sliderView.setSliderAdapter(adapter);

        fStore.collection("user").document(USER_ID)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    USER_NAME = documentSnapshot.getString("username");

                }
            }
        });

        DocumentReference documentReference = fStore.collection("user")
                .document(USER_ID)
                .collection("jobs")
                .document(JOB_ID);

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    tv_tradeJobDescription.setText(documentSnapshot.getString("description"));
                    jobCreationDate = documentSnapshot.getString("creation_date");
                    imageCount = documentSnapshot.getLong("imageCount");

                    GeoPoint gp = documentSnapshot.getGeoPoint("location");
                    tv_jobAddress.setText(getCompleteAddressString(gp.getLatitude(), gp.getLongitude()));

                }
            }
        });

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                addImagesToSlider(sliderView);
            }
        }, 300);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM); //set indicator animation by using IndicatorAnimationType. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
                sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
                sliderView.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH);
                sliderView.setIndicatorSelectedColor(Color.WHITE);
                sliderView.setIndicatorUnselectedColor(Color.GRAY);
                sliderView.setScrollTimeInSec(4); //set scroll delay in seconds :
                sliderView.startAutoCycle();

                sliderView.setOnIndicatorClickListener(new DrawController.ClickListener() {
                    @Override
                    public void onIndicatorClicked(int position) {
                        //Log.i("GGG", "onIndicatorClicked: " + sliderView.getCurrentPagePosition());
                    }
                });

                tv_tradePostedBy.setText("Posted by " + USER_NAME + " on " + jobCreationDate);
            }
        }, 600);

        btnMessageAdvertiser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewJob.this, ChatActivity.class);
                intent.putExtra("MODE", "JOB_VISIT");
                intent.putExtra("ADVERTISER_ID", USER_ID);
                intent.putExtra("ADVERTISER_NAME", USER_NAME);
                intent.putExtra("JOB_ID", JOB_ID);
                intent.putExtra("JOB_DATE", jobCreationDate);
                intent.putExtra("JOB_ADDRESS", tv_jobAddress.getText());
                startActivity(intent);
            }
        });


    }

    public void addImagesToSlider(View view){
        if(imageCount == 0){
            //tv_currentImg.setVisibility(View.INVISIBLE);
        }else{
            StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                    .child("images")
                    .child(USER_ID)
                    .child("jobs")
                    .child(JOB_ID);

            for(int l = 0; l < imageCount; l++){
                SliderItem sliderItem = new SliderItem();
                StorageReference sr = null;
                sr = storageReference.child(jobCreationDate + "_image_" + l + ".jpeg");
                sr.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            Log.d("URL: ", task.getResult().toString());
                            sliderItem.setImageUrl(task.getResult().toString());
                            adapter.addItem(sliderItem);
                        }else{
                            Log.e("Error loading images", task.getException().getLocalizedMessage());
                        }
                    }
                });
            }
        }
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
}