package com.novoseltech.handymano.views.professional.job;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.novoseltech.handymano.R;
import com.novoseltech.handymano.adapter.JobViewHolder;
import com.novoseltech.handymano.adapter.JobsAdapter;
import com.novoseltech.handymano.model.JobsModel;
import com.novoseltech.handymano.model.ProjectsModel;
import com.novoseltech.handymano.views.standard.job.JobsActivity;
import com.novoseltech.handymano.views.standard.job.StandardJobViewActivity;
import com.novoseltech.handymano.views.standard.project.ProjectList;
import com.novoseltech.handymano.views.standard.project.ViewProject;

import java.util.ArrayList;
import java.util.List;

public class JobsList extends AppCompatActivity {

    private static final String TAG = "";


    FirebaseFirestore fStore;
    FirebaseAuth mAuth;
    FirebaseUser user;

    List<String> usersAL = new ArrayList<>();
    List<String> jobsAL = new ArrayList<>();

    RecyclerView rv_regularJobList;
    RecyclerView.Adapter adapter;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jobs_list);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        rv_regularJobList = findViewById(R.id.rv_regularJobList);

        //Query


        fStore.collection("user").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if(document.getString("accountType").equals("Standard")){
                                    usersAL.add(document.getId());
                                    String docId = document.getId();
                                    fStore.collection("user")
                                            .document(docId)
                                            .collection("jobs")
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    for(QueryDocumentSnapshot documentSnapshot : task.getResult()){
                                                        jobsAL.add(docId + ',' + documentSnapshot.getId());
                                                    }
                                                }
                                            });
                                }
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }


                });





        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < jobsAL.size(); i++){
                    Log.d("ArrayList jobs " + i, jobsAL.get(i));
                }


                adapter = new JobsAdapter(jobsAL, getApplicationContext());

                rv_regularJobList.setHasFixedSize(true);
                rv_regularJobList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                rv_regularJobList.setAdapter(adapter);


            }
        }, 1000);
    }


    @Override
    protected void onStop() {
        super.onStop();
        //adapter.stopListening();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //adapter.
    }
}