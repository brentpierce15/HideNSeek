package com.example.hide_n_seek;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.firebase.database.FirebaseDatabase;

public class EndScreen extends AppCompatActivity {

   //private TextView mTextMessage;
    private TextView textView;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    //mTextMessage.setText(R.string.title_home);

                    Intent homeIntent = new Intent(EndScreen.this, Menu.class);
                    startActivity(homeIntent);

                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String winner = getIntent().getStringExtra("winner");
        setContentView(R.layout.activity_end_screen);

        //mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        textView = (TextView) findViewById(R.id.textView);
        textView.setText(winner + " Found the Hider!");
        textView.setTextSize(20);
    }

    private void deleteData(){

    }

}
