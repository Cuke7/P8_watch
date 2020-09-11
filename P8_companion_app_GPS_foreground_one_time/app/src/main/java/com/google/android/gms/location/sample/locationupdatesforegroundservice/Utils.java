/**
 * Copyright 2017 Google Inc. All Rights Reserved.
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

package com.google.android.gms.location.sample.locationupdatesforegroundservice;


import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.preference.PreferenceManager;

import androidx.annotation.RequiresApi;

import java.text.DateFormat;
import java.util.Date;

class Utils {

    static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_location_updates";

    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
    static boolean requestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
    }

    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state.
     */
    static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
                .apply();
    }

    /**
     * Returns the {@code location} object as a human readable string.
     * @param location  The {@link Location}.
     */
    static String getLocationText(Location location) {
        return location == null ? "Unknown location" : location.getLatitude() + "," + location.getLongitude();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    static String getLocationLog(Location location) {
        String response = "";

        if(location != null){
            response = response + location.getLatitude() + ", " + location.getLongitude() + ", " + DateFormat.getDateTimeInstance().format(location.getTime());
        }

        if(location.hasAccuracy()){
            response = response + ", " + location.getAccuracy();
        }else {
            response = response + ", "  + "-1";
        }

        if(location.hasAltitude()){
            response = response + ", " + location.getAltitude();
        }else {
            response = response + ", "  + "-1";
        }

        if(location.hasVerticalAccuracy()){
            response = response + ", " + location.getVerticalAccuracyMeters();
        }else {
            response = response + ", "  + "-1";
        }

        if(location.hasSpeed()){
            response = response + ", " + location.getSpeed();
        }else {
            response = response + ", "  + "-1";
        }

        if(location.hasSpeedAccuracy()){
            response = response + ", " + location.getSpeedAccuracyMetersPerSecond();
        }else {
            response = response + ", "  + "-1";
        }

        response = response + ", " + location.getExtras().getInt("satellites") ;

        return response;

        //return location == null ? "Unknown location" : location.getLatitude() + "," + location.getLongitude() + "," + DateFormat.getDateTimeInstance().format(new Date())+ "," + location.getAccuracy();
        //return location == null ? "Unknown location" : location.toString();
        //return location == null ? "Unknown location" : DateFormat.getDateTimeInstance().format(location.getTime());
    }


    static String getLocationTitle(Context context) {
        return context.getString(R.string.location_updated,
                DateFormat.getDateTimeInstance().format(new Date()));
    }
}
