package com.beppeben.cook4;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.beppeben.cook4.domain.C4Query;
import com.beppeben.cook4.domain.C4User;
import com.beppeben.cook4.ui.Cook4MeFragment;
import com.beppeben.cook4.ui.Cook4YouFragment;
import com.beppeben.cook4.ui.OnVisibleListener;
import com.beppeben.cook4.ui.PendingSwapsFragment;
import com.beppeben.cook4.ui.PendingTransactionsFragment;
import com.beppeben.cook4.ui.SlidingTabLayout;
import com.beppeben.cook4.utils.CurrencyUtils;
import com.beppeben.cook4.utils.Globals;
import com.beppeben.cook4.utils.ImageCache;
import com.beppeben.cook4.utils.LocationUtils;
import com.beppeben.cook4.utils.SectionsPagerAdapter;
import com.beppeben.cook4.utils.TagsUtils;
import com.beppeben.cook4.utils.Utils;
import com.beppeben.cook4.utils.net.HttpContext;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.common.api.GoogleApiClient;

import org.joda.time.DateTime;
import org.springframework.http.HttpHeaders;

import java.util.List;


public class MainActivity extends DrawerActivity implements 
        LocationListener, ViewPager.OnPageChangeListener, LoginFragment.TaskCallbacks {

    public Location location;
    public static Boolean activityVisible = false;
    public Cook4MeFragment extrafrag;
    private LocationManager service;
    private C4User me;
    private SlidingTabLayout mSlidingTabLayout;
    private static SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private boolean doubleBackToExitPressedOnce;
    private LoginFragment mLoginFragment;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor prefsEditor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLayout(R.layout.activity_main, savedInstanceState);
        me = Globals.getMe(this);
        Utils.setExceptionHandler();
        if (me.getId() == null) {
            Globals.registered = false;
            Globals.registering = false;
        }
        Globals.mainActivity = this;
        activityVisible = true;
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), this);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        rootLayout = (RelativeLayout) findViewById(R.id.root_main);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);
        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(mViewPager);
        mSlidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.tab_selector));
        mSlidingTabLayout.setOnPageChangeListener(this);
        service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean gps_enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network_enabled = service.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!gps_enabled && !network_enabled) {
            me.setUnlocated(true);
        } else {
            me.setUnlocated(false);
            Location location_gps = service.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location location_net = service.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            location = LocationUtils.getBetterLocation(location_gps, location_net);
            if (location != null) {
                location.getTime();
                DateTime now = new DateTime();
                if (now.getMillis() - location.getTime() > 1000 * 60 * 5)
                    location = null;
                else {
                    me.setLatitude(location.getLatitude());
                    me.setLongitude(location.getLongitude());
                }
            }
            requestUpdates();
        }
        getCookLabels();
        CurrencyUtils.initialize();
        if (!Globals.splashshown) {
            showSplash();
            showDelayedLocationDialog();
        }
        FragmentManager fm = getSupportFragmentManager();
        mLoginFragment = (LoginFragment) fm.findFragmentByTag("login_fragment");
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        prefsEditor = sharedPref.edit();
        connectOrShowNetworkDialog();
    }

    public void requestUpdates() {
        Criteria criteria_coarse = new Criteria();
        criteria_coarse.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria_coarse.setAltitudeRequired(false);
        criteria_coarse.setBearingRequired(false);
        criteria_coarse.setSpeedRequired(false);
        service.requestLocationUpdates(1000 * 30, 0, criteria_coarse, this, null);
    }

    private void showSplash() {
        Globals.splashshown = true;
        final Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.splash_layout);
        dialog.setCancelable(false);
        dialog.show();
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if ((dialog != null) && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                } catch (Exception e) {
                }
            }
        }, 3000);
    }

    private void showDelayedLocationDialog() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (location == null && !me.isUnlocated()) {
                    showLocationDialog();
                }
            }
        }, 10000);
    }

    public int getPage() {
        return mViewPager.getCurrentItem();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppEventsLogger.activateApp(this);
        activityVisible = true;
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        requestUpdates();
        if (me == null) {
            Globals.initializeMe(this);
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void changeFragment() {
        String fragName = "";
        if (getIntent().getExtras() != null)
            fragName = getIntent().getExtras().getString("select_fragment");
        if (fragName != null) {
            if (fragName.equals("pending")) mViewPager.setCurrentItem(3);
            else if (fragName.equals("chat")) {
                String from_user = getIntent().getExtras().getString("user_name");
                Long from_id = getIntent().getExtras().getLong("user_id");
                Intent chatIntent = new Intent(this, ChatActivity.class);
                chatIntent.putExtra("user_name", from_user);
                chatIntent.putExtra("user_id", from_id);
                startActivity(chatIntent);
            } else if (fragName.equals("pending_swaps")) {
                mViewPager.setCurrentItem(2);
            }
        }
    }

    private void getCookLabels() {
        android.content.res.Resources res = getResources();
        android.content.res.TypedArray lev = res.obtainTypedArray(R.array.scorelevels);
        Globals.cookLevels = new float[lev.length()];
        for (int i = 0; i < lev.length(); i++) {
            Globals.cookLevels[i] = lev.getFloat(i, 0F);
        }
        Globals.cookLabels = res.getStringArray(R.array.scorelabels);
        lev.recycle();
    }

    private boolean onBackPressed(FragmentManager fm, Fragment f) {
        if (fm != null) {
            if (fm.getBackStackEntryCount() > 0) {
                List<Fragment> fragList2 = fm.getFragments();
                if (fragList2 != null && fragList2.size() > 0) {
                    for (Fragment frag : fragList2) {
                        if (frag == null) continue;
                        if (onBackPressed(frag.getChildFragmentManager(), frag)) {
                            return true;
                        }
                    }
                }
                if (f instanceof OnVisibleListener) {
                    ((OnVisibleListener) f).onVisible();
                }
                fm.popBackStackImmediate();
                return true;
            }
            int curr = mViewPager.getCurrentItem();
            List<Fragment> fragList = fm.getFragments();
            if (fragList != null && fragList.size() > 0) {
                for (Fragment frag : fragList) {
                    if (frag == null) continue;
                    if (curr == 0 && !(frag instanceof Cook4MeFragment)) continue;
                    if (curr == 1 && !(frag instanceof Cook4YouFragment)) continue;
                    if (curr == 3 && !(frag instanceof PendingTransactionsFragment)) continue;
                    if (curr == 2 && !(frag instanceof PendingSwapsFragment)) continue;
                    if (frag.isVisible()) {
                        if (onBackPressed(frag.getChildFragmentManager(), frag)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        onBackPressed(true);
    }

    private boolean onBackPressed(boolean toast) {
        FragmentManager fm = getSupportFragmentManager();
        if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
            mDrawerLayout.closeDrawer(mDrawerList);
            return true;
        }
        if (onBackPressed(fm, null)) {
            return true;
        }
        if (doubleBackToExitPressedOnce) {
            Globals.reset(this, false);
            super.onBackPressed();
            return true;
        }
        doubleBackToExitPressedOnce = true;
        if (toast)
            Toast.makeText(this, getString(R.string.press_again_exit), Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
        return false;
    }

    public void backToRoot() {
        try {
            while (onBackPressed(false)) {}
        } catch (IllegalStateException ignored) {
            //trying to fix some bug here, may need to save state in the future
        }
    }

    public void launchQuery(C4Query query) {
        Cook4MeFragment f1 = mSectionsPagerAdapter.getFrag1();
        backToRoot();
        if (extrafrag != null) extrafrag.launchQuery(query);
        else f1.launchQuery(query);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void showLocationDialog() {
        if (!activityVisible) return;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setTitle(getString(R.string.geolocation_problem));
        alertDialogBuilder
                .setMessage(getString(R.string.troubles_finding_location))
                .setCancelable(false)
                .setNeutralButton(getString(R.string.retry), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        showDelayedLocationDialog();
                    }
                })
                .setPositiveButton(getString(R.string.location_settings), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                        MainActivity.this.finish();
                    }
                })
                .setNegativeButton(getString(R.string.manual_location), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        me.setUnlocated(true);
                        connectOrShowNetworkDialog();
                        dialog.dismiss();
                    }
                });
        alertDialogBuilder.create().show();
    }

    private void connectOrShowNetworkDialog() {
        if (isOnline()) {
            if (!Globals.registered && !Globals.registering) {
                if (!me.isUnlocated() && (location == null || me.getLatitude() == null)) return;
                Globals.registering = true;
                if (mLoginFragment == null) {
                    mLoginFragment = new LoginFragment();
                    getSupportFragmentManager().beginTransaction().add(mLoginFragment, "login_fragment").commit();
                }
            }
            return;
        }
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Offline");
        alertDialogBuilder
                .setMessage(getString(R.string.internet_inactive))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.retry), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        connectOrShowNetworkDialog();
                        return;
                    }
                })
                .setNegativeButton(getString(R.string.exit), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //dialog.cancel();
                        MainActivity.this.finish();
                    }
                });
        alertDialogBuilder.create().show();
    }

    @Override
    public void onLocationChanged(Location newloc) {
        requestUpdates();
        Log.d(getClass().getSimpleName(), "Last location retreived.");
        if (LocationUtils.getBetterLocation(newloc, location) != location) {
            Log.d(getClass().getSimpleName(), "Last location better than previous.");
            me.setLatitude(newloc.getLatitude());
            me.setLongitude(newloc.getLongitude());
            Float dist = 0F;
            if (location != null) {
                dist = LocationUtils.getDistance(location.getLatitude(), location.getLongitude(),
                        newloc.getLatitude(), newloc.getLongitude());
            }

            if (dist > 30) {
                //location is sensibly different, so update address and redownload dishes
                Log.d(getClass().getSimpleName(), "Last location to be used for update.");
                Globals.updateLocInfo = true;
                location = newloc;
                if (Globals.registered) refresh(true, false, false, false);
            } else {
                //location is a bit different, only update the address if accuracy is higher (or if it is null)
                if (me.getAddress() != null && location != null && newloc.getAccuracy() < location.getAccuracy())
                    return;
                location = newloc;
                final Context ctx = this;
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        LocationUtils.getLocationInfo(ctx);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void t) {
                        if (Globals.registered) refreshNoDown(true, false, false, false);
                        return;
                    }
                }.execute();
            }
        }
        connectOrShowNetworkDialog();
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
    }

    @Override
    protected void onPause() {
        service.removeUpdates(this);
        AppEventsLogger.deactivateApp(this);
        Globals.saveMe(this, me);
        super.onPause();
    }


    public void refresh(final boolean redownload) {
        runOnUiThread(new Runnable() {
            public void run() {
                mSectionsPagerAdapter.refresh(redownload);
            }
        });
    }

    public static void refresh(boolean f1, boolean f2, boolean f3, boolean f4) {
        if (mSectionsPagerAdapter != null)
            mSectionsPagerAdapter.refresh(f1, f2, f3, f4);
    }

    public static void refreshNoDown(boolean f1, boolean f2, boolean f3, boolean f4) {
        if (mSectionsPagerAdapter != null)
            mSectionsPagerAdapter.refreshNoDown(f1, f2, f3, f4);
    }

    public void removeLoadDishesBar() {
        mSectionsPagerAdapter.getFrag1().removeProgressBar();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ImageCache.save();
        Globals.registering = false;
        activityVisible = false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment frag = mSectionsPagerAdapter.getFrag2();
        List<Fragment> fragments = frag.getChildFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment != null)
                    fragment.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void onStop() {
        activityVisible = false;
        GoogleApiClient mGoogleApiClient = Globals.mGoogleApiClient;
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        mSectionsPagerAdapter.updateBackButton(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onPreExecute() {
    }

    @Override
    public void onProgressUpdate(int percent) {
        if (percent == 1) refresh(false);
    }

    @Override
    public void onCancelled() {
    }

    @Override
    public void onPostExecute(Object[] obj) {
        Globals.registering = false;
        if (obj == null || obj[0] == null) {
            Toast.makeText(this, getString(R.string.check_internet), Toast.LENGTH_LONG).show();
            refresh(false);
            removeLoadDishesBar();
            return;
        }
        String s = (String) obj[0];
        Boolean t = (Boolean) obj[1];
        if (s.startsWith("TOKEN:")) {
            String token = s.split(":")[1];
            HttpHeaders headers = HttpContext.getInstance().getDefaultHeaders();
            headers.remove("Token");
            headers.add("Token", token);
        }
        if (s.equals("OK") || s.startsWith("TOKEN:")) {
            if (me.getLoginMethod().equals("email")) {
                prefsEditor.putBoolean("pass_confirmed", true);
                prefsEditor.commit();
            }
            Globals.registered = true;
            refresh(true);
            if (t != null && t) {
                Log.d(getClass().getSimpleName(), "Forcing tag_layout refresh");
                TagsUtils.refreshTags(this, true);
            } else TagsUtils.refreshTags(this, false);
            ImageCache.initialize(this);
            if (!me.isUnlocated()) changeFragment();
            return;
        }
        if (s.equals("BAD_FB_TOKEN")) {
            Intent i = new Intent(getApplicationContext(), RegistrationActivity.class);
            i.putExtra("FORCE_FB_LOGIN", "ok");
            startActivity(i);
            finish();
            return;
        }
        String err = "";
        boolean doRegister = true;
        if (s.equals("ERROR_BAD_VERSION")) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle(getString(R.string.error));
            alertDialogBuilder
                    .setMessage(getString(R.string.update_app))
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            final String appPackageName = getPackageName();
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                            } catch (android.content.ActivityNotFoundException anfe) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                            }
                            finish();
                        }
                    });
            alertDialogBuilder.create().show();
            return;
        }
        if (s.equals("ERROR_NO_USER")) err = getString(R.string.account_not_existing);
        if (s.equals("ERROR_NO_PASSWORD")) err = getString(R.string.account_no_pass);
        if (s.equals("ERROR_WRONG_PASSWORD")) err = getString(R.string.password_incorrect);
        if (s.equals("ERROR_NOT_CONFIRMED")) err = getString(R.string.account_not_confirmed);
        if (s.equals("ERROR_GOOGLE")) err = getString(R.string.authentication_problem);
        if (s.equals("ERROR_FB")) err = getString(R.string.authentication_problem);
        if (s.equals("ERROR_BANNED")) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle(getString(R.string.error));
            alertDialogBuilder
                    .setMessage(getString(R.string.error_banned))
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    });
            alertDialogBuilder.create().show();
            return;
        }
        Toast.makeText(this, err, Toast.LENGTH_LONG).show();
        prefsEditor.putBoolean("pass_confirmed", false);
        prefsEditor.commit();
        me.setLoginMethod("");
        if (doRegister) {
            Intent i = new Intent(getApplicationContext(), RegistrationActivity.class);
            startActivity(i);
            finish();
            return;
        }
        refresh(false);
        removeLoadDishesBar();
    }
}