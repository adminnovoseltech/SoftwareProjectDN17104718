package com.novoseltech.handymano.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.novoseltech.handymano.R;
import com.novoseltech.handymano.views.message.ChatActivity;
import com.novoseltech.handymano.views.professional.job.ViewJob;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {

    private List<String> messagesArrayList;
    private Context context;

    public MessagesAdapter(List<String> messagesArrayList, Context context) {
        this.messagesArrayList = messagesArrayList;
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

        String toSplit = messagesArrayList.get(position);

        String[] messageData = toSplit.split(",");

        String userID = messageData[0];
        String username = messageData[1];

        holder.messageSender.setText(username);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("USER_ID", userID);
                intent.putExtra("USERNAME", username);
                intent.putExtra("MODE", "MENU");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

                //Toast.makeText(context, holder.jobTitle.getText(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return messagesArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView messageSender;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageSender = itemView.findViewById(R.id.tv_chatSenderUsername);
        }
    }
}
