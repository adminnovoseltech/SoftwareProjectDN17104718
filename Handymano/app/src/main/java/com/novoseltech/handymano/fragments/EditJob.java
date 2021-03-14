package com.novoseltech.handymano.fragments;

import android.Manifest;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.common.data.BitmapTeleporter;
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
import com.nostra13.universalimageloader.core.ImageLoader;
import com.novoseltech.handymano.R;
import com.novoseltech.handymano.adapter.SliderAdapter;
import com.novoseltech.handymano.functions.ImageDownloader;
import com.novoseltech.handymano.model.SliderItem;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.IndicatorView.draw.controller.DrawController;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditJob#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditJob extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    ConstraintLayout cl_editJob;
    ConstraintLayout cl_savingChanges;

    String JOB_ID;
    String jobCreationDate = "";
    long imageCount = 0;

    int PICK_IMAGE_MULTIPLE = 1000;
    String imageEncoded;
    List<String> imagesEncodedList;

    Uri mImageUri;
    ArrayList<Uri> mArrayUri = new ArrayList<Uri>();

    Date dt = Calendar.getInstance().getTime();
    SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
    String todayDate = df.format(dt);


    EditText et_jobTitle;
    EditText et_jobDescription;
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
    TextView tv_jb;
    ScrollView sv_jb;
    CardView cv_jb;
    ImageView iv_jb;





    ImageView iv_temp;
    public EditJob() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EditJob.
     */
    // TODO: Rename and change types and number of parameters
    public static EditJob newInstance(String param1, String param2) {
        EditJob fragment = new EditJob();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_job, container, false);
        // Inflate the layout for this fragment
        cl_editJob = view.findViewById(R.id.cl_editJob);
        cl_savingChanges = view.findViewById(R.id.cl_savingChanges);
        cl_savingChanges.setVisibility(View.GONE);
        JOB_ID = getArguments().getString("JOB_ID");

        et_jobTitle = view.findViewById(R.id.et_ej_jobTitle);
        et_jobTitle.setText(JOB_ID);
        et_jobDescription = view.findViewById(R.id.et_ej_jobDescription);
        iv_deleteImg = view.findViewById(R.id.iv_deleteImgFromStack);
        iv_addImg = view.findViewById(R.id.iv_addImageToStack);
        btn_saveChanges = view.findViewById(R.id.btn_saveJobChanges);

        sliderView = view.findViewById(R.id.imageSliderJobEdit);

        //Activity elements
        tv_jb = getActivity().findViewById(R.id.tv_jobTitle);
        sv_jb = getActivity().findViewById(R.id.svJob);
        cv_jb = getActivity().findViewById(R.id.cv_carousel_job);
        iv_jb = getActivity().findViewById(R.id.iv_stdJobMore);


        iv_temp = view.findViewById(R.id.iv_temp);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new SliderAdapter(getContext());
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
                    Toast.makeText(getContext(), "No image to delete", Toast.LENGTH_SHORT).show();
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

                if(et_jobTitle.getText().toString().length() >= 10 && et_jobTitle.getText().toString().length() <= 50 && et_jobDescription.getText().toString().length() >= 10 && et_jobDescription.getText().toString().length() <= 400 && (initialImages.size() > 0)){
                    cl_editJob.setVisibility(View.INVISIBLE);
                    cl_savingChanges.setVisibility(View.VISIBLE);
                    //Objects
                    Map<String, Object> job = new HashMap<>();
                    String jobTitle = et_jobTitle.getText().toString();
                    String jobDescription = et_jobDescription.getText().toString();

                    List<Uri> images = new ArrayList<>();

                    ImageLoader imageLoader = ImageLoader.getInstance();

                    // Enable if permission granted
                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_GRANTED) {
                        for(int i = 0; i < initialImages.size(); i++){

                            //Get image URL from initialImages array list
                            //if the URL is from Firebase Storage then use Picasso to get the bitmat, load it into new Target, get the uri and add it to the "images" array list
                            Uri uri = Uri.parse(initialImages.get(i).getImageUrl());

                            if(uri.toString().contains("firebasestorage.googleapis")){

                                Glide.with(view)
                                        .asBitmap()
                                        .load(initialImages.get(i).getImageUrl())
                                        .into(new CustomTarget<Bitmap>() {
                                            @Override
                                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                                String path = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), resource, "Title", null);
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
                                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                                    Uri tmpUri = null;
                                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                                    String path = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), bitmap, "Title", null);
                                    tmpUri = Uri.parse(path);
                                    images.add(tmpUri);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        Log.d("URI", "Log");

                    }
                    // Else ask for permission
                    else {
                        ActivityCompat.requestPermissions(getActivity(), new String[]
                                { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
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
                                                                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), images.get(i));
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
                                                                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), images.get(i));
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



                        Toast.makeText(getContext(), "Upload completed", Toast.LENGTH_SHORT).show();


                        //btn_createJob.setVisibility(View.VISIBLE);
                        //fStoreList.setVisibility(View.VISIBLE);
                        getActivity().finish();
                        startActivity(getActivity().getIntent());
                        getActivity().getSupportFragmentManager().beginTransaction().remove(EditJob.this).commit();
                        cl_editJob.setVisibility(View.VISIBLE);
                        cl_savingChanges.setVisibility(View.GONE);
                        tv_jb.setVisibility(View.VISIBLE);
                        sv_jb.setVisibility(View.VISIBLE);
                        cv_jb.setVisibility(View.VISIBLE);
                        iv_jb.setVisibility(View.VISIBLE);



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
                        Toast.makeText(getContext(), "You must attach at least 1 image to the job", Toast.LENGTH_SHORT).show();
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
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_IMAGE_MULTIPLE);
        }else{
            if(requestCode == PICK_IMAGE_MULTIPLE && resultCode == getActivity().RESULT_OK &&
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

                    Cursor cursor = getActivity().getContentResolver().query(mImageUri, filePathColumn,
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
                            Cursor cursor = getActivity().getContentResolver().query(uri, filePathColumn,
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
                Toast.makeText(getContext(), "Request code: " + requestCode + " and result code: " + resultCode,
                        Toast.LENGTH_LONG).show();
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
}


