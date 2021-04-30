package com.novoseltech.handymano.views.standard.feedback;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.novoseltech.handymano.R;
import com.novoseltech.handymano.model.FeedbackModel;
import com.novoseltech.handymano.views.professional.feedback.FeedbackList;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class FeedbackActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    String UID = mAuth.getCurrentUser().getUid();

    String TRADE_UID = "";

    ShapeableImageView profileImage;

    //Feedback banner
    int oneStarCount = 2;
    int twoStarCount = 7;
    int threeStarCount = 32;
    int fourStarCount = 78;
    int fiveStarCount = 67;

    double totalRating = 0.0;


    //Feedback list
    private RecyclerView fStoreList;
    private FirestoreRecyclerAdapter adapter;

    ConstraintLayout cl_feedbackList;
    LayoutInflater layoutInflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        drawerLayout = findViewById(R.id.drawer_layout_standard);

        TRADE_UID = getIntent().getStringExtra("USER_ID");

        FirebaseUser user = mAuth.getCurrentUser();
        profileImage = drawerLayout.findViewById(R.id.profilePicture);

        if(mAuth.getCurrentUser().getPhotoUrl() != null){
            Glide.with(getApplicationContext())
                    .load(user.getPhotoUrl())
                    .into(profileImage);
        }else{
            Log.d("TAG", "Profile image not found. Loading default image.");
        }

        //Feedback banner
        TextView tv_ratingCountOne = findViewById(R.id.tv_ratingCountOne);
        TextView tv_ratingCountTwo = findViewById(R.id.tv_ratingCountTwo);
        TextView tv_ratingCountThree = findViewById(R.id.tv_ratingCountThree);
        TextView tv_ratingCountFour = findViewById(R.id.tv_ratingCountFour);
        TextView tv_ratingCountFive = findViewById(R.id.tv_ratingCountFive);
        TextView tv_totalRating = findViewById(R.id.tv_totalRating);

        //Feedback list
        cl_feedbackList = findViewById(R.id.cl_fList);
        layoutInflater = LayoutInflater.from(getApplicationContext());


        /**
         *
         * RATING STATISTICS RETRIEVAL
         *
         * **/

        fStore.collection("rating")
                .document(TRADE_UID)
                .collection("feedback")
                .whereEqualTo("stars", 5)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(DocumentSnapshot documentSnapshot : task.getResult()){
                        fiveStarCount++;
                    }
                }else{
                    Log.d("LOG", "Error getting documents");
                }
            }
        });

        fStore.collection("rating")
                .document(TRADE_UID)
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
                .document(TRADE_UID)
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
                .document(TRADE_UID)
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
                .document(TRADE_UID)
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



        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                //Feedback banner -- CONTINUED
                int totalRates = oneStarCount + twoStarCount + threeStarCount + fourStarCount + fiveStarCount;
                double totalScore = oneStarCount + (twoStarCount * 2) + (threeStarCount * 3) + (fourStarCount * 4) + (fiveStarCount * 5);
                totalRating = totalScore / totalRates;


                tv_ratingCountOne.setText(String.valueOf(oneStarCount));
                tv_ratingCountTwo.setText(String.valueOf(twoStarCount));
                tv_ratingCountThree.setText(String.valueOf(threeStarCount));
                tv_ratingCountFour.setText(String.valueOf(fourStarCount));
                tv_ratingCountFive.setText(String.valueOf(fiveStarCount));
                tv_totalRating.setText(String.valueOf(round(totalRating, 1)));


            }
        }, 500);

        //Feedback list
        fStoreList = findViewById(R.id.rv_ratingList);

        //Query
        Query query = fStore.collection("rating")
                .document(TRADE_UID)
                .collection("feedback")
                .orderBy("creation_date", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<FeedbackModel> options = new FirestoreRecyclerOptions.Builder<FeedbackModel>()
                .setQuery(query, FeedbackModel.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<FeedbackModel, FeedbackActivity.FeedbackViewHolder>(options) {

            @NonNull
            @Override
            public FeedbackActivity.FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trade_feedback_list_item, parent, false);
                return new FeedbackActivity.FeedbackViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull FeedbackActivity.FeedbackViewHolder holder, int position, @NonNull FeedbackModel model) {

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
                            Log.d("DOWNLOAD URL", task.getResult().toString());

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

        //private TextView feedbackCreationDate;
        private TextView feedbackAuthor;
        private TextView feedbackComment;
        private ImageView feedbackImage;

        LinearLayout ll_starContainer;

        public FeedbackViewHolder(@NonNull View itemView) {
            super(itemView);

            //feedbackCreationDate = itemView.findViewById(R.id.tv_tradeFeedbackAuthor);
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
}