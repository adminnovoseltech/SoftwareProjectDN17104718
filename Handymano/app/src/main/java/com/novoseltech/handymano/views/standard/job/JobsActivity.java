package com.novoseltech.handymano.views.standard.job;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.novoseltech.handymano.MainActivity;
import com.novoseltech.handymano.R;
import com.novoseltech.handymano.model.JobsModel;
import com.novoseltech.handymano.views.message.MessageMenu;
import com.novoseltech.handymano.views.standard.HomeActivityStandard;
import com.novoseltech.handymano.views.standard.StandardProfileActivity;

/**
 @author Denis Novosel
 @student_id 17104718
 @email x17104718@student.ncirl.ie
 @github https://github.com/adminnovoseltech/SoftwareProjectDN17104718
 @class JobsActivity.java
 **/

public class JobsActivity extends AppCompatActivity {

    //Layout components
    private DrawerLayout drawerLayout;
    private Button btn_newJob;
    private RecyclerView fStoreList;
    private CircularImageView profileImage;
    private TextView tv_UserName;

    //Firebase components
    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();;
    private FirebaseUser user = mAuth.getCurrentUser();

    //Variables
    private FirestoreRecyclerAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jobs);
        drawerLayout = findViewById(R.id.drawer_layout_standard);


        profileImage = drawerLayout.findViewById(R.id.civ_profilePictureStandard);
        if(user.getPhotoUrl() != null){
            Glide.with(getApplicationContext())
                    .load(user.getPhotoUrl())
                    .into(profileImage);
        }

        tv_UserName = drawerLayout.findViewById(R.id.text_UserName_Standard);
        fStore.collection("user")
                .document(user.getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    tv_UserName.setText(documentSnapshot.getString("username"));
                }
            }
        });

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

                        Intent intent  = new Intent(getApplicationContext(), StandardJobViewActivity.class);
                        intent.putExtra("JOB_ID", holder.jobTitle.getText());
                        startActivity(intent);

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

                Intent intent = new Intent(JobsActivity.this, CreateJob.class);
                startActivity(intent);
            }
        });
    }

    private class JobsViewHolder extends RecyclerView.ViewHolder{

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

    public void ClickJobs(View view){
        //recreate the activity
        finish();
        startActivity(getIntent());
    }

    public void ClickLogOut(View view) {
        logout();
    }

    public void ClickHome(View view) {
        Intent intent = new Intent(JobsActivity.this, HomeActivityStandard.class);
        finish();
        startActivity(intent);
    }

    public void ClickProfile(View view) {
        Intent intent = new Intent(JobsActivity.this, StandardProfileActivity.class);
        finish();
        startActivity(intent);
    }

    public void ClickMessages(View view) {
        Intent intent = new Intent(JobsActivity.this, MessageMenu.class);
        intent.putExtra("USER_TYPE", "Standard");
        finish();
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {

    }
}