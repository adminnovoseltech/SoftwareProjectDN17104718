package com.novoseltech.handymano.views.standard.feedback;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddRating extends AppCompatActivity {
    //FIREBASE
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    //MY DETAILS
    String MY_UID = "";
    String MY_USERNAME = "";

    //TRADE DETAILS
    String TRADE_UID = "";
    String TRADE_USERNAME = "";

    //FEEDBACK DETAILS
    float ratingStars = 0.0f;
    String feedbackComment = "";
    Date dt = Calendar.getInstance().getTime();
    SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
    String todayDate = df.format(dt);
    String FEEDBACK_MODE = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_rating);

        MY_UID = getIntent().getStringExtra("MY_UID");
        MY_USERNAME = getIntent().getStringExtra("MY_USERNAME");
        TRADE_UID = getIntent().getStringExtra("TRADE_UID");
        TRADE_USERNAME = getIntent().getStringExtra("TRADE_USERNAME");
        FEEDBACK_MODE = getIntent().getStringExtra("FEEDBACK_MODE");

        //LAYOUT OBJECTS
        TextView tv_addTradeRatingTitle = findViewById(R.id.tv_addTradeRatingTitle);
        RatingBar rb_tradeRatingTemp = findViewById(R.id.rb_tradeRatingTemp);
        EditText et_ml_tradeFeedback = findViewById(R.id.et_ml_tradeFeedback);
        Button btn_saveTradeFeedback = findViewById(R.id.btn_saveTradeFeedback);

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
}