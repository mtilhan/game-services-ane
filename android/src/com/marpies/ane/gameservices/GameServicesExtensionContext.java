/**
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

package com.marpies.ane.gameservices;

import android.app.Activity;
import android.util.Log;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.marpies.ane.gameservices.events.GameServicesEvent;
import com.marpies.ane.gameservices.functions.*;
import com.marpies.ane.gameservices.functions.achievements.*;
import com.marpies.ane.gameservices.functions.leaderboards.LoadLeaderBoardFunction;
import com.marpies.ane.gameservices.functions.leaderboards.ReportScoreFunction;
import com.marpies.ane.gameservices.functions.leaderboards.ShowLeaderboardsUIFunction;
import com.marpies.ane.gameservices.utils.AIR;
import com.marpies.ane.gameservices.functions.IsSupportedFunction;
import com.marpies.ane.gameservices.utils.GameServicesHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import static com.marpies.ane.gameservices.utils.AIR.dispatchEvent;
import static com.marpies.ane.gameservices.utils.AIR.log;

public class GameServicesExtensionContext extends FREContext
		implements GameServicesHelper.GameServicesHelperListener {
	private static final String TAG = "GameServicesExtContext";
	public static GameServicesHelper mHelper;
	private List<Activity> _activityInstances;
	@Override
	public Map<String, FREFunction> getFunctions() {
		Map<String, FREFunction> functions = new HashMap<String, FREFunction>();

		functions.put( "init", new InitFunction() );
		functions.put( "auth", new AuthenticateFunction() );
		functions.put( "isSupported", new IsSupportedFunction() );
		functions.put( "isAuthenticated", new IsAuthenticatedFunction() );
		functions.put( "signOut", new SignOutFunction() );

		/* Achievements */
		functions.put( "unlockAchievement", new UnlockAchievementFunction() );
		functions.put( "setAchievementSteps", new SetAchievementStepsFunction() );
		functions.put( "incrementAchievement", new IncrementAchievementFunction() );
		functions.put( "setAchievementProgress", new SetAchievementProgressFunction() );
		functions.put( "showAchievementBanner", new ShowAchievementBannerFunction() );
		functions.put( "loadAchievements", new LoadAchievementsFunction() );
		functions.put( "showAchievementsUI", new ShowAchievementsUIFunction() );
		functions.put( "reportAchievements", new ReportAchievementsFunction() );
		functions.put( "resetAchievements", new ResetAchievementsFunction() );
		functions.put( "revealAchievement", new RevealAchievementFunction() );

        /* Leaderboards */
        functions.put( "reportScore", new ReportScoreFunction() );
        functions.put( "showLeaderboardsUI", new ShowLeaderboardsUIFunction() );
		functions.put( "loadLeaderboard", new LoadLeaderBoardFunction() );

		return functions;
	}

	@Override
	public void dispose() {
		AIR.setContext( null );
	}
	public void logEvent(String eventName)
	{
		Log.i(TAG, eventName);
	}
//	public void dispatchEvent(String eventName)
//	{
//		dispatchEvent(eventName, "OK");
//	}
//
//
//
//	public void dispatchEvent(String eventName, String eventData)
//	{
//		logEvent(eventName);
//		if (eventData == null)
//		{
//			eventData = "OK";
//		}
//		dispatchStatusEventAsync(eventName, eventData);
//	}

	public GameServicesHelper createHelperIfNeeded(Activity activity)
	{
		if (mHelper == null)
		{
			log("create helper");
			mHelper = new GameServicesHelper(activity, 0);
			log("setup");
			mHelper.setup(this);
		}
		return mHelper;
	}
	public void registerActivity(Activity activity)
	{
		if (_activityInstances == null)
		{
			_activityInstances = new ArrayList<Activity>();
		}
		_activityInstances.add(activity);
	}

	public void signOut()
	{
		logEvent("signOut");

		mHelper.signOut();
		//dispatchEvent("ON_SIGN_OUT_SUCCESS");
	}

	public Boolean isSignedIn()
	{
		Boolean val = mHelper.isSignedIn();
		logEvent("isSignedIn_"+val);
		return val;
	}

	@Override
	public void onSignInFailed() {
		logEvent("onSignInFailed");
		//dispatchEvent("ON_SIGN_IN_FAIL");
		if (_activityInstances != null)
		{
			for (Activity activity : _activityInstances)
			{
				if (activity != null)
				{
					activity.finish();
				}
			}
			_activityInstances = null;
		}
	}

	@Override
	public void onSignInSucceeded() {
		logEvent("onSignInSucceeded");
		//AIR.dispatchEvent( GameServicesEvent.AUTH_SUCCESS, "Auth Success" );
		//dispatchEvent("ON_SIGN_IN_SUCCESS");
		if (_activityInstances != null)
		{
			for (Activity activity : _activityInstances)
			{
				if (activity != null)
				{
					activity.finish();
				}
			}
			_activityInstances = null;
		}
	}
}
