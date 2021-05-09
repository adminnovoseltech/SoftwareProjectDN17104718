package com.novoseltech.handymano.views.professional;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.novoseltech.handymano.MainActivity;
import com.novoseltech.handymano.R;
import com.novoseltech.handymano.fragments.AddressSelect;
import com.novoseltech.handymano.fragments.PasswordConfirmationDialog;
import com.novoseltech.handymano.views.message.MessageMenu;
import com.novoseltech.handymano.views.professional.feedback.FeedbackList;
import com.novoseltech.handymano.views.professional.job.JobsList;
import com.novoseltech.handymano.views.professional.project.ProjectsActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProfessionalProfileActivity extends AppCompatActivity implements PasswordConfirmationDialog.PasswordConfirmationDialogListener{

    DrawerLayout drawerLayout;

    private static final String TAG = "LOG: ";
    private static final int PICK_FROM_GALLERY = 10000;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    String UID = mAuth.getCurrentUser().getUid();

    String username;
    String email = mAuth.getCurrentUser().getEmail();
    String phoneNo;

    double latitude;
    double longitude;
    String radius;

    double tmpLat;
    double tmpLon;
    String tmpRad;
    String tmpAddress;

    String trade;
    String yearsOfExp;

    String pass = "";

    ShapeableImageView iv_pp_profilePhoto;

    //Location
    FrameLayout mapFrame;

    //Editing data
    TextInputLayout til_pp_username;
    TextInputLayout til_pp_phoneNo;
    TextInputLayout til_pp_email;

    EditText et_pp_username;
    EditText et_pp_phoneNo;
    EditText et_pp_email;

    Button btn_edit_pp;
    Button btn_save_pp;
    Button btn_chooseLocation;
    Button btn_saveLoc;

    ScrollView sv_pro;
    ConstraintLayout cl_editProfile;

    Boolean editMode = false;

    TextView tv_currentAddress;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professional_profile);
        drawerLayout = findViewById(R.id.drawer_layout_professional);

        cl_editProfile = findViewById(R.id.cl_editProfileForm);

        TextView tv_pp_username = findViewById(R.id.tv_pp_username);
        TextView tv_pp_email = findViewById(R.id.tv_pp_email);
        tv_pp_email.setText(email);
        TextView tv_pp_phoneNo = findViewById(R.id.tv_pp_phoneNo);

        tv_currentAddress = findViewById(R.id.textView_currentAddress);
        tv_currentAddress.setVisibility(View.VISIBLE);
        tv_currentAddress.setVisibility(View.VISIBLE);

        iv_pp_profilePhoto = findViewById(R.id.iv_pp_profilePhoto);

        mapFrame = findViewById(R.id.frame_loc_pro);
        mapFrame.setVisibility(View.GONE);

        sv_pro = findViewById(R.id.scrollView2);


        //Editing data
        til_pp_username = findViewById(R.id.layout_pp_username_edit);
        til_pp_phoneNo = findViewById(R.id.layout_pp_phoneno_edit);
        til_pp_email = findViewById(R.id.layout_pp_email_edit);

        et_pp_username = findViewById(R.id.et_pp_username_edit);
        et_pp_phoneNo = findViewById(R.id.et_pp_phoneno_edit);
        et_pp_email = findViewById(R.id.et_pp_email_edit);

        til_pp_username.setVisibility(View.INVISIBLE);
        til_pp_phoneNo.setVisibility(View.INVISIBLE);
        til_pp_email.setVisibility(View.INVISIBLE);

        btn_edit_pp = findViewById(R.id.btn_edit_pp);
        btn_save_pp = findViewById(R.id.btn_save_pp);

        btn_save_pp.setVisibility(View.INVISIBLE);

        btn_chooseLocation = findViewById(R.id.btn_chooseLoc);
        btn_chooseLocation.setVisibility(View.GONE);
        AddressSelect af = new AddressSelect();

        FirebaseUser user = mAuth.getCurrentUser();

        if(mAuth.getCurrentUser().getPhotoUrl() != null){
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .into(iv_pp_profilePhoto);
        }

        CircularImageView profileImage = drawerLayout.findViewById(R.id.civ_profilePictureProfessional);
        TextView tv_drawerUsername = drawerLayout.findViewById(R.id.text_UserName_Professional);

        if(mAuth.getCurrentUser().getPhotoUrl() != null){
            Glide.with(getApplicationContext())
                    .load(user.getPhotoUrl())
                    .into(profileImage);
        }else{
            Log.d("TAG", "Profile image not found. Loading default image.");
        }

        fStore.collection("user").document(UID).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot documentSnapshot = task.getResult();

                            username = documentSnapshot.getString("username");
                            phoneNo = documentSnapshot.getString("phoneNo");

                            GeoPoint gp = documentSnapshot.getGeoPoint("location");
                            latitude = gp.getLatitude();
                            longitude = gp.getLongitude();
                            radius = documentSnapshot.getString("radius");

                            tmpLat = gp.getLatitude();
                            tmpLon = gp.getLongitude();
                            tmpRad = documentSnapshot.getString("radius");

                            String address = getAddressFromLatLng(tmpLat, tmpLon);

                            tv_currentAddress.setText(address);
                            tv_drawerUsername.setText(username);
                            trade = documentSnapshot.getString("category");
                            yearsOfExp = documentSnapshot.getString("experience");


                            tv_pp_username.setText(documentSnapshot.getString("username"));
                            tv_pp_phoneNo.setText(documentSnapshot.getString("phoneNo"));

                            et_pp_username.setText(username);
                            et_pp_phoneNo.setText(phoneNo);
                            et_pp_email.setText(email);


                        }else {
                            Exception exception = task.getException();
                            Log.e(TAG, String.valueOf(exception));
                        }
                    }
                });

        btn_edit_pp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_edit_pp.setVisibility(View.INVISIBLE);

                til_pp_username.setVisibility(View.VISIBLE);
                til_pp_phoneNo.setVisibility(View.VISIBLE);
                til_pp_email.setVisibility(View.VISIBLE);
                btn_chooseLocation.setVisibility(View.VISIBLE);

                tv_pp_username.setVisibility(View.INVISIBLE);
                tv_pp_phoneNo.setVisibility(View.INVISIBLE);
                tv_pp_email.setVisibility(View.INVISIBLE);
                tv_currentAddress.setVisibility(View.GONE);

                btn_save_pp.setVisibility(View.VISIBLE);

                editMode = true;
            }
        });



        btn_save_pp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_save_pp.setVisibility(View.INVISIBLE);

                til_pp_username.setVisibility(View.INVISIBLE);
                til_pp_phoneNo.setVisibility(View.INVISIBLE);
                til_pp_email.setVisibility(View.INVISIBLE);
                btn_chooseLocation.setVisibility(View.INVISIBLE);

                tv_pp_username.setVisibility(View.VISIBLE);
                tv_pp_phoneNo.setVisibility(View.VISIBLE);
                tv_pp_email.setVisibility(View.VISIBLE);
                tv_currentAddress.setVisibility(View.VISIBLE);


                //If nothing was changed
                if(et_pp_username.getText().toString().equals(username) &&
                        et_pp_phoneNo.getText().toString().equals(phoneNo) &&
                        et_pp_email.getText().toString().equals(email) &&
                        (tmpLat == latitude) &&
                        (tmpLon == longitude) &&
                        (tmpRad == radius)



                        //((tmpAddress.equals(getAddressFromLatLng(tmpLat, tmpLon)) || tv_currentAddress.getText().toString().equals(getAddressFromLatLng(tmpLat, tmpLon)) )) &&
                        /*tmpRad.equals(radius)*/){

                    Log.d(TAG, "Nothing to update!");

                }else{
                    //If something was changed
                    openDialog();

                    Log.d(et_pp_username.getText().toString(), username);
                    Log.d(et_pp_email.getText().toString(), phoneNo);
                    Log.d(et_pp_email.getText().toString(), email);
                    //Log.d(tmpRad, radius);

                }


                btn_edit_pp.setVisibility(View.VISIBLE);

                editMode = false;

            }
        });




        //Location choice
        btn_chooseLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("mode", "Edit");
                bundle.putDouble("lat", tmpLat);
                bundle.putDouble("lon", tmpLon);
                bundle.putString("radius", tmpRad);
                af.setArguments(bundle);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_loc_pro, af)
                        .commit();

                cl_editProfile.setVisibility(View.GONE);
                mapFrame.setVisibility(View.VISIBLE);
            }
        });


        iv_pp_profilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (ActivityCompat.checkSelfPermission(ProfessionalProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ProfessionalProfileActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_FROM_GALLERY);
                    } else {
                        /*Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(galleryIntent, PICK_FROM_GALLERY);*/

                        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/*");
                        //Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if(intent.resolveActivity(getPackageManager()) != null){
                            startActivityForResult(intent, PICK_FROM_GALLERY);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_FROM_GALLERY){
            switch (resultCode){
                case RESULT_OK:
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = { MediaStore.Images.Media.DATA };

                    Cursor cursor = getContentResolver().query(selectedImage,
                            filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String picturePath = cursor.getString(columnIndex);
                    cursor.close();

                    Bitmap bitmap = BitmapFactory.decodeFile(picturePath);

                    iv_pp_profilePhoto.setImageBitmap(bitmap);
                    uploadImageToFirebaseStorage(bitmap);
            }
        }
    }

    private void uploadImageToFirebaseStorage(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        StorageReference reference = FirebaseStorage.getInstance().getReference()
                .child("images")
                .child(UID)
                .child("profile_image_" + UID + ".jpeg");

        reference.putBytes(baos.toByteArray())
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        getDownloadUrl(reference);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure: ", e.getCause());
            }
        });
    }

    private void getDownloadUrl(StorageReference reference){
        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.d(TAG, "onSuccess: " + uri);
                setUserProfileUrl(uri);
            }
        });
    }

    private void setUserProfileUrl(Uri uri) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setPhotoUri(uri)
                .build();
        user.updateProfile(request)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults)
    {
        switch (requestCode) {
            case PICK_FROM_GALLERY:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    //Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if(intent.resolveActivity(getPackageManager()) != null){
                        startActivityForResult(intent, PICK_FROM_GALLERY);
                    }
                } else {
                    //Show error message that prevents that informs them about permission
                }
                break;
        }
    }

    public void openDialog(){
        PasswordConfirmationDialog exampleDialog = new PasswordConfirmationDialog();
        exampleDialog.show(getSupportFragmentManager(), "example dialog");
    }

    @Override
    public Boolean passwordMatch(){
        //If password is matching and it is correct password then do the following

        //Update username and phone number (Updating user's Firestore document)

        GeoPoint gp = new GeoPoint(tmpLat, tmpLon);

        Map<String, Object> userHmap = new HashMap<>();
        userHmap.put("username", et_pp_username.getText().toString());
        userHmap.put("phoneNo", et_pp_phoneNo.getText().toString());
        userHmap.put("email", et_pp_email.getText().toString());
        userHmap.put("location", gp);
        userHmap.put("radius", tmpRad);
        fStore.collection("user").document(UID).update(userHmap);

        //Update account's email

        if(et_pp_email.getText().toString().equals(email)){
            Log.d(TAG, "Email will not be updated - no change detected");
        }else{
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            AuthCredential credential = EmailAuthProvider.getCredential(email, pass);
            user.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d(TAG, "User re-authenticated");

                            user.updateEmail(et_pp_email.getText().toString())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Log.d(TAG, "Email updated!");
                                        }
                                    });
                        }
                    });
        }
        finish();
        startActivity(getIntent());

        return true;

    }

    @Override
    public void applyPass(String password){

        pass = password;
    }

    public String getAddressFromLatLng(double lat, double lng){
        Geocoder geocoder;
        List<Address> addresses;
        String address = null;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(lat, lng, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();


        } catch (IOException e) {
            e.printStackTrace();
        }

        return address;

    }

    public void ClickMenu(View view) {
        openDrawer(drawerLayout);
    }

    public void ClickProfile(View view) {


        //recreate the activity

        finish();
        startActivity(getIntent());
    }

    public void ClickProjects(View view){
        finish();
        Intent intent = new Intent(ProfessionalProfileActivity.this, ProjectsActivity.class);
        startActivity(intent);
    }

    public void ClickJobs(View view){
        finish();
        Intent intent = new Intent(ProfessionalProfileActivity.this, JobsList.class);
        startActivity(intent);
    }


    public static void openDrawer(DrawerLayout drawerLayout) {
        //Open drawer layout
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public void ClickLogOut(View view) {
        logout();
    }

    public void ClickHome(View view){
        finish();
        Intent intent = new Intent(ProfessionalProfileActivity.this, HomeActivityProfessional.class);

        startActivity(intent);
    }

    public void ClickMessages(View view){
        finish();
        Intent intent = new Intent(ProfessionalProfileActivity.this, MessageMenu.class);
        intent.putExtra("USER_TYPE", "Professional");
        startActivity(intent);
    }

    public void ClickFeedback(View view){
        Intent intent = new Intent(ProfessionalProfileActivity.this, FeedbackList.class);
        finish();
        startActivity(intent);
    }

    public void logout(){
        //Close app
        //Initialize alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //Set title
        builder.setTitle("Log out");
        //Set message
        builder.setMessage("Are you sure you want to log out ?");
        //Yes button
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                FirebaseAuth.getInstance().signOut();
                finish();
                Intent intent = new Intent(ProfessionalProfileActivity.this, MainActivity.class);
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

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    public void setLocationData(String lat, String lon, String rad){
        tmpLat = Double.parseDouble(lat);
        tmpLon = Double.parseDouble(lon);
        tmpRad = rad;

        String address = getAddressFromLatLng(tmpLat, tmpLon);
        tmpAddress = address;
        tv_currentAddress.setText(address);


        if(editMode){
            btn_save_pp.setVisibility(View.VISIBLE);
            btn_edit_pp.setVisibility(View.GONE);
        }else{
            btn_save_pp.setVisibility(View.GONE);
            btn_edit_pp.setVisibility(View.VISIBLE);
        }
    }




}