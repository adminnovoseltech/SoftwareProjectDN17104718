package com.novoseltech.handymano.views.professional.project;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.novoseltech.handymano.model.ProjectsModel;
import com.novoseltech.handymano.views.message.MessageMenu;
import com.novoseltech.handymano.views.professional.HomeActivityProfessional;
import com.novoseltech.handymano.views.professional.ProfessionalProfileActivity;
import com.novoseltech.handymano.views.professional.feedback.FeedbackList;
import com.novoseltech.handymano.views.professional.job.JobsList;

public class ProjectsActivity extends AppCompatActivity {

    //Navigation drawer
    DrawerLayout drawerLayout;

    private RecyclerView fStoreList;
    private FirestoreRecyclerAdapter adapter;

    FirebaseFirestore fStore;
    FirebaseAuth mAuth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);
        drawerLayout = findViewById(R.id.drawer_layout_professional);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        CircularImageView profileImage = drawerLayout.findViewById(R.id.civ_profilePictureProfessional);
        TextView tv_drawerUsername = drawerLayout.findViewById(R.id.text_UserName_Professional);

        FirebaseUser user = mAuth.getCurrentUser();

        if(mAuth.getCurrentUser().getPhotoUrl() != null){
            Glide.with(getApplicationContext())
                    .load(user.getPhotoUrl())
                    .into(profileImage);
        }else{
            Log.d("TAG", "Profile image not found. Loading default image.");
        }

        fStore.collection("user")
                .document(user.getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    tv_drawerUsername.setText(documentSnapshot.getString("username"));
                }
            }
        });

        fStoreList = findViewById(R.id.firestoreListProjects);

        //Query
        Query query = fStore.collection("user")
                .document(user.getUid())
                .collection("projects")
                .orderBy("creation_date", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ProjectsModel> options = new FirestoreRecyclerOptions.Builder<ProjectsModel>()
                .setQuery(query, ProjectsModel.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<ProjectsModel, ProjectsViewHolder>(options) {

            @NonNull
            @Override
            public ProjectsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_list_item_single, parent, false);
                return new ProjectsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ProjectsViewHolder holder, int position, @NonNull ProjectsModel model) {
                holder.projectTitle.setText(model.getTitle());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent intent  = new Intent(getApplicationContext(), ViewProjectActivity.class);
                        intent.putExtra("PROJECT_ID", holder.projectTitle.getText());
                        startActivity(intent);
                    }
                });
            }
        };

        fStoreList.setHasFixedSize(true);
        fStoreList.setLayoutManager(new LinearLayoutManager(this));
        fStoreList.setAdapter(adapter);

        Button btn_createProject = findViewById(R.id.btn_createProject);
        btn_createProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(ProjectsActivity.this, CreateProject.class);
                startActivity(intent);
            }
        });

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

    public void ClickMenu(View view) {
        openDrawer(drawerLayout);
    }

    public void ClickProfile(View view) {
        finish();
        Intent intent = new Intent(ProjectsActivity.this, ProfessionalProfileActivity.class);
        startActivity(intent);
    }

    public void ClickProjects(View view){
        //recreate the activity

        finish();
        startActivity(getIntent());
    }

    public void ClickMessages(View view){
        finish();
        Intent intent = new Intent(ProjectsActivity.this, MessageMenu.class);
        intent.putExtra("USER_TYPE", "Professional");
        startActivity(intent);
    }

    public void ClickJobs(View view){
        finish();
        Intent intent = new Intent(ProjectsActivity.this, JobsList.class);
        startActivity(intent);
    }


    public static void openDrawer(DrawerLayout drawerLayout) {
        //Open drawer layout
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public void ClickLogOut(View view) {
        logout();
    }

    public void ClickHome(View view){
        finish();
        Intent intent = new Intent(ProjectsActivity.this, HomeActivityProfessional.class);

        startActivity(intent);
    }

    public void ClickFeedback(View view){
        Intent intent = new Intent(ProjectsActivity.this, FeedbackList.class);
        finish();
        startActivity(intent);
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
                Intent intent = new Intent(ProjectsActivity.this, MainActivity.class);
                startActivity(intent);
                //functions.redirectActivity(HomeActivityProfessional.this, MainActivity.class);

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

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Toast.makeText(getApplicationContext(), "Works", Toast.LENGTH_SHORT).show();
    }
}