package com.example.hide_n_seek;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.location.LocationRequest;
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
import com.google.firebase.database.core.utilities.Tree;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public class Seeker extends AppCompatActivity implements OnMapReadyCallback {

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

        latLng = new LatLng(37.4219983,-122.084);

        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(Seeker.this);

        lobbyName = getIntent().getStringExtra("lobbyName");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Lobbies").child(lobbyName).child("Hider's Location");
        Log.d("seeker", "made it");

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
}
