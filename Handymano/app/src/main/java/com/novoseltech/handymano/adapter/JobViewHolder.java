package com.novoseltech.handymano.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.novoseltech.handymano.R;

public class JobViewHolder extends RecyclerView.ViewHolder {
    private TextView jobTitle;

    public JobViewHolder(@NonNull View itemView) {
        super(itemView);
        jobTitle = itemView.findViewById(R.id.list_jobTitle);
    }
}
