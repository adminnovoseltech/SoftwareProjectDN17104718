package com.novoseltech.handymano.views.professional.feedback;

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
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.novoseltech.handymano.MainActivity;
import com.novoseltech.handymano.R;
import com.novoseltech.handymano.model.FeedbackModel;
import com.novoseltech.handymano.model.ProjectsModel;
import com.novoseltech.handymano.views.message.MessageMenu;
import com.novoseltech.handymano.views.professional.HomeActivityProfessional;
import com.novoseltech.handymano.views.professional.ProfessionalProfileActivity;
import com.novoseltech.handymano.views.professional.job.JobsList;
import com.novoseltech.handymano.views.professional.project.ProfessionalProjectViewActivity;
import com.novoseltech.handymano.views.professional.project.ProjectsActivity;

public class FeedbackList extends AppCompatActivity {
    DrawerLayout drawerLayout;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    String UID = mAuth.getCurrentUser().getUid();

    ShapeableImageView profileImage;

    //Feedback banner


    //Feedback list
    private RecyclerView fStoreList;
    private FirestoreRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_list);
        drawerLayout = findViewById(R.id.drawer_layout_professional);

        FirebaseUser user = mAuth.getCurrentUser();

        profileImage = drawerLayout.findViewById(R.id.profilePictureProfessional);

        if(mAuth.getCurrentUser().getPhotoUrl() != null){
            Glide.with(getApplicationContext())
                    .load(user.getPhotoUrl())
                    .into(profileImage);
        }else{
            Log.d("TAG", "Profile image not found. Loading default image.");
        }




        //Feedback banner


        //Feedback list
        fStoreList = findViewById(R.id.rv_tradeRatingList);

        //Query
        Query query = fStore.collection("rating")
                .document(user.getUid())
                .collection("feedback")
                .orderBy("creation_date", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<FeedbackModel> options = new FirestoreRecyclerOptions.Builder<FeedbackModel>()
                .setQuery(query, FeedbackModel.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<FeedbackModel, FeedbackList.FeedbackViewHolder>(options) {

            @NonNull
            @Override
            public FeedbackList.FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trade_feedback_list_item, parent, false);
                return new FeedbackList.FeedbackViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull FeedbackList.FeedbackViewHolder holder, int position, @NonNull FeedbackModel model) {

                holder.feedbackAuthor.setText(model.getUsername());
                holder.feedbackComment.setText(model.getFeedback_text());


            }
        };

        fStoreList.setHasFixedSize(true);
        fStoreList.setLayoutManager(new LinearLayoutManager(this));
        fStoreList.setAdapter(adapter);

    }

    private class FeedbackViewHolder  extends RecyclerView.ViewHolder{

        //private TextView feedbackCreationDate;
        private TextView feedbackAuthor;
        private TextView feedbackComment;

        public FeedbackViewHolder(@NonNull View itemView) {
            super(itemView);

            //feedbackCreationDate = itemView.findViewById(R.id.tv_tradeFeedbackAuthor);
            feedbackAuthor = itemView.findViewById(R.id.tv_tradeFeedbackAuthor);
            feedbackComment = itemView.findViewById(R.id.tv_tradeFeedbackComment);
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
        Intent intent = new Intent(FeedbackList.this, ProfessionalProfileActivity.class);
        startActivity(intent);
    }

    public void ClickProjects(View view){
        finish();
        Intent intent = new Intent(FeedbackList.this, ProjectsActivity.class);
        startActivity(intent);
    }

    public void ClickJobs(View view){
        finish();
        Intent intent = new Intent(FeedbackList.this, JobsList.class);
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
        Intent intent = new Intent(FeedbackList.this, HomeActivityProfessional.class);

        startActivity(intent);
    }

    public void ClickMessages(View view){
        finish();
        Intent intent = new Intent(FeedbackList.this, MessageMenu.class);
        intent.putExtra("USER_TYPE", "Professional");
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
                Intent intent = new Intent(FeedbackList.this, MainActivity.class);
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