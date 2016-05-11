package com.beppeben.cook4.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;


public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ComponentName comp = new ComponentName(context.getApplicationContext(),
                GcmIntentService.class.getName());
        startWakefulService(context.getApplicationContext(), (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }
}