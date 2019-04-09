package com.example.hide_n_seek;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Menu extends AppCompatActivity {

    private TextView mTextMessage;
    private String m_Text = "";
    String personName;


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
        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        personName = account.getDisplayName().substring(0,account.getDisplayName().indexOf(" "));
    }

    //code gotten from https://stackoverflow.com/questions/10903754/input-text-dialog-android
    public void createLobby(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create A Lobby Name");

        // Set up the input
        final EditText input = new EditText(this);
        final EditText input2 = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_Text = input.getText().toString();
                createLobbyHelper(m_Text,personName);
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


    public void createLobbyHelper(final String lobbyName, final String name){

        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Lobbies");
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //Check to see if that lobby exists
                if (snapshot.hasChild(lobbyName)) {
                }else{
                    //If it doesn't, it creates one with that name
                    mDatabase.child(lobbyName).child("PlayerList").push().setValue(name);
                    mDatabase.child(lobbyName).child("Hider").setValue(name);

                    //to make sure there is always a value in the Hider's location
                    LatLng latLng = new LatLng(37.4219983,-122.084);
                    mDatabase.child(lobbyName).child("Hider's Location").setValue(latLng);

                    mDatabase.child(lobbyName).child("Hider Found").setValue(false);

                    Intent locatorIntent = new Intent(Menu.this, Locator.class);
                    locatorIntent.putExtra("lobbyName", lobbyName);
                    startForegroundService(locatorIntent);


                    Intent hiderIntent = new Intent(Menu.this, DisplayOnMap.class);
                    hiderIntent.putExtra("lobbyName", lobbyName);
                    startActivity(hiderIntent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void joinLobby(View view){
        //code gotten from https://stackoverflow.com/questions/10903754/input-text-dialog-android
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
                m_Text = input.getText().toString();
                joinLobbyHelper(m_Text,personName);
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


    public void joinLobbyHelper(final String lobbyName, final String name){

        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Lobbies");
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.hasChild(lobbyName)) {
                    mDatabase.child(lobbyName).child("PlayerList").push().setValue(name);

                    Intent endGameServiceIntent = new Intent(Menu.this, EndGameService.class);
                    endGameServiceIntent.putExtra("lobbyName", lobbyName);
                    startForegroundService(endGameServiceIntent);

                    Intent intent = new Intent(Menu.this, DisplayOnMap.class);
                    intent.putExtra("lobbyName", lobbyName);
                    startActivity(intent);
                }else{
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void makeAlert(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("Please Select any option");
        dialog.setTitle("Dialog Box");
    }

}
