package com.marpies.ane.gameservices.functions.leaderboards;

import static com.marpies.ane.gameservices.GameServicesExtensionContext.mHelper;

import androidx.annotation.NonNull;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREObject;
import com.google.android.gms.games.AnnotatedData;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardScoreBuffer;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.marpies.ane.gameservices.events.GameServicesEvent;
import com.marpies.ane.gameservices.functions.BaseFunction;
import com.marpies.ane.gameservices.utils.AIR;
import com.marpies.ane.gameservices.utils.FREObjectUtils;
import com.marpies.ane.gameservices.utils.GSAchievementUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class LoadLeaderBoardFunction  extends BaseFunction {
    @Override
    public FREObject call(FREContext context, FREObject[] args) {
        super.call(context, args);

        AIR.log( "GameServices::loadLeaderBoard" );

        String leaderboardId = (args[0] == null) ? null : FREObjectUtils.getString( args[0] );
        Integer timeSpan = (args[1] == null) ? LeaderboardVariant.TIME_SPAN_ALL_TIME : FREObjectUtils.getInt( args[1] );
        Integer collection = (args[2] == null) ? LeaderboardVariant.COLLECTION_PUBLIC : FREObjectUtils.getInt( args[2] );
        Integer maxResult = (args[3] == null) ? 25 : FREObjectUtils.getInt( args[3] );



        if( leaderboardId == null ) {
            AIR.dispatchEvent( GameServicesEvent.LEADERBOARDS_UI_ERROR, "The leaderboard id must be set on Android." );
            return null;
        }

        AIR.log( "GameServices::leaID=>"+leaderboardId);


        mHelper.getmLeaderboardsClient().loadTopScores(leaderboardId, timeSpan, collection, maxResult)
                .addOnSuccessListener(new OnSuccessListener<AnnotatedData<LeaderboardsClient.LeaderboardScores>>() {
                    @Override
                    public void onSuccess(AnnotatedData<LeaderboardsClient.LeaderboardScores> leaderboardScoresAnnotatedData) {

                        AIR.log("LOADED!!");
                        JSONArray result = new JSONArray();

                        LeaderboardScoreBuffer scoreBuffer = leaderboardScoresAnnotatedData.get().getScores();
                        Iterator<LeaderboardScore> it = scoreBuffer.iterator();

                        AIR.log("scorecount:"+scoreBuffer.getCount());
                        while(it.hasNext()){
                            LeaderboardScore temp = it.next();
                            //AIR.log("player"+temp.getScoreHolderDisplayName()+" id:"+temp.getRawScore() + " Rank: "+temp.getRank());
                            JSONObject json = new JSONObject();

                            try
                            {
                                json.put( "userName", temp.getScoreHolderDisplayName() );
                                json.put( "score", temp.getRawScore() );
                                json.put( "rank", temp.getRank() );
                                result.put( json.toString() );
                            } catch( JSONException e )
                            {
                                AIR.dispatchEvent( GameServicesEvent.LEADERBOARDS_UI_ERROR, e.getMessage() );
                            }

                        }

                        JSONObject response = new JSONObject();
                        try
                        {
                            response.put( "scores", result );
                            AIR.log(response.toString());
                            AIR.dispatchEvent( GameServicesEvent.LEADERBOARD_LOAD_SUCCESS, response.toString() );
                        } catch( JSONException e )
                        {
                            AIR.dispatchEvent( GameServicesEvent.LEADERBOARDS_UI_ERROR, e.getMessage() );
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        AIR.dispatchEvent( GameServicesEvent.LEADERBOARDS_UI_ERROR, "There was an issue communicating with leaderboards." );
                    }
                });
        //mHelper.showLeaderboardsUI( leaderboardId );
        return null;

    }
}
