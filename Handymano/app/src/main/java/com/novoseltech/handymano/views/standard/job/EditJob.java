package com.novoseltech.handymano.views.standard.job;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.novoseltech.handymano.R;
import com.novoseltech.handymano.adapter.SliderAdapter;
import com.novoseltech.handymano.fragments.AddressSelect;
import com.novoseltech.handymano.model.SliderItem;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.IndicatorView.draw.controller.DrawController;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.google.common.reflect.Reflection.initialize;

public class EditJob extends AppCompatActivity {

    //Layout components
    private ConstraintLayout cl_editJob;
    private ConstraintLayout cl_savingChanges;
    private EditText et_jobTitle;
    private EditText et_jobDescription;
    private ImageView iv_deleteImg;
    private ImageView iv_addImg;
    private Button btn_saveChanges;
    private SliderView sliderView;
    private ImageView iv_temp;
    private AutoCompleteTextView dropdownJobCategory;
    private AutoCompleteTextView dropdownJobStatus;
    private FrameLayout fl_editJobAddress;
    private ImageView iv_editJobAddress;
    private TextView tv_jobAddress;

    //Firebase components
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();

    //Variables
    private String JOB_ID;
    private String jobCreationDate = "";
    private long imageCount = 0;
    private String JOB_CATEGORY = "N/A";
    private static final int REQUEST_STORAGE_PERMISSION_CODE = 1000;
    private static final int WRITE_REQUEST = 1;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private String imageEncoded;
    private List<String> imagesEncodedList;
    private Uri mImageUri;
    private ArrayList<Uri> mArrayUri = new ArrayList<Uri>();
    private Date dt = Calendar.getInstance().getTime();
    private SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
    private String todayDate = df.format(dt);
    private String UID = user.getUid();
    private SliderAdapter adapter;
    private List<SliderItem> initialImages = new ArrayList<>();
    private String jobCategory ="";
    private String jobStatus = "";
    private double tmpLat;
    private double tmpLon;
    private double newLat;
    private double newLon;
    private AddressSelect af = new AddressSelect();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_job);

        iv_editJobAddress = findViewById(R.id.iv_editJobAddressButton);
        tv_jobAddress = findViewById(R.id.tv_jobAddressEdit);
        fl_editJobAddress = findViewById(R.id.fl_editJobAddress);
        fl_editJobAddress.setVisibility(View.GONE);

        cl_editJob = findViewById(R.id.cl_editJob);
        cl_savingChanges = findViewById(R.id.cl_savingChanges);
        cl_savingChanges.setVisibility(View.GONE);
        JOB_ID = getIntent().getStringExtra("JOB_ID");

        et_jobTitle = findViewById(R.id.et_ej_jobTitle);
        et_jobTitle.setText(JOB_ID);
        et_jobDescription = findViewById(R.id.et_ej_jobDescription);
        iv_deleteImg = findViewById(R.id.iv_deleteImgFromStack);
        iv_addImg = findViewById(R.id.iv_addImageToStack);
        btn_saveChanges = findViewById(R.id.btn_saveJobChanges);

        sliderView = findViewById(R.id.imageSliderJobEdit);

        iv_temp = findViewById(R.id.iv_temp);

        dropdownJobCategory = findViewById(R.id.dropdownJobCategory_edit);
        dropdownJobStatus = findViewById(R.id.dropdownJobStatus_edit);

        adapter = new SliderAdapter(this);
        sliderView.setSliderAdapter(adapter);

        DocumentReference documentReference = fStore.collection("user")
                .document(user.getUid())
                .collection("jobs")
                .document(JOB_ID);

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    et_jobDescription.setText(documentSnapshot.getString("description"));
                    jobCreationDate = documentSnapshot.getString("creation_date");
                    imageCount = documentSnapshot.getLong("imageCount");
                    jobCategory = documentSnapshot.getString("category");

                    //dropdownJobCategory.setText(jobCategory);
                    jobStatus = documentSnapshot.getString("status");
                    //dropdownJobStatus.setText(jobStatus);
                    GeoPoint gp = documentSnapshot.getGeoPoint("location");
                    tmpLat = gp.getLatitude();
                    tmpLon = gp.getLongitude();
                    tv_jobAddress.setText(getCompleteAddressString(tmpLat, tmpLon));


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
                "Building",
                "Carpentry",
                "Plumbing",
                "Electricity",
                "Metal works"
        };

        if(jobCategory.equals("Builder")){
            dropdownJobCategory.setText("Building");
        }else if(jobCategory.equals("Carpenter")){
            dropdownJobCategory.setText("Carpentry");
        }else if(jobCategory.equals("Plumber")){
            dropdownJobCategory.setText("Plumbing");
        }else if(jobCategory.equals("Electrician")){
            dropdownJobCategory.setText("Electricity");
        }else if(jobCategory.equals("Metal worker")){
            dropdownJobCategory.setText("Metal works");
        }

        ArrayAdapter<String> adapterJobCategory = new ArrayAdapter<>(this, R.layout.services_category_layout, R.id.tv_1, JOB_CATEGORY);
        dropdownJobCategory.setAdapter(adapterJobCategory);
        dropdownJobCategory.setInputType(0);

        dropdownJobCategory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(dropdownJobCategory.getEditableText().toString().equals("Building")){
                    jobCategory = "Builder";
                }else if(dropdownJobCategory.getEditableText().toString().equals("Carpentry")){
                    jobCategory = "Carpenter";
                }else if(dropdownJobCategory.getEditableText().toString().equals("Plumbing")){
                    jobCategory = "Plumber";
                }else if(dropdownJobCategory.getEditableText().toString().equals("Electricity")){
                    jobCategory = "Electrician";
                }else if(dropdownJobCategory.getEditableText().toString().equals("Metal works")){
                    jobCategory = "Metal worker";
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //Job status - creating the dropdown
        final String[] JOB_STATUS = new String[] {
                "Active",
                "Private"
        };

        dropdownJobStatus.setText(jobStatus);

        ArrayAdapter<String> adapterJobStatus = new ArrayAdapter<>(this, R.layout.services_category_layout, R.id.tv_1, JOB_STATUS);

        dropdownJobStatus.setAdapter(adapterJobStatus);
        dropdownJobStatus.setInputType(0);

        dropdownJobStatus.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(dropdownJobStatus.getEditableText().toString().equals("Active")){
                    jobStatus = "Active";
                }else if(dropdownJobStatus.getEditableText().toString().equals("Private")){
                    jobStatus = "Private";
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        iv_deleteImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(initialImages.size() > 0){
                    initialImages.remove(sliderView.getCurrentPagePosition());
                    adapter.deleteItem(sliderView.getCurrentPagePosition());

                }else{
                    Toast.makeText(getApplicationContext(), "No image to delete", Toast.LENGTH_SHORT).show();
                }
            }
        });

        iv_addImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select pictures"), REQUEST_STORAGE_PERMISSION_CODE);


            }
        });

        iv_editJobAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cl_editJob.setVisibility(View.GONE);
                fl_editJobAddress.setVisibility(View.VISIBLE);

                Bundle bundle = new Bundle();
                bundle.putString("mode", "JobEdit");
                bundle.putDouble("lat", tmpLat);
                bundle.putDouble("lon", tmpLon);
                af.setArguments(bundle);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fl_editJobAddress, af)
                        .commit();
            }
        });



        btn_saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(et_jobTitle.getText().toString().length() >= 10 && et_jobTitle.getText().toString().length() <= 50 && et_jobDescription.getText().toString().length() >= 10 && et_jobDescription.getText().toString().length() <= 400 && (initialImages.size() > 0)){
                    cl_editJob.setVisibility(View.INVISIBLE);
                    cl_savingChanges.setVisibility(View.VISIBLE);
                    //Objects
                    Map<String, Object> job = new HashMap<>();
                    String jobTitle = et_jobTitle.getText().toString();
                    String jobDescription = et_jobDescription.getText().toString();

                    List<Uri> images = new ArrayList<>();

                    for(int i = 0; i < initialImages.size(); i++){

                        //Get image URL from initialImages array list
                        //if the URL is from Firebase Storage then use Picasso to get the bitmap, load it into new Target, get the uri and add it to the "images" array list
                        Uri uri = Uri.parse(initialImages.get(i).getImageUrl());

                        ContentValues values = new ContentValues();
                        values.put(MediaStore.Images.Media.DISPLAY_NAME, "image_job.jpg");
                        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                        values.put(MediaStore.Images.Media.RELATIVE_PATH,
                                Environment.DIRECTORY_PICTURES + "/Handymano");

                        Uri tempImgUri =
                                getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                        values);

                        //For
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){

                            //If Android SDK is 28 or higher than execute this
                            //If image is existing on Firebase Storage
                            if(uri.toString().contains("firebasestorage.googleapis")){

                                Log.d("NEW tempImgUri", tempImgUri.toString());
                                OutputStream imageOutStream;
                                try {
                                    imageOutStream = getContentResolver().openOutputStream(tempImgUri);
                                    Glide.with(view)
                                            .asBitmap()
                                            .load(initialImages.get(i).getImageUrl())
                                            .into(new CustomTarget<Bitmap>() {
                                                @Override
                                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {

                                                    resource.compress(Bitmap.CompressFormat.JPEG, 100, imageOutStream);
                                                    try {
                                                        imageOutStream.close();
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }

                                                    images.add(tempImgUri);
                                                }

                                                @Override
                                                public void onLoadCleared(@Nullable Drawable placeholder) {

                                                }
                                            });
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }




                            }else{
                                //If the image is not existing job image from Firebase Storage then get the bitmap of the image, get the uri and add it to the URI array list "images"
                                OutputStream imageOutStream;
                                Bitmap bitmap;
                                try {
                                    imageOutStream = getContentResolver().openOutputStream(tempImgUri);
                                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageOutStream);
                                    imageOutStream.close();
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                Log.d("Local tempImgUri", tempImgUri.toString());
                                images.add(tempImgUri);
                            }

                            //If Android SDK is between 23 and 27 (inclusive)
                        }else{
                            //If image is from Firebase Storage
                            if(uri.toString().contains("firebasestorage.googleapis")){

                                Glide.with(view)
                                        .asBitmap()
                                        .load(initialImages.get(i).getImageUrl())
                                        .into(new CustomTarget<Bitmap>() {
                                            @Override
                                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                                String path = MediaStore.Images.Media.insertImage(getContentResolver(), resource, "Title", null);
                                                Log.d("URI", path);
                                                Uri tmpUri = Uri.parse(path);
                                                images.add(tmpUri);
                                            }

                                            @Override
                                            public void onLoadCleared(@Nullable Drawable placeholder) {

                                            }
                                        });

                            }else{
                                //If the image is not existing job image from Firebase Storage then get the bitmap of the image, get the uri and add it to the URI array list "images"

                                try {
                                    Bitmap bitmap = null;
                                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                                    Uri tmpUri = null;
                                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                                    String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
                                    tmpUri = Uri.parse(path);
                                    images.add(tmpUri);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                    }

                    Handler hand1 = new Handler();
                    hand1.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Execute after loading the images
                            //Content validation
                            if(jobTitle.length() >= 10 && jobTitle.length() <= 50 && jobDescription.length() >= 10 && jobDescription.length() <= 400 && (images.size() > 0)){
                                job.put("title", jobTitle);
                                job.put("description", jobDescription);
                                job.put("creation_date", todayDate);
                                job.put("imageCount", images.size());
                                job.put("category", jobCategory);
                                job.put("status", jobStatus);
                                GeoPoint gp = new GeoPoint(tmpLat, tmpLon);
                                job.put("location", gp);

                                //If job title is not changed
                                //Update the document
                                if(jobTitle.equals(JOB_ID)){
                                    fStore.collection("user").document(UID)
                                            .collection("jobs")
                                            .document(jobTitle)
                                            .update(job)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){

                                                        //Delete all images for the job from storage
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
                                                                        Log.d("DELETE LOG", "Deleted image " + (j+1));
                                                                    }else{
                                                                        Log.e("DELETE LOG ", task.getException().getLocalizedMessage());
                                                                    }
                                                                }
                                                            });
                                                        }

                                                        //Upload all images to Firebase storage folder for the project
                                                        for(int i = 0; i < images.size(); i++){
                                                            Bitmap bitmap = null;
                                                            try {
                                                                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), images.get(i));
                                                                uploadImageToFirebaseStorage(bitmap, i);
                                                                getContentResolver().delete(images.get(i), null, null);
                                                            } catch (IOException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }


                                                    }else{
                                                        Log.d("TASK LOG ", task.getException().getLocalizedMessage());
                                                    }
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e("EXCEPTION: ", e.getLocalizedMessage());
                                        }
                                    });
                                }else{
                                    //If job title was changed
                                    //Delete the job document
                                    fStore.collection("user").document(UID)
                                            .collection("jobs")
                                            .document(JOB_ID)
                                            .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Log.d("TAG: ", "Document deleted");
                                            }else{
                                                Log.e("EXCEPTION", task.getException().getLocalizedMessage());
                                            }
                                        }
                                    });
                                    //Create new job document
                                    fStore.collection("user").document(UID)
                                            .collection("jobs")
                                            .document(jobTitle)
                                            .set(job)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        //Delete images from Firebase Storage for the old job title
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
                                                                        Log.d("DELETE LOG: ", "Deleted image " + (j+1));
                                                                    }else{
                                                                        Log.e("DELETE LOG: ", task.getException().getLocalizedMessage());
                                                                    }
                                                                }
                                                            });
                                                        }

                                                        //Upload images for the job from URI array list
                                                        for(int i = 0; i < images.size(); i++){
                                                            Bitmap bitmap = null;
                                                            try {
                                                                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), images.get(i));
                                                                uploadImageToFirebaseStorage(bitmap, i);
                                                            } catch (IOException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }

                                                    }else{
                                                        Log.d("TASK LOG: ", task.getException().getLocalizedMessage());
                                                    }
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e("EXCEPTION: ", e.getLocalizedMessage());
                                        }
                                    });
                                }

                                Intent intent = new Intent(EditJob.this, JobsActivity.class);
                                startActivity(intent);
                                cl_editJob.setVisibility(View.VISIBLE);
                                cl_savingChanges.setVisibility(View.GONE);


                            }else{
                                Log.d("LOG: ", "Size of images AL " + images.size());
                                Log.d("LOG: ", "Size of initialImages AL " + initialImages.size());
                            }
                        }
                    }, 1500);

                }else{
                    if(et_jobTitle.getText().toString().length() < 10 || et_jobTitle.getText().toString().length() > 50){
                        et_jobTitle.setError("Title length must be between 10 and 50 characters!");
                        et_jobTitle.requestFocus();
                    }else if(initialImages.size() == 0){
                        Toast.makeText(getApplicationContext(), "You must attach at least 1 image to the job", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        if(et_jobDescription.getText().toString().length() < 10){
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getParent(), new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION_CODE);
        }else{
            if(requestCode == REQUEST_STORAGE_PERMISSION_CODE && resultCode == RESULT_OK &&
                    data != null){
                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                imagesEncodedList = new ArrayList<String>();

                //if one image is selected
                if(data.getData() != null){
                    mImageUri = null;
                    mImageUri = data.getData();

                    SliderItem sliderItem = new SliderItem();
                    sliderItem.setImageUrl(mImageUri.toString());
                    initialImages.add(sliderItem);
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
                        mArrayUri.clear();
                        for(int i = 0; i < clipData.getItemCount(); i++){
                            ClipData.Item item = clipData.getItemAt(i);
                            Uri uri = item.getUri();
                            mArrayUri.add(uri);
                            Cursor cursor = getContentResolver().query(uri, filePathColumn,
                                    null, null, null);
                            cursor.moveToFirst();

                            SliderItem sliderItem = new SliderItem();
                            sliderItem.setImageUrl(uri.toString());
                            initialImages.add(sliderItem);
                            adapter.addItem(sliderItem);

                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                            imageEncoded = cursor.getString(columnIndex);
                            imagesEncodedList.add(imageEncoded);
                            cursor.close();

                        }

                        Log.d("MULTIPLE IMAGE PICKER: ", "Selected Images" + mArrayUri.size());
                    }
                }


            }else {
            }
        }
    }

    public void addImagesToSlider(View view){

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
                        initialImages.add(sliderItem);
                        adapter.addItem(sliderItem);
                    }else{
                        Log.e("Error loading images", task.getException().getLocalizedMessage());
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_STORAGE_PERMISSION_CODE) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                //Show error message that prevents that informs them about permission
                Toast.makeText(getApplicationContext(), "Storage permission is denied. Please allow it in the Settings to enable this functionality.", Toast.LENGTH_SHORT).show();

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

    public void discardChanges(){
        //Close app
        //Initialize alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //Set title
        builder.setTitle("Discard");
        //Set message
        builder.setMessage("Your changes are not saved. Continue ?");
        //Yes button
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
                Intent intent = new Intent(EditJob.this, StandardJobViewActivity.class);
                intent.putExtra("JOB_ID", JOB_ID);
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

    public void setLocationData(String lat, String lon){
        tmpLat = Double.parseDouble(lat);
        tmpLon = Double.parseDouble(lon);
        tv_jobAddress.setText(getCompleteAddressString(tmpLat, tmpLon));

    }

    @Override
    public void onBackPressed() {
        discardChanges();
    }
}