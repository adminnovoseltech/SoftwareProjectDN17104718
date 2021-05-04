package com.novoseltech.handymano.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.novoseltech.handymano.R;
import com.novoseltech.handymano.views.professional.job.ViewJob;
import com.novoseltech.handymano.views.professional.project.ProfessionalProjectViewActivity;

import java.util.ArrayList;
import java.util.List;

public class JobsAdapter extends RecyclerView.Adapter<JobsAdapter.ViewHolder> {

    private List<String> jobsArrayList;
    private Context context;

    public JobsAdapter(List<String> jobsArrayList, Context context) {
        this.jobsArrayList = jobsArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public JobsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.jobs_list_item_single, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobsAdapter.ViewHolder holder, int position) {

        String toSplit = jobsArrayList.get(position);
        String[] jobData = toSplit.split(",");
        String user = jobData[0];
        String title = jobData[1];

        holder.jobTitle.setText(title);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, ViewJob.class);
                intent.putExtra("USER_ID", user);
                intent.putExtra("JOB_ID", title);
                context.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return jobsArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView jobTitle;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            jobTitle = itemView.findViewById(R.id.list_jobTitle);
        }
    }
}
