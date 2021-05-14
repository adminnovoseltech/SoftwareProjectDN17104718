package com.novoseltech.handymano.views.standard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.novoseltech.handymano.MainActivity;
import com.novoseltech.handymano.R;
import com.novoseltech.handymano.model.ServicesModel;
import com.novoseltech.handymano.views.message.MessageMenu;
import com.novoseltech.handymano.views.standard.job.JobsActivity;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivityStandard extends AppCompatActivity {

    //Layout components
    private DrawerLayout drawerLayout;
    private LinearLayout homeNavLayout;
    private LinearLayout messageNavLayout;
    private LinearLayout jobsNavLayout;
    private RecyclerView fStoreList;
    private CircularImageView profileImage;
    private TextView tv_UserName;
    private EditText et_stdUserLocation;
    private TextInputLayout til_stdUserLocation;
    private Button btn_search;
    private Button btn_expandSearchOptions;
    private TextInputLayout til_dropdownServiceCategory;
    private RecyclerView fList;

    //Firebase components
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    private FirebaseFirestore tempFireStore = FirebaseFirestore.getInstance();

    //Variables
    private static final String TAG = "LOG: ";
    private String UID = user.getUid();
    private FirestoreRecyclerAdapter adapter;
    private static final int REQUEST_LOCATION_PERMISSION_CODE = 1;
    private LocationManager locationManager;
    private String std_latitude;
    private String std_longitude;
    private Boolean currentLocationIcon = true;
    private Map<String, Object> pUsers = new HashMap<>();
    private ArrayList accounts = new ArrayList();
    private String categorySelected = "All categories";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_standard);
        drawerLayout = findViewById(R.id.drawer_layout_standard);

        /**
         * Code for standard user account type
         */

        //Empty "temp" subcollection
        tempFireStore.collection("user").document(UID).collection("temp")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                        tempFireStore.collection("user").document(UID).collection("temp")
                                .document(queryDocumentSnapshot.getId())
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                    }
                                });
                    }
                }else{
                    Log.e(TAG, String.valueOf(task.getException()));
                }
            }
        });


        profileImage = drawerLayout.findViewById(R.id.civ_profilePictureStandard);

        if(user.getPhotoUrl() != null){
            Glide.with(getApplicationContext())
                    .load(user.getPhotoUrl())
                    .into(profileImage);
        }

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(getApplicationContext(), StandardProfileActivity.class);
                startActivity(intent);
            }
        });

        tv_UserName = drawerLayout.findViewById(R.id.text_UserName_Standard);
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

        //Location text input field
        et_stdUserLocation = findViewById(R.id.standardUserLocationET);
        et_stdUserLocation.setFocusable(false);
        til_stdUserLocation = findViewById(R.id.standardUserLocationLayout);
        til_stdUserLocation.setHint("Current location");

        //Location based search button
        btn_search = findViewById(R.id.btn_professionalSearch);
        btn_expandSearchOptions = findViewById(R.id.btn_expandSearchOptions);

        btn_expandSearchOptions.setVisibility(View.GONE);

        final AutoCompleteTextView dropdownServiceCategory = findViewById(R.id.dropdownServiceCategoryMenu);
        til_dropdownServiceCategory = findViewById(R.id.dropdownServiceCategoryLayout);

        //recycler view layout modification
        fList = findViewById(R.id.firestoreList);

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if(getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION_CODE);
                }else{
                    if(currentLocationIcon){

                        //Clear temp collection
                        clearTempCollection();
                        //Clear array
                        accounts.clear();
                        //Get users location
                        getCurrentLocation();
                        //Calling the method to retrieve professional users account based on their and user's location and travelling preferences
                        getProUsersFromCurrentLocation();
                        //Stop adapter listening
                        adapter.stopListening();
                        //Start adaper listening
                        adapter.startListening();
                        //Setting the RecyclerView to visible
                        fStoreList.setVisibility(View.VISIBLE);


                        //CHANGE

                        //Set invisible
                        btn_expandSearchOptions.setVisibility(View.VISIBLE);

                        //Set visible
                        btn_search.setVisibility(View.GONE);
                        til_stdUserLocation.setVisibility(View.GONE);
                        dropdownServiceCategory.setVisibility(View.GONE);
                        til_dropdownServiceCategory.setVisibility(View.GONE);


                    }else{

                        //Clear temp collection
                        clearTempCollection();
                        //Clear array
                        accounts.clear();
                        //Get users location
                        //getCurrentLocation();

                        getLocationFromAddress(getApplicationContext(), et_stdUserLocation.getText().toString());

                        //Calling the method to retrieve professional users account based on their and user's location and travelling preferences
                        getProUsersFromCurrentLocation();
                        //Stop adapter listening
                        adapter.stopListening();
                        //Start adaper listening
                        adapter.startListening();
                        //Setting the RecyclerView to visible
                        fStoreList.setVisibility(View.VISIBLE);
                        //Set invisible
                        btn_expandSearchOptions.setVisibility(View.VISIBLE);

                        //Set visible
                        btn_search.setVisibility(View.GONE);
                        til_stdUserLocation.setVisibility(View.GONE);
                        dropdownServiceCategory.setVisibility(View.GONE);
                        til_dropdownServiceCategory.setVisibility(View.GONE);

                    }
                }


            }
        });

        //Start icon of Text Input Layout click listener
        til_stdUserLocation.setStartIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //If current icon is current location, set the map icon
                if(currentLocationIcon){
                    til_stdUserLocation.setStartIconDrawable(R.drawable.ic_map);
                    et_stdUserLocation.setFocusableInTouchMode(true);
                    til_stdUserLocation.setHint("Custom address");
                    currentLocationIcon = false;
                    fStoreList.setVisibility(View.GONE);

                }else{//If current icon is map, set the current location icon
                    til_stdUserLocation.setStartIconDrawable(R.drawable.ic_currentlocation);
                    et_stdUserLocation.setFocusable(false);
                    til_stdUserLocation.setHint("Current location");
                    currentLocationIcon = true;
                    fStoreList.setVisibility(View.GONE);

                }

            }
        });


        btn_expandSearchOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Set invisible
                btn_expandSearchOptions.setVisibility(View.GONE);

                //Set visible
                btn_search.setVisibility(View.VISIBLE);
                til_stdUserLocation.setVisibility(View.VISIBLE);
                dropdownServiceCategory.setVisibility(View.VISIBLE);
                til_dropdownServiceCategory.setVisibility(View.VISIBLE);

            }
        });

        fStoreList = findViewById(R.id.firestoreList);

        //Query
        Query query = fStore.collection("user").document(UID).collection("temp");
        //RecyclerOptions
        FirestoreRecyclerOptions<ServicesModel> options = new FirestoreRecyclerOptions.Builder<ServicesModel>()
                .setQuery(query, ServicesModel.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<ServicesModel, ServicesViewHolder>(options) {
            @NonNull
            @Override
            public ServicesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_single, parent, false);
                return new ServicesViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ServicesViewHolder holder, int position, @NonNull ServicesModel model) {
                holder.list_username.setText(model.getUsername());
                holder.list_category.setText(model.getCategory());
                holder.list_distance.setText(model.getDistance() + " km away");

                Log.d("USER_ID", model.getUser_id());


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
                                    .into(holder.list_image);
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Glide.with(getApplicationContext())
                                .load(R.drawable.ic_profile_512)
                                .into(holder.list_image);
                    }
                });


                holder.list_distance.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_locationpin, 0, 0, 0);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent  = new Intent(getApplicationContext(), ViewProfessionalActivity.class);
                        intent.putExtra("USER_ID", String.valueOf(accounts.get(position)));
                        startActivity(intent);
                    }
                });

            }
        };

        fStoreList.setHasFixedSize(true);
        fStoreList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        fStoreList.setAdapter(adapter);


        adapter.startListening();
        fStoreList.setVisibility(View.GONE);

        //Dropdown for service categories
        //Services category - creating the dropdown
        final String[] SERVICE_CATEGORY = new String[] {
                "All categories",
                "Builder",
                "Carpenter",
                "Plumber",
                "Electrician",
                "Metal worker"
        };

        final ArrayAdapter<String> adapterServiceCategory = new ArrayAdapter<>(getApplicationContext(), R.layout.service_layout, R.id.tv_category, SERVICE_CATEGORY);

        dropdownServiceCategory.setAdapter(adapterServiceCategory);
        //Set category dropdown to all categories
        dropdownServiceCategory.setText("All categories", false);

        dropdownServiceCategory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                categorySelected = dropdownServiceCategory.getEditableText().toString();
                Log.d(TAG, categorySelected);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //Linear layout listener for home page fragment
        homeNavLayout = drawerLayout.findViewById(R.id.homeNavigation);
        homeNavLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        //Linear layout listener for messages page fragment
        messageNavLayout = drawerLayout.findViewById(R.id.messageNavigation);
        messageNavLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(HomeActivityStandard.this, MessageMenu.class);
                finish();
                intent.putExtra("USER_TYPE", "Standard");
                startActivity(intent);
            }
        });

        //Linear layout listener for jobs page fragment
        jobsNavLayout = drawerLayout.findViewById(R.id.jobsNavigation);
        jobsNavLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivityStandard.this, JobsActivity.class);
                finish();
                startActivity(intent);
            }
        });

        /**
         * end of onCreate
         */
    }

    public void getProUsersFromCurrentLocation(){
        double userLat = Double.valueOf(std_latitude);
        double userLon = Double.valueOf(std_longitude);

        //Temporary FireStore collection which stores professional service providers which are in the radius
        // or willing to travel accross whole Ireland in subcollection
        //under user's profile so that it can be used in a query that is used in FirestoreRecyclerOptions

        if(categorySelected.equals("All categories")){
            tempFireStore.collection("user").whereEqualTo("accountType", "Professional").get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()){
                                for (QueryDocumentSnapshot document : task.getResult()){
                                    GeoPoint gp = document.getGeoPoint("location");
                                    int radius = Integer.parseInt((String) document.get("radius"));

                                    double proLat = gp.getLatitude();
                                    double proLon = gp.getLongitude();
                                    double distance = distanceBetweenTwoCoordinates(userLat, userLon, proLat, proLon);

                                    if(distance <= radius || radius == 0){
                                        accounts.add(document.getId());
                                        pUsers.put("location", document.getGeoPoint("location"));
                                        pUsers.put("radius", document.getString("radius"));
                                        pUsers.put("username", document.getString("username"));
                                        pUsers.put("category", document.getString("category"));
                                        pUsers.put("distance", round(distance, 2));
                                        pUsers.put("user_id", document.getId());

                                        fStore.collection("user").document(UID)
                                                .collection("temp")
                                                .document(document.getId())
                                                .set(pUsers)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d(TAG, "Pro user successfully added");
                                                        pUsers.clear();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }

                                }
                            }
                        }
                    });
        }else{
            tempFireStore.collection("user").whereEqualTo("accountType", "Professional").whereEqualTo("category", categorySelected).get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()){
                                for (QueryDocumentSnapshot document : task.getResult()){
                                    GeoPoint gp = document.getGeoPoint("location");
                                    int radius = Integer.parseInt((String) document.get("radius"));

                                    double proLat = gp.getLatitude();
                                    double proLon = gp.getLongitude();
                                    double distance = distanceBetweenTwoCoordinates(userLat, userLon, proLat, proLon);

                                    if(distance <= radius || radius == 0){
                                        accounts.add(document.getId());
                                        pUsers.put("location", document.getGeoPoint("location"));
                                        pUsers.put("radius", document.getString("radius"));
                                        pUsers.put("username", document.getString("username"));
                                        pUsers.put("category", document.getString("category"));
                                        pUsers.put("distance", round(distance, 2));
                                        pUsers.put("user_id", document.getId());

                                        fStore.collection("user").document(UID)
                                                .collection("temp")
                                                .document(document.getId())
                                                .set(pUsers)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d(TAG, "Pro user successfully added");
                                                        pUsers.clear();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }

                                }
                            }
                        }
                    });
        }


    }

    public void clearTempCollection(){
        tempFireStore.collection("user").document(UID).collection("temp")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                        tempFireStore.collection("user").document(UID).collection("temp")
                                .document(queryDocumentSnapshot.getId())
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                    }
                                });
                    }
                }
            }
        });
    }

    public double distanceBetweenTwoCoordinates(double lat1, double lon1, double lat2, double lon2){
        //https://www.movable-type.co.uk/scripts/latlong.html
        //Used 'haversine' formula to calculate the distance between two coordinates

        int earthRadius = 6371;

        double dLat = degreesToRadians(lat2 - lat1);
        double dLon = degreesToRadians(lon2 - lon1);

        lat1 = degreesToRadians(lat1);
        lat2 = degreesToRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.sin(dLon / 2) * Math.sin(dLon / 2)
                * Math.cos(lat1) * Math.cos(lat2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadius * c;
    }

    public double degreesToRadians(double degrees){
        return degrees * (Math.PI / 180);
    }

    public void getCurrentLocation() {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            OnGPS();
        } else {
            getLocation();
        }

    }

    public void getLocationFromAddress(Context context, String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {

            }

            Address location = address.get(0);
            std_latitude = String.valueOf(location.getLatitude());
            std_longitude = String.valueOf(location.getLongitude());

        } catch (IOException ex) {

            ex.printStackTrace();
        }
    }

    private void OnGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes", new  DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void getLocation() {
        Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (locationGPS != null) {
            double lat = locationGPS.getLatitude();
            double lon = locationGPS.getLongitude();
            std_latitude = String.valueOf(lat);
            std_longitude = String.valueOf(lon);
        } else {
            Toast.makeText(this, "Unable to find location.", Toast.LENGTH_SHORT).show();
        }
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
                Intent intent = new Intent(HomeActivityStandard.this, MainActivity.class);
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

    public void ClickLogOut(View view) {
        logout();
    }

    public static double round(double value, int places) {
        //https://stackoverflow.com/questions/2808535/round-a-double-to-2-decimal-places
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private class ServicesViewHolder extends RecyclerView.ViewHolder{
        private TextView list_username;
        private TextView list_category;
        private TextView list_distance;
        private CircularImageView list_image;

        public ServicesViewHolder(@NonNull View itemView) {
            super(itemView);
            list_username = itemView.findViewById(R.id.tv_tradeListUsername);
            list_category = itemView.findViewById(R.id.tv_tradeListCategory);
            list_distance = itemView.findViewById(R.id.tv_tradeListDistance);
            list_image = itemView.findViewById(R.id.iv_tradeListImage);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        adapter.stopListening();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //Set title
        builder.setTitle("Close application");
        //Set message
        builder.setMessage("Do you want to close the application ?");
        //Yes button
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                finishAndRemoveTask();

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_LOCATION_PERMISSION_CODE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){

            }else{
                Toast.makeText(getApplicationContext(), "Location permission is denied. Please allow it in the Settings.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}