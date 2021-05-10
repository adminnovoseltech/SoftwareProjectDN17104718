package com.novoseltech.handymano.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

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
import com.novoseltech.handymano.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PasswordChangeDialog extends DialogFragment {

    //Layout components
    private EditText et_oldPassword;
    private EditText et_newPasswordOne;
    private EditText et_newPasswordTwo;

    //Firebase objects
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public PasswordChangeDialog() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.password_change_dialog, null);

        et_oldPassword = view.findViewById(R.id.et_oldPassword);
        et_newPasswordOne = view.findViewById(R.id.et_newPasswordOne);
        et_newPasswordTwo = view.findViewById(R.id.et_newPasswordTwo);


        String userEmail = user.getEmail();


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
                if(et_oldPassword.getText().toString().equals("") || et_newPasswordOne.getText().toString().equals("") || et_newPasswordTwo.getText().toString().equals("")){
                    Toast.makeText(getContext(), "Cannot have empty fields", Toast.LENGTH_SHORT).show();
                }else{
                    if(et_newPasswordOne.getText().toString().equals(et_newPasswordTwo.getText().toString())){

                        if(isValidPassword(et_newPasswordOne.getText().toString())){
                            AuthCredential credential = EmailAuthProvider.getCredential(userEmail, et_oldPassword.getText().toString());
                            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        user.updatePassword(et_newPasswordOne.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Toast.makeText(getContext(), "Password is updated", Toast.LENGTH_SHORT).show();
                                                    dialog.dismiss();
                                                }else{
                                                    Toast.makeText(getContext(), task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }else{
                                        et_oldPassword.setError("Password incorrect");
                                    }
                                }
                            });
                        }else{
                            et_newPasswordOne.setError("Password must at least contain 1 digit, 1 uppercase, 1 lowercase and at least 8 characters in length");
                            et_newPasswordOne.requestFocus();
                            et_newPasswordTwo.setError("Password must at least contain 1 digit, 1 uppercase, 1 lowercase and at least 8 characters in length");
                            et_newPasswordTwo.requestFocus();
                        }


                    }else{
                        et_newPasswordOne.setError("Passwords do not match");
                        et_newPasswordTwo.setError("Passwords do not match");
                    }
                }

            }
        });

        return dialog;
    }

    public boolean isValidPassword(final String password){
        //https://androidfreetutorial.wordpress.com/2018/01/04/regular-expression-for-password-field-in-android/
        Pattern pattern;
        Matcher matcher;

        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}$";

        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();
    }
}