package com.infobox.hasnat.ume.ume.Utils;

import android.app.Application;
import android.content.Context;

public class UserLastSeenTime extends Application{

    /*
     * Copyright 2012 Google Inc.
     *
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     *
     *      http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;


    public static String getTimeAgo(long time, Context applicationContext) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }


        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {

            return "Active just now";
            //return null;
        }

        // TODO: localize
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "Active few seconds ago";

        } else if (diff < 2 * MINUTE_MILLIS) {
            return "Active a minute ago";

        } else if (diff < 50 * MINUTE_MILLIS) {
            return "Active " + diff / MINUTE_MILLIS + " minutes ago";

        } else if (diff < 90 * MINUTE_MILLIS) {
            return "Active an hour ago";

        } else if (diff < 24 * HOUR_MILLIS) {
            return "Active " + diff / HOUR_MILLIS + " hours ago";

        } else if (diff < 48 * HOUR_MILLIS) {
            return "Active on yesterday";

        } else {
            return "Active " + diff / DAY_MILLIS + " days ago";
        }
    }

}
