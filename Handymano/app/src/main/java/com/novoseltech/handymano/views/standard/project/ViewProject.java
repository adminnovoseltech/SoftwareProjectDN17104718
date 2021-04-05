package com.novoseltech.handymano.views.standard.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.novoseltech.handymano.R;
import com.novoseltech.handymano.adapter.SliderAdapter;
import com.novoseltech.handymano.model.SliderItem;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.IndicatorView.draw.controller.DrawController;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import org.w3c.dom.Text;

public class ViewProject extends AppCompatActivity {

    TextView tv_tradeProjectTitle;
    TextView tv_tradeProjectDescription;


    //Image slider
    SliderView sliderView;
    private SliderAdapter adapter;
    long imageCount = 0;
    String projectCreationDate = "";

    //Firebase objects
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    String USER_ID;
    String PROJECT_ID;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_project);

        String user_id = getIntent().getStringExtra("USER_ID");
        String project_id = getIntent().getStringExtra("PROJECT_ID");

        USER_ID = user_id;
        PROJECT_ID = project_id;

        tv_tradeProjectTitle = findViewById(R.id.tv_tradeProjectTitle);
        tv_tradeProjectDescription = findViewById(R.id.tv_tradeProjectDescription);
        sliderView = findViewById(R.id.image_slider_trade_project);
        adapter = new SliderAdapter(getApplicationContext());
        sliderView.setSliderAdapter(adapter);


        DocumentReference documentReference = fStore.collection("user")
                .document(user_id)
                .collection("projects")
                .document(project_id);

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    tv_tradeProjectTitle.setText(documentSnapshot.getString("title"));
                    tv_tradeProjectDescription.setText(documentSnapshot.getString("description"));
                    projectCreationDate = documentSnapshot.getString("creation_date");
                    imageCount = documentSnapshot.getLong("imageCount");

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
                        Log.i("GGG", "onIndicatorClicked: " + sliderView.getCurrentPagePosition());
                    }
                });
            }
        }, 600);


    }

    public void addImagesToSlider(View view){
        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child("images")
                .child(USER_ID)
                .child("projects")
                .child(PROJECT_ID);

        for(int l = 0; l < imageCount; l++){
            SliderItem sliderItem = new SliderItem();
            StorageReference sr = null;
            sr = storageReference.child(projectCreationDate + "_image_" + l + ".jpeg");
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