package com.novoseltech.myhandyman;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity{


    private AppBarConfiguration mAppBarConfiguration;


    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    String UID;
    String accountType = "";
    String email = "";
    //DocumentReference docRef = fStore.collection("user").document(mAuth.getCurrentUser().getUid());

    Functions functions;

    Map<String, Object> userData = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        final Handler handler = new Handler(Looper.getMainLooper());



        //userData = new HashMap<>();



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
            }
        });



        functions = new Functions();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);



        NavigationView navigationView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        View headerView = navigationView.getHeaderView(0);


        TextView textView_HeaderUsername = (TextView) headerView.findViewById(R.id.textView_headerUsername);
        TextView textView_HeaderEmail = (TextView)headerView.findViewById(R.id.textView_headerEmail);

        ImageView profileImage = headerView.findViewById(R.id.imageView_ProfileImage);








        /*mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow, R.id.nav_jobs).setDrawerLayout(drawer)
                .build();*/

        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home, R.id.profileFragment).setDrawerLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);




        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Works",Toast.LENGTH_SHORT).show();
                drawer.closeDrawer(GravityCompat.START);
                Navigation.findNavController(profileImage).navigate(R.id.profileFragment);

                //Navigation.findNavController(view).navigate(R.id.navigateToProfile);

            }
        });



        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(accountType.equals("Professional")){

                    navigationView.getMenu().clear();
                    navigationView.inflateMenu(R.menu.activity_main_drawer_professional);
                    textView_HeaderUsername.setText(userData.get("username").toString());
                    textView_HeaderEmail.setText(userData.get("email").toString());
                }else{

                    navigationView.getMenu().clear();
                    navigationView.inflateMenu(R.menu.activity_main_drawer_standard);
                    //textView_HeaderEmail.setText(email);
                    textView_HeaderUsername.setText(userData.get("username").toString());
                    textView_HeaderEmail.setText(userData.get("email").toString());

                }
            }
        }, 1000);

    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.home, menu);

        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
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


    public void ClickLogOut(MenuItem item) {
        logout();
    }
}