package com.example.hide_n_seek;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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


public class Locator extends Service {

    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private String lobbyName;

    private String strDateFormat = "hh:mm:ss a";
    private DateFormat dateFormat = new SimpleDateFormat(strDateFormat);

    private int locationRequestCode = 1000;
    private final int ONE_MINUTE = 1000*10;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate();

        Intent notificationIntent = new Intent(this, Locator.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("My Awesome App")
                .setContentText("Doing some work...")
                .setContentIntent(pendingIntent).build();

        startForeground(1337, notification);


        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Lobbies").child(lobbyName);
        LatLng latLng = new LatLng(37.4219983,-122.084);
        mDatabase.child("Hider's Location").setValue(latLng);
        Log.d("here","made a dummy ");

        mFusedLocationClient  = LocationServices.getFusedLocationProviderClient(this);
        lobbyName = getIntent().getStringExtra("lobbyName");

        createLocationRequest();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //Location Permission already granted
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        } else {
            //Request Location Permission
            checkLocationPermission();
        }
    }


    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);

                //Place current location marker
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                //move map camera

                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Lobbies").child(lobbyName);
                mDatabase.child("Hider's Location").setValue(latLng);

            }
        }
    };

    private void createLocationRequest(){
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(ONE_MINUTE); // one minute interval
        mLocationRequest.setFastestInterval(ONE_MINUTE);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void checkLocationPermission(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, locationRequestCode);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
