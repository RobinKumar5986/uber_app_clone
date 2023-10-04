package com.example.myapplication.Maps;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivityDriverMapBinding;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.myapplication.databinding.ActivityCusetomerMapBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class CustomerMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClients;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    Button callButton;
    private LatLng pickUpLocation;


private ActivityCusetomerMapBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

     binding = ActivityCusetomerMapBinding.inflate(getLayoutInflater());
     setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        callButton=findViewById(R.id.btnCall);
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Function for creating request for an uber
                String userId= Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                Toast.makeText(CustomerMapActivity.this, "Requesting is Clicked user "+userId, Toast.LENGTH_SHORT).show();
                DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Customer Requests");

                //creating the geo fire for the new geo fire location we can also create our own system
                GeoFire geoFire=new GeoFire(reference);
                geoFire.setLocation(userId,new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()));
                //creating marker for the pickUp location
                pickUpLocation=new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
                mMap.addMarker(new MarkerOptions().position(pickUpLocation).title("PickUp Here"));

                callButton.setText("Finding the Driver....");
            }
        });

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));//the smaller the number the camera will be closer in the Map or Ground value range from 1 to 21
    }
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
        mLocationRequest = new LocationRequest();//In every 1 second It request for the location...
        mLocationRequest.setInterval(1000); //calling in every 1 second for the location may be....
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);//the real time accuracy but the problem is that it takes lots of battery

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClients, mLocationRequest, this); //this is for requesting for updates

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        mLastLocation=location;
        LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));//the smaller the number the camera will be closer in the Map or Ground value range from 1 to 21

        //-----------Saving the Location to the Firebase Database----------//
        /*Database Model
            CustomerRequests:
                |----CustomerId:
                    |----Latitude : "   "
                    |----Longitude  : "   "
        */
        //--------Creating the firebase database reference--------//
//        String userId= Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
//        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Customer Request");
//
//        //-------Setting up the geofire reference--------//
//        GeoFire geoFire=new GeoFire(reference);
//        //under the userId the child the location is going to be stored
//        geoFire.setLocation(userId,new GeoLocation(location.getLatitude(),location.getLongitude()));

    }
    protected void onStop() {
        super.onStop();
        //--------Creating the firebase database reference--------//
//        String userId= Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
//        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Customer Request");
//
//        //-------Setting up the geofire reference for Logout function--------//
//        GeoFire geoFire=new GeoFire(reference);
//        //under the userId the child the location is going to be stored
//        geoFire.removeLocation(userId);
    }
}