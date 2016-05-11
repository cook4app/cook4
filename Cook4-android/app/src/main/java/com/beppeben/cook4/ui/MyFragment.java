package com.beppeben.cook4.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.beppeben.cook4.MainActivity;
import com.beppeben.cook4.R;
import com.beppeben.cook4.RegistrationActivity;
import com.beppeben.cook4.domain.C4User;
import com.beppeben.cook4.utils.Globals;

import java.lang.reflect.Field;
import java.util.Timer;
import java.util.TimerTask;

public abstract class MyFragment extends Fragment implements FragmentManager.OnBackStackChangedListener {

    private Timer timer;
    private final static int seconds = 60;
    private int downloadcounter = 0;
    private FragmentManager mRetainedChildFragmentManager;

    private ActionBarActivity mAct;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (mRetainedChildFragmentManager != null) {
            //restore the last retained child fragment manager to the new
            //created fragment
            try {
                Field childFMField = Fragment.class.getDeclaredField("mChildFragmentManager");
                childFMField.setAccessible(true);
                childFMField.set(this, mRetainedChildFragmentManager);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {
            mRetainedChildFragmentManager = getChildFragmentManager();
        }
    }

    public void registerTimer(final boolean redownload) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (redownload && downloadcounter >= 3) {
                            update(true);
                            downloadcounter = 0;
                        } else update(false);
                        downloadcounter++;
                    }
                });

            }
        }, seconds * 1000, seconds * 1000);
    }

    @Override
    public void onPause() {
        if (timer != null)
            timer.cancel();
        super.onPause();
    }

    public abstract void update(boolean redownload);

    protected void setLocationTab(String address, View root) {

        TextView loc = (TextView) root.findViewById(R.id.location);
        MainActivity act = ((MainActivity) getActivity());
        if (getActivity() == null) return;
        if (Globals.registered) {
            C4User me = Globals.getMe(getActivity());
            if (me.isUnlocated() && !me.isModData())
                loc.setText(getString(R.string.location_not_available));
            else if (address == null || address.isEmpty())
                loc.setText(getString(R.string.error_address));
            else loc.setText(address);
            root.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    update(true);
                }
            });
        } else if (!Globals.registered && !Globals.registering) {
            if (act.location == null) {
                loc.setText(getString(R.string.retrieving_location) + "...");
                return;
            }
            loc.setText(getString(R.string.not_connected_retry));
            loc.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    Intent i = new Intent(getActivity().getApplicationContext(), RegistrationActivity.class);
                    startActivity(i);
                    getActivity().finish();

                }

            });
        } else if (Globals.registering) {
            loc.setText(getString(R.string.registering));
        } else {
            loc.setText(getString(R.string.error_address));
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAct = (ActionBarActivity) getActivity();
        getChildFragmentManager().addOnBackStackChangedListener(this);
        mAct.getSupportFragmentManager().addOnBackStackChangedListener(this);
        updateBackButton();
    }

    @Override
    public void onBackStackChanged() {
        updateBackButton();
    }

    public void updateBackButton() {
        if (mAct == null || !(mAct instanceof MainActivity)) return;
        FragmentManager fm = getChildFragmentManager();
        MainActivity act = (MainActivity) mAct;
        int page = act.getPage();
        if (page == 0 && this instanceof Cook4MeFragment ||
                page == 1 && this instanceof Cook4YouFragment ||
                page == 2 && this instanceof PendingSwapsFragment ||
                page == 3 && this instanceof PendingTransactionsFragment) {

            if (fm.getBackStackEntryCount() > 0) {
                act.mDrawerToggle.setDrawerIndicatorEnabled(false);
                act.actionBar.setDisplayHomeAsUpEnabled(true);
                act.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            } else {
                act.mDrawerToggle.setDrawerIndicatorEnabled(true);
                act.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            }
        }
    }
}