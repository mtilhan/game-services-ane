package com.marpies.ane.gameservices.leaderboards {

    public class GSLeaderboardScore {

        private var mUserName:String;
        private var mScore:int;
        private var mRank:int;

        public function GSLeaderboardScore( userName:String, score:int, rank:Number ) {
            mUserName = userName;
            mScore = score;
            mRank = rank;
        }

        public function toString():String {
            return "{GSLeaderboardScore userName: " + mUserName + " score: " + mScore + " rank: " + mRank + "}";
        }

        internal static function fromJSONArray( jsonArray:Array ):Vector.<GSLeaderboardScore> {
            var result:Vector.<GSLeaderboardScore> = new <GSLeaderboardScore>[];
            var length:int = jsonArray.length;
            for( var i:int = 0; i < length; ++i ) {
                result[i] = GSLeaderboardScore.fromJSON( jsonArray[i] );
            }
            return result;
        }

        private static function fromJSON( json:Object ):GSLeaderboardScore {
            if( json is String ) {
                json = JSON.parse( json as String );
            }

            var score:GSLeaderboardScore = new GSLeaderboardScore(
                    json.userName,
                    ("score" in json) ? json.score : 0,
                    ("rank" in json) ? json.rank : 0
            );

            return score;
        }


        public function get userName():String {
            return mUserName;
        }

        public function get score():int {
            return mScore;
        }

        public function get rank():int {
            return mRank;
        }

    }

}
