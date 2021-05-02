package com.novoseltech.handymano.views.standard.feedback;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.novoseltech.handymano.MainActivity;
import com.novoseltech.handymano.R;
import com.novoseltech.handymano.model.FeedbackModel;
import com.novoseltech.handymano.views.message.MessageMenu;
import com.novoseltech.handymano.views.professional.feedback.FeedbackList;
import com.novoseltech.handymano.views.standard.HomeActivityStandard;
import com.novoseltech.handymano.views.standard.StandardProfileActivity;
import com.novoseltech.handymano.views.standard.job.JobsActivity;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class FeedbackActivity extends AppCompatActivity {

    //FIREBASE
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    //NAVIGATION DRAWERS
    DrawerLayout drawerLayout;
    ShapeableImageView profileImage;
    TextView tv_myUsername;

    //MY DETAILS
    String MY_USERNAME = "";
    String MY_UID = mAuth.getCurrentUser().getUid();

    //TRADE DETAILS
    String TRADE_UID = "";
    String TRADE_USERNAME = "";

    //FEEDBACK BANNER
    int oneStarCount = 2;
    int twoStarCount = 7;
    int threeStarCount = 32;
    int fourStarCount = 78;
    int fiveStarCount = 67;
    double totalRating = 0.0;

    //FEEDBACK LIST
    private RecyclerView fStoreList;
    private FirestoreRecyclerAdapter adapter;
    ConstraintLayout cl_feedbackList;
    LayoutInflater layoutInflater;
    String FEEDBACK_MODE = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        drawerLayout = findViewById(R.id.drawer_layout_standard);
        tv_myUsername = findViewById(R.id.text_UserName_Standard);

        TRADE_UID = getIntent().getStringExtra("USER_ID");
        TRADE_USERNAME = getIntent().getStringExtra("TRADE_USERNAME");

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

        fStore.collection("user")
                .document(MY_UID)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    MY_USERNAME = documentSnapshot.getString("username");
                    tv_myUsername.setText(MY_USERNAME);

                }

            }
        });

        fStore.collection("rating")
                .document(TRADE_UID)
                .collection("feedback")
                .document(MY_UID)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    Toast.makeText(getApplicationContext(), "Exists", Toast.LENGTH_SHORT).show();
                    FEEDBACK_MODE = "EDIT";
                }else{
                    Toast.makeText(getApplicationContext(), "Does not exist", Toast.LENGTH_SHORT).show();
                    FEEDBACK_MODE = "NEW";
                }

            }
        });

        //New feedback
        Button btn_addTradeFeedback = findViewById(R.id.btn_addTradeFeedback);
        btn_addTradeFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FeedbackActivity.this, AddRating.class);
                intent.putExtra("TRADE_UID", TRADE_UID);
                intent.putExtra("TRADE_USERNAME", TRADE_USERNAME);
                intent.putExtra("MY_UID", MY_UID);
                intent.putExtra("MY_USERNAME", MY_USERNAME);
                intent.putExtra("FEEDBACK_MODE", FEEDBACK_MODE);
                startActivity(intent);
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
                if(FEEDBACK_MODE.equals("NEW")){
                    btn_addTradeFeedback.setText("ADD FEEDBACK");
                }else if(FEEDBACK_MODE.equals("EDIT")){
                    btn_addTradeFeedback.setText("EDIT FEEDBACK");
                }


            }
        }, 500);

        //Feedback list
        fStoreList = findViewById(R.id.rv_ratingList);

        //Query
        Query query = fStore.collection("rating")
                .document(TRADE_UID)
                .collection("feedback")
                //.orderBy("creation_date", Query.Direction.DESCENDING)
                .whereNotEqualTo("feedback_text", "");

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

                if(model.getUsername().equals(MY_USERNAME)){
                    holder.feedbackAuthor.setText("Your feedback on " + model.getCreation_date() + " :");
                }else{
                    holder.feedbackAuthor.setText(model.getUsername() + " on " + model.getCreation_date() + " :");
                }
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

                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Toast.makeText(getApplicationContext(), "IT WORKS", Toast.LENGTH_SHORT).show();
                        return true;
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
                Intent intent = new Intent(FeedbackActivity.this, MainActivity.class);
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

        Intent intent = new Intent(FeedbackActivity.this, JobsActivity.class);
        finish();
        startActivity(intent);
    }

    public void ClickLogOut(View view) {
        finish();
        logout();
    }

    public void ClickHome(View view) {
        Intent intent = new Intent(FeedbackActivity.this, HomeActivityStandard.class);
        finish();
        startActivity(intent);
    }

    public void ClickProfile(View view) {
        Intent intent = new Intent(FeedbackActivity.this, StandardProfileActivity.class);
        finish();
        startActivity(intent);
    }

    public void ClickMessages(View view) {
        Intent intent = new Intent(FeedbackActivity.this, MessageMenu.class);
        intent.putExtra("USER_TYPE", "Standard");
        finish();
        startActivity(intent);
    }
}