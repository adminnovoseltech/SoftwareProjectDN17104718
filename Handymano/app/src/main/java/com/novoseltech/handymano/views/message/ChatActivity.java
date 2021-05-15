package com.novoseltech.handymano.views.message;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Notification;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.novoseltech.handymano.Functions;
import com.novoseltech.handymano.R;
import com.novoseltech.handymano.adapter.ChatAdapter;
import com.novoseltech.handymano.model.ChatModel;
import com.novoseltech.handymano.views.professional.job.ViewJob;
import com.novoseltech.handymano.views.professional.project.ProjectsActivity;
import com.novoseltech.handymano.views.professional.project.ViewProjectActivity;
import com.novoseltech.handymano.views.standard.ViewProfessionalActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener{

    //Layout components
    private RecyclerView rv_chatContent;
    private EditText et_chatMessage;
    private CircularImageView civ_profileImageChat;
    private TextView tv_chatSenderName;
    private ImageView iv_chatMoreButton;

    //Firebase components
    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String UID = user.getUid();

    //Variables
    private LinearLayoutManager linearLayoutManager;
    private ChatAdapter chatAdapter;
    private String SENDER_NAME = "";
    private String RECIPIENT_ID = "";
    private String RECIPIENT_NAME = "";
    private String MODE = "";
    private String USER_TYPE = "";
    private String JOB_ID = "";
    private String JOB_DATE = "";
    private List<String> messageRecipientsSender = new ArrayList<>();
    private List<String> messageRecipientsReceiver = new ArrayList<>();
    private Functions appFunctions = new Functions();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Layout
        et_chatMessage = findViewById(R.id.et_chatMessage);
        civ_profileImageChat = findViewById(R.id.civ_chatActivityProfileImage);
        tv_chatSenderName = findViewById(R.id.tv_chatSenderName);
        iv_chatMoreButton = findViewById(R.id.iv_chatMoreButton);

        //Get the mode of how the chat with the recipient was accessed
        MODE = getIntent().getStringExtra("MODE");
        if(MODE.equals("PROFILE_VISIT")){
            //This mode is retrieved when Standard user clicks on the chat icon on Professional's profile
            RECIPIENT_ID = getIntent().getStringExtra("TRADE_ID");
            RECIPIENT_NAME = getIntent().getStringExtra("TRADE_NAME");

        }else if(MODE.equals("JOB_VISIT")){
            //This mode is retrieved when Professional user clicks on "Message advertiser" button on the job ad
            RECIPIENT_ID = getIntent().getStringExtra("ADVERTISER_ID");
            RECIPIENT_NAME = getIntent().getStringExtra("ADVERTISER_NAME");
            JOB_ID = getIntent().getStringExtra("JOB_ID");

            //JOB_ID = getIntent().getStringExtra("JOB_ID");
            JOB_DATE = getIntent().getStringExtra("JOB_DATE");
            et_chatMessage.setText("Hello, I would like to offer my services on job ad " + "'" + JOB_ID +
                    "'" +" that you posted on " + JOB_DATE + ".");
        }else{
            //This mode is retrieved when the chat is accessed through the RecyclerView click on MessageMenu activity
            RECIPIENT_ID = getIntent().getStringExtra("USER_ID");
            RECIPIENT_NAME = getIntent().getStringExtra("USERNAME");
        }

        //Getting and loading the recipient image in the chat
        StorageReference storageReference = FirebaseStorage.getInstance()
                .getReference().child("images")
                .child(RECIPIENT_ID)
                .child("profile_image_" + RECIPIENT_ID + ".jpeg");

        storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {

                if(task.isSuccessful()){
                    Glide.with(getApplicationContext())
                            .load(task.getResult().toString())
                            .into(civ_profileImageChat);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Glide.with(getApplicationContext())
                        .load(R.drawable.ic_profile_512)
                        .into(civ_profileImageChat);
            }
        });

        tv_chatSenderName.setText(RECIPIENT_NAME);

        CollectionReference chatReference = fStore.collection("chat").document(UID).collection(RECIPIENT_ID);
        CollectionReference receiverReference = fStore.collection("chat").document(RECIPIENT_ID).collection(UID);

        //CHECK IF RECIPIENTS CHAT DOCUMENT CONTAINS LIST OF CHATS
        fStore.collection("chat").document(RECIPIENT_ID)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){

                    if(task.getResult().contains("recipients")){
                        DocumentSnapshot documentSnapshot = task.getResult();
                        messageRecipientsReceiver = (List<String>) documentSnapshot.get("recipients");

                    }else{
                        messageRecipientsReceiver.add("");
                    }

                }
            }
        });

        //CHECK IF MY CHAT DOCUMENT CONTAINS LIST OF CHATS
        fStore.collection("chat").document(UID)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){

                    if(task.getResult().contains("recipients")){
                        DocumentSnapshot documentSnapshot = task.getResult();
                        messageRecipientsSender = (List<String>) documentSnapshot.get("recipients");

                    }else{
                        messageRecipientsSender.add("");
                    }

                }
            }
        });

        //GET MY USERNAME
        fStore.collection("user").document(UID)
        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    SENDER_NAME = documentSnapshot.getString("username");
                    USER_TYPE = documentSnapshot.getString("accountType");

                }
            }
        });

        findViewById(R.id.ic_sendMessage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!appFunctions.containsOffensiveWord(et_chatMessage.getText().toString())){
                    ChatModel chat = new ChatModel(user.getUid(),SENDER_NAME, et_chatMessage.getText().toString(), new Date());
                    receiverReference.add(chat);
                    chatReference.add(chat);
                    et_chatMessage.setText("");

                    if(!messageRecipientsReceiver.contains(UID + "," + SENDER_NAME)){

                        messageRecipientsReceiver.add(UID + "," + SENDER_NAME);
                        fStore.collection("chat").document(RECIPIENT_ID)
                                .update("recipients", messageRecipientsReceiver)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                    }
                                });

                    }

                    if(!messageRecipientsSender.contains(RECIPIENT_ID + "," + RECIPIENT_NAME)){
                        messageRecipientsSender.add(RECIPIENT_ID + "," + RECIPIENT_NAME);

                        fStore.collection("chat").document(UID)
                                .update("recipients", messageRecipientsSender)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                    }
                                });
                    }
                }else{
                    et_chatMessage.setError("Message contains offensive word(s). Please remove the offensive word(s) and send the message again.");
                    et_chatMessage.requestFocus();
                }




            }
        });
        

        rv_chatContent = findViewById(R.id.rv_chatContent);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        rv_chatContent.setLayoutManager(linearLayoutManager);

        Query query = FirebaseFirestore.getInstance()
                .collection("chat").document(UID).collection(RECIPIENT_ID).orderBy("timestamp", Query.Direction.ASCENDING);



        FirestoreRecyclerOptions<ChatModel> options = new FirestoreRecyclerOptions.Builder<ChatModel>().setQuery(query, ChatModel.class).build();
        chatAdapter = new ChatAdapter(options, SENDER_NAME);
        chatAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                rv_chatContent.scrollToPosition(chatAdapter.getItemCount() - 1);


            }
        });




        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(chatAdapter.getItemCount() == 0){
                    iv_chatMoreButton.setVisibility(View.GONE);
                }else{
                    iv_chatMoreButton.setVisibility(View.VISIBLE);
                }
            }
        }, 1000);

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

    public void ClickMenuMessages(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.setOnMenuItemClickListener(this);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.message_actions, popupMenu.getMenu());
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.option_delete:
                AlertDialog.Builder deleteJobDialog = new AlertDialog.Builder(ChatActivity.this);
                deleteJobDialog.setTitle("Delete conversation")
                        .setMessage("You are about to delete the conversation. Continue?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                fStore.collection("chat")
                                        .document(user.getUid())
                                        .collection(RECIPIENT_ID)
                                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if(task.isSuccessful()){
                                            for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                                                fStore.collection("chat")
                                                        .document(user.getUid())
                                                        .collection(RECIPIENT_ID)
                                                        .document(queryDocumentSnapshot.getId())
                                                        .delete()
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Log.d("TAG", "Document deleted");
                                                            }
                                                        });
                                            }

                                        }
                                    }
                                });

                                int indexToRemove = messageRecipientsSender.indexOf(RECIPIENT_ID + "," + RECIPIENT_NAME);

                                messageRecipientsSender.remove(indexToRemove);


                                fStore.collection("chat").document(UID)
                                        .update("recipients", messageRecipientsSender)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Log.d("LOG", "Recipient list updated");
                                            }
                                        });

                                Intent intent;
                                if(MODE.equals("PROFILE_VISIT")){
                                    intent = new Intent(ChatActivity.this, ViewProfessionalActivity.class);
                                    intent.putExtra("USER_ID", RECIPIENT_ID);

                                }else if(MODE.equals("JOB_VISIT")){
                                    intent = new Intent(ChatActivity.this, ViewJob.class);
                                    intent.putExtra("USER_ID", RECIPIENT_ID);
                                    intent.putExtra("JOB_ID", JOB_ID);
                                }else{
                                    intent = new Intent(ChatActivity.this, MessageMenu.class);
                                    intent.putExtra("USER_TYPE", USER_TYPE);
                                }


                                //Intent intent = new Intent(ChatActivity.this, MessageMenu.class);
                                //intent.putExtra("USER_TYPE", USER_TYPE);
                                finish();
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();

                return true;
            default:
                return false;
        }

    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}