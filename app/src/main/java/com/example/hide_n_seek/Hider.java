package com.example.hide_n_seek;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class Hider extends AppCompatActivity
        implements OnMapReadyCallback {

//    private GoogleMap mGoogleMap;
//    private SupportMapFragment mapFrag;
//    private LocationRequest mLocationRequest;
//    private Location mLastLocation;
//    private Marker mCurrLocationMarker;
//    private FusedLocationProviderClient mFusedLocationClient;
//    private String lobbyName;
//
//    private String strDateFormat = "hh:mm:ss a";
//    private DateFormat dateFormat = new SimpleDateFormat(strDateFormat);
//
//    private int locationRequestCode = 1000;
//    private final int ONE_MINUTE = 1000*10;

    private static final String TAG = "Seekers: ";
    private GoogleMap mGoogleMap;
    private SupportMapFragment mapFrag;
    private String lobbyName;
    private DatabaseReference mDatabase;
    private LatLng latLng;
    private Marker mCurrLocationMarker;

    private String strDateFormat = "hh:mm:ss a";
    private DateFormat dateFormat = new SimpleDateFormat(strDateFormat);

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        getSupportActionBar().setTitle("HideNSeek");

        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(Hider.this);

        lobbyName = getIntent().getStringExtra("lobbyName");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Lobbies").child(lobbyName).child("Hider's Location");

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Double> mapOfLatLng = (Map<String, Double>) dataSnapshot.getValue();
                latLng = new LatLng(mapOfLatLng.get("latitude"), mapOfLatLng.get("longitude"));

                Date date = new Date();
                String formattedDate= dateFormat.format(date);

                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker.remove();
                }
                MarkerOptions markerOptions = new MarkerOptions().position(latLng);

                markerOptions.title(String.format("Player's Location at " + formattedDate ));
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_maps);
//        getSupportActionBar().setTitle("HideNSeek");
//
//        mFusedLocationClient  = LocationServices.getFusedLocationProviderClient(this);
//        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
//        mapFrag.getMapAsync(Hider.this);
//        lobbyName = getIntent().getStringExtra("lobbyName");
//
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        createLocationRequest();
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            //Location Permission already granted
//            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
//        } else {
//            //Request Location Permission
//            checkLocationPermission();
//        }
//    }
//
//
//
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        mGoogleMap = googleMap;
//        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
//
//        createLocationRequest();
//
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            //Location Permission already granted
//            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
//            mGoogleMap.setMyLocationEnabled(true);
//        } else {
//            //Request Location Permission
//           checkLocationPermission();
//        }
//
//    }
//
//
//    LocationCallback mLocationCallback = new LocationCallback() {
//        @Override
//        public void onLocationResult(LocationResult locationResult) {
//            List<Location> locationList = locationResult.getLocations();
//            if (locationList.size() > 0) {
//                //The last location in the list is the newest
//                Location location = locationList.get(locationList.size() - 1);
//                mLastLocation = location;
//                if (mCurrLocationMarker != null) {
//                    mCurrLocationMarker.remove();
//                }
//
//                //Place current location marker
//                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//                MarkerOptions markerOptions = new MarkerOptions();
//                markerOptions.position(latLng);
//                Date date = new Date();
//                String formattedDate= dateFormat.format(date);
//                markerOptions.title(String.format("Player's Location at " + formattedDate ));
//                Log.i("Time", "Player's Location at time: " + formattedDate);
//                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
//                mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);
//                //move map camera
//                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
//
//                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Lobbies").child(lobbyName);
//                mDatabase.child("Hider's Location").setValue(latLng);
//
//            }
//        }
//    };
//
//    private void createLocationRequest(){
//        mLocationRequest = new LocationRequest();
//        mLocationRequest.setInterval(ONE_MINUTE); // one minute interval
//        mLocationRequest.setFastestInterval(ONE_MINUTE);
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//    }
//
//    private void checkLocationPermission(){
//        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, locationRequestCode);
//    }


}
