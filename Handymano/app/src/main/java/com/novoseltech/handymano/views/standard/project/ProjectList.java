package com.novoseltech.handymano.views.standard.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import com.novoseltech.handymano.model.ProjectsModel;
import com.novoseltech.handymano.views.message.MessageMenu;
import com.novoseltech.handymano.views.standard.HomeActivityStandard;
import com.novoseltech.handymano.views.standard.StandardProfileActivity;
import com.novoseltech.handymano.views.standard.job.JobsActivity;


public class ProjectList extends AppCompatActivity {

    //Navigation drawer
    DrawerLayout drawerLayout;

    private RecyclerView rv_tradeProjectList;
    private FirestoreRecyclerAdapter adapter;

    FirebaseFirestore fStore;
    FirebaseAuth mAuth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_list);

        drawerLayout = findViewById(R.id.drawer_layout_standard);

        String user_id = getIntent().getStringExtra("USER_ID");

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        CircularImageView profileImage = drawerLayout.findViewById(R.id.civ_profilePictureStandard);
        if(user.getPhotoUrl() != null){
            Glide.with(getApplicationContext())
                    .load(user.getPhotoUrl())
                    .into(profileImage);
        }

        TextView tv_UserName = drawerLayout.findViewById(R.id.text_UserName_Standard);
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

        rv_tradeProjectList = findViewById(R.id.rv_tradeProjectList);

        //Query
        Query query = fStore.collection("user")
                .document(user_id)
                .collection("projects")
                .orderBy("creation_date", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ProjectsModel> options = new FirestoreRecyclerOptions.Builder<ProjectsModel>()
                .setQuery(query, ProjectsModel.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<ProjectsModel, ProjectList.ProjectsViewHolder>(options) {

            @NonNull
            @Override
            public ProjectList.ProjectsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_list_item_single, parent, false);
                return new ProjectList.ProjectsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ProjectList.ProjectsViewHolder holder, int position, @NonNull ProjectsModel model) {
                holder.projectTitle.setText(model.getTitle());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent intent  = new Intent(getApplicationContext(), ViewProject.class);
                        intent.putExtra("USER_ID", user_id);
                        intent.putExtra("PROJECT_ID", holder.projectTitle.getText());
                        //intent.putExtra("PROJECT_ID", holder.itemView.getId())
                        startActivity(intent);
                    }
                });
            }
        };

        rv_tradeProjectList.setHasFixedSize(true);
        rv_tradeProjectList.setLayoutManager(new LinearLayoutManager(this));
        rv_tradeProjectList.setAdapter(adapter);
    }

    private class ProjectsViewHolder  extends RecyclerView.ViewHolder{

        private TextView projectTitle;

        public ProjectsViewHolder(@NonNull View itemView) {
            super(itemView);

            projectTitle = itemView.findViewById(R.id.list_projectTitle);
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
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
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
                Intent intent = new Intent(ProjectList.this, MainActivity.class);
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

    public void ClickJobs(View view){
        //recreate the activity

        Intent intent = new Intent(ProjectList.this, JobsActivity.class);
        startActivity(intent);
    }

    public void ClickLogOut(View view) {
        logout();
    }

    public void ClickHome(View view) {
        Intent intent = new Intent(ProjectList.this, HomeActivityStandard.class);
        startActivity(intent);
    }

    public void ClickProfile(View view) {
        Intent intent = new Intent(ProjectList.this, StandardProfileActivity.class);
        startActivity(intent);
    }

    public void ClickMessages(View view) {
        Intent intent = new Intent(ProjectList.this, MessageMenu.class);
        intent.putExtra("USER_TYPE", "Standard");
        startActivity(intent);
    }


}