package com.novoseltech.handymano.views.standard.job;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.novoseltech.handymano.MainActivity;
import com.novoseltech.handymano.R;
import com.novoseltech.handymano.adapter.SliderAdapter;
import com.novoseltech.handymano.model.SliderItem;
import com.novoseltech.handymano.views.message.MessageMenu;
import com.novoseltech.handymano.views.standard.HomeActivityStandard;
import com.novoseltech.handymano.views.standard.StandardProfileActivity;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.IndicatorView.draw.controller.DrawController;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import java.util.List;
import java.util.Locale;

/**
 @author Denis Novosel
 @student_id 17104718
 @email x17104718@student.ncirl.ie
 @github https://github.com/adminnovoseltech/SoftwareProjectDN17104718
 @class StandardJobViewActivity.java
 **/

public class StandardJobViewActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener{

    //Layout components
    private SliderView sliderView;
    private ConstraintLayout cl_jobView;
    private TextView tv_jobTitle;
    private ScrollView svJob;
    private CardView cv_carousel_job;
    private ImageView iv_jobMore;
    private CircularImageView profileImage;
    private TextView tv_UserName;
    private TextView tv_jobDescription;

    //Firebase components
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();

    //Variables
    private static final String TAG = "LOG: ";
    private String JOB_ID;
    private String jobCreationDate = "";
    private long imageCount = 0;
    private static final int REQUEST_STORAGE_PERMISSION_CODE = 1000;
    private SliderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_standard_job_view);

        JOB_ID = getIntent().getStringExtra("JOB_ID");
        cl_jobView = findViewById(R.id.cl_jobViewStandard);

        sliderView = findViewById(R.id.imageSliderJob);
        adapter = new SliderAdapter(getApplicationContext());
        sliderView.setSliderAdapter(adapter);

        iv_jobMore = findViewById(R.id.iv_stdJobMore);

        //Layout objects
        tv_jobTitle = findViewById(R.id.tv_jobTitle);
        tv_jobDescription = findViewById(R.id.tv_jobDescription);
        tv_jobTitle.setText(JOB_ID);

        svJob = findViewById(R.id.svJob);
        cv_carousel_job = findViewById(R.id.cv_carousel_job);

        DocumentReference documentReference = fStore.collection("user")
                .document(user.getUid())
                .collection("jobs")
                .document(JOB_ID);

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    tv_jobDescription.setText(documentSnapshot.getString("description"));
                    jobCreationDate = documentSnapshot.getString("creation_date");
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
        }, 1000);

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
        }, 1500);
    }

    public void addImagesToSlider(View view){
        if(imageCount == 0){

        }else{
            StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                    .child("images")
                    .child(user.getUid())
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

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.option_edit:

                if(getApplicationContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || getApplicationContext().
                        checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION_CODE);
                }else{
                    Intent intent = new Intent(StandardJobViewActivity.this, EditJob.class);
                    intent.putExtra("JOB_ID", JOB_ID);
                    startActivity(intent);
                }

                return true;
            case R.id.option_delete:
                AlertDialog.Builder deleteJobDialog = new AlertDialog.Builder(StandardJobViewActivity.this);
                deleteJobDialog.setTitle("Delete job")
                        .setMessage("You are about to delete the job. Continue?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                                        .child("images")
                                        .child(user.getUid())
                                        .child("jobs")
                                        .child(JOB_ID);

                                for(int l = 0; l < imageCount; l++){
                                    StorageReference sr = null;
                                    sr = storageReference.child(jobCreationDate + "_image_" + l + ".jpeg");
                                    int j = l;
                                    sr.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Log.d(TAG, "Deleted image " + (j+1));
                                            }else{
                                                Log.e(TAG, task.getException().getLocalizedMessage());
                                            }
                                        }
                                    });
                                }

                                fStore.collection("user")
                                        .document(user.getUid())
                                        .collection("jobs")
                                        .document(JOB_ID)
                                        .delete();

                                Intent intent = new Intent(StandardJobViewActivity.this, JobsActivity.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();

                return true;
            default:
                return false;

        }
    }

    public void showJobMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.setOnMenuItemClickListener(this);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.actions, popupMenu.getMenu());
        popupMenu.show();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_STORAGE_PERMISSION_CODE) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(StandardJobViewActivity.this, EditJob.class);
                intent.putExtra("JOB_ID", JOB_ID);
                startActivity(intent);
            } else {
                //Show error message that prevents that informs them about permission
                Toast.makeText(getApplicationContext(), "Storage permission is denied. Please allow it in the Settings to enable this functionality.", Toast.LENGTH_SHORT).show();

            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}