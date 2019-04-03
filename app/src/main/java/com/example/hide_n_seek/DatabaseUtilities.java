package com.example.hide_n_seek;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.support.v4.content.ContextCompat.startActivity;

public class DatabaseUtilities {


    public static void createLobby(String lobbyName, String name){

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Lobbies").child(lobbyName).child("PlayerList").push().setValue(name);
    }

    public static void joinLobby(final String lobbyName, final String name){

        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Lobbies");
        boolean lobbyFound;
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.hasChild(lobbyName)) {
                    Log.d("lobby name here","here");
                    mDatabase.child(lobbyName).child("PlayerList").push().setValue(name);

                }else{
                    Log.d("lobby name not here","here");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public static void setFoundTrue(){

    }
    public void setFoundFalse(){

    }

}


