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

package com.marpies.ane.gameservices.functions.achievements;

//import android.support.annotation.NonNull;
import androidx.annotation.NonNull;
import com.adobe.fre.FREContext;
import com.adobe.fre.FREObject;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.achievement.Achievements;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.marpies.ane.gameservices.events.GameServicesEvent;
import com.marpies.ane.gameservices.functions.BaseFunction;
import com.marpies.ane.gameservices.utils.AIR;
import com.marpies.ane.gameservices.utils.FREObjectUtils;
import com.marpies.ane.gameservices.utils.GameServicesHelper;

import java.util.concurrent.TimeUnit;

import static com.marpies.ane.gameservices.GameServicesExtensionContext.mHelper;

public class IncrementAchievementFunction extends BaseFunction /*implements ResultCallback<Achievements.UpdateAchievementResult>*/ {

	@Override
	public FREObject call( FREContext context, FREObject[] args ) {
		super.call( context, args );

		AIR.log( "GameServices::incrementAchievement" );
		String achievementId = FREObjectUtils.getString( args[0] );
		int numSteps = FREObjectUtils.getInt( args[1] );
		boolean immediate = FREObjectUtils.getBoolean( args[2] );

		AIR.getContext().createHelperIfNeeded(context.getActivity());
		if( AIR.getContext().isSignedIn() ) {
			if( immediate ) {
//				PendingResult<Achievements.UpdateAchievementResult> result = Games.Achievements.incrementImmediate( helper.getClient(), achievementId, numSteps );
//				result.setResultCallback( this, 10, TimeUnit.SECONDS );
				mHelper.getmAchievementsClient().incrementImmediate(achievementId, numSteps)
						.addOnCompleteListener(new OnCompleteListener<Boolean>() {
							@Override
							public void onComplete(@NonNull Task<Boolean> task) {
								if(task.isSuccessful())
								{
									AIR.log( "Successfully incremented achievement steps" );
									AIR.dispatchEvent( GameServicesEvent.ACHIEVEMENT_UPDATE_SUCCESS );
								}
								else
								{
									AIR.log( "Failed to increment achievement: " + task.getException().toString() );
									AIR.dispatchEvent( GameServicesEvent.ACHIEVEMENT_UPDATE_ERROR, task.getException().toString() );
								}
								}
							});
				return null;
			}

//			Games.Achievements.increment( helper.getClient(), achievementId, numSteps );
			mHelper.getmAchievementsClient().increment(achievementId, numSteps);
			AIR.log( "Successfully incremented steps for achievement: " + achievementId );
			AIR.dispatchEvent( GameServicesEvent.ACHIEVEMENT_UPDATE_SUCCESS );
		} else {
			mHelper.dispatchAchievementUpdateError();
		}

		return null;
	}

//	@Override
//	public void onResult( @NonNull Achievements.UpdateAchievementResult result ) {
//		com.google.android.gms.common.api.Status status = result.getStatus();
//
//		if( !status.isSuccess() ) {
//			AIR.log( "Failed to increment achievement: " + status.getStatusMessage() );
//			AIR.dispatchEvent( GameServicesEvent.ACHIEVEMENT_UPDATE_ERROR, status.getStatusMessage() );
//			return;
//		}
//
//		AIR.log( "Successfully incremented achievement steps" );
//		AIR.dispatchEvent( GameServicesEvent.ACHIEVEMENT_UPDATE_SUCCESS );
//	}
}

