package com.novoseltech.handymano.views.standard.job;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.novoseltech.handymano.R;
import com.novoseltech.handymano.adapter.SliderAdapter;
import com.novoseltech.handymano.model.SliderItem;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.IndicatorView.draw.controller.DrawController;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CreateJob extends AppCompatActivity {

    int PICK_IMAGE_MULTIPLE = 2014;
    long imageCount = 0;

    String imageEncoded;
    List<String> imagesEncodedList;
    Button btn_saveJob;
    Button btn_createJob;
    RecyclerView fStoreList;

    EditText et_jobTitle;
    EditText et_jobDescription;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    String UID = user.getUid();

    Uri mImageUri;
    ArrayList<Uri> mArrayUri = new ArrayList<Uri>();

    Date dt = Calendar.getInstance().getTime();
    SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
    String todayDate = df.format(dt);

    ImageView iv_addImg;
    ImageView iv_deleteImg;

    SliderView sliderView;
    SliderAdapter adapter;

    List<SliderItem> imagesArrayList = new ArrayList<>();
    AutoCompleteTextView dropdownJobCategory;
    private String jobCategory ="N/A";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_job);

        btn_saveJob = findViewById(R.id.btn_saveJob);
        et_jobTitle = findViewById(R.id.et_jobTitle);
        et_jobDescription = findViewById(R.id.et_jobDescription);
        iv_addImg = findViewById(R.id.iv_addJobImg);
        iv_deleteImg = findViewById(R.id.iv_deleteJobImg);
        sliderView = findViewById(R.id.imageSliderJobAdd);

        dropdownJobCategory = findViewById(R.id.dropdownJobCategory);

        adapter = new SliderAdapter(getApplicationContext());
        sliderView.setSliderAdapter(adapter);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM); //set indicator animation by using IndicatorAnimationType. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
                sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
                sliderView.setIndicatorSelectedColor(Color.WHITE);
                sliderView.setIndicatorUnselectedColor(Color.GRAY);

                sliderView.setOnIndicatorClickListener(new DrawController.ClickListener() {
                    @Override
                    public void onIndicatorClicked(int position) {
                        Log.i("GGG", "onIndicatorClicked: " + sliderView.getCurrentPagePosition());
                    }
                });


            }
        }, 600);

        //Job category - creating the dropdown
        final String[] JOB_CATEGORY = new String[] {
                "Builder",
                "Carpenter",
                "Plumber",
                "Electrician",
                "Metal worker"
        };

        ArrayAdapter<String> adapterJobCategory = new ArrayAdapter<>(this, R.layout.services_category_layout, R.id.tv_1, JOB_CATEGORY);

        //final AutoCompleteTextView dropdownJobCategory = view.findViewById(R.id.dropdownJobCategory);
        dropdownJobCategory.setAdapter(adapterJobCategory);
        dropdownJobCategory.setInputType(0);

        dropdownJobCategory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(dropdownJobCategory.getEditableText().toString().equals("Builder")){
                    jobCategory = "Building";
                    Toast.makeText(getApplicationContext(), jobCategory, Toast.LENGTH_SHORT).show();
                }else if(dropdownJobCategory.getEditableText().toString().equals("Carpenter")){
                    jobCategory = "Carpentry";
                    Toast.makeText(getApplicationContext(), jobCategory, Toast.LENGTH_SHORT).show();
                }else if(dropdownJobCategory.getEditableText().toString().equals("Plumber")){
                    jobCategory = "Plumbing";
                    Toast.makeText(getApplicationContext(), jobCategory, Toast.LENGTH_SHORT).show();
                }else if(dropdownJobCategory.getEditableText().toString().equals("Electrician")){
                    jobCategory = "Electricity";
                    Toast.makeText(getApplicationContext(), jobCategory, Toast.LENGTH_SHORT).show();
                }else if(dropdownJobCategory.getEditableText().toString().equals("Metal worker")){
                    jobCategory = "Metal works";
                    Toast.makeText(getApplicationContext(), jobCategory, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        iv_addImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select pictures"), PICK_IMAGE_MULTIPLE);
            }
        });

        iv_deleteImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imagesArrayList.size() > 0){
                    imagesArrayList.remove(sliderView.getCurrentPagePosition());
                    adapter.deleteItem(sliderView.getCurrentPagePosition());
                }else{
                    Toast.makeText(getApplicationContext(), "No images to delete", Toast.LENGTH_SHORT).show();
                }

            }
        });



        btn_saveJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Objects
                Map<String, Object> job = new HashMap<>();
                String jobTitle = et_jobTitle.getText().toString();
                String jobDescription = et_jobDescription.getText().toString();


                //Content validation
                if(jobTitle.length() >= 10 && jobTitle.length() <= 50 && jobDescription.length() >= 10 && jobDescription.length() <= 400 && (imagesArrayList.size() > 0) && !jobCategory.equals("N/A")){
                    job.put("title", jobTitle);
                    job.put("description", jobDescription);
                    job.put("creation_date", todayDate);
                    job.put("imageCount", imagesArrayList.size());
                    job.put("status", "active");
                    job.put("category", jobCategory);

                    fStore.collection("user").document(UID)
                            .collection("jobs")
                            .document(jobTitle)
                            .set(job)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    for(int i = 0; i < imagesArrayList.size(); i++){
                                        Bitmap bitmap = null;
                                        try {
                                            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(imagesArrayList.get(i).getImageUrl()));
                                            uploadImageToFirebaseStorage(bitmap, i);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                    btn_createJob.setVisibility(View.VISIBLE);
                    fStoreList.setVisibility(View.VISIBLE);
                    //getActivity().getSupportFragmentManager().beginTransaction().remove(AddJob.this).commit();

                }else{
                    if(jobTitle.length() < 10 || jobTitle.length() > 50){
                        et_jobTitle.setError("Title length must be between 10 and 50 characters!");
                        et_jobTitle.requestFocus();
                    }else if(imagesArrayList.size() == 0){
                        Toast.makeText(getApplicationContext(), "You must attach at least 1 image to the job", Toast.LENGTH_SHORT).show();
                    }else if(jobCategory.equals("N/A")){
                        Toast.makeText(getApplicationContext(), "Job category must be selected", Toast.LENGTH_SHORT).show();
                    }else{
                        if(jobDescription.length() < 10){
                            et_jobDescription.setError("Description length must be at least 10 characters!");
                        }else{
                            et_jobDescription.setError("Description can contain max 400 characters!");
                        }
                        et_jobDescription.requestFocus();
                    }
                }
            }
        });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        try {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getParent(), new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_IMAGE_MULTIPLE);
            }else{
                // When an Image is picked
                if (requestCode == PICK_IMAGE_MULTIPLE && resultCode == RESULT_OK
                        && null != data) {
                    String[] filePathColumn = { MediaStore.Images.Media.DATA };
                    imagesEncodedList = new ArrayList<String>();

                    //if one image is selected
                    if(data.getData() != null){
                        mImageUri = null;
                        mImageUri = data.getData();

                        SliderItem sliderItem = new SliderItem();
                        sliderItem.setImageUrl(mImageUri.toString());
                        imagesArrayList.add(sliderItem);
                        adapter.addItem(sliderItem);

                        Cursor cursor = getContentResolver().query(mImageUri, filePathColumn,
                                null, null, null);
                        cursor.moveToFirst();

                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        imageEncoded = cursor.getString(columnIndex);
                        cursor.close();

                    }else{
                        //if multiple images are selected
                        if(data.getClipData() != null) {
                            ClipData clipData = data.getClipData();
                            for(int i = 0; i < clipData.getItemCount(); i++){
                                ClipData.Item item = clipData.getItemAt(i);
                                Uri uri = item.getUri();
                                Cursor cursor = getContentResolver().query(uri, filePathColumn,
                                        null, null, null);
                                cursor.moveToFirst();
                                SliderItem sliderItem = new SliderItem();
                                sliderItem.setImageUrl(uri.toString());
                                imagesArrayList.add(sliderItem);
                                adapter.addItem(sliderItem);

                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                imageEncoded = cursor.getString(columnIndex);
                                imagesEncodedList.add(imageEncoded);
                                cursor.close();

                            }
                            Log.d("MULTIPLE IMAGE PICKER: ", "Selected Images" + mArrayUri.size());
                        }
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Request code: " + requestCode + " and result code: " + resultCode,
                            Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if(requestCode == PICK_IMAGE_MULTIPLE) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select pictures"), PICK_IMAGE_MULTIPLE);
            } else {
                //Show error message that prevents that informs them about permission
            }
        }
    }

    private void uploadImageToFirebaseStorage(Bitmap bitmap, int imgCount) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        StorageReference reference = FirebaseStorage.getInstance().getReference()
                .child("images")
                .child(UID)
                .child("jobs")
                .child(et_jobTitle.getText().toString())
                .child(todayDate + "_image_" + imgCount + ".jpeg");
        reference.putBytes(baos.toByteArray())
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d("TAG", "Upload of image " + imgCount + " successful");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("TAG", "onFailure: ", e.getCause());
            }
        });
    }
}