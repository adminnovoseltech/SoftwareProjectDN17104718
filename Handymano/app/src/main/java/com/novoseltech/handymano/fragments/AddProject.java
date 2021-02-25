package com.novoseltech.handymano.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.novoseltech.handymano.R;
import com.novoseltech.handymano.views.professional.ProjectsActivity;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddProject#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddProject extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    Button btn_saveProject;

    Button btn_createProject;

    EditText et_projectTitle;
    EditText et_projectDescription;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    String UID = user.getUid();

    public AddProject() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddProject.
     */
    // TODO: Rename and change types and number of parameters
    public static AddProject newInstance(String param1, String param2) {
        AddProject fragment = new AddProject();
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
        View view = inflater.inflate(R.layout.fragment_add_project, container, false);

        btn_saveProject = view.findViewById(R.id.btn_saveProject);
        et_projectTitle = view.findViewById(R.id.et_projectTitle);
        et_projectDescription = view.findViewById(R.id.et_projectDescription);
        btn_createProject = getActivity().findViewById(R.id.btn_createProject);


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btn_saveProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Objects
                Map<String, Object> project = new HashMap<>();
                String pTitle = et_projectTitle.getText().toString();
                String pDesc = et_projectDescription.getText().toString();


                //Content validation
                if(pTitle.length() >= 10 && pTitle.length() <= 50 && pDesc.length() >= 10 && pDesc.length() <= 400){
                    project.put("title", pTitle);
                    project.put("description", pDesc);
                    fStore.collection("user").document(UID)
                            .collection("projects")
                            .document(pTitle)
                            .set(project)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    //Toast.makeText(getContext(), "Project created", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                    btn_createProject.setVisibility(View.VISIBLE);
                    getActivity().getSupportFragmentManager().beginTransaction().remove(AddProject.this).commit();

                }else{
                    if(pTitle.length() < 10 || pTitle.length() > 50){
                        et_projectTitle.setError("Title length must be between 10 and 50 characters!");
                        et_projectTitle.requestFocus();
                    }else{
                        if(pDesc.length() < 10){
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
}