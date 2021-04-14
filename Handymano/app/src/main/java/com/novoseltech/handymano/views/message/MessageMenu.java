package com.novoseltech.handymano.views.message;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.novoseltech.handymano.R;
import com.novoseltech.handymano.adapter.JobsAdapter;
import com.novoseltech.handymano.adapter.MessagesAdapter;
import com.novoseltech.handymano.model.MessagesModel;

import java.util.ArrayList;
import java.util.List;

public class MessageMenu extends AppCompatActivity {

    FirebaseFirestore fStore;
    FirebaseAuth mAuth;
    FirebaseUser user;

    RecyclerView rv_chatList;
    RecyclerView.Adapter adapter;

    List<String> messageReceipients = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_menu);


        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        rv_chatList = findViewById(R.id.rv_chatList);

        fStore.collection("chat").document(user.getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    messageReceipients = (List<String>) documentSnapshot.get("recipients");
                }
            }
        });

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < messageReceipients.size(); i++){
                    Log.d("MessageReceipient " + i, messageReceipients.get(i));
                }


                adapter = new MessagesAdapter(messageReceipients, getApplicationContext());

                rv_chatList.setHasFixedSize(true);
                rv_chatList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                rv_chatList.setAdapter(adapter);


            }
        }, 1000);


    }




}