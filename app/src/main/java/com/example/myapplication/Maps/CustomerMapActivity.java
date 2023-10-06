package com.example.myapplication.Maps;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivityCusetomerMapBinding;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class CustomerMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClients;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    //----Buttons-------//
    Button btnLogOut;
    Button callButton;
    private LatLng pickUpLocation;

    //--------------//
    static int radious=1; //radii for searching area for the car


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cusetomer_map);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map2);
        Objects.requireNonNull(mapFragment).getMapAsync(this);
        mapFragment.getMapAsync(this);
        //---------------------------//
        btnLogOut=findViewById(R.id.btnLogOut);
        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(CustomerMapActivity.this, "Sign Out Successfully", Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(CustomerMapActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        callButton=findViewById(R.id.btnCall);
        callButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                if(mLastLocation!=null) {
                    String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("CustomerRequests");
                    GeoFire geoFire = new GeoFire(reference);
                    geoFire.setLocation(userId, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));

                    //Creating A PickUp Location for the driver...
                    //setting a custom marker form the drawables
                    BitmapDescriptor customMarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.marker);

                    pickUpLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(pickUpLocation).title("Pick me Here").icon(customMarkerIcon));
                    //-----------------//


                    if(!isDriverFound) {
                        callButton.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                        callButton.setText("Finding The Driver..");
                        callButton.setClickable(false);
                    }
                    radious=1; // for second if the driver not founded for the first time;
                    getClosestDriver();

                }
            }
        });
    }//end of onCreate Method

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));//the smaller the number the camera will be closer in the Map or Ground value range from 1 to 21

//        //---------button for calling the uber-------//
//        binding.btnCall.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(CustomerMapActivity.this, "Button Clicked", Toast.LENGTH_SHORT).show();
////                //Function for creating request for an uber
////                String userId= Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
////                Toast.makeText(CustomerMapActivity.this, "Requesting is Clicked user "+userId, Toast.LENGTH_SHORT).show();
////                DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Customer Requests");
////
////                //creating the geo fire for the new geo fire location we can also create our own system
////                GeoFire geoFire=new GeoFire(reference);
////                geoFire.setLocation(userId,new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()));
////                //creating marker for the pickUp location
////                pickUpLocation=new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
////                mMap.addMarker(new MarkerOptions().position(pickUpLocation).title("PickUp Here"));
////                callButton.setText("Finding the Driver....");
//
////                getClosestDriver();
//            }
//        });
    }//ens of OnMapReady Function
    //this is a user defined function for Creating the API Client...
    protected synchronized  void buildGoogleApiClient(){
        //use for getting and setting the location
        mGoogleApiClients=new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClients.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(mGoogleApiClients.isConnected()){
            mLocationRequest = new LocationRequest();//In every 1 second It request for the location...
            mLocationRequest.setInterval(1000); //calling in every 1 second for the location may be....
            mLocationRequest.setFastestInterval(1000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);//the real time accuracy but the problem is that it takes lots of battery
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClients, mLocationRequest, this); //this is for requesting for updates
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection Failed...", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        mLastLocation=location;
        LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));//the smaller the number the camera will be closer in the Map or Ground value range from 1 to 21
    }


    //----------Creating a function for finding the closest driver in the area---------//
    private boolean isDriverFound=false;
    private String driverId;
    private void getClosestDriver() {
        DatabaseReference driverLocation=FirebaseDatabase.getInstance().getReference().child("DriversAvailable");
        GeoFire geoFire=new GeoFire(driverLocation);
        GeoQuery geoQuery=geoFire.queryAtLocation(new GeoLocation(pickUpLocation.latitude,pickUpLocation.longitude) , radious);//latitude and longitude is the center of the search location
        geoQuery.removeAllListeners(); //Just a prevention Measure So no issue can occur
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() { // In the function Key is the Id of the driver and location is the location under the key
            @SuppressLint("SetTextI18n")
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!isDriverFound) {
                    isDriverFound = true;
                    driverId=key;
                    Log.d("Founded","Yes founded "+key);
                    callButton.setText("Diver Founded");
                    callButton.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                    callButton.setClickable(true);
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(!isDriverFound){
                    if(radious<5) {
                        Log.d("increasing Radius by : ",radious+"");
                        radious+=1;
                        getClosestDriver();
                    }
                    else {
                        Toast.makeText(CustomerMapActivity.this, "No Driver Found", Toast.LENGTH_SHORT).show();
                        callButton.setText("Request Uber");
                        callButton.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                        callButton.setClickable(true);
//                        isDriverFound=true;
                    }

                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }// End of getClosestDriver Function...

}