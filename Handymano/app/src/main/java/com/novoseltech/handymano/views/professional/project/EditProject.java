package com.novoseltech.handymano.views.professional.project;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EditProject extends AppCompatActivity {

    ConstraintLayout cl_editProject;
    ConstraintLayout cl_savingProjectChanges;

    String PROJECT_ID;
    String projectCreationDate = "";
    long imageCount = 0;

    int PICK_IMAGE_MULTIPLE = 1000;
    private static final int WRITE_REQUEST = 1;
    String imageEncoded;
    List<String> imagesEncodedList;

    Uri mImageUri;
    ArrayList<Uri> mArrayUri = new ArrayList<Uri>();

    Date dt = Calendar.getInstance().getTime();
    SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
    String todayDate = df.format(dt);


    EditText et_projectTitle;
    EditText et_projectDescription;
    ImageView iv_deleteImg;
    ImageView iv_addImg;
    Button btn_saveChanges;


    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    String UID = user.getUid();

    SliderView sliderView;
    SliderAdapter adapter;

    List<SliderItem> initialImages = new ArrayList<>();

    //Activity elements
    TextView tv_pr;
    ScrollView sv_pr;
    CardView cv_pr;
    ImageView iv_pr;

    ImageView iv_temp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_project);

        cl_editProject = findViewById(R.id.cl_editProject);
        cl_savingProjectChanges = findViewById(R.id.cl_savingProjectChanges);
        cl_savingProjectChanges.setVisibility(View.GONE);
        PROJECT_ID = getIntent().getStringExtra("PROJECT_ID");

        et_projectTitle = findViewById(R.id.et_ep_projectTitle);
        et_projectTitle.setText(PROJECT_ID);
        et_projectDescription = findViewById(R.id.et_ep_projectDescription);
        iv_deleteImg = findViewById(R.id.iv_deleteImgFromProjectStack);
        iv_addImg = findViewById(R.id.iv_addImageToProjectStack);
        btn_saveChanges = findViewById(R.id.btn_saveProjectChanges);

        sliderView = findViewById(R.id.imageSliderProjectEdit);

        //Activity elements
        //tv_pr = getActivity().findViewById(R.id.tv_projectTitle);
        //sv_pr = getActivity().findViewById(R.id.svProject);
        //cv_pr = getActivity().findViewById(R.id.cv_carousel_project);
        //iv_pr = getActivity().findViewById(R.id.iv_proProjectMore);


        iv_temp = findViewById(R.id.iv_temp1);

        adapter = new SliderAdapter(getApplicationContext());
        sliderView.setSliderAdapter(adapter);

        DocumentReference documentReference = fStore.collection("user")
                .document(user.getUid())
                .collection("projects")
                .document(PROJECT_ID);

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    et_projectDescription.setText(documentSnapshot.getString("description"));
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
                startActivityForResult(Intent.createChooser(intent, "Select pictures"), PICK_IMAGE_MULTIPLE);


            }
        });

        btn_saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(et_projectTitle.getText().toString().length() >= 10 && et_projectTitle.getText().toString().length() <= 50 && et_projectDescription.getText().toString().length() >= 10 && et_projectDescription.getText().toString().length() <= 400 && (initialImages.size() > 0)){
                    cl_editProject.setVisibility(View.INVISIBLE);
                    cl_savingProjectChanges.setVisibility(View.VISIBLE);
                    //Objects
                    Map<String, Object> project = new HashMap<>();
                    String projectTitle = et_projectTitle.getText().toString();
                    String projectDescription = et_projectDescription.getText().toString();

                    List<Uri> images = new ArrayList<>();

                    if (checkPermission())
                    {
                        // Code for above or equal 23 API Oriented Device
                        // Your Permission granted already .Do next code

                        for(int i = 0; i < initialImages.size(); i++){

                            //Get image URL from initialImages array list
                            //if the URL is from Firebase Storage then use Picasso to get the bitmap, load it into new Target, get the uri and add it to the "images" array list
                            Uri uri = Uri.parse(initialImages.get(i).getImageUrl());

                            ContentValues values = new ContentValues();
                            values.put(MediaStore.Images.Media.DISPLAY_NAME, "image_project.jpg");
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
                                    //If the image is not existing project image from Firebase Storage then get the bitmap of the image, get the uri and add it to the URI array list "images"
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
                                    //If the image is not existing project image from Firebase Storage then get the bitmap of the image, get the uri and add it to the URI array list "images"

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
                                if(projectTitle.length() >= 10 && projectTitle.length() <= 50 && projectDescription.length() >= 10 && projectDescription.length() <= 400 && (images.size() > 0)){
                                    project.put("title", projectTitle);
                                    project.put("description", projectDescription);
                                    project.put("creation_date", todayDate);
                                    project.put("imageCount", images.size());

                                    //If project title is not changed
                                    //Update the document
                                    if(projectTitle.equals(PROJECT_ID)){
                                        fStore.collection("user").document(UID)
                                                .collection("projects")
                                                .document(projectTitle)
                                                .update(project)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){

                                                            //Delete all images for the project from storage
                                                            StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                                                                    .child("images")
                                                                    .child(user.getUid())
                                                                    .child("projects")
                                                                    .child(PROJECT_ID);

                                                            for(int l = 0; l < imageCount; l++){
                                                                StorageReference sr = null;
                                                                sr = storageReference.child(projectCreationDate + "_image_" + l + ".jpeg");
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
                                                            Log.d("TASK LOG: ", task.getException().getLocalizedMessage());
                                                        }
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.e("EXCEPTION: ", e.getLocalizedMessage());
                                            }
                                        });
                                    }else{
                                        //If project title was changed
                                        //Delete the project document
                                        fStore.collection("user").document(UID)
                                                .collection("projects")
                                                .document(PROJECT_ID)
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
                                        //Create new project document
                                        fStore.collection("user").document(UID)
                                                .collection("projects")
                                                .document(projectTitle)
                                                .set(project)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            //Delete images from Firebase Storage for the old project title
                                                            StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                                                                    .child("images")
                                                                    .child(user.getUid())
                                                                    .child("projects")
                                                                    .child(PROJECT_ID);


                                                            for(int l = 0; l < imageCount; l++){
                                                                StorageReference sr = null;
                                                                sr = storageReference.child(projectCreationDate + "_image_" + l + ".jpeg");
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

                                                            //Upload images for the project from URI array list
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

                                    finish();
                                    Intent intent = new Intent(EditProject.this, ProjectsActivity.class);
                                    startActivity(intent);
                                    cl_editProject.setVisibility(View.VISIBLE);
                                    cl_savingProjectChanges.setVisibility(View.GONE);


                                }else{
                                    Log.d("LOG: ", "Size of images AL " + images.size());
                                    Log.d("LOG: ", "Size of initialImages AL " + initialImages.size());
                                }
                            }
                        }, 1500);

                    } else {
                        requestPermission(); // Code for permission
                    }

                }else{
                    if(et_projectTitle.getText().toString().length() < 10 || et_projectTitle.getText().toString().length() > 50){
                        et_projectTitle.setError("Title length must be between 10 and 50 characters!");
                        et_projectTitle.requestFocus();
                    }else if(initialImages.size() == 0){
                        Toast.makeText(getApplicationContext(), "You must attach at least 1 image to the project", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        if(et_projectDescription.getText().toString().length() < 10){
                            et_projectDescription.setError("Description length must be at least 10 characters!");
                        }else{
                            et_projectDescription.setError("Description can contain max 400 characters!");
                        }
                        et_projectDescription.requestFocus();
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
                .child("projects")
                .child(et_projectTitle.getText().toString())
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
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_IMAGE_MULTIPLE);
        }else{
            if(requestCode == PICK_IMAGE_MULTIPLE && resultCode == RESULT_OK &&
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
                Toast.makeText(getApplicationContext(), "Request code: " + requestCode + " and result code: " + resultCode,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    public void addImagesToSlider(View view){

        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child("images")
                .child(user.getUid())
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
                        initialImages.add(sliderItem);
                        adapter.addItem(sliderItem);
                    }else{
                        Log.e("Error loading images", task.getException().getLocalizedMessage());
                    }
                }
            });
        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(getParent(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(getApplicationContext(), "Write External Storage permission allows us to store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(getParent(), new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case WRITE_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted");
                } else {
                    Log.e("value", "Permission Denied");
                }
                break;
        }
    }
}