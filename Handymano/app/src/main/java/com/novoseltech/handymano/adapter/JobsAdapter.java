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
import com.novoseltech.handymano.views.professional.job.ViewJob;

import java.util.List;

/**
 @author Denis Novosel
 @student_id 17104718
 @email x17104718@student.ncirl.ie
 @github https://github.com/adminnovoseltech/SoftwareProjectDN17104718
 @class JobsAdapter.java
 **/

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
        //Displaying ArrayList data inside of the RecyclerView
        //JobsList activity iterates through user documents and checks if they are Standard user
        //If they are then it checks if they have any documents created in "jobs" collection
        //If they do then the activity iterates through those documents to check for the category and location
        //If the criteria is matched then the job poster UID and the job name are added to the ArrayList separated by the "," character
        //This array list is then split here to show the job names on the RecyclerView and user's ID as well as the job name are passed
        //With the intent when job is clicked on the load the job data
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
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
