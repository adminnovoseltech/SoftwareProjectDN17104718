package com.novoseltech.handymano.views.standard;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
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
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.novoseltech.handymano.MainActivity;
import com.novoseltech.handymano.R;
import com.novoseltech.handymano.fragments.PasswordChangeDialog;
import com.novoseltech.handymano.fragments.PasswordConfirmationDialog;
import com.novoseltech.handymano.views.message.MessageMenu;
import com.novoseltech.handymano.views.standard.job.JobsActivity;
import com.novoseltech.handymano.views.standard.job.StandardJobViewActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StandardProfileActivity extends AppCompatActivity implements PasswordConfirmationDialog.PasswordConfirmationDialogListener {

    //Layout components
    DrawerLayout drawerLayout;
    CircularImageView iv_sp_profilePhoto;
    TextInputLayout til_sp_username;
    TextInputLayout til_sp_phoneNo;
    TextInputLayout til_sp_email;
    EditText et_sp_username;
    EditText et_sp_phoneNo;
    EditText et_sp_email;
    Button btn_edit_sp;
    Button btn_save_sp;
    Button btn_stdUserChangePassword;
    CircularImageView profileImage;
    TextView tv_UserName;
    TextView tv_sp_username;
    TextView tv_sp_email;
    TextView tv_sp_phoneNo;

    //Firebase components
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();

    //Variables
    private static final String TAG = "LOG";
    private static final int PICK_FROM_GALLERY = 10000;
    String UID = mAuth.getCurrentUser().getUid();
    String username;
    String email = mAuth.getCurrentUser().getEmail();
    String phoneNo;
    String pass = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_standard_profile);
        drawerLayout = findViewById(R.id.drawer_layout_standard);

        profileImage = drawerLayout.findViewById(R.id.civ_profilePictureStandard);
        if(user.getPhotoUrl() != null){
            Glide.with(getApplicationContext())
                    .load(user.getPhotoUrl())
                    .into(profileImage);
        }

        tv_UserName = drawerLayout.findViewById(R.id.text_UserName_Standard);
        fStore.collection("user")
                .document(user.getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    tv_UserName.setText(documentSnapshot.getString("username"));
                }
            }
        });

        tv_sp_username = findViewById(R.id.tv_sp_username);
        tv_sp_email = findViewById(R.id.tv_sp_email);
        tv_sp_email.setText(email);
        tv_sp_phoneNo = findViewById(R.id.tv_sp_phoneNo);

        iv_sp_profilePhoto = findViewById(R.id.iv_sp_profilePhoto);

        //Editing data
        til_sp_username = findViewById(R.id.layout_sp_username_edit);
        til_sp_phoneNo = findViewById(R.id.layout_sp_phoneno_edit);
        til_sp_email = findViewById(R.id.layout_sp_email_edit);
        et_sp_username = findViewById(R.id.et_sp_username_edit);
        et_sp_phoneNo = findViewById(R.id.et_sp_phoneno_edit);
        et_sp_email = findViewById(R.id.et_sp_email_edit);
        til_sp_username.setVisibility(View.INVISIBLE);
        til_sp_phoneNo.setVisibility(View.INVISIBLE);
        til_sp_email.setVisibility(View.INVISIBLE);
        btn_edit_sp = findViewById(R.id.btn_edit_sp);
        btn_save_sp = findViewById(R.id.btn_save_sp);
        btn_save_sp.setVisibility(View.INVISIBLE);
        btn_stdUserChangePassword = findViewById(R.id.btn_stdUserChangePassword);

        if(mAuth.getCurrentUser().getPhotoUrl() != null){
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .into(iv_sp_profilePhoto);
        }

        fStore.collection("user").document(UID).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot documentSnapshot = task.getResult();

                            username = documentSnapshot.getString("username");
                            phoneNo = documentSnapshot.getString("phoneNo");
                            tv_sp_username.setText(documentSnapshot.getString("username"));
                            tv_sp_phoneNo.setText(documentSnapshot.getString("phoneNo"));
                            et_sp_username.setText(username);
                            et_sp_phoneNo.setText(phoneNo);
                            et_sp_email.setText(email);

                        }else {
                            Exception exception = task.getException();
                        }
                    }
                });

        btn_edit_sp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_edit_sp.setVisibility(View.INVISIBLE);
                til_sp_username.setVisibility(View.VISIBLE);
                til_sp_phoneNo.setVisibility(View.VISIBLE);
                til_sp_email.setVisibility(View.VISIBLE);
                tv_sp_username.setVisibility(View.INVISIBLE);
                tv_sp_phoneNo.setVisibility(View.INVISIBLE);
                tv_sp_email.setVisibility(View.INVISIBLE);
                btn_save_sp.setVisibility(View.VISIBLE);
                btn_stdUserChangePassword.setVisibility(View.GONE);
            }
        });



        btn_save_sp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_save_sp.setVisibility(View.INVISIBLE);
                til_sp_username.setVisibility(View.INVISIBLE);
                til_sp_phoneNo.setVisibility(View.INVISIBLE);
                til_sp_email.setVisibility(View.INVISIBLE);
                tv_sp_username.setVisibility(View.VISIBLE);
                tv_sp_phoneNo.setVisibility(View.VISIBLE);
                tv_sp_email.setVisibility(View.VISIBLE);
                btn_stdUserChangePassword.setVisibility(View.VISIBLE);

                //If nothing was changed
                if(et_sp_username.getText().toString().equals(username) &&
                        et_sp_phoneNo.getText().toString().equals(phoneNo) &&
                        et_sp_email.getText().toString().equals(email)){

                    Log.d(TAG, "Nothing to update!");

                }else{
                    //If something was changed
                    openDialog();
                }

                btn_edit_sp.setVisibility(View.VISIBLE);

            }
        });

        btn_stdUserChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        iv_sp_profilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (ActivityCompat.checkSelfPermission(StandardProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(StandardProfileActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_FROM_GALLERY);
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

                    iv_sp_profilePhoto.setImageBitmap(bitmap);
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
                        Toast.makeText(getApplicationContext(), "Profile image updated", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Profile image failed", Toast.LENGTH_SHORT).show();
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
        Map<String, Object> userHmap = new HashMap<>();
        userHmap.put("username", et_sp_username.getText().toString());
        userHmap.put("phoneNo", et_sp_phoneNo.getText().toString());
        userHmap.put("email", et_sp_email.getText().toString());
        fStore.collection("user").document(UID).update(userHmap);

        //Update account's email

        if(et_sp_email.getText().toString().equals(email)){
            Log.d(TAG, "Email will not be updated - no change detected");
        }else{
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            AuthCredential credential = EmailAuthProvider.getCredential(email, pass);
            user.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d(TAG, "User re-authenticated");

                            user.updateEmail(et_sp_email.getText().toString())
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

    public void logout(){
        //Close app
        //Initialize alert dialog
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
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
                Intent intent = new Intent(StandardProfileActivity.this, MainActivity.class);
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

    public void ClickMenu(View view) {
        openDrawer(drawerLayout);
    }

    public static void openDrawer(DrawerLayout drawerLayout) {
        //Open drawer layout
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public static void closeDrawer(DrawerLayout drawerLayout) {
        //Close drawer layout
        //Check condition
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            //When drawer is open
            //Close drawer
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    public void ClickJobs(View view){
        //recreate the activity

        Intent intent = new Intent(StandardProfileActivity.this, JobsActivity.class);
        startActivity(intent);
    }

    public void ClickLogOut(View view) {
        logout();
    }

    public void ClickHome(View view) {
        Intent intent = new Intent(StandardProfileActivity.this, HomeActivityStandard.class);
        startActivity(intent);
    }

    public void ClickProfile(View view) {
        finish();
        startActivity(getIntent());
    }

    public void ClickMessages(View view) {
        Intent intent = new Intent(StandardProfileActivity.this, MessageMenu.class);
        intent.putExtra("USER_TYPE", "Standard");
        startActivity(intent);
    }

    public void showDialog(){
        PasswordChangeDialog exampleDialog = new PasswordChangeDialog();
        exampleDialog.show(getSupportFragmentManager(), "example dialog");
    }


}


