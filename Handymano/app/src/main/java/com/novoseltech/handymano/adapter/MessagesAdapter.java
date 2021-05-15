package com.novoseltech.handymano.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.novoseltech.handymano.R;
import com.novoseltech.handymano.views.message.ChatActivity;
import com.novoseltech.handymano.views.professional.job.ViewJob;

import org.ocpsoft.prettytime.PrettyTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.SimpleTimeZone;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {
    //Initial functionality was developed from this:
    //https://medium.com/@akhilkc9/simple-android-chat-application-using-firestorerecycleradapter-7f632da2eaee

    //This only entailed a chat room where anyone could join. I have rebuilt the complete system
    //and the only common functionality is displaying the message data in the chat bubbles

    private List<String> messagesArrayList;
    private List<String> lastMessagesSent;
    private Context context;
    PrettyTime p = new PrettyTime();

    public MessagesAdapter(List<String> messagesArrayList, List<String> lastMessageSent, Context context) {
        this.messagesArrayList = messagesArrayList;
        this.lastMessagesSent = lastMessageSent;
        this.context = context;
    }

    @NonNull
    @Override
    public MessagesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_list_item, parent, false);
        return new MessagesAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessagesAdapter.ViewHolder holder, int position) {
        //MessageMenu activity iterates through Firestore chat documents to check user's recipient list
        //User's recipient list is structured as "recipientUID,recipientDisplayName" and that data is then stored in the ArrayList
        //Which is passed to this adapter and the data is then extracted to show recipients display name in the RecyclerView
        //And recipients UID and name are then displayed to chat activity to load message data
        String toSplit = messagesArrayList.get(position);
        String[] messageData = toSplit.split(",");
        String userID = messageData[0];
        String username = messageData[1];

        holder.messageSender.setText(username);

        //Similar as to above, activity iterates through the messaging between sender and recipients and retrieves last message sent
        //Which is then shown inside of the RecyclerView
        String messageToSplit = lastMessagesSent.get(position);
        String[] lastMessageData = messageToSplit.split(",");
        String lmTimestamp = lastMessageData[0];
        String lmMessage = lastMessageData[1];

        holder.lastMessageTimestamp.setText(lmTimestamp);
        holder.lastMessage.setText(lmMessage);

        StorageReference storageReference = FirebaseStorage.getInstance()
                .getReference().child("images")
                .child(userID)
                .child("profile_image_" + userID + ".jpeg");

        storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {

                if(task.isSuccessful()){

                    Glide.with(context)
                            .load(task.getResult().toString())
                            .into(holder.messageSenderImage);
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Glide.with(context)
                        .load(R.drawable.ic_profile_512)
                        .into(holder.messageSenderImage);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("USER_ID", userID);
                intent.putExtra("USERNAME", username);
                intent.putExtra("MODE", "MENU");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return messagesArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView messageSender;
        public TextView lastMessage;
        public TextView lastMessageTimestamp;
        public CircularImageView messageSenderImage;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageSender = itemView.findViewById(R.id.tv_chatSenderUsername);
            lastMessage = itemView.findViewById(R.id.tv_lastMessageSent);
            lastMessageTimestamp = itemView.findViewById(R.id.tv_lastMessageTimestamp);
            messageSenderImage = itemView.findViewById(R.id.iv_chatSenderImageList);
        }
    }
}
