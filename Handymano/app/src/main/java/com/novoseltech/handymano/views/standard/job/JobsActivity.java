package com.novoseltech.handymano.views.standard.job;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.novoseltech.handymano.MainActivity;
import com.novoseltech.handymano.R;
import com.novoseltech.handymano.fragments.AddJob;
import com.novoseltech.handymano.fragments.AddProject;
import com.novoseltech.handymano.model.JobsModel;
import com.novoseltech.handymano.model.ProjectsModel;
import com.novoseltech.handymano.views.professional.ProfessionalProject;
import com.novoseltech.handymano.views.professional.ProjectsActivity;
import com.novoseltech.handymano.views.standard.HomeActivityStandard;

public class JobsActivity extends AppCompatActivity {

    //Navigation drawer
    DrawerLayout drawerLayout;


    //UI objects
    Button btn_newJob;

    private RecyclerView fStoreList;
    private FirestoreRecyclerAdapter adapter;

    //Firebase objects
    FirebaseFirestore fStore;
    FirebaseAuth mAuth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jobs);
        drawerLayout = findViewById(R.id.drawer_layout_standard);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        fStoreList = findViewById(R.id.firestoreListJobs);

        //Query
        Query query = fStore.collection("user")
                .document(user.getUid())
                .collection("jobs")
                .orderBy("creation_date", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<JobsModel> options = new FirestoreRecyclerOptions.Builder<JobsModel>()
                .setQuery(query, JobsModel.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<JobsModel, JobsActivity.JobsViewHolder>(options) {

            @NonNull
            @Override
            public JobsActivity.JobsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.jobs_list_item_single, parent, false);
                return new JobsActivity.JobsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull JobsActivity.JobsViewHolder holder, int position, @NonNull JobsModel model) {
                holder.jobTitle.setText(model.getTitle());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        /*

                        Intent intent  = new Intent(getApplicationContext(), ProfessionalProject.class);
                        intent.putExtra("PROJECT_ID", holder.jobTitle.getText());
                        startActivity(intent);


                         */
                    }
                });
            }
        };

        fStoreList.setHasFixedSize(true);
        fStoreList.setLayoutManager(new LinearLayoutManager(this));
        fStoreList.setAdapter(adapter);

        //UI objects
        btn_newJob = findViewById(R.id.btn_newJob);

        //Listeners
        btn_newJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddJob addJob_fragment = new AddJob();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.frame_job_add, addJob_fragment);
                transaction.addToBackStack(null);
                transaction.commit();

                btn_newJob.setVisibility(View.GONE);
                fStoreList.setVisibility(View.GONE);
            }
        });
    }

    private class JobsViewHolder  extends RecyclerView.ViewHolder{

        private TextView jobTitle;

        public JobsViewHolder(@NonNull View itemView) {
            super(itemView);

            jobTitle = itemView.findViewById(R.id.list_jobTitle);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }


    public void logout(){
        //Close app
        //Initialize alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //Set title
        builder.setTitle("Log out");
        //Set message
        builder.setMessage("Are you sure you want to log out ?");
        //Yes button
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                FirebaseAuth.getInstance().signOut();
                finish();
                Intent intent = new Intent(JobsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        //No button
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Dismiss dialog
                dialogInterface.dismiss();
            }
        });
        //Show dialog
        builder.show();
    }

    public void ClickMenu(View view) {
        openDrawer(drawerLayout);
    }

    public static void openDrawer(DrawerLayout drawerLayout) {
        //Open drawer layout
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public static void closeDrawer(DrawerLayout drawerLayout) {
        //Close drawer layout
        //Check condition
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            //When drawer is open
            //Close drawer
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    public void ClickLogOut(View view) {
        logout();
    }

    public void ClickHome(View view) {
    }

    public void ClickProfile(View view) {
    }

    public void ClickMessages(View view) {
    }
}