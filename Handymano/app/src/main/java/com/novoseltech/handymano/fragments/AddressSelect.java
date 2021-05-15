package com.novoseltech.handymano.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.textfield.TextInputLayout;
import com.novoseltech.handymano.BusinessRegistrationActivity;
import com.novoseltech.handymano.R;
import com.novoseltech.handymano.views.professional.ProfessionalProfileActivity;
import com.novoseltech.handymano.views.standard.job.CreateJob;
import com.novoseltech.handymano.views.standard.job.EditJob;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddressSelect#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddressSelect extends Fragment implements OnMapReadyCallback{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    //Layout components
    private Button btn_searchLocation;
    private Button btn_saveLocation;
    private Button btn_cancelLocation;
    private EditText et_address;
    private EditText et_radius;
    private TextInputLayout til_radius;
    private SupportMapFragment mapFragment;
    private String[] coordinates;
    private String mode;
    private String tmpRad;
    private Double tmpLat;
    private Double tmpLon;
    private FrameLayout fl_bra;
    private ConstraintLayout cl_bra;
    private FrameLayout fl_ppa;
    private ConstraintLayout cl_ppa;
    private FrameLayout fl_ej;
    private ConstraintLayout cl_ej;
    private Button btn_scej;
    private FrameLayout fl_cj;
    private ConstraintLayout cl_cj;

    public AddressSelect() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddressSelect.
     */
    // TODO: Rename and change types and number of parameters
    public static AddressSelect newInstance(String param1, String param2) {
        AddressSelect fragment = new AddressSelect();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_address_select, container, false);

        fl_bra = getActivity().findViewById(R.id.frame_location);
        cl_bra = getActivity().findViewById(R.id.cl_regFormBusiness);

        fl_ppa = getActivity().findViewById(R.id.frame_loc_pro);
        cl_ppa = getActivity().findViewById(R.id.cl_editProfileForm);

        fl_ej = getActivity().findViewById(R.id.fl_editJobAddress);
        cl_ej = getActivity().findViewById(R.id.cl_editJob);
        btn_scej = getActivity().findViewById(R.id.btn_saveJobChanges);

        fl_cj = getActivity().findViewById(R.id.fl_jobAddressLayout);
        cl_cj = getActivity().findViewById(R.id.cl_jobCreationLayout);

        btn_searchLocation = view.findViewById(R.id.btn_selectLocation);
        btn_saveLocation = view.findViewById(R.id.btn_afSaveLocation);
        btn_saveLocation.setVisibility(View.GONE);

        btn_cancelLocation = view.findViewById(R.id.btn_afCancel);
        btn_cancelLocation.setVisibility(View.GONE);

        et_address = view.findViewById(R.id.address_textInput);
        et_radius = view.findViewById(R.id.radius_textInput);
        til_radius = view.findViewById(R.id.radius_layout);

        //Setting default radius
        et_radius.setText("0");
        coordinates = new String[3];

        mode = getArguments().getString("mode");
        tmpRad = getArguments().getString("radius");
        tmpLat = getArguments().getDouble("lat");
        tmpLon = getArguments().getDouble("lon");


        mapFragment = (SupportMapFragment)this
                .getChildFragmentManager().findFragmentById(R.id.map_frag);

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {

                if(mode.equals("Edit")){
                    LatLng point = new LatLng(tmpLat, tmpLon);
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 7));
                    addMarkerToTheMap(point);

                    coordinates[0] = String.valueOf(tmpLat);
                    coordinates[1] = String.valueOf(tmpLon);
                    coordinates[2] = tmpRad;

                    btn_saveLocation.setVisibility(View.VISIBLE);
                    btn_cancelLocation.setVisibility(View.VISIBLE);

                }else if(mode.equals("NewJob")){

                    LatLng point = new LatLng(53.2734, -7.77832031);
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 7));
                    et_radius.setVisibility(View.GONE);

                    btn_saveLocation.setVisibility(View.GONE);
                    btn_cancelLocation.setVisibility(View.VISIBLE);



                }else if(mode.equals("JobEdit")){

                    LatLng point = new LatLng(tmpLat, tmpLon);
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 7));
                    addMarkerToTheMap(point);
                    coordinates[0] = String.valueOf(tmpLat);
                    coordinates[1] = String.valueOf(tmpLon);

                    btn_saveLocation.setVisibility(View.VISIBLE);
                    btn_cancelLocation.setVisibility(View.VISIBLE);
                    til_radius.setVisibility(View.GONE);

                }else{

                    LatLng point = new LatLng(53.2734, -7.77832031);
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 7));
                    coordinates[2] = "0";
                    btn_cancelLocation.setVisibility(View.VISIBLE);

                }

            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(mode.equals("Edit")){

            et_radius.setText(tmpRad);
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(getContext(), Locale.getDefault());

            try {
                addresses = geocoder.getFromLocation(tmpLat, tmpLon, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                //String city = addresses.get(0).getLocality();
                //String state = addresses.get(0).getAdminArea();
                //String country = addresses.get(0).getCountryName();
                //String postalCode = addresses.get(0).getPostalCode();
                //String knownName = addresses.get(0).getFeatureName();

                et_address.setText(address);
            } catch (IOException e) {
                e.printStackTrace();
            }



        }else if(mode.equals("JobEdit")){
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(getContext(), Locale.getDefault());

            try {
                addresses = geocoder.getFromLocation(tmpLat, tmpLon, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                et_address.setText(address);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        btn_searchLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String address = et_address.getText().toString();

                if(address.equals("")){
                    Toast.makeText(getContext(), "Location cannot be empty",
                            Toast.LENGTH_SHORT).show();
                }else{
                    LatLng point = getLocationFromAddress(getContext(), address);

                    //Check permission
                    if(ActivityCompat.checkSelfPermission(getContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        //If permission is granted

                        //Check if it is Ireland
                        String countryName = getCountryFromCoordinates(point.latitude, point.longitude);

                        if(countryName.equals("Ireland")){
                            addMarkerToTheMap(point);
                            coordinates[0] = String.valueOf(point.latitude);
                            coordinates[1] = String.valueOf(point.longitude);
                            //coordinates[2] = et_radius.getText().toString();

                            if(mode.equals("NewReg")){
                                btn_saveLocation.setVisibility(View.VISIBLE);
                            }else if(mode.equals("NewJob")){
                                btn_saveLocation.setVisibility(View.VISIBLE);
                            }


                        }else{
                            Toast.makeText(getContext(), "Location must be set in Ireland", Toast.LENGTH_SHORT).show();
                        }

                    }else{
                        //When permission is denied
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
                    }
                }


            }
        });

        btn_saveLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String radius = et_radius.getText().toString();
                if(mode.equals("NewReg")){
                    BusinessRegistrationActivity bra = (BusinessRegistrationActivity) getActivity();
                    if(radius == null){
                        Toast.makeText(getContext(), "Radius cannot be empty", Toast.LENGTH_SHORT).show();
                    }else{
                        fl_bra.setVisibility(View.GONE);
                        cl_bra.setVisibility(View.VISIBLE);
                        bra.setLocationData(coordinates[0], coordinates[1], radius);
                    }
                }else if(mode.equals("Edit")){
                    ProfessionalProfileActivity ppa = (ProfessionalProfileActivity) getActivity();

                    fl_ppa.setVisibility(View.GONE);
                    cl_ppa.setVisibility(View.VISIBLE);

                    ppa.setLocationData(coordinates[0], coordinates[1], radius);

                }else if(mode.equals("JobEdit")){
                    EditJob ej = (EditJob) getActivity();

                    fl_ej.setVisibility(View.GONE);
                    cl_ej.setVisibility(View.VISIBLE);
                    btn_scej.setVisibility(View.VISIBLE);
                    ej.setLocationData(coordinates[0], coordinates[1]);
                }else if(mode.equals("NewJob")){
                    CreateJob cj = (CreateJob) getActivity();

                    fl_cj.setVisibility(View.GONE);
                    cl_cj.setVisibility(View.VISIBLE);
                    cj.setLocationData(coordinates[0], coordinates[1]);
                }

            }
        });

        btn_cancelLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mode.equals("NewReg")){
                    fl_bra.setVisibility(View.GONE);
                    cl_bra.setVisibility(View.VISIBLE);
                }else if(mode.equals("Edit")){
                    fl_ppa.setVisibility(View.GONE);
                    cl_ppa.setVisibility(View.VISIBLE);
                }else if(mode.equals("JobEdit")){
                    fl_ej.setVisibility(View.GONE);
                    cl_ej.setVisibility(View.VISIBLE);
                    btn_scej.setVisibility(View.VISIBLE);
                }else if(mode.equals("NewJob")){
                    fl_cj.setVisibility(View.GONE);
                    cl_cj.setVisibility(View.VISIBLE);
                }

            }
        });

        et_radius.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(et_radius.getText().toString().equals("")){
                    et_radius.setText("0");
                }else if(Double.parseDouble(et_radius.getText().toString()) < 0.0){
                    et_radius.setText("0");
                }else{
                    String radius = et_radius.getText().toString();
                    coordinates[2] = radius;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });



    }



    public LatLng getLocationFromAddress(Context context, String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }

            Address location = address.get(0);

            p1 = new LatLng(location.getLatitude(), location.getLongitude() );

        } catch (IOException ex) {

            ex.printStackTrace();
        }

        return p1;
    }

    public void addMarkerToTheMap(LatLng point){

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                googleMap.clear();

                //Create marker
                MarkerOptions options = new MarkerOptions().position(point)
                        .title("Your location");
                //Zoom to location
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 10));

                //Add marker
                googleMap.addMarker(options);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        LatLng point = new LatLng(53.2734, -7.77832031);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 10));

    }

    public String getLocationData(int i){
        return coordinates[i];
    }

    private String getCountryFromCoordinates(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);

                strAdd = returnedAddress.getCountryName();
            } else {
                Log.d("My Current", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("My Current", "Cannot get Address!");
        }
        return strAdd;
    }
}