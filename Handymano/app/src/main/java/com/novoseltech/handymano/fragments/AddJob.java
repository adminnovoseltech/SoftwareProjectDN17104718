package com.novoseltech.handymano.fragments;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddJob#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddJob extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    int PICK_IMAGE_MULTIPLE = 2014;

    String imageEncoded;
    List<String> imagesEncodedList;
    ImageView iv_first;
    ImageView iv_second;
    TextView tv_imageCount;

    Button btn_saveJob;
    Button btn_createJob;
    RecyclerView fStoreList;
    Button btn_addImages;

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

    ImageView iv_cancel_first;
    ImageView iv_cancel_second;

    public AddJob() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddJob.
     */
    // TODO: Rename and change types and number of parameters
    public static AddJob newInstance(String param1, String param2) {
        AddJob fragment = new AddJob();
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_job, container, false);

        btn_saveJob = view.findViewById(R.id.btn_saveJob);
        btn_addImages = view.findViewById(R.id.btn_addJobPictures);
        et_jobTitle = view.findViewById(R.id.et_jobTitle);
        et_jobDescription = view.findViewById(R.id.et_jobDescription);
        btn_createJob = getActivity().findViewById(R.id.btn_newJob);
        fStoreList = getActivity().findViewById(R.id.firestoreListJobs);

        iv_first = view.findViewById(R.id.iv_firstImage);
        iv_second = view.findViewById(R.id.iv_secondImage);
        tv_imageCount = view.findViewById(R.id.tv_imgCount);

        iv_first.setVisibility(View.INVISIBLE);
        iv_second.setVisibility(View.INVISIBLE);

        iv_cancel_first = view.findViewById(R.id.iv_cancel_first_image);
        iv_cancel_first.setVisibility(View.INVISIBLE);
        iv_cancel_second = view.findViewById(R.id.iv_cancel_second_image);
        iv_cancel_second.setVisibility(View.INVISIBLE);


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btn_addImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Clear images
                Glide.with(getContext())
                        .clear(iv_first);
                Glide.with(getContext())
                        .clear(iv_second);
                tv_imageCount.setVisibility(View.INVISIBLE);
                iv_first.setVisibility(View.INVISIBLE);
                iv_second.setVisibility(View.INVISIBLE);
                iv_cancel_first.setVisibility(View.INVISIBLE);
                iv_cancel_second.setVisibility(View.INVISIBLE);

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select pictures"), PICK_IMAGE_MULTIPLE);
            }
        });

        btn_saveJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Objects
                Map<String, Object> job = new HashMap<>();
                String pTitle = et_jobTitle.getText().toString();
                String pDesc = et_jobDescription.getText().toString();


                //Content validation
                if(pTitle.length() >= 10 && pTitle.length() <= 50 && pDesc.length() >= 10 && pDesc.length() <= 400 && (mImageUri != null || mArrayUri.size() > 0)){
                    job.put("title", pTitle);
                    job.put("description", pDesc);
                    job.put("creation_date", todayDate);

                    if(mImageUri == null){
                        job.put("imageCount", mArrayUri.size());
                    }else{
                        job.put("imageCount", 1);
                    }
                    fStore.collection("user").document(UID)
                            .collection("jobs")
                            .document(pTitle)
                            .set(job)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    //Toast.makeText(getContext(), "Project created", Toast.LENGTH_SHORT).show();
                                    if(mArrayUri.size() > 0){
                                        for(int i = 0; i < mArrayUri.size(); i++){
                                            Bitmap bitmap = null;
                                            try {
                                                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), mArrayUri.get(i));
                                                uploadImageToFirebaseStorage(bitmap, i);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }else{
                                        try {
                                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), mImageUri);
                                            uploadSingleImageToFirebase(bitmap);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                    }

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                    btn_createJob.setVisibility(View.VISIBLE);
                    fStoreList.setVisibility(View.VISIBLE);
                    getActivity().getSupportFragmentManager().beginTransaction().remove(AddJob.this).commit();

                }else{
                    if(pTitle.length() < 10 || pTitle.length() > 50){
                        et_jobTitle.setError("Title length must be between 10 and 50 characters!");
                        et_jobTitle.requestFocus();
                    }else if(mImageUri == null || mArrayUri.size() == 0){
                        Toast.makeText(getContext(), "You must attach at least 1 image to the job", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        if(pDesc.length() < 10){
                            et_jobDescription.setError("Description length must be at least 10 characters!");
                        }else{
                            et_jobDescription.setError("Description can contain max 400 characters!");
                        }
                        et_jobDescription.requestFocus();
                    }
                }
            }
        });

        iv_cancel_first.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mArrayUri.size() == 0){
                    Glide.with(getContext()).clear(iv_first);
                    iv_first.setVisibility(View.INVISIBLE);
                    iv_cancel_first.setVisibility(View.INVISIBLE);
                    tv_imageCount.setVisibility(View.INVISIBLE);
                    mImageUri = null;
                }else if(mArrayUri.size() == 1){
                    Glide.with(getContext()).clear(iv_first);
                    iv_first.setVisibility(View.INVISIBLE);
                    iv_cancel_first.setVisibility(View.INVISIBLE);
                    tv_imageCount.setVisibility(View.INVISIBLE);
                    mArrayUri.remove(0);
                }
                else if(mArrayUri.size() == 2){
                    Glide.with(getContext()).clear(iv_first);
                    Glide.with(getContext()).clear(iv_second);
                    mArrayUri.remove(0);
                    iv_second.setVisibility(View.INVISIBLE);
                    iv_cancel_second.setVisibility(View.INVISIBLE);
                    tv_imageCount.setVisibility(View.INVISIBLE);
                    Glide.with(getContext()).load(mArrayUri.get(0)).into(iv_first);
                }else{
                    Glide.with(getContext()).clear(iv_first);
                    Glide.with(getContext()).clear(iv_second);
                    mArrayUri.remove(0);
                    //iv_second.setVisibility(View.INVISIBLE);
                    //iv_cancel_second.setVisibility(View.INVISIBLE);
                    if(mArrayUri.size() == 2){
                        tv_imageCount.setVisibility(View.INVISIBLE);
                    }else{
                        tv_imageCount.setText("+" + (mArrayUri.size() - 2));
                    }
                    Glide.with(getContext()).load(mArrayUri.get(0)).into(iv_first);
                    Glide.with(getContext()).load(mArrayUri.get(1)).into(iv_second);
                }


            }
        });

        iv_cancel_second.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mArrayUri.size() == 2){
                    Glide.with(getContext()).clear(iv_first);
                    Glide.with(getContext()).clear(iv_second);
                    mArrayUri.remove(0);
                    iv_second.setVisibility(View.INVISIBLE);
                    iv_cancel_second.setVisibility(View.INVISIBLE);
                    tv_imageCount.setVisibility(View.INVISIBLE);
                    Glide.with(getContext()).load(mArrayUri.get(0)).into(iv_first);
                }else{
                    Glide.with(getContext()).clear(iv_first);
                    Glide.with(getContext()).clear(iv_second);
                    mArrayUri.remove(0);
                    if(mArrayUri.size() == 2){
                        tv_imageCount.setVisibility(View.INVISIBLE);
                    }else{
                        tv_imageCount.setText("+" + (mArrayUri.size() - 2));
                    }
                    Glide.with(getContext()).load(mArrayUri.get(0)).into(iv_first);
                    Glide.with(getContext()).load(mArrayUri.get(1)).into(iv_second);
                }
            }
        });

        tv_imageCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "To be added!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        try {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_IMAGE_MULTIPLE);
            }else{
                // When an Image is picked
                if (requestCode == PICK_IMAGE_MULTIPLE && resultCode == getActivity().RESULT_OK
                        && null != data) {
                    // Get the Image from data

                    String[] filePathColumn = { MediaStore.Images.Media.DATA };
                    imagesEncodedList = new ArrayList<String>();
                    if(data.getData()!=null){

                        //Uri mImageUri=data.getData();
                        mImageUri = null;
                        mImageUri = data.getData();

                        // Get the cursor
                        Cursor cursor = getActivity().getContentResolver().query(mImageUri,
                                filePathColumn, null, null, null);
                        // Move to first row
                        cursor.moveToFirst();

                        Glide.with(getContext())
                                .load(mImageUri)
                                .into(iv_first);
                        iv_first.setVisibility(View.VISIBLE);
                        iv_cancel_first.setVisibility(View.VISIBLE);
                        iv_cancel_second.setVisibility(View.INVISIBLE);


                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        imageEncoded  = cursor.getString(columnIndex);
                        cursor.close();

                    } else {
                        if (data.getClipData() != null) {
                            ClipData mClipData = data.getClipData();
                            //ArrayList<Uri> mArrayUri = new ArrayList<Uri>();
                            mArrayUri.clear();
                            for (int i = 0; i < mClipData.getItemCount(); i++) {

                                ClipData.Item item = mClipData.getItemAt(i);
                                Uri uri = item.getUri();
                                mArrayUri.add(uri);
                                // Get the cursor
                                Cursor cursor = getActivity().getContentResolver().query(uri, filePathColumn, null, null, null);
                                // Move to first row
                                cursor.moveToFirst();

                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                imageEncoded  = cursor.getString(columnIndex);
                                imagesEncodedList.add(imageEncoded);
                                cursor.close();

                            }
                            if(mArrayUri.size() > 2){
                                Glide.with(getContext())
                                        .load(mArrayUri.get(0))
                                        .into(iv_first);
                                Glide.with(getContext())
                                        .load(mArrayUri.get(1))
                                        .into(iv_second);
                                int imageCount = mArrayUri.size() - 2;
                                tv_imageCount.setText("+" + imageCount);

                                iv_first.setVisibility(View.VISIBLE);
                                iv_second.setVisibility(View.VISIBLE);
                                tv_imageCount.setVisibility(View.VISIBLE);
                                iv_cancel_first.setVisibility(View.VISIBLE);
                                iv_cancel_second.setVisibility(View.VISIBLE);

                            }else if(mArrayUri.size() == 2){
                                Glide.with(getContext())
                                        .load(mArrayUri.get(0))
                                        .into(iv_first);
                                Glide.with(getContext())
                                        .load(mArrayUri.get(1))
                                        .into(iv_second);

                                iv_first.setVisibility(View.VISIBLE);
                                iv_second.setVisibility(View.VISIBLE);
                                tv_imageCount.setVisibility(View.INVISIBLE);
                                iv_cancel_first.setVisibility(View.VISIBLE);
                                iv_cancel_second.setVisibility(View.VISIBLE);
                            }else{
                                iv_first.setVisibility(View.INVISIBLE);
                                iv_second.setVisibility(View.INVISIBLE);
                                tv_imageCount.setVisibility(View.INVISIBLE);
                                iv_cancel_first.setVisibility(View.INVISIBLE);
                                iv_cancel_second.setVisibility(View.INVISIBLE);
                            }
                            Log.d("MULTIPLE IMAGE PICKER: ", "Selected Images" + mArrayUri.size());
                        }
                    }
                } else {
                    Toast.makeText(getContext(), "Request code: " + requestCode + " and result code: " + resultCode,
                            Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_LONG)
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

    private void uploadSingleImageToFirebase(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        StorageReference reference = FirebaseStorage.getInstance().getReference()
                .child("images")
                .child(UID)
                .child("jobs")
                .child(et_jobTitle.getText().toString())
                .child(todayDate + "_image_0.jpeg");

        reference.putBytes(baos.toByteArray())
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d("TAG", "Upload of image  successful");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("TAG", "onFailure: ", e.getCause());
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
}