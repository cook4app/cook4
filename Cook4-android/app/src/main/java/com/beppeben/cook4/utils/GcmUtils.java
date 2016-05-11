package com.beppeben.cook4.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.beppeben.cook4.R;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

public class GcmUtils {

    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final String LOG_TAG = GcmUtils.class.getName();

    public static boolean registerGCM(Context ctx) {
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(ctx);
        String regid = getRegistrationId(ctx);
        Globals.getMe(ctx).setRegid(regid);
        final String sender_id = ctx.getResources().getString(R.string.gcm_sender_id);
        if (regid.isEmpty()) {
            try {
                regid = gcm.register(sender_id);
                if (regid == null || regid.isEmpty()) return false;
                storeRegistrationId(ctx, regid);
                Globals.getMe(ctx).setRegid(regid);
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }

    private static String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGcmPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(LOG_TAG, "Registration id to GCM not found.");
            return "";
        }

        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(LOG_TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private static SharedPreferences getGcmPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    private static void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGcmPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(LOG_TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }


}
