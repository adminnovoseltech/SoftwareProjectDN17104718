package com.novoseltech.myhandyman;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity{




    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    String UID;
    String accountType = "";
    String email = "";

    Functions functions;

    Map<String, Object> userData = new HashMap<>();

    //Navigation drawer

    DrawerLayout drawerLayout;


    //End of navigation drawer

    TextView tv_UserName;
    ImageView profileImage;

    LinearLayout homeNavLayout;
    LinearLayout messageNavLayout;
    LinearLayout jobsNavLayout;
    LinearLayout projectsNavLayout;







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Handler handler = new Handler(Looper.getMainLooper());




        //drawerLayout_standard = findViewById(R.id.drawer_layout_standard);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        UID = mAuth.getCurrentUser().getUid();







        fStore.collection("user").document(UID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                accountType = documentSnapshot.getString("accountType");
                email = documentSnapshot.getString("email");
                userData.put("accountType", documentSnapshot.getString("accountType"));
                userData.put("username", documentSnapshot.getString("username"));
                userData.put("email", documentSnapshot.getString("email"));
                userData.put("phoneNo", documentSnapshot.get("phoneNo"));

                //if(accountType.equals("Professional"))
            }
        });

        functions = new Functions();


        handler.postDelayed(new Runnable() {

            //Create new fragment objects
            ProfileFragment pf = new ProfileFragment();
            HomeFragment hf = new HomeFragment();
            MessagesFragment mf = new MessagesFragment();
            JobsFragment jf = new JobsFragment();
            ProjectsFragment prjf = new ProjectsFragment();

            @Override
            public void run() {


                //On start of application load fragment for home page
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.frame, hf);
                ft.commit();

                //Depending on account type load different activity layout and drawer layout
                if(accountType.equals("Professional")){
                    //Navigation drawer
                    setContentView(R.layout.activity_home_professional);
                    drawerLayout = findViewById(R.id.drawer_layout);

                    tv_UserName = drawerLayout.findViewById(R.id.text_UserName_Professional);
                    tv_UserName.setText(userData.get("username").toString());

                    //Linear layout listener for projects page fragment
                    projectsNavLayout = drawerLayout.findViewById(R.id.projectsNavigation);
                    projectsNavLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                            ft.replace(R.id.frame, prjf);
                            ft.commit();
                            drawerLayout.closeDrawer(GravityCompat.START);
                        }
                    });


                    //Standard user
                }else{
                    //Navigation drawer
                    setContentView(R.layout.activity_home_standard);
                    drawerLayout = findViewById(R.id.drawer_layout_standard);

                    tv_UserName = drawerLayout.findViewById(R.id.text_UserName_Standard);
                    tv_UserName.setText(userData.get("username").toString());


                }

                //Profile image listener on navigation drawer
                //Get the drawer layout, retrieve id of profile pic open fragment
                profileImage = drawerLayout.findViewById(R.id.profilePicture);
                profileImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.frame, pf);
                        ft.commit();
                        drawerLayout.closeDrawer(GravityCompat.START);

                        Toast.makeText(getApplicationContext(), "It works", Toast.LENGTH_SHORT).show();

                    }
                });

                //Linear layout listener for home page fragment
                homeNavLayout = drawerLayout.findViewById(R.id.homeNavigation);
                homeNavLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.frame, hf);
                        ft.commit();
                        drawerLayout.closeDrawer(GravityCompat.START);
                    }
                });

                //Linear layout listener for messages page fragment
                messageNavLayout = drawerLayout.findViewById(R.id.messageNavigation);
                messageNavLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.frame, mf);
                        ft.commit();
                        drawerLayout.closeDrawer(GravityCompat.START);
                    }
                });

                //Linear layout listener for jobs page fragment
                jobsNavLayout = drawerLayout.findViewById(R.id.jobsNavigation);
                jobsNavLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.frame, jf);
                        ft.commit();
                        drawerLayout.closeDrawer(GravityCompat.START);
                    }
                });










            }
        }, 1000);




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
                functions.redirectActivity(HomeActivity.this, MainActivity.class);

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

    public void ClickLogOut(View view) {
        logout();
    }

    public void ClickHome(View view) {
    }

    public void ClickProfile(View view) {

    }

    public void ClickMessages(View view) {
    }

    private void loadFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment);
        drawerLayout.closeDrawer(GravityCompat.START);
        fragmentTransaction.addToBackStack(null);
    }


}