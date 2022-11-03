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

import android.widget.ImageView;
import com.adobe.fre.FREContext;
import com.adobe.fre.FREObject;
import com.google.android.gms.games.Games;
import com.marpies.ane.gameservices.functions.BaseFunction;
import com.marpies.ane.gameservices.utils.AIR;
import com.marpies.ane.gameservices.utils.FREObjectUtils;
import com.marpies.ane.gameservices.utils.GameServicesHelper;

import static com.marpies.ane.gameservices.GameServicesExtensionContext.mHelper;

public class ShowAchievementBannerFunction extends BaseFunction {

	@Override
	public FREObject call( FREContext context, FREObject[] args ) {
		super.call( context, args );

		AIR.getContext().createHelperIfNeeded(context.getActivity());
		if( AIR.getContext().isSignedIn() ) {
			boolean showBanner = FREObjectUtils.getBoolean( args[0] );
			AIR.log( "GameServices::showAchievementBanner - " + showBanner );
			if( showBanner ) {
				AIR.log( "Setting AIR activity decor view" );
				//Games.setViewForPopups( helper.getClient(), AIR.getContext().getActivity().getWindow().getDecorView() );
				Games.getGamesClient(AIR.getContext().getActivity(), mHelper.getmSignedInAccount()).setViewForPopups(AIR.getContext().getActivity().getWindow().getDecorView());
			} else {
				AIR.log( "Setting dummy view" );
				ImageView dummyView = new ImageView( AIR.getContext().getActivity() );
//				Games.setViewForPopups( helper.getClient(), dummyView );
				Games.getGamesClient(AIR.getContext().getActivity(), mHelper.getmSignedInAccount()).setViewForPopups(dummyView);

			}
		}

		return null;
	}

}

