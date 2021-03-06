package com.example.hide_n_seek;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EndGameService extends Service {

    private String lobbyName;
    private String seekerName;

    @Override
    public void onCreate(){
        super.onCreate();
        startMyOwnForeground();

    }

    public int onStartCommand(Intent intent, int a, int b){
        lobbyName = intent.getStringExtra("lobbyName");
        seekerName = intent.getStringExtra("seekerName");

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
                    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(EndGameService.this);
                    localBroadcastManager.sendBroadcast(new Intent("com.example.hide_n_seek.action.close"));

                    //moves to the end screen with some data from the match
                    Intent endGameIntent = new Intent(EndGameService.this, EndScreen.class);
                    endGameIntent.putExtra("lobbyName", lobbyName);
                    endGameIntent.putExtra("winner",winner);
                    startActivity(endGameIntent);

                    //stops service
                    stopForeground(true);
                    stopSelf();
                    //stops listener we are currently inside of
                    mDatabaseHiderLocation.removeEventListener(this);
                    FirebaseDatabase.getInstance().getReference().child("Lobbies").child(lobbyName).removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return a;
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
        startForeground(3, notification);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
