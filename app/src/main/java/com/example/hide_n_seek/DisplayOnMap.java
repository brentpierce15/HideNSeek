package com.example.hide_n_seek;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
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
import java.util.Map;


public class DisplayOnMap extends AppCompatActivity
        implements OnMapReadyCallback {


    private static final String TAG = "Seekers: ";
    private GoogleMap mGoogleMap;
    private SupportMapFragment mapFrag;
    private String lobbyName;
    private DatabaseReference mDatabaseHiderLocation;
    private DatabaseReference mDatabaseHiderFound;
    private String hiderName;
    private LatLng latLng;
    private Marker mCurrLocationMarker;
    private ValueEventListener hiderListener;

    private String strDateFormat = "hh:mm:ss a";
    private DateFormat dateFormat = new SimpleDateFormat(strDateFormat);

    //so the locator service can end this activity
    //https://stackoverflow.com/questions/25841544/how-to-finish-activity-from-service-class-in-android
    LocalBroadcastManager mLocalBroadcastManager;
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("com.example.hide_n_seek.action.close")){
                mDatabaseHiderLocation.removeEventListener(hiderListener);
                mLocalBroadcastManager.unregisterReceiver(mBroadcastReceiver);
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        getSupportActionBar().setTitle("HideNSeek");

        lobbyName = getIntent().getStringExtra("lobbyName");
        mDatabaseHiderLocation = FirebaseDatabase.getInstance().getReference().child("Lobbies").child(lobbyName).child("Hider's Location");
        mDatabaseHiderFound = FirebaseDatabase.getInstance().getReference().child("Lobbies").child(lobbyName).child("Hider Found");
        DatabaseReference nameRef = FirebaseDatabase.getInstance().getReference().child("Lobbies").child(lobbyName).child("Hider");
        nameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                hiderName = dataSnapshot.getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(DisplayOnMap.this);

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("com.example.hide_n_seek.action.close");
        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, mIntentFilter);


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        mDatabaseHiderLocation.addValueEventListener(hiderListener = new ValueEventListener() {
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

                markerOptions.title(String.format(hiderName + "'s Location at " + formattedDate));
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    public void onClickFound(View view){
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        FirebaseDatabase.getInstance().getReference().child("Lobbies").child(lobbyName).child("Winner")
                .setValue(account.getDisplayName().substring(0,account.getDisplayName().indexOf(" ")));
        mDatabaseHiderFound.setValue(true);

    }

    protected void onDestroy() {
        super.onDestroy();
    }

}
