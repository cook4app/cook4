package com.beppeben.cook4;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.beppeben.cook4.domain.C4User;
import com.beppeben.cook4.utils.GcmUtils;
import com.beppeben.cook4.utils.Globals;
import com.beppeben.cook4.utils.LocationUtils;
import com.beppeben.cook4.utils.LogsToServer;
import com.beppeben.cook4.utils.net.HttpContext;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Locale;

public class LoginFragment extends Fragment {

    public interface TaskCallbacks {
        void onPreExecute();
        void onProgressUpdate(int percent);
        void onCancelled();
        void onPostExecute(Object[] obj);
    }

    private static final String LOG_TAG = LoginFragment.class.getName();

    private TaskCallbacks mCallbacks;
    private LoginTask mTask;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor prefsEditor;
    private C4User me;
    private Context ctx;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (TaskCallbacks) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        me = Globals.getMe(getActivity());
        // Create and execute the background task.
        mTask = new LoginTask();
        mTask.execute();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    private class LoginTask extends AsyncTask<Void, Integer, Object[]> {

        @Override
        protected void onPreExecute() {
            if (mCallbacks != null) {
                mCallbacks.onPreExecute();
                ctx = getActivity().getApplicationContext();
                sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
                prefsEditor = sharedPref.edit();
            }
        }

        @Override
        protected Object[] doInBackground(Void... ignore) {
            if (!GcmUtils.registerGCM(ctx)) return null;
            if (!isAdded()) return null;

            HttpContext context = HttpContext.getInstance();
            HttpHeaders headers = context.getDefaultHeaders();
            headers.remove("Token");
            String loginMethod = me.getLoginMethod();
            String token = null;
            if (loginMethod.equals("google")) {
                token = getGoogleToken();
                if (token == null)
                    return new Object[]{"ERROR_GOOGLE", null};
            } else if (loginMethod.equals("facebook")) {
                token = sharedPref.getString("fb_token", null);
                if (token == null)
                    return new Object[]{"ERROR_FB", null};
            }
            if (token != null) headers.add("Token", token);

            LocationUtils.getLocationInfo(ctx);
            //insert a refresh to visualize the address
            publishProgress(1);

            formatAppVersion();
            me.setLanguage(Locale.getDefault().getDisplayLanguage(Locale.UK));

            C4User netUser = null;
            try {
                RestTemplate restTemplate = context.getDefaultRestTemplate();
                restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
                ResponseEntity<C4User> responseEntity = restTemplate.exchange(context.getBaseUrl(), HttpMethod.POST,
                        new HttpEntity<C4User>(me, context.getDefaultHeaders()), C4User.class);
                netUser = responseEntity.getBody();
                if (netUser == null) return null;
                //take some info about ourselves from server
                me.setId(netUser.getId());
                me.setName(netUser.getName());
                me.setPhotoId(netUser.getPhotoId());
                me.setGeneralExperience(netUser.getGeneralExperience());
                me.setGeneralRating(netUser.getGeneralRating());
                me.setFoodRating(netUser.getFoodRating());
                me.setSellExperience(netUser.getSellExperience());
                me.setDescription(netUser.getDescription());
                me.setTotalEarned(netUser.getTotalEarned());
                me.setTotalSpent(netUser.getTotalSpent());
                me.setPrivilege(netUser.getPrivilege());
                me.setPayEmail(netUser.getPayEmail());

                Log.d(LOG_TAG, "Received user info for " + netUser.getName());
                Log.d(LOG_TAG, "id: " + netUser.getId());
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getLocalizedMessage(), e);
                LogsToServer.send(e);
                return null;
            }

            if (netUser != null && netUser.getMessage() != null)
                return new Object[]{netUser.getMessage(), netUser.getRefreshTags()};
            else return new Object[]{"OK", netUser.getRefreshTags()};
        }

        @Override
        protected void onProgressUpdate(Integer... percent) {
            if (mCallbacks != null) {
                mCallbacks.onProgressUpdate(percent[0]);
            }
        }

        @Override
        protected void onCancelled() {
            if (mCallbacks != null) {
                mCallbacks.onCancelled();
            }
        }

        @Override
        protected void onPostExecute(Object[] obj) {
            if (mCallbacks != null) {
                mCallbacks.onPostExecute(obj);
            }
        }
    }

    private void formatAppVersion() {
        PackageInfo pInfo;
        int version = 0;
        try {
            pInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
            version = pInfo.versionCode;
            me.setVersionCode("ANDROID:" + version);

        } catch (PackageManager.NameNotFoundException e1) {
            e1.printStackTrace();
        }
    }

    private String getGoogleToken() {
        String token = null;
        String scope = "audience:server:client_id:" + ctx.getResources().getString(R.string.google_client_id);
        try {
            token = GoogleAuthUtil.getToken(ctx, me.getEmail(), scope);
        } catch (IOException | GoogleAuthException e3) {
            e3.printStackTrace();
            LogsToServer.send(e3);
        }
        return token;
    }
}
