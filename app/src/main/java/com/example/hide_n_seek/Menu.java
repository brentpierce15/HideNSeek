package com.example.hide_n_seek;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Menu extends AppCompatActivity {

    private String lobbyNameInput = "";
    String personName;
    private int locationRequestCode = 101;



    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        personName = account.getDisplayName().substring(0,account.getDisplayName().indexOf(" "));

        //requests location permissions if not already granted
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, locationRequestCode);
        }
    }

    //code gotten from https://stackoverflow.com/questions/10903754/input-text-dialog-android
    public void createLobby(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create A Lobby Name");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                lobbyNameInput = input.getText().toString();
                createLobbyHelper(lobbyNameInput,personName);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }


    private void createLobbyHelper(final String lobbyName, final String name){

        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Lobbies");
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //Check to see if that lobby exists
                if (snapshot.hasChild(lobbyName)) {
                    String message = "That lobby already exists. Please enter a different lobby name";
                    alertBuilder("Invalid Lobby Name", message);
                }else{//If it doesn't, it creates one with that name
                    mDatabase.child(lobbyName).child("PlayerList").push().setValue(name);
                    mDatabase.child(lobbyName).child("Hider").setValue(name);

                    //to make sure there is always a value in the Hider's location
                    LatLng latLng = new LatLng(37.4219983,-122.084);
                    mDatabase.child(lobbyName).child("Hider's Location").setValue(latLng);

                    mDatabase.child(lobbyName).child("Hider Found").setValue(false);

                    //foreground service to grab user's gps location and check to see if the game ended
                    Intent locatorIntent = new Intent(Menu.this, Locator.class);
                    locatorIntent.putExtra("lobbyName", lobbyName);
                    startForegroundService(locatorIntent);

                    //start the DisplayOnMap Activty
                    Intent displayMapIntent = new Intent(Menu.this, DisplayOnMap.class);
                    displayMapIntent.putExtra("lobbyName", lobbyName);
                    startActivity(displayMapIntent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //code gotten from https://stackoverflow.com/questions/10903754/input-text-dialog-android
    public void joinLobby(View view){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter an Existing Lobby Name");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                lobbyNameInput = input.getText().toString();
                joinLobbyHelper(lobbyNameInput,personName);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }


    private void joinLobbyHelper(final String lobbyName, final String name){

        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Lobbies");
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //checks to see if that lobby exists
                if (snapshot.hasChild(lobbyName)) {
                    mDatabase.child(lobbyName).child("PlayerList").push().setValue(name);

                    //runs foreground service that checks to see if that game has ended
                    Intent endGameServiceIntent = new Intent(Menu.this, EndGameService.class);
                    endGameServiceIntent.putExtra("lobbyName", lobbyName);
                    startForegroundService(endGameServiceIntent);

                    //start the DisplayOnMap Activty
                    Intent intent = new Intent(Menu.this, DisplayOnMap.class);
                    intent.putExtra("lobbyName", lobbyName);
                    startActivity(intent);

                }else{//If the lobby does not exist, it gives the user an alert saying so
                    String message = "That lobby does not exist. Please enter another lobby name";
                    alertBuilder("Invalid Lobby Name", message);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void alertBuilder(String title, String message){
        final AlertDialog.Builder dialog = new AlertDialog.Builder(Menu.this);
        dialog.setCancelable(false);
        dialog.setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

}
