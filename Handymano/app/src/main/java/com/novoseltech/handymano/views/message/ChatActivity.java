package com.novoseltech.handymano.views.message;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.novoseltech.handymano.R;
import com.novoseltech.handymano.adapter.ChatAdapter;
import com.novoseltech.handymano.model.ChatModel;

import java.util.Date;

public class ChatActivity extends AppCompatActivity {

    RecyclerView rv_chatContent;
    LinearLayoutManager linearLayoutManager;

    EditText et_chatMessage;
    ChatAdapter chatAdapter;

    String SENDER_NAME = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);



        String RECEIPIENT_ID = getIntent().getStringExtra("USER_ID");
        //String RECEIPIENT_NAME =

        FirebaseFirestore fStore = FirebaseFirestore.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String UID = user.getUid();

        String messageReceiver;
        String messageReceiverUID;

        CollectionReference chatReference = fStore.collection("chat").document(UID).collection(RECEIPIENT_ID);
        CollectionReference receiverReference = fStore.collection("chat").document(RECEIPIENT_ID).collection(UID);


        fStore.collection("user").document(UID)
        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    SENDER_NAME = documentSnapshot.getString("username");
                }
            }
        });

        et_chatMessage = findViewById(R.id.et_chatMessage);

        findViewById(R.id.ic_sendMessage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatModel chat = new ChatModel(user.getUid(),SENDER_NAME, et_chatMessage.getText().toString(), new Date());

                receiverReference.add(chat);


                chatReference.add(chat);

                et_chatMessage.setText("");

                Log.d("receiverMessage is ", String.valueOf(receiverReference));
                //Log.d("chat is ", chat.);

            }
        });

        rv_chatContent = findViewById(R.id.rv_chatContent);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        rv_chatContent.setLayoutManager(linearLayoutManager);

        Query query = FirebaseFirestore.getInstance()
                .collection("chat").document(UID).collection(RECEIPIENT_ID).orderBy("timestamp", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<ChatModel> options = new FirestoreRecyclerOptions.Builder<ChatModel>().setQuery(query, ChatModel.class).build();
        chatAdapter = new ChatAdapter(options, SENDER_NAME);
        chatAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                rv_chatContent.scrollToPosition(chatAdapter.getItemCount() - 1);
            }
        });
        rv_chatContent.setAdapter(chatAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        chatAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        chatAdapter.stopListening();
    }
}