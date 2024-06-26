/**
 * Copyright 2017 Marcel Piestansky (http://marpies.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.marpies.ane.gameservices.functions;

import android.content.Intent;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREObject;
import com.marpies.ane.gameservices.utils.AIR;
import com.marpies.ane.gameservices.utils.FREObjectUtils;
import com.marpies.ane.gameservices.utils.GameServicesHelper;
import com.marpies.ane.gameservices.utils.SignInActivity;

public class InitFunction extends BaseFunction {

	@Override
	public FREObject call( FREContext context, FREObject[] args ) {
		super.call( context, args );
		AIR.log( "GameServices::init new" );

		//if( GameServicesHelper.getInstance().isInitialized() ) return null;

		boolean showLogs = FREObjectUtils.getBoolean( args[0] );
		AIR.setLogEnabled( showLogs );
		AIR.log( "GameServices::init1" );
		Intent intent = new Intent(context.getActivity().getApplicationContext(), SignInActivity.class);
		intent.putExtra("shouldStartSignInFlow", false);
		try {
			context.getActivity().startActivity(intent);
	   	} catch (Exception e) {
			AIR.log( "GameServices::Error"+e.getMessage());
		}
		//GameServicesHelper.getInstance().init();

		return null;
	}

}
