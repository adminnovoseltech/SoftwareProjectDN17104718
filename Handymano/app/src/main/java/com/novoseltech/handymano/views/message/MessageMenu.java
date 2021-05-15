package com.novoseltech.handymano.views.message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.novoseltech.handymano.MainActivity;
import com.novoseltech.handymano.R;
import com.novoseltech.handymano.adapter.MessagesAdapter;
import com.novoseltech.handymano.views.professional.HomeActivityProfessional;
import com.novoseltech.handymano.views.professional.ProfessionalProfileActivity;
import com.novoseltech.handymano.views.professional.feedback.FeedbackList;
import com.novoseltech.handymano.views.professional.job.JobsList;
import com.novoseltech.handymano.views.professional.project.ProjectsActivity;
import com.novoseltech.handymano.views.standard.HomeActivityStandard;
import com.novoseltech.handymano.views.standard.StandardProfileActivity;
import com.novoseltech.handymano.views.standard.job.JobsActivity;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.List;

public class MessageMenu extends AppCompatActivity {

    //Layout components
    private DrawerLayout drawerLayout;
    private RecyclerView rv_chatList;
    private TextView tv_UserName;
    private LinearLayout homeNavLayout;
    private LinearLayout messageNavLayout;
    private LinearLayout jobsNavLayout;
    private LinearLayout projectsNavLayout;
    private LinearLayout feedbackNavLayout;
    private LinearLayout appLogoutLayout;

    //Firebase components
    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();;
    private FirebaseUser user = mAuth.getCurrentUser();

    //Objeects
    private RecyclerView.Adapter adapter;
    private List<String> messageReceipients = new ArrayList<>();
    private List<String> lastMessageSent = new ArrayList<>();
    private String USER_TYPE = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        USER_TYPE = getIntent().getStringExtra("USER_TYPE");

        if(USER_TYPE.equals("Professional")){
            setContentView(R.layout.activity_message_menu_professional);
            drawerLayout = findViewById(R.id.drawer_layout_professional);

            tv_UserName = drawerLayout.findViewById(R.id.text_UserName_Professional);
            CircularImageView profileImage = drawerLayout.findViewById(R.id.civ_profilePictureProfessional);

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
                        tv_UserName.setText(documentSnapshot.getString("username"));
                    }
                }
            });

            /***********************************************
             * Navigation drawer listeners
             *************************************************/

            //Profile image click lister
            profileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MessageMenu.this, ProfessionalProfileActivity.class);
                    finish();
                    startActivity(intent);

                }
            });

            //Linear layout listener for home page
            homeNavLayout = drawerLayout.findViewById(R.id.homeNavigation);
            homeNavLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MessageMenu.this, HomeActivityProfessional.class);
                    finish();
                    startActivity(intent);
                }
            });

            //Linear layout listener for projects page
            projectsNavLayout = drawerLayout.findViewById(R.id.projectsNavigation);
            projectsNavLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MessageMenu.this, ProjectsActivity.class);
                    finish();
                    startActivity(intent);
                }
            });

            jobsNavLayout = drawerLayout.findViewById(R.id.jobsNavigation);
            jobsNavLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MessageMenu.this, JobsList.class);
                    finish();
                    startActivity(intent);
                }
            });

            messageNavLayout = drawerLayout.findViewById(R.id.messageNavigation);
            messageNavLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = getIntent();
                    finish();
                    intent.putExtra("USER_TYPE", "Professional");
                    startActivity(intent);
                }
            });

            feedbackNavLayout = drawerLayout.findViewById(R.id.feedbackNavigation);
            feedbackNavLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                    Intent intent = new Intent(MessageMenu.this, FeedbackList.class);
                    startActivity(intent);
                }
            });

            appLogoutLayout = drawerLayout.findViewById(R.id.logoutNavigation);
            appLogoutLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    logout();
                }
            });



            /*************************************************************
             * end of navigation drawer listeners
             *************************************************************/
        }else{
            setContentView(R.layout.activity_message_menu);
            drawerLayout = findViewById(R.id.drawer_layout_standard);

            tv_UserName = drawerLayout.findViewById(R.id.text_UserName_Standard);
            CircularImageView profileImage = drawerLayout.findViewById(R.id.civ_profilePictureStandard);

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
                        tv_UserName.setText(documentSnapshot.getString("username"));
                    }
                }
            });

            /***********************************************
             * Navigation drawer listeners
             *************************************************/

            //Profile image click lister
            profileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MessageMenu.this, StandardProfileActivity.class);
                    finish();
                    startActivity(intent);

                }
            });

            //Linear layout listener for home page
            homeNavLayout = drawerLayout.findViewById(R.id.homeNavigation);
            homeNavLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MessageMenu.this, HomeActivityStandard.class);
                    finish();
                    startActivity(intent);
                }
            });

            jobsNavLayout = drawerLayout.findViewById(R.id.jobsNavigation);
            jobsNavLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MessageMenu.this, JobsActivity.class);
                    finish();
                    startActivity(intent);
                }
            });

            messageNavLayout = drawerLayout.findViewById(R.id.messageNavigation);
            messageNavLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = getIntent();
                    finish();
                    intent.putExtra("USER_TYPE", "Standard");
                    startActivity(intent);
                }
            });

            appLogoutLayout = drawerLayout.findViewById(R.id.logoutNavigation);
            appLogoutLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    logout();
                }
            });



            /*************************************************************
             * end of navigation drawer listeners
             *************************************************************/
        }

        rv_chatList = findViewById(R.id.rv_chatList);

        //Checking in the "chat" collection if the user has any recipients in the array
        fStore.collection("chat").document(user.getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    messageReceipients = (List<String>) documentSnapshot.get("recipients");

                    if(messageReceipients != null){
                        //If the recipients list is not empty then add them to the ArrayList that will be passed to the adapter
                        //so that the recipients can be shown in the RecyclerView
                        for(int i = 0; i < messageReceipients.size(); i++){

                            String toSplit = messageReceipients.get(i);
                            String[] messageData = toSplit.split(",");
                            String userID = messageData[0];

                            fStore.collection("chat").document(user.getUid())
                                    .collection(userID)
                                    .orderBy("timestamp", Query.Direction.DESCENDING)
                                    .limit(1)
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                            if(value != null){
                                                //For each of the recipients get the last message sent and add it to the lastMessageSent ArrayList
                                                //so that it can be shown in the RecyclerView under the recipients name
                                                List<DocumentSnapshot> dsList = value.getDocuments();

                                                if(dsList.isEmpty()){

                                                }else{
                                                    //Get the last message's timestamp and calculate when it was sent using the PrettyTime
                                                    //https://github.com/ocpsoft/prettytime

                                                    DocumentSnapshot docSnap = dsList.get(0);
                                                    PrettyTime p = new PrettyTime();
                                                    Timestamp ts = docSnap.getTimestamp("timestamp");
                                                    String timestamp = p.format(ts.toDate());
                                                    lastMessageSent.add(timestamp + "," + docSnap.getString("message"));
                                                }

                                            }else{

                                            }
                                        }
                                    });
                        }
                    }

                }
            }
        });

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Wait 1000ms until the data is loaded and then show the data in the RecyclerView
                adapter = new MessagesAdapter(messageReceipients, lastMessageSent, getApplicationContext());
                rv_chatList.setHasFixedSize(true);
                rv_chatList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                rv_chatList.setAdapter(adapter);

            }
        }, 1000);

    }

    public void ClickMenu(View view) {
        openDrawer(drawerLayout);
    }

    public static void openDrawer(DrawerLayout drawerLayout) {
        //Open drawer layout
        drawerLayout.openDrawer(GravityCompat.START);
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
                Intent intent = new Intent(MessageMenu.this, MainActivity.class);
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
    }
}