/*
 * Copyright 2017 Marcel Piestansky (http://marpies.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.marpies.ane.gameservices.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.AccelerateInterpolator;
//import android.support.annotation.NonNull;
import androidx.annotation.NonNull;
//import android.support.annotation.Nullable;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
//import com.adobe.air.AndroidActivityWrapper;
//import com.adobe.air.IAIRGSActivityResultCallback;
//import com.adobe.air.IAIRGSActivityStateCallback;
import com.adobe.air.AndroidActivityWrapper;
import com.adobe.air.IAIRGSActivityResultCallback;
import com.adobe.air.IAIRGSActivityStateCallback;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.AnnotatedData;
import com.google.android.gms.games.EventsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.games.event.Event;
import com.google.android.gms.games.event.EventBuffer;
import com.marpies.ane.gameservices.events.GameServicesEvent;
import org.json.JSONObject;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class GameServicesHelper /*implements
		IAIRGSActivityStateCallback, IAIRGSActivityResultCallback
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener*/ {

	private static final int AUTH_RESULT_CODE = 9283;
	private static final int ACHIEVEMENTS_UI_RESULT_CODE = 3948;
	private static final int LEADERBOARDS_UI_RESULT_CODE = 4820;
	public static final String TAG = "GameServicesHelper";

	/** Listener for sign-in success or failure events. */
	public interface GameServicesHelperListener {
		/**
		 * Called when sign-in fails. As a result, a "Sign-In" button can be
		 * shown to the user; when that button is clicked, call
		 *
		 * @link{GamesHelper#beginUserInitiatedSignIn . Note that not all calls
		 *                                            to this method mean an
		 *                                            error; it may be a result
		 *                                            of the fact that automatic
		 *                                            sign-in could not proceed
		 *                                            because user interaction
		 *                                            was required (consent
		 *                                            dialogs). So
		 *                                            implementations of this
		 *                                            method should NOT display
		 *                                            an error message unless a
		 *                                            call to @link{GamesHelper#
		 *                                            hasSignInError} indicates
		 *                                            that an error indeed
		 *                                            occurred.
		 */
		void onSignInFailed();

		/** Called when sign-in succeeds. */
		void onSignInSucceeded();
	}
	// configuration done?
	private boolean mSetupDone = false;
	// Listener
	GameServicesHelperListener mListener = null;

	// request codes we use when invoking an external activity
	private static final int RC_UNUSED = 5001;
	public static final int RC_SIGN_IN = 9001;
	//private static GameServicesHelper mInstance = new GameServicesHelper();
	private Handler mHandler;
	// Print debug logs?
	boolean mDebugLog = true;
	/**
	 * The Activity we are bound to. We need to keep a reference to the Activity
	 * because some games methods require an Activity (a Context won't do). We
	 * are careful not to leak these references: we release them on onStop().
	 */
	Activity mActivity = null;

	// app context
	Context mAppContext = null;
	// What clients were requested? (bit flags)
	int mRequestedClients = 0;
	// Whether to automatically try to sign in on onStart(). We only set this
	// to true when the sign-in process fails or the user explicitly signs out.
	// We set it back to false when the user initiates the sign in process.
	boolean mConnectOnStart = true;
	// are we currently connecting?
	private boolean mConnecting = false;
	// Are we expecting the result of a resolution flow?
	boolean mExpectingResolution = false;

	//private GoogleApiClient mGoogleApiClient;
	private GoogleSignInClient mGoogleSignInClient = null;
	private boolean mUserAuth;
	private boolean mPendingAchievementsUI;
	private boolean mPendingLeaderboardsUI;
	private String mPendingLeaderboardId;
	private ConnectionResult mPendingConnectionResult;
	GoogleSignInAccount mSignedInAccount = null;

	// Client variables

	private AchievementsClient mAchievementsClient;
	private LeaderboardsClient mLeaderboardsClient;
	//private EventsClient mEventsClient;
	private PlayersClient mPlayersClient;

	// The diplay name of the signed in user.
	private String mDisplayName = "";
	// The error that happened during sign-in.
	SignInFailureReason mSignInFailureReason = null;

//	private GameServicesHelper() {
//	}
	public GameServicesHelper(Activity activity, int clientsToUse) {
		mActivity = activity;
		mAppContext = activity.getApplicationContext();
		mRequestedClients = clientsToUse;
		mHandler = new Handler();
	}
	/**
	 * Performs setup on this GameHelper object. Call this from the onCreate()
	 * method of your Activity. This will create the clients and do a few other
	 * initialization tasks. Next, call @link{#onStart} from the onStart()
	 * method of your Activity.
	 *
	 * @param listener
	 *            The listener to be notified of sign-in events.
	 */
	public void setup(GameServicesHelperListener listener) {
		if (mSetupDone) {
			String error = "GameServicesHelper: you cannot call GameHelper.setup() more than once!";
			logError(error);
			throw new IllegalStateException(error);
		}
		mListener = listener;
		debugLog("Setup: requested clients: " + mRequestedClients);

		mGoogleSignInClient = GoogleSignIn.getClient(mActivity,
				new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).build());
		mUserAuth = false;
		mSetupDone = true;
	}
	public void startSignInIntent() {
		mActivity.startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
	}
	public void onStart(Activity act, boolean connectOnStart)
	{
		mActivity = act;
		mAppContext = act.getApplicationContext();

		mConnectOnStart = connectOnStart;

		debugLog("onStart");
		assertConfigured("onStart");
	}
	/** Call this method from your Activity's onStart(). */
	public void onResume(Activity act, boolean connectOnStart) {
		mActivity = act;
		mAppContext = act.getApplicationContext();

		mConnectOnStart = connectOnStart;

		debugLog("onResume");
		assertConfigured("onResume");

		signInSilently();

//		if (mConnectOnStart) {
//			if (isSignedIn()) {
//				Log.w(TAG,
//						"GameServicesHelper: client was already Signed In");
//			} else {
//				debugLog("Connecting client.");
//				mConnecting = true;
//				signInSilently();
//				//startSignInIntent();
//				//mGoogleApiClient.connect();
//			}
//		} else {
//			debugLog("Not attempting to connect becase mConnectOnStart=false");
//			debugLog("Instead, reporting a sign-in failure.");
////            mHandler.postDelayed( () -> { notifyListener(false);}, 1000 );
//		}
	}

	/** Call this method from your Activity's onStop(). */
	public void onStop() {
		debugLog("onStop");
		assertConfigured("onStop");
		if (isSignedIn()) {
			debugLog("Disconnecting client due to onStop");
			onDisconnected();
			//mGoogleApiClient.disconnect();
		} else {
			debugLog("Client already disconnected when we got onStop.");
		}
		mConnecting = false;
		mExpectingResolution = false;

		// let go of the Activity reference
		mActivity = null;
	}
	void assertConfigured(String operation) {
		if (!mSetupDone) {
			String error = "GameHelper error: Operation attempted without setup: "
					+ operation
					+ ". The setup() method must be called before attempting any other operation.";
			logError(error);
			throw new IllegalStateException(error);
		}
	}
	void debugLog(String message) {
		if (mDebugLog) {
			Log.d(TAG, "GameHelper: " + message);
		}
	}

	void logWarn(String message) {
		Log.w(TAG, "!!! GameHelper WARNING: " + message);
	}

	void logError(String message) {
		Log.e(TAG, "*** GameHelper ERROR: " + message);
	}

//	public static GameServicesHelper getInstance() {
//		return mInstance;
//	}

	public static boolean checkPlayServices( Context context ) {
		GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
		int resultCode = apiAvailability.isGooglePlayServicesAvailable( context );
		return resultCode == ConnectionResult.SUCCESS;
	}
	public boolean isSignedIn() {
		return GoogleSignIn.getLastSignedInAccount(mGoogleSignInClient.getApplicationContext()) != null;
	}


	public void signIn() {
		if( isInitialized() ) {
			startSignInIntent();
		}
	}
	public void signInSilently() {
		Log.d(TAG, "signInSilently()");

		mGoogleSignInClient.silentSignIn().addOnCompleteListener(mActivity,
				new OnCompleteListener<GoogleSignInAccount>() {
					@Override
					public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
						if (task.isSuccessful()) {
							Log.d(TAG, "signInSilently(): success");
							onConnected(task.getResult());
						} else {
							Log.d(TAG, "signInSilently(): failure", task.getException());
							onDisconnected();
						}
					}
				});
	}
	public void signOut() {
		Log.d(TAG, "signOut()");

		if (!isSignedIn()) {
			Log.w(TAG, "signOut() called, but was not signed in!");
			return;
		}

		mGoogleSignInClient.signOut().addOnCompleteListener(AIR.getContext().getActivity(),
				new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						boolean successful = task.isSuccessful();
						Log.d(TAG, "signOut(): " + (successful ? "success" : "failed"));

						onDisconnected();
					}
				});
	}

	/**
	 *
	 * Achievements
	 *
	 */

	public void showAchievementsUI() {
		if( isAuthenticated() ) {
			AIR.log( "GameServicesHelper | initialized and connected, showing UI" );
			AIR.dispatchEvent( GameServicesEvent.ACHIEVEMENT_UI_SHOW );
			mAchievementsClient.getAchievementsIntent()
					.addOnSuccessListener(new OnSuccessListener<Intent>() {
						@Override
						public void onSuccess(Intent intent) {
							AIR.getContext().getActivity().startActivityForResult(intent, ACHIEVEMENTS_UI_RESULT_CODE);
						}
					})
					.addOnFailureListener(new OnFailureListener() {
						@Override
						public void onFailure(@NonNull Exception e) {
							handleException(e, "There was an issue communicating with achievements");
							AIR.dispatchEvent( GameServicesEvent.ACHIEVEMENT_UI_ERROR, "There was an issue communicating with achievements" );
						}
					});
			//AIR.getContext().getActivity().startActivityForResult( Games.Achievements.getAchievementsIntent( mGoogleApiClient ), ACHIEVEMENTS_UI_RESULT_CODE );
		} else {
			AIR.log( "GameServicesHelper | not initialized or connected" );
			AIR.dispatchEvent( GameServicesEvent.ACHIEVEMENT_UI_ERROR, "User is not signed in." );
		}
	}

	public void dispatchAchievementUpdateError() {
		AIR.log( "Cannot update achievement(s), user is not signed in." );
		AIR.dispatchEvent( GameServicesEvent.ACHIEVEMENT_UPDATE_ERROR, "Cannot update achievement(s), user is not signed in." );
	}

    /**
     *
     * Leaderboards
     *
     */

	public void showLeaderboardsUI( String leaderboardId ) {
		if( isAuthenticated() ) {
			AIR.log( "GameServicesHelper | initialized and connected, showing UI" );
			mPendingLeaderboardId = leaderboardId;
			AIR.dispatchEvent( GameServicesEvent.LEADERBOARDS_UI_SHOW );
			mLeaderboardsClient.getLeaderboardIntent(mPendingLeaderboardId)
					.addOnSuccessListener(new OnSuccessListener<Intent>() {
						@Override
						public void onSuccess(Intent intent) {
							AIR.getContext().getActivity().startActivityForResult(intent, LEADERBOARDS_UI_RESULT_CODE);
						}
					})
					.addOnFailureListener(new OnFailureListener() {
						@Override
						public void onFailure(@NonNull Exception e) {
							AIR.dispatchEvent( GameServicesEvent.LEADERBOARDS_UI_ERROR, "There was an issue communicating with leaderboards." );
							handleException(e, "There was an issue communicating with leaderboards.");
						}
					});
			//AIR.getContext().getActivity().startActivityForResult( Games.Leaderboards.getLeaderboardIntent( mGoogleApiClient, leaderboardId ), LEADERBOARDS_UI_RESULT_CODE );
		} else {
			AIR.log( "GameServicesHelper | not initialized or connected" );
			AIR.dispatchEvent( GameServicesEvent.LEADERBOARDS_UI_ERROR, "User is not signed in." );
		}
	}

	/**
	 *
	 *
	 * Getters
	 *
	 *
	 */

//	public GoogleApiClient getClient() {
//		return mGoogleApiClient;
//	}
	public GoogleSignInClient getClient() {
		return mGoogleSignInClient;
	}
	public AchievementsClient getmAchievementsClient() { return mAchievementsClient;}
	public LeaderboardsClient getmLeaderboardsClient() { return mLeaderboardsClient;}
	public GoogleSignInAccount getmSignedInAccount() {return mSignedInAccount;}

	public boolean isInitialized() {
		//return mGoogleApiClient != null;
		return mGoogleSignInClient != null;
	}

	public boolean isAuthenticated() {
		//return isInitialized() && mGoogleApiClient.isConnected();
		return GoogleSignIn.getLastSignedInAccount(mGoogleSignInClient.getApplicationContext()) != null;
	}
	private void handleException(Exception e, String details) {
		int status = 0;

		if (e instanceof ApiException) {
			ApiException apiException = (ApiException) e;
			status = apiException.getStatusCode();
		}

		//String message = getString(R.string.status_exception_error, details, status, e);
		String message = "There was an issue with sign in.  Please try again later." + details + status + e;

		new AlertDialog.Builder(AIR.getContext().getActivity().getApplicationContext())
				.setMessage(message)
				.setNeutralButton("OK", null)
				.show();
	}

	/**
	 * AIR activity state / result
	 */

//	@Override
//	public void onActivityStateChanged( AndroidActivityWrapper.ActivityState activityState ) {
//		AIR.log( "GameServicesHelper | Activity state changed: " + activityState );
//		if( activityState == AndroidActivityWrapper.ActivityState.DESTROYED ) {
//			if( mGoogleApiClient.isConnected() ) {
//				mGoogleApiClient.disconnect();
//				mGoogleApiClient = null;
//			}
//			signOut();
//			AndroidActivityWrapper.GetAndroidActivityWrapper().removeActivityResultListener( this );
//			AndroidActivityWrapper.GetAndroidActivityWrapper().removeActivityStateChangeListner( this );
//		}
//		else if(activityState == AndroidActivityWrapper.ActivityState.RESUMED)
//		{
//			signInSilently();
//		}
//	}

//	@Override
//	public void onConfigurationChanged( Configuration configuration ) { }

	//@Override
	public void onActivityResult( int requestCode, int resultCode, Intent intent ) {
		AIR.log("GameServices::onActivityResult requestCode: "+ requestCode + " resultCode: " + resultCode );
		if( requestCode == RC_SIGN_IN )
		{
			mUserAuth = false;
			AIR.log( "GameServices::onActivityResult requestCode == AUTH_RESULT_CODE, is success: " + (resultCode == Activity.RESULT_OK) );
			handleUserSignIn( resultCode, intent );
		}
		else if( requestCode == ACHIEVEMENTS_UI_RESULT_CODE )
		{
			AIR.log( "GameServices::onActivityResult requestCode == ACHIEVEMENTS_UI_RESULT_CODE" );
			handleAchievementsUIRequest( resultCode );
		}
		else if( requestCode == LEADERBOARDS_UI_RESULT_CODE ) {
			AIR.log( "GameServices::onActivityResult requestCode == LEADERBOARDS_UI_RESULT_CODE" );
			handleLeaderboardsUIRequest( resultCode );
		}
	}
	private void handleUserSignIn( int resultCode , Intent intent) {
		if( resultCode == Activity.RESULT_OK ) {
			mUserAuth = false;
			Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(intent);
			try
			{
				GoogleSignInAccount account = task.getResult(ApiException.class);
				onConnected(account);
			}
			catch (ApiException apiException)
			{
				String message = apiException.getMessage();
				if (message == null || message.isEmpty())
				{
					message = "There was an issue with sign in.  Please try again later";
				}
				onDisconnected();
				AIR.log(message);
				AIR.dispatchEvent( GameServicesEvent.AUTH_ERROR, message );
//				new androidx.appcompat.app.AlertDialog.Builder(AIR.getContext().getActivity().getApplicationContext())
//						.setMessage(message)
//						.setNeutralButton(android.R.string.ok, null)
//						.show();
			}
		} else {
			String errorMessage = (resultCode == Activity.RESULT_CANCELED) ? "User has declined signing in." : "There was an error signing the user in.";
			AIR.log( errorMessage );
			AIR.dispatchEvent( GameServicesEvent.AUTH_ERROR, errorMessage );
		}
	}
	void notifyListener(boolean success) {
		debugLog("Notifying LISTENER of sign-in "
				+ (success ? "SUCCESS"
				: mSignInFailureReason != null ? "FAILURE ("+ mSignInFailureReason.toString() +")"
				: "FAILURE (no error)"));
		if (mListener != null) {
			if (success) {
				mListener.onSignInSucceeded();
			} else {
				mListener.onSignInFailed();
			}
		}
	}
	// Represents the reason for a sign-in failure
	public static class SignInFailureReason {
		public static final int NO_ACTIVITY_RESULT_CODE = -100;
		int mServiceErrorCode = 0;
		int mActivityResultCode = NO_ACTIVITY_RESULT_CODE;

		public int getServiceErrorCode() {
			return mServiceErrorCode;
		}

		public int getActivityResultCode() {
			return mActivityResultCode;
		}

		public SignInFailureReason(int serviceErrorCode, int activityResultCode) {
			mServiceErrorCode = serviceErrorCode;
			mActivityResultCode = activityResultCode;
		}

		public SignInFailureReason(int serviceErrorCode) {
			this(serviceErrorCode, NO_ACTIVITY_RESULT_CODE);
		}

		@Override
		public String toString() {
			return "SignInFailureReason(serviceErrorCode:"
					+ GameServicesHelperUtils.errorCodeToString(mServiceErrorCode)
					+ ((mActivityResultCode == NO_ACTIVITY_RESULT_CODE) ? ")"
					: (",activityResultCode:"
					+ GameServicesHelperUtils
					.activityResponseCodeToString(mActivityResultCode) + ")"));
		}
	}

	/**
	 *
	 *
	 * Private API
	 *
	 *
	 */
	public void onConnected(GoogleSignInAccount googleSignInAccount) {
		Log.d(TAG, "onConnected(): connected to Google APIs");
		mSignedInAccount = googleSignInAccount;

		mAchievementsClient = Games.getAchievementsClient(AIR.getContext().getActivity(), mSignedInAccount);
		mLeaderboardsClient = Games.getLeaderboardsClient(AIR.getContext().getActivity(), mSignedInAccount);
		//mEventsClient = Games.getEventsClient(AIR.getContext().getActivity(), mSignedInAccount);
		mPlayersClient = Games.getPlayersClient(AIR.getContext().getActivity(), mSignedInAccount);
		//AIR.dispatchEvent( GameServicesEvent.AUTH_SUCCESS, "Auth Success" );
		// Set the greeting appropriately on main menu
		mPlayersClient.getCurrentPlayer()
				.addOnCompleteListener(new OnCompleteListener<Player>() {
					@Override
					public void onComplete(@NonNull Task<Player> task) {
						String displayName;
						if (task.isSuccessful()) {
							AIR.log( "GameServicesHelper | user signed in" );
							displayName = task.getResult().getDisplayName();
							Player player = task.getResult();
							JSONObject response = GSPlayerUtils.getJSON(player);
							AIR.dispatchEvent( GameServicesEvent.AUTH_SUCCESS, response.toString() );
						} else {
							Exception e = task.getException();
							handleException(e, "There was an issue communicating with players.");
							displayName = "???";
						}
						mDisplayName = displayName;
						AIR.log("Display Name: " + mDisplayName);
					}
				});
		succeedSignIn();
	}
	void succeedSignIn() {
		debugLog("succeedSignIn");
		mSignInFailureReason = null;
		mConnectOnStart = true;
		//mUserInitiatedSignIn = false;
		mConnecting = false;
		//mConnectionResult = null;
		notifyListener(true);
	}
	public void onDisconnected() {
		Log.d(TAG, "onDisconnected()");

		mAchievementsClient = null;
		mLeaderboardsClient = null;
		mPlayersClient = null;
		notifyListener(false);
	}

	private void resolveSignInConnectionResult( @NonNull ConnectionResult connectionResult ) {
		AIR.log( "GameServicesHelper::resolveSignInConnectionResult ErrorCode:" + connectionResult.getErrorCode() );
		mUserAuth = false;
		mPendingConnectionResult = null;

		try {
			connectionResult.startResolutionForResult( AIR.getContext().getActivity(), AUTH_RESULT_CODE );
			AIR.dispatchEvent( GameServicesEvent.WILL_PRESENT_AUTH_DIALOG );
		} catch( IntentSender.SendIntentException e ) {
			e.printStackTrace();
			//mGoogleApiClient.connect();
		}
	}

	private void resolvePendingAchievementsUIError( @NonNull ConnectionResult connectionResult ) {
		AIR.log( "GameServicesHelper::resolveSignInConnectionResult ErrorCode:" + connectionResult.getErrorCode() );

		try {
			connectionResult.startResolutionForResult( AIR.getContext().getActivity(), AUTH_RESULT_CODE );
		} catch( IntentSender.SendIntentException e ) {
			e.printStackTrace();
			//mGoogleApiClient.connect();
		}
	}

	private void handleAchievementsUIRequest( int resultCode ) {
		/* UI was not shown, client in inconsistent state, needs reconnect */
		if( resultCode == GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED ) {
			AIR.log( "GameServices | client must be reconnected before showing achievements UI" );
			mPendingAchievementsUI = true;
			//mGoogleApiClient.reconnect();
		} else {
			AIR.dispatchEvent( GameServicesEvent.ACHIEVEMENT_UI_HIDE );
		}
	}

	private void handleLeaderboardsUIRequest( int resultCode ) {
		/* UI was not shown, client in inconsistent state, needs reconnect */
		if( resultCode == GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED ) {
			AIR.log( "GameServices | client must be reconnected before showing leaderboards UI" );
			mPendingLeaderboardsUI = true;
			//mGoogleApiClient.reconnect();
		} else {
			mPendingLeaderboardId = null;
			AIR.dispatchEvent( GameServicesEvent.LEADERBOARDS_UI_HIDE );
		}
	}



}
