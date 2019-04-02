package com.example.hide_n_seek;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.TreeMap;

public class RealTimeFirebase extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    public static void publishGeoLocation(LatLng latLng){
        //FirebaseDatabase database = FirebaseDatabase.getInstance();
        //creates node
        //DatabaseReference myRef = database.getReference("User's Location2:");
        //makes branches on tat node
        //myRef.push().setValue(latLng);

        DatabaseReference  database = FirebaseDatabase.getInstance().getReference();
        database.child("User's Location2:").setValue(latLng);

    }

}
