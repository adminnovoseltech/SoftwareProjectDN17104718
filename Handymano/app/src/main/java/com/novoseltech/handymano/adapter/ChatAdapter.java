package com.novoseltech.handymano.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.novoseltech.handymano.R;
import com.novoseltech.handymano.model.ChatModel;

import org.ocpsoft.prettytime.PrettyTime;

public class ChatAdapter extends FirestoreRecyclerAdapter<ChatModel, ChatAdapter.ViewHolder> {
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    PrettyTime prettyTime = new PrettyTime();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    String CURRENT_USERNAME;
    String MESSAGE_SENDER = "";

    public ChatAdapter(@NonNull FirestoreRecyclerOptions<ChatModel> options, String SENDER_NAME) {
        super(options);
        CURRENT_USERNAME = SENDER_NAME;
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull ChatModel model) {
        MESSAGE_SENDER = model.getSender();
        holder.message.setText(model.getMessage());
        holder.sender.setText(model.getSender());
        holder.timestamp.setText(prettyTime.format(model.getTimestamp()));

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if(viewType == MSG_TYPE_RIGHT){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_right, parent, false);
        }else{
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_left, parent, false);

        }

        return new ViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        if(getItem(position).getUser_id().equals(user.getUid())){
            return MSG_TYPE_RIGHT;
        }else{
            return MSG_TYPE_LEFT;
        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView sender, message, timestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            sender = itemView.findViewById(R.id.sendername);
            message = itemView.findViewById(R.id.textmessage);
            timestamp = itemView.findViewById(R.id.timestamp);
        }
    }
}
