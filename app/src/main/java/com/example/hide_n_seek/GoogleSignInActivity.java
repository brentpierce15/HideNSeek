package com.example.hide_n_seek;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.*;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.*;
import android.content.*;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import android.support.v7.app.*;
import java.util.HashMap;
import java.util.Map;
import static android.content.ContentValues.TAG;

/**
 * Taken from ACES App:
 * Created by Kyle Workman, Kevin Barbian, Megan Janssen, Tan Nguyen, Tyler May
 * References: https://www.youtube.com/watch?v=-ywVw2O1pP8
 *
 */

public class GoogleSignInActivity extends AppCompatActivity {

    final static int PERMISSION_ALL = 1;
    public static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    final static String[] PERMISSIONS = {android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION};  //Permissions for Location Services
    private SignInButton signInButton;
    private Button aboutPageButton;
    private GoogleApiClient googleApiClient;
    private static final int RC_SIGN_IN = 1;
    private FirebaseAuth mAuth;                                 // Authentication for Firebase database
    private static final String TAG = "Sign in Activity";
    private FirebaseAuth.AuthStateListener authStateListener;   //Checks when user state has changed
    private ProgressBar spinner;
    private TextView privacyView;
    public static final String KEY_UPDATE_REQUIRED = "force_update_required";
    public static final String KEY_CURRENT_VERSION = "force_update_current_version";
    public static final String KEY_UPDATE_URL = "force_update_store_url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.google_sign_in);
        authStateListener = new FirebaseAuth.AuthStateListener() {
            /**
             * Handles
             * users signing in, if the email is an Augustana College Email it signs them in and launches Google Maps activity
             * If the email is not an Augustana email address it displays a toast and does not sign them in
             *
             * @param firebaseAuth - User account attempting to sign in
             */
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null ) {
                    startActivity(new Intent(GoogleSignInActivity.this, Menu.class));
                } else if (firebaseAuth.getCurrentUser() != null) {
                    signInButton.setEnabled(true);
                    aboutPageButton.setEnabled(true);
                    spinner.setVisibility(View.GONE);
                    Auth.GoogleSignInApi.signOut(googleApiClient);
                    FirebaseAuth.getInstance().signOut();
                }

            }
        };
        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(authStateListener);
        setLayout();
        signInButton.setEnabled(true);
        aboutPageButton.setEnabled(true);
        privacyView.setEnabled(true);
        createNotificationChannel();
    }

    private void displayNoConnectionMsg() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false);
        dialog.setTitle("Connection Error")
                .setMessage("Please check your internet connection")
                .setCancelable(false)
                .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

    private void setLayout() {
        spinner = findViewById(R.id.ctrlActivityIndicator);
        spinner.setVisibility(View.GONE);
        signInButton = (SignInButton) findViewById(R.id.google_btn);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        signInButton.setEnabled(false);

        googleApiClient = new GoogleApiClient.Builder(getApplicationContext()).enableAutoManage(GoogleSignInActivity.this, new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                Toast.makeText(GoogleSignInActivity.this, "Error", Toast.LENGTH_LONG).show();
                signInButton.setEnabled(true);
                aboutPageButton.setEnabled(true);
                spinner.setVisibility(View.GONE);
            }
        }).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            /**
             * Handles the sign in when the user clicks the signInButton
             * Launches offline activity if ACES is offline
             * Signs user in if ACES is online
             */
            public void onClick(View view) {
                signInButton.setEnabled(false);
                aboutPageButton.setEnabled(false);
                spinner.setVisibility(View.VISIBLE);
                signIn();
            }
        });

        aboutPageButton = (Button) findViewById(R.id.about_btn);
        aboutPageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //startActivity(new Intent(GoogleSignInActivity.this, AboutPageActivity.class));
            }
        });
        aboutPageButton.setEnabled(false);

        privacyView = findViewById(R.id.privacyView);
        String text = privacyView.getText().toString();
        SpannableString content = new SpannableString(text);
        content.setSpan(new UnderlineSpan(), 0, text.length(), 0);
        privacyView.setText(content);
        privacyView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //startActivity(new Intent(Google_SignIn.this, PrivacyViewActivity.class));
            }
        });
        privacyView.setEnabled(false);
    }

    // Create the channel for push notifications
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Aces";
            String description = "To notify you of your ride's arrival.";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("default", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    protected void onStart() {
        super.onStart();
    }

    // Check to see if permission is granted--if it is then sign them in, otherwise it will need to be requested again
    private void signIn() {
        if (!isPermissionGranted()) {
            // Should we show an explanation?
            Log.d("w", "TEST");
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        } else {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }


    }

    // A reference we used to help with permissions, although we aren't using marshmallow: https://stackoverflow.com/questions/33666071/android-marshmallow-request-permission
    private boolean isPermissionGranted() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.v("mylog", "Permission is granted");
            return true;
        } else {
            Log.v("mylog", "Permission not granted");
            return false;
        }
    }

    /**
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                System.out.println("SIGN IN RESULT " + result.getStatus());
                if (result.getStatus().getStatusCode() == GoogleSignInStatusCodes.NETWORK_ERROR) {
                    displayNoConnectionMsg();
                }
                signInButton.setEnabled(true);
                aboutPageButton.setEnabled(true);
                spinner.setVisibility(View.GONE);
                //TODO: google sign in failed
            }
        }
    }
    //reference for where we learned to implement this: https://developers.google.com/identity/sign-in/android/

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:failed");
                            signInButton.setEnabled(true);
                            aboutPageButton.setEnabled(true);
                            spinner.setVisibility(View.GONE);
                            Toast.makeText(GoogleSignInActivity.this, "Authentication:Failed", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    public void onBackPressed() {
        moveTaskToBack(true);
    }

    /**
     * Stops code from continuing to run until user answers permission request
     *
     * @param requestCode  - ID for permission request
     * @param permissions  - string storing the permissions being granted
     * @param grantResults - int array of permission results
     *                     <p>
     *                     references: https://stackoverflow.com/questions/32714787/android-m-permissions-onrequestpermissionsresult-not-being-called
     *                     https://developer.android.com/reference/android/support/v4/app/ActivityCompat.OnRequestPermissionsResultCallback
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
}

