package com.beppeben.cook4.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.beppeben.cook4.MainActivity;
import com.beppeben.cook4.domain.C4User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.api.GoogleApiClient;

public class Globals {

    public static final int BIG_IMG_SIZE = 700;
    public static final int SMALL_IMG_SIZE = 300;
    public static final int HIGH_QUALITY = 50;
    public static final int LOW_QUALITY = 30;

    private static C4User me;
    public volatile static Boolean registered = false;
    public volatile static Boolean registering = false;
    public static float[] cookLevels;
    public static String[] cookLabels;
    public volatile static MainActivity mainActivity;
    public volatile static GoogleApiClient mGoogleApiClient;
    public volatile static boolean updateLocInfo = false;
    public static boolean splashshown;

    //get info on current user (as retrieved from server or previously saved)
    public static C4User getMe(Context context) {
        if (me == null) {
            me = new C4User();
            if (context != null) {
                initializeMe(context);
            }
        }
        return me;
    }

    public static void initializeMe(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = preferences.getString("user_object", null);
        if (json != null) {
            try {
                me = objectMapper.readValue(json, C4User.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (me != null) {
            me.setLatitude(null);
            me.setLongitude(null);
            me.setAddress(null);
            me.setCity(null);
        }
    }

    public static void saveMe(Context context) {
        saveMe(context, me);
    }

    public static void saveMe(Context context, C4User me) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = preferences.edit();
        ObjectMapper objectMapper = new ObjectMapper();
        String json = null;
        try {
            json = objectMapper.writeValueAsString(me);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        if (json != null) {
            prefsEditor.putString("user_object", json);
            prefsEditor.commit();
        }
    }

    public static String getLabel(float score) {
        if (cookLevels == null || cookLabels == null) return "no label";
        return StringUtils.getLabel(score, cookLevels, cookLabels);
    }

    public static void reset(Context ctx, boolean delete) {
        if (mainActivity != null) mainActivity.finish();
        if (delete) {
            me = new C4User();
            saveMe(ctx);
        }
        init(ctx);
    }

    public static void init(Context ctx) {
        SectionsPagerAdapter.clearFragments();
        registered = false;
        registering = false;
        cookLevels = null;
        cookLabels = null;
        mainActivity = null;
        splashshown = false;
    }

}
