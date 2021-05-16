package com.novoseltech.handymano.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.novoseltech.handymano.MainActivity;
import com.novoseltech.handymano.R;

import java.util.ArrayList;
import java.util.List;

/**
 @author Denis Novosel
 @student_id 17104718
 @email x17104718@student.ncirl.ie
 @github https://github.com/adminnovoseltech/SoftwareProjectDN17104718
 @class ProfileDeleteDialog.java
 **/

public class ProfileDeleteDialog extends DialogFragment {
    private static final String TAG = ProfileDeleteDialog.class.getSimpleName();

    //Idea from
    //1. https://stackoverflow.com/questions/38114689/how-to-delete-a-firebase-user-from-android-app
    //2. https://firebase.google.com/docs/auth/admin/manage-users#java_7

    //Layout components
    private EditText et_passwordConfirmationOne;
    private EditText et_passwordConfirmationTwo;

    //Firebase objects
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileDeleteDialog() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileDeleteFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileDeleteDialog newInstance(String param1, String param2) {
        ProfileDeleteDialog fragment = new ProfileDeleteDialog();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Handler handler = new Handler();
        //We are building the dialog which when shown will inflate the layout file of the fragment with two
        //EditText fields

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_profile_delete, null);

        et_passwordConfirmationOne = view.findViewById(R.id.et_passwordConfirmationOne);
        et_passwordConfirmationTwo = view.findViewById(R.id.et_passwordConfirmationTwo);

        String email = user.getEmail();
        String UID = user.getUid();
        String USER_TYPE = getArguments().getString("USER_TYPE");
        String USERNAME = getArguments().getString("USERNAME");
        String RECIPIENT_ENTRY = UID + "," + USERNAME;

        builder.setView(view).setTitle("Password change")
                .setPositiveButton("Confirm", null)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        final AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Check if password fields are empty
                if(et_passwordConfirmationOne.getText().toString().equals("") || et_passwordConfirmationTwo.getText().toString().equals("")){
                    Toast.makeText(getContext(), "Password must be entered", Toast.LENGTH_SHORT).show();
                }else{
                    //Are the passwords same?
                    if(et_passwordConfirmationOne.getText().toString().equals(et_passwordConfirmationTwo.getText().toString())){
                        //Building the credential used for re-authentication
                        AuthCredential credential = EmailAuthProvider.getCredential(email, et_passwordConfirmationOne.getText().toString());
                        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){

                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            //If we have a standard account
                                            if(USER_TYPE.equals("Standard")){

                                                //We are iterating through all the jobs created by the user
                                                //So that we have details for deleting images on Firebase Storage
                                                fStore.collection("user")
                                                        .document(UID)
                                                        .collection("jobs")
                                                        .get()
                                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                                                //Get each job details
                                                                for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                                                                    long imgCount = queryDocumentSnapshot.getLong("imageCount");
                                                                    String jobCreationDate = queryDocumentSnapshot.getString("creation_date");
                                                                    String jobTitle = queryDocumentSnapshot.getId();

                                                                    //Start deleting images
                                                                    StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                                                                            .child("images")
                                                                            .child(UID)
                                                                            .child("jobs")
                                                                            .child(jobTitle);

                                                                    for(int l = 0; l < imgCount; l++){
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
                                                                }
                                                                //Delete user's profile image
                                                                StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                                                                        .child("images")
                                                                        .child(UID)
                                                                        .child("profile_image_" + UID + ".jpeg");
                                                                storageReference.delete()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if(task.isSuccessful()){
                                                                                    Log.d(TAG, "Profile image deleted");
                                                                                }else{

                                                                                }
                                                                            }
                                                                        });
                                                                //After we deleted all the images then delete user's document in Firestore
                                                                fStore.collection("user")
                                                                        .document(UID)
                                                                        .delete()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if(task.isSuccessful()){
                                                                                    Log.d(TAG, "User document deleted");
                                                                                }

                                                                            }
                                                                        });


                                                            }
                                                        });


                                            }else{
                                                //Same functionality as above
                                                //but for Professional users
                                                //and deleting Projects instead of Jobs
                                                fStore.collection("user")
                                                        .document(UID)
                                                        .collection("projects")
                                                        .get()
                                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                                                                    long imgCount = queryDocumentSnapshot.getLong("imageCount");
                                                                    String projectCreationDate = queryDocumentSnapshot.getString("creation_date");
                                                                    String projectTitle = queryDocumentSnapshot.getId();

                                                                    //Start deleting images
                                                                    StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                                                                            .child("images")
                                                                            .child(UID)
                                                                            .child("projects")
                                                                            .child(projectTitle);

                                                                    for(int l = 0; l < imgCount; l++){
                                                                        StorageReference sr = null;
                                                                        sr = storageReference.child(projectCreationDate + "_image_" + l + ".jpeg");
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
                                                                }


                                                                StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                                                                        .child("images")
                                                                        .child(UID)
                                                                        .child("profile_image_" + UID + ".jpeg");
                                                                storageReference.delete()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if(task.isSuccessful()){
                                                                                    Log.d(TAG, "Profile image deleted");
                                                                                }else{

                                                                                }
                                                                            }
                                                                        });

                                                                fStore.collection("user")
                                                                        .document(UID)
                                                                        .delete()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if(task.isSuccessful()){
                                                                                    Log.d(TAG, "User document deleted");
                                                                                }

                                                                            }
                                                                        });


                                                            }
                                                        });
                                            }

                                            //Deleting the user's chat document
                                            fStore.collection("chat")
                                                    .document(UID)
                                                    .delete()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                Log.d(TAG, "User document deleted");
                                                            }
                                                        }
                                                    });

                                            //Deleting the user's "rating" document
                                            fStore.collection("rating")
                                                    .document(UID)
                                                    .delete()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                Log.d(TAG, "User document deleted");
                                                            }
                                                        }
                                                    });

                                            //Iterating through the chats of other users to delete us from their recipient list as well as deleting the
                                            //chats stored in their subcollection
                                            fStore.collection("chat")
                                                    .whereArrayContains("recipients", RECIPIENT_ENTRY)
                                                    .get()
                                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                            for(QueryDocumentSnapshot documentSnapshot: task.getResult()){
                                                                List<String> recipients = new ArrayList<>();
                                                                recipients = (List<String>) documentSnapshot.get("recipients");
                                                                int indexToRemove = recipients.indexOf(RECIPIENT_ENTRY);
                                                                recipients.remove(indexToRemove);
                                                                String SENDER_UID = documentSnapshot.getId();
                                                                fStore.collection("chat")
                                                                        .document(SENDER_UID)
                                                                        .update("recipients", recipients)
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if(task.isSuccessful()){
                                                                                    Log.d(TAG, "Recipients updated");
                                                                                }else{

                                                                                }
                                                                            }
                                                                        });

                                                                fStore.collection("chat")
                                                                        .document(SENDER_UID)
                                                                        .collection(UID)
                                                                        .get()
                                                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                                for(QueryDocumentSnapshot documentSnapshot1 : task.getResult()){
                                                                                    fStore.collection("chat")
                                                                                            .document(SENDER_UID)
                                                                                            .collection(UID)
                                                                                            .document(documentSnapshot1.getId())
                                                                                            .delete()
                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                    if(task.isSuccessful()){
                                                                                                        Log.d(TAG, "Document deleted successfully");
                                                                                                    }else{

                                                                                                    }
                                                                                                }
                                                                                            });
                                                                                }
                                                                            }
                                                                        });


                                                            }
                                                        }
                                                    });

                                        }
                                    }, 2000);

                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    //User deleted
                                                    if(task.isSuccessful()){
                                                        //Account is deleted
                                                        //Transfer the user to the MainActivity

                                                        Intent intent = new Intent(getContext(), MainActivity.class);
                                                        startActivity(intent);

                                                    }else{
                                                        Toast.makeText(getContext(), task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                    }, 6000);

                                }else{
                                    Toast.makeText(getContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }else{
                        Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        return dialog;
    }
}