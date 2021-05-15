package com.novoseltech.handymano.views.professional.project;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
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
import com.novoseltech.handymano.Functions;
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

public class CreateProject extends AppCompatActivity {

    //Layout components
    private Button btn_saveProject;
    private EditText et_projectTitle;
    private EditText et_projectDescription;
    private ImageView iv_addImg;
    private ImageView iv_deleteImg;
    private SliderView sliderView;
    private SliderAdapter adapter;

    //Firebase components
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();

    //Variables
    private int REQUEST_STORAGE_PERMISSION_CODE = 2014;
    private long imageCount = 0;
    private String imageEncoded;
    private List<String> imagesEncodedList;
    private String UID = user.getUid();
    private Uri mImageUri;
    private ArrayList<Uri> mArrayUri = new ArrayList<Uri>();
    private Date dt = Calendar.getInstance().getTime();
    private SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
    private String todayDate = df.format(dt);
    private List<SliderItem> imagesArrayList = new ArrayList<>();
    private Functions appFunctions = new Functions();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_project);

        btn_saveProject = findViewById(R.id.btn_saveProject);
        et_projectTitle = findViewById(R.id.et_projectTitle);
        et_projectDescription = findViewById(R.id.et_projectDescription);

        iv_addImg = findViewById(R.id.iv_addProjectImg);
        iv_deleteImg = findViewById(R.id.iv_deleteProjectImg);
        sliderView = findViewById(R.id.imagesSliderProjectAdd);

        adapter = new SliderAdapter(getApplicationContext());
        sliderView.setSliderAdapter(adapter);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Image slider settings
                sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM); //set indicator animation by using IndicatorAnimationType. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
                sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
                sliderView.setIndicatorSelectedColor(Color.WHITE);
                sliderView.setIndicatorUnselectedColor(Color.GRAY);

                sliderView.setOnIndicatorClickListener(new DrawController.ClickListener() {
                    @Override
                    public void onIndicatorClicked(int position) {
                        //Log.i("GGG", "onIndicatorClicked: " + sliderView.getCurrentPagePosition());
                    }
                });

            }
        }, 600);

        iv_addImg.setOnClickListener(new View.OnClickListener() {
            //When adding images check for permission
            @Override
            public void onClick(View v) {
                //If the permission is not granted
                //WRITE_EXTERNAL_STORAGE is required for Android v < 10
                if(getApplicationContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || getApplicationContext().
                        checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION_CODE);
                }else{
                    //If storage permission is granted
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select pictures"), REQUEST_STORAGE_PERMISSION_CODE);
                }

            }
        });

        //Deleting SliderItems from the ArrayList and from the Image slider
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

        btn_saveProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Objects
                Map<String, Object> project = new HashMap<>();
                String projectTitle = et_projectTitle.getText().toString();
                String projectDescription = et_projectDescription.getText().toString();


                //Content validation
                if(projectTitle.length() >= 10 && projectTitle.length() <= 50 && projectDescription.length() >= 10
                        && projectDescription.length() <= 400 && (imagesArrayList.size() > 0)
                        && !appFunctions.containsOffensiveWord(projectTitle) && !appFunctions.containsOffensiveWord(projectDescription)){
                    project.put("title", projectTitle);
                    project.put("description", projectDescription);
                    project.put("creation_date", todayDate);
                    project.put("imageCount", imagesArrayList.size());

                    //Save the project data in the
                    fStore.collection("user").document(UID)
                            .collection("projects")
                            .document(projectTitle)
                            .set(project)
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

                    Intent intent = new Intent(CreateProject.this, ProjectsActivity.class);
                    startActivity(intent);

                }else{
                    if(projectTitle.length() < 10 || projectTitle.length() > 50){
                        et_projectTitle.setError("Title length must be between 10 and 50 characters!");
                        et_projectTitle.requestFocus();
                    }else if(imagesArrayList.size() == 0){
                        Toast.makeText(getApplicationContext(), "You must attach at least 1 image to the project", Toast.LENGTH_SHORT).show();
                    }else if(appFunctions.containsOffensiveWord(projectTitle)){
                        et_projectTitle.setError("Project title contains an offensive word. Please change it to something more appropriate.");
                        et_projectTitle.requestFocus();
                    }else if(appFunctions.containsOffensiveWord(projectDescription)){
                        et_projectDescription.setError("Project description contains an offensive word(s). Please remove the offensive word(s).");
                        et_projectDescription.requestFocus();
                    }else{
                        if(projectDescription.length() < 10){
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        try {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getParent(), new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION_CODE);
            }else{
                // When an Image is picked
                if (requestCode == REQUEST_STORAGE_PERMISSION_CODE && resultCode == RESULT_OK
                        && null != data) {
                    String[] filePathColumn = { MediaStore.Images.Media.DATA };

                    //Ideas from:
                    //1. https://stackoverflow.com/questions/59902190/how-to-get-the-path-of-selected-image-from-gallery
                    //2. https://stackoverflow.com/questions/3879992/how-to-get-bitmap-from-an-uri
                    //3. https://stackoverflow.com/questions/19585815/select-multiple-images-from-android-gallery
                    imagesEncodedList = new ArrayList<String>();

                    //if one image is selected
                    if(data.getData() != null){
                        mImageUri = null;
                        mImageUri = data.getData();

                        //Add single image to the array list of slider items
                        //and load the image into the carousel
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
                                //Get the uri of each picture selected
                                //and add them to arraylist of SliderItems as well as load them into the carousel
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
        if(requestCode == REQUEST_STORAGE_PERMISSION_CODE) {
            // If request is cancelled, the result arrays are empty.
            //Idea from https://stackoverflow.com/questions/19585815/select-multiple-images-from-android-gallery
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select pictures"), REQUEST_STORAGE_PERMISSION_CODE);
            } else {
                //Show error message that prevents that informs them about permission
                Toast.makeText(getApplicationContext(), "Storage permission is denied. Please allow it in the Settings to enable this functionality.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadImageToFirebaseStorage(Bitmap bitmap, int imgCount) {
        //https://firebase.google.com/docs/storage/android/start
        //Uploading images based on the number of images in the arraylist
        //then name the folder based on the UID of creator,
        //subfolder as the project name
        // and image files as the date and the number of image in the index
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

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        discardChanges();
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
                Intent intent = new Intent(CreateProject.this, ProjectsActivity.class);
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
}