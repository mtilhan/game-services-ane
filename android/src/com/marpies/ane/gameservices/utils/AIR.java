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

package com.marpies.ane.gameservices.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.games.Game;
import com.marpies.ane.gameservices.GameServicesExtensionContext;

import java.util.List;

public class AIR {

	private static final String TAG = "GameServices";
	private static boolean mLogEnabled = false;


	private static GameServicesExtensionContext mContext;

	public static void log( String message ) {
		if( mLogEnabled ) {
			Log.d( TAG, message );
		}
	}


	public static void dispatchEvent( String eventName ) {
		dispatchEvent( eventName, "" );
	}

	public static void dispatchEvent( String eventName, String message ) {
		mContext.dispatchStatusEventAsync( eventName, message );
	}

	public static void startActivity( Class<?> activityClass, Bundle extras ) {
		Intent intent = new Intent( mContext.getActivity().getApplicationContext(), activityClass );
		ResolveInfo info = mContext.getActivity().getPackageManager().resolveActivity( intent, 0 );
		if( info == null ) {
			log( "Activity " + activityClass.getSimpleName() + " could not be started. Make sure you specified the activity in the android manifest." );
			return;
		}
		if( extras != null ) {
			intent.putExtras( extras );
		}
		mContext.getActivity().startActivity( intent );
	}

	/**
	 *
	 *
	 * Getters / Setters
	 *
	 *
	 */

	public static GameServicesExtensionContext getContext() {
		return mContext;
	}
	public static void setContext( GameServicesExtensionContext context ) {
		mContext = context;
	}

	public static Boolean getLogEnabled() {
		return mLogEnabled;
	}
	public static void setLogEnabled( Boolean value ) {
		mLogEnabled = value;
	}
	
}
