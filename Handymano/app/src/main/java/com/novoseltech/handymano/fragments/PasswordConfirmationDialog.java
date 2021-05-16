package com.novoseltech.handymano.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.novoseltech.handymano.R;

/**
 @author Denis Novosel
 @student_id 17104718
 @email x17104718@student.ncirl.ie
 @github https://github.com/adminnovoseltech/SoftwareProjectDN17104718
 @class PasswordConfirmationDialog.java
 **/

public class PasswordConfirmationDialog extends DialogFragment {
    //This fragment and all of it's usages (Saving Professional and Standard profile) can be
    //updated to use simpler version. See PasswordConfirmationDialog

    //Layout components
    private EditText et_password1;
    private EditText et_password2;
    private PasswordConfirmationDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.password_confirmation_layout, null);
        builder.setView(view)
                .setTitle("Password confirmation")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setPositiveButton("OK", null);

        final AlertDialog dialog = builder.create();
        dialog.show();

        //Idea from https://stackoverflow.com/questions/49357150/how-to-update-email-from-firebase-in-android
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String password1 = et_password1.getText().toString();
                String password2 = et_password2.getText().toString();


                if(password1.equals(password2)){

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password1);
                    user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                listener.applyPass(password1);
                                listener.passwordMatch();
                                dialog.dismiss();
                            }else{
                                et_password1.setError("Incorrect password!");
                                et_password2.setError("Incorrect password!");
                            }
                        }
                    });

                }else{
                    et_password1.setError("Passwords do not match!");
                    et_password2.setError("Passwords do not match!");

                }
            }
        });

        et_password1 = view.findViewById(R.id.editTextTextPassword);
        et_password2 = view.findViewById(R.id.editTextTextPassword3);

        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (PasswordConfirmationDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement ExampleDialogListener");
        }
    }

    public interface PasswordConfirmationDialogListener {
        Boolean passwordMatch();
        void applyPass(String pass);
    }

}


