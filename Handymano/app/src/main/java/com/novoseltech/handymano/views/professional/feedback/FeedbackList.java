package com.novoseltech.handymano.views.professional.feedback;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.novoseltech.handymano.MainActivity;
import com.novoseltech.handymano.R;
import com.novoseltech.handymano.model.FeedbackModel;
import com.novoseltech.handymano.views.message.MessageMenu;
import com.novoseltech.handymano.views.professional.HomeActivityProfessional;
import com.novoseltech.handymano.views.professional.ProfessionalProfileActivity;
import com.novoseltech.handymano.views.professional.job.JobsList;
import com.novoseltech.handymano.views.professional.project.ProjectsActivity;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class FeedbackList extends AppCompatActivity {
    DrawerLayout drawerLayout;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    //Feedback banner
    int oneStarCount = 0;
    int twoStarCount = 0;
    int threeStarCount = 0;
    int fourStarCount = 0;
    int fiveStarCount = 0;

    double totalRating = 0.0;


    //Feedback list
    private RecyclerView fStoreList;
    private FirestoreRecyclerAdapter adapter;

    ConstraintLayout cl_tradeFeedbackList;
    LayoutInflater layoutInflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_list);
        drawerLayout = findViewById(R.id.drawer_layout_professional);

        FirebaseUser user = mAuth.getCurrentUser();

        CircularImageView profileImage = drawerLayout.findViewById(R.id.civ_profilePictureProfessional);
        TextView tv_drawerUsername = drawerLayout.findViewById(R.id.text_UserName_Professional);

        if(mAuth.getCurrentUser().getPhotoUrl() != null){
            Glide.with(getApplicationContext())
                    .load(user.getPhotoUrl())
                    .into(profileImage);
        }else{
            Log.d("TAG", "Profile image not found. Loading default image.");
        }

        //Feedback banner
        TextView tv_tradeRatingCountOne = findViewById(R.id.tv_tradeRatingCountOne);
        TextView tv_tradeRatingCountTwo = findViewById(R.id.tv_tradeRatingCountTwo);
        TextView tv_tradeRatingCountThree = findViewById(R.id.tv_tradeRatingCountThree);
        TextView tv_tradeRatingCountFour = findViewById(R.id.tv_tradeRatingCountFour);
        TextView tv_tradeRatingCountFive = findViewById(R.id.tv_tradeRatingCountFive);
        TextView tv_tradeTotalRating = findViewById(R.id.tv_tradeTotalRating);

        //Feedback list
        cl_tradeFeedbackList = findViewById(R.id.cl_tradeFeedbackList);
        layoutInflater = LayoutInflater.from(getApplicationContext());

        fStore.collection("rating")
                .document(user.getUid())
                .collection("feedback")
                .whereEqualTo("stars", 5)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(DocumentSnapshot documentSnapshot : task.getResult()){
                        fiveStarCount++;
                    }
                    Log.d("DOCUMENT COUNT", String.valueOf(fiveStarCount));
                }else{
                    Log.d("LOG", "Error getting documents");
                }
            }
        });

        fStore.collection("rating")
                .document(user.getUid())
                .collection("feedback")
                .whereEqualTo("stars", 4)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(DocumentSnapshot documentSnapshot : task.getResult()){
                        fourStarCount++;
                    }
                }else{
                    Log.d("LOG", "Error getting documents");
                }
            }
        });

        fStore.collection("rating")
                .document(user.getUid())
                .collection("feedback")
                .whereEqualTo("stars", 3)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(DocumentSnapshot documentSnapshot : task.getResult()){
                        threeStarCount++;
                    }
                }else{
                    Log.d("LOG", "Error getting documents");
                }
            }
        });

        fStore.collection("rating")
                .document(user.getUid())
                .collection("feedback")
                .whereEqualTo("stars", 2)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(DocumentSnapshot documentSnapshot : task.getResult()){
                        twoStarCount++;
                    }
                }else{
                    Log.d("LOG", "Error getting documents");
                }
            }
        });

        fStore.collection("rating")
                .document(user.getUid())
                .collection("feedback")
                .whereEqualTo("stars", 1)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(DocumentSnapshot documentSnapshot : task.getResult()){
                        oneStarCount++;
                    }
                }else{
                    Log.d("LOG", "Error getting documents");
                }
            }
        });

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

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                //Feedback banner -- CONTINUED
                int totalRates = oneStarCount + twoStarCount + threeStarCount + fourStarCount + fiveStarCount;
                double totalScore = oneStarCount + (twoStarCount * 2) + (threeStarCount * 3) + (fourStarCount * 4) + (fiveStarCount * 5);
                totalRating = totalScore / totalRates;

                tv_tradeRatingCountOne.setText(String.valueOf(oneStarCount));
                tv_tradeRatingCountTwo.setText(String.valueOf(twoStarCount));
                tv_tradeRatingCountThree.setText(String.valueOf(threeStarCount));
                tv_tradeRatingCountFour.setText(String.valueOf(fourStarCount));
                tv_tradeRatingCountFive.setText(String.valueOf(fiveStarCount));

                if(totalScore == 0.0 || totalRates == 0){
                    tv_tradeTotalRating.setText("0.0");
                }else{
                    tv_tradeTotalRating.setText(String.valueOf(round(totalRating, 1)));
                }

                //tv_tradeTotalRating.setText(String.valueOf(round(totalRating, 1)));

            }
        }, 500);

        //Feedback list
        fStoreList = findViewById(R.id.rv_tradeRatingList);

        //Query
        Query query = fStore.collection("rating")
                .document(user.getUid())
                .collection("feedback")
                .whereNotEqualTo("feedback_text", "");
                //.orderBy("creation_date", Query.Direction.DESCENDING);

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

                holder.feedbackAuthor.setText(model.getUsername() + " on " + model.getCreation_date() + " :");
                holder.feedbackComment.setText(model.getFeedback_text());

                if(model.getStars() == 5){
                    View starLayout = layoutInflater.inflate(R.layout.five_star_layout, null, false);
                    holder.ll_starContainer.addView(starLayout);
                }else if(model.getStars() == 4){
                    View starLayout = layoutInflater.inflate(R.layout.four_star_layout, null, false);
                    holder.ll_starContainer.addView(starLayout);
                }else if(model.getStars() == 3){
                    View starLayout = layoutInflater.inflate(R.layout.three_star_layout, null, false);
                    holder.ll_starContainer.addView(starLayout);
                }else if(model.getStars() == 2){
                    View starLayout = layoutInflater.inflate(R.layout.two_star_layout, null, false);
                    holder.ll_starContainer.addView(starLayout);
                }else{
                    View starLayout = layoutInflater.inflate(R.layout.one_star_layout, null, false);
                    holder.ll_starContainer.addView(starLayout);
                }

                StorageReference storageReference = FirebaseStorage.getInstance()
                        .getReference().child("images")
                        .child(model.getUser_id())
                        .child("profile_image_" + model.getUser_id() + ".jpeg");

                storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        if(task.isSuccessful()){

                            Glide.with(getApplicationContext())
                                    .load(task.getResult().toString())
                                    .into(holder.feedbackImage);
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Glide.with(getApplicationContext())
                                .load(R.drawable.ic_profile_512)
                                .into(holder.feedbackImage);
                    }
                });


            }
        };

        fStoreList.setHasFixedSize(true);
        fStoreList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        fStoreList.setAdapter(adapter);

    }

    private class FeedbackViewHolder  extends RecyclerView.ViewHolder{

        private TextView feedbackAuthor;
        private TextView feedbackComment;
        private ImageView feedbackImage;

        LinearLayout ll_starContainer;

        public FeedbackViewHolder(@NonNull View itemView) {
            super(itemView);

            feedbackAuthor = itemView.findViewById(R.id.tv_tradeFeedbackAuthor);
            feedbackComment = itemView.findViewById(R.id.tv_tradeFeedbackComment);
            feedbackImage = itemView.findViewById(R.id.iv_tradeFeedbackProfileImage);
            ll_starContainer = itemView.findViewById(R.id.ll_starContainer);
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

    public static double round(double value, int places) {
        //https://stackoverflow.com/questions/2808535/round-a-double-to-2-decimal-places
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
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

    public void ClickFeedback(View view){
        finish();
        startActivity(getIntent());
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