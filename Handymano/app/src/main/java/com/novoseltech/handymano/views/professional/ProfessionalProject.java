package com.novoseltech.handymano.views.professional;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.novoseltech.handymano.R;
import com.novoseltech.handymano.model.OnSwipeTouchListener;

import java.util.ArrayList;
import java.util.List;

public class ProfessionalProject extends AppCompatActivity {


    String projectCreationDate = "";
    long imageCount = 0;
    int current_img_no = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professional_project);

        String PROJECT_ID = getIntent().getStringExtra("PROJECT_ID");
        List<Uri> imageUriArray = new ArrayList<>();


        //Firebase objects
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        //Carousel
        ImageView imageViewCarousel = findViewById(R.id.imageViewGallery);
        TextView tv_currentImg = findViewById(R.id.tv_currentImg);


        //Layout objects
        TextView tv_projectTitle = findViewById(R.id.tv_projectTitle);
        TextView tv_projectDescription = findViewById(R.id.tv_projectDescription);

        tv_projectTitle.setText(PROJECT_ID);

        DocumentReference documentReference = fStore.collection("user")
                .document(user.getUid())
                .collection("projects")
                .document(PROJECT_ID);

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    tv_projectDescription.setText(documentSnapshot.getString("description"));
                    projectCreationDate = documentSnapshot.getString("creation_date");
                    imageCount = documentSnapshot.getLong("imageCount");

                }
            }
        });

        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Loading project images from Firebase Storage
                StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                        .child("images")
                        .child(user.getUid())
                        .child("projects")
                        .child(PROJECT_ID);

                for(int l = 0; l < imageCount; l++){
                    StorageReference sr = null;
                    sr = storageReference.child(projectCreationDate + "_image_" + l + ".jpeg");
                    //Toast.makeText(getApplicationContext(), String.valueOf(sr), Toast.LENGTH_LONG).show();
                    sr.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if(task.isSuccessful()){
                                imageUriArray.add(task.getResult());
                            }
                        }
                    });
                }

                tv_currentImg.setText("1 / " + imageCount);

            }
        }, 300);

        Handler handler1 = new Handler();
        handler1.postDelayed(new Runnable() {
            @Override
            public void run() {
                Glide.with(ProfessionalProject.this)
                        .load(imageUriArray.get(0))
                        .into(imageViewCarousel);
            }
        }, 600);


        imageViewCarousel.setOnTouchListener(new OnSwipeTouchListener(ProfessionalProject.this){
            public void onSwipeTop() {
                //Toast.makeText(ProfessionalProject.this, "top", Toast.LENGTH_SHORT).show();
            }
            public void onSwipeRight() {
                //Toast.makeText(ProfessionalProject.this, "right", Toast.LENGTH_SHORT).show();
                Glide.with(ProfessionalProject.this)
                        .load(imageUriArray.get(current_img_no - 1))
                        .into(imageViewCarousel);

                if(current_img_no < 0){
                    current_img_no = (int) (imageCount - 1);
                    Glide.with(ProfessionalProject.this)
                            .load(imageUriArray.get(current_img_no))
                            .into(imageViewCarousel);
                }else{
                    current_img_no = current_img_no - 1;
                }

                tv_currentImg.setText((current_img_no + 1) + " / " + imageCount);

            }
            public void onSwipeLeft() {
                //Toast.makeText(ProfessionalProject.this, "left", Toast.LENGTH_SHORT).show();
                Glide.with(ProfessionalProject.this)
                        .load(imageUriArray.get(current_img_no + 1))
                        .into(imageViewCarousel);

                if(current_img_no == (imageCount - 1)){
                    current_img_no = 0;
                    Glide.with(ProfessionalProject.this)
                            .load(imageUriArray.get(current_img_no))
                            .into(imageViewCarousel);
                }else{
                    current_img_no = current_img_no + 1;
                }

                tv_currentImg.setText((current_img_no + 1) + " / " + imageCount);

            }
            public void onSwipeBottom() {
                //Toast.makeText(ProfessionalProject.this, "bottom", Toast.LENGTH_SHORT).show();
            }
        });

    }

}