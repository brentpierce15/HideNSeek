package com.example.hide_n_seek;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

/*
    A service that retrieves location data from the Hider. This needed to be a service as services
    can run in the background, unlike activities. That way a user could access other functions of
    their phone while playing.
 */
public class Locator extends Service {

    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private String lobbyName;
    private final int ONE_MINUTE = 1000*60;

    @Override
    public void onCreate(){
        super.onCreate();
        startMyOwnForeground();

    }

    public int onStartCommand(Intent intent, int a, int b){
        lobbyName = intent.getStringExtra("lobbyName");
        mFusedLocationClient  = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //the FusedLocationProviderClient requests user's location with certain parameters and the request will loop every minute
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        }

        //Checks to see if the game has ended
        final DatabaseReference mDatabaseHiderLocation = FirebaseDatabase.getInstance().getReference().child("Lobbies").child(lobbyName);
        mDatabaseHiderLocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //If this value turns to true, it means the hider has been found, and the game is over
                if(dataSnapshot.child("Hider Found").getValue().toString().equals("true")){

                    String winner = dataSnapshot.child("Winner").getValue().toString();
                    //https://stackoverflow.com/questions/25841544/how-to-finish-activity-from-service-class-in-android
                    //to end DisplayOnMap activity
                    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(Locator.this);
                    localBroadcastManager.sendBroadcast(new Intent("com.example.hide_n_seek.action.close"));

                    //stops location requests
                    mFusedLocationClient.removeLocationUpdates(mLocationCallback);

                    //moves to the end screen with some data from the match
                    Intent endGameIntent = new Intent(Locator.this, EndScreen.class);
                    endGameIntent.putExtra("lobbyName", lobbyName);
                    endGameIntent.putExtra("winner",winner);
                    startActivity(endGameIntent);

                    //stops service
                    stopForeground(true);
                    stopSelf();
                    //stops listener we are currently inside of
                    mDatabaseHiderLocation.removeEventListener(this);
                    //removes data from the match so it doesn't pile up in firebase
                    FirebaseDatabase.getInstance().getReference().child("Lobbies").child(lobbyName).removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return a;
    }


    //recieves the location results and uploads it to firebase
    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);

                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                //sets hider's location in firebase
                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Lobbies").child(lobbyName);
                mDatabase.child("Hider's Location").setValue(latLng);

            }
        }
    };

    //Location request that focuses on having a high accuracy return at expense of battery
    private void createLocationRequest(){
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(ONE_MINUTE); // one minute interval
        mLocationRequest.setFastestInterval(ONE_MINUTE);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //https://stackoverflow.com/questions/47531742/startforeground-fail-after-upgrade-to-android-8-1
    //creates a notification channel that is necessary when putting a service to the foreground
    private void startMyOwnForeground(){
        String NOTIFICATION_CHANNEL_ID = "com.example.hide_n_seek";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
        chan.setLightColor(Color.BLUE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("Hope the game is going well! :)")
                .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

}
