package com.marpies.ane.gameservices.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.EventsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

public class SignInActivity extends Activity implements GameServicesHelper.GameServicesHelperListener
{
    private static final String TAG = "SignInActivity";
    private boolean shouldStartSignInFlow;
    private GameServicesHelper mHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        shouldStartSignInFlow = true;
        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            Log.d(TAG, "hasExtra");
            shouldStartSignInFlow = extras.getBoolean("shouldStartSignInFlow"); // this will prevent prompting the UI at launch if the user hasn't register yet with Google Play
            Log.d(TAG, "shouldStartSignInFlow : " +Boolean.toString(shouldStartSignInFlow));
        }
        Log.d(TAG, "shouldStartSignInFlow2 : " +Boolean.toString(shouldStartSignInFlow));
        mHelper = AIR.getContext().createHelperIfNeeded(this);

        AIR.getContext().registerActivity(this);
        // Create the client used to sign in to Google services.
//        mGoogleSignInClient = GoogleSignIn.getClient(this,
//                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).build());

    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");

        // Since the state of the signed in user can change when the activity is not active
        // it is recommended to try and sign in silently from when the app resumes.
        //signInSilently();
        //GameServicesHelper.getInstance().signInSilently();
        mHelper.onResume(this, !shouldStartSignInFlow);
    }
    @Override
    protected void onStart()
    {
        super.onStart();
        Log.d(TAG, "onStart()");
        Log.d(TAG, "autosignIn");
        //mHelper.signInSilently();
        //mHelper.onStart(this, !shouldStartSignInFlow);
        mHelper.onStart(this, !shouldStartSignInFlow);
        if (shouldStartSignInFlow)
        {
            Log.d(TAG, "signIn");
            mHelper.startSignInIntent();
            //mHelper.beginUserInitiatedSignIn();
        }

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        mHelper.onActivityResult(requestCode, resultCode, intent);
        finish();
    }

    @Override
    public void onSignInFailed() {
        AIR.getContext().onSignInFailed();
    }

    @Override
    public void onSignInSucceeded() {
        AIR.getContext().onSignInSucceeded();
    }

}
