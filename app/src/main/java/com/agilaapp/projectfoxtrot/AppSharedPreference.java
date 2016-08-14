package com.agilaapp.projectfoxtrot;

import android.content.Context;
import android.content.SharedPreferences;

public class AppSharedPreference {

    private static final String SHARED_PREFERENCE_KEY = "com.agilaapp.projectfoxtrot.SHARED_PREFERENCE_KEY";
    private static final String SEARCH_STATUS = "searchStatus";
    private static final String LAST_LOCATION_LAT = "lastLocationLat";
    private static final String LAST_LOCATION_LONG = "lastLocationLong";

    private static AppSharedPreference sInstance;
    private final SharedPreferences mPref;

    private AppSharedPreference(Context context) {
        mPref = context.getSharedPreferences(SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
    }

    public static synchronized AppSharedPreference getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new AppSharedPreference(context);
        }
        return sInstance;
    }

    public void setSearch(boolean isEnabled) {
        mPref.edit()
            .putBoolean(SEARCH_STATUS,isEnabled)
            .apply();
    }

    public boolean getSearch() {
        return mPref.getBoolean(SEARCH_STATUS,false);
    }

    public boolean isSearchNotEmpty() {
        return mPref.contains(SEARCH_STATUS);
    }

    public void setLastLocationLat(long lastLocationLat) {
        mPref.edit()
                .putLong(LAST_LOCATION_LAT,lastLocationLat)
                .apply();
    }

    public double getLastLocationLat() {
        return Double.longBitsToDouble(mPref.getLong(LAST_LOCATION_LAT,0));
    }

    public boolean isLastLocationLatNotEmpty() {
        return mPref.contains(LAST_LOCATION_LAT);
    }

    public void setLastLocationLong(long lastLocationLong) {
        mPref.edit()
                .putLong(LAST_LOCATION_LONG,lastLocationLong)
                .apply();
    }

    public boolean isLastLocationLongNotEmpty() {
        return mPref.contains(LAST_LOCATION_LONG);
    }

    public double getLastLocationLong() {
        return Double.longBitsToDouble(mPref.getLong(LAST_LOCATION_LONG,0));
    }

}
