package com.novoseltech.handymano.views.standard.feedback;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.novoseltech.handymano.R;
import com.novoseltech.handymano.views.standard.ViewProfessionalActivity;
import com.novoseltech.handymano.views.standard.job.CreateJob;
import com.novoseltech.handymano.views.standard.job.JobsActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddRating extends AppCompatActivity {

    //Layout components
    private TextView tv_addTradeRatingTitle;
    private RatingBar rb_tradeRatingTemp;
    private EditText et_ml_tradeFeedback;
    private Button btn_saveTradeFeedback;

    //Firebase components
    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    //Objects
    private float ratingStars = 0.0f;
    private Date dt = Calendar.getInstance().getTime();
    private SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
    private String todayDate = df.format(dt);
    private String feedbackComment = "";
    private String FEEDBACK_MODE = "";
    private String MY_UID = "";
    private String MY_USERNAME = "";
    private String TRADE_UID = "";
    private String TRADE_USERNAME = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_rating);

        MY_UID = getIntent().getStringExtra("MY_UID");
        MY_USERNAME = getIntent().getStringExtra("MY_USERNAME");
        TRADE_UID = getIntent().getStringExtra("TRADE_UID");
        TRADE_USERNAME = getIntent().getStringExtra("TRADE_USERNAME");
        FEEDBACK_MODE = getIntent().getStringExtra("FEEDBACK_MODE");

        tv_addTradeRatingTitle = findViewById(R.id.tv_addTradeRatingTitle);
        rb_tradeRatingTemp = findViewById(R.id.rb_tradeRatingTemp);
        et_ml_tradeFeedback = findViewById(R.id.et_ml_tradeFeedback);
        btn_saveTradeFeedback = findViewById(R.id.btn_saveTradeFeedback);

        if(FEEDBACK_MODE.equals("EDIT")){
            fStore.collection("rating")
                    .document(TRADE_UID)
                    .collection("feedback")
                    .document(MY_UID)
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        DocumentSnapshot documentSnapshot = task.getResult();
                        et_ml_tradeFeedback.setText(documentSnapshot.getString("feedback_text"));
                        rb_tradeRatingTemp.setRating((float)documentSnapshot.getLong("stars"));
                    }
                }
            });
        }

        //FEEDBACK
        tv_addTradeRatingTitle.setText("Please rate services provided by " + TRADE_USERNAME);

        btn_saveTradeFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ratingStars = rb_tradeRatingTemp.getRating();
                feedbackComment = et_ml_tradeFeedback.getText().toString();

                if(ratingStars == 0.0f){
                    Toast.makeText(getApplicationContext(), "You must rate the user", Toast.LENGTH_SHORT).show();
                }else if(feedbackComment.length() > 500){
                    Toast.makeText(getApplicationContext(), "Feedback comment cannot exceed 500 characters", Toast.LENGTH_SHORT).show();
                }else{
                    long stars = (long) ratingStars;
                    Map<String, Object> feedbackMap = new HashMap<>();
                    feedbackMap.put("creation_date", todayDate);
                    feedbackMap.put("feedback_text", feedbackComment);
                    feedbackMap.put("stars", stars);
                    feedbackMap.put("user_id", MY_UID);
                    feedbackMap.put("username", MY_USERNAME);

                    fStore.collection("rating")
                            .document(TRADE_UID)
                            .collection("feedback")
                            .document(MY_UID)
                            .set(feedbackMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Intent intent = new Intent(AddRating.this, ViewProfessionalActivity.class);
                                        intent.putExtra("USER_ID", TRADE_UID);
                                        startActivity(intent);
                                    }else{
                                        Toast.makeText(getApplicationContext(), "Rating could not be set. Please try again later.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

    }

    public void discardChanges(){
        //Close app
        //Initialize alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //Set title
        builder.setTitle("Discard");
        //Set message
        builder.setMessage("Your changes are not saved. Continue ?");
        //Yes button
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
                Intent intent = new Intent(AddRating.this, FeedbackActivity.class);
                intent.putExtra("USER_ID", TRADE_UID);
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

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        discardChanges();

    }
}