package com.beppeben.cook4.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.beppeben.cook4.R;
import com.beppeben.cook4.ui.Cook4MeFragment;
import com.beppeben.cook4.ui.Cook4YouFragment;
import com.beppeben.cook4.ui.MyFragment;
import com.beppeben.cook4.ui.PendingSwapsFragment;
import com.beppeben.cook4.ui.PendingTransactionsFragment;

import java.util.Locale;

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private Context ctx;
    private static Cook4MeFragment frag1;
    private static Cook4YouFragment frag2;
    private static PendingSwapsFragment frag3;
    private static PendingTransactionsFragment frag4;


    public SectionsPagerAdapter(FragmentManager fm, Context ctx) {
        super(fm);
        this.ctx = ctx;
    }

    public static void clearFragments() {
        frag1 = null;
        frag2 = null;
        frag3 = null;
        frag4 = null;
    }

    public void updateBackButton(int position) {
        ((MyFragment) getItem(position)).updateBackButton();
    }


    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                if (frag1 == null) frag1 = new Cook4MeFragment();
                return frag1;

            case 1:
                if (frag2 == null) frag2 = new Cook4YouFragment();
                return frag2;

            case 2:
                if (frag3 == null) frag3 = new PendingSwapsFragment();
                return frag3;

            case 3:
                if (frag4 == null) frag4 = new PendingTransactionsFragment();
                return frag4;

        }

        return new Fragment();
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Locale l = Locale.getDefault();
        switch (position) {
            case 0:
                return ctx.getString(R.string.title_section1).toUpperCase(l);
            case 1:
                return ctx.getString(R.string.title_section2).toUpperCase(l);
            case 2:
                return ctx.getString(R.string.title_section3).toUpperCase(l);
            case 3:
                return ctx.getString(R.string.title_section4).toUpperCase(l);
        }
        return null;
    }


    public void refresh(boolean redownload) {
        if (frag1 == null) return;
        if (frag1.getActivity() == null) {    //check if fragments are already attached
            new DelayedRefreshTask().execute();
            return;
        }
        frag1.update(redownload);
        frag2.update(redownload);
        frag3.update(redownload);
        frag4.update(redownload);
    }


    public class DelayedRefreshTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void response) {
            refresh(true);
        }
    }


    public void refresh(boolean f1, boolean f2, boolean f3, boolean f4) {
        if (f1) frag1.update(true);
        if (f2) frag2.update(true);
        if (f3) frag3.update(true);
        if (f4) frag4.update(true);
    }

    public void refreshNoDown(boolean f1, boolean f2, boolean f3, boolean f4) {
        if (f1) frag1.update(false);
        if (f2) frag2.update(false);
        if (f3) frag3.update(false);
        if (f4) frag4.update(false);
    }

    public Cook4MeFragment getFrag1() {
        return frag1;
    }

    public Cook4YouFragment getFrag2() {
        return frag2;
    }

}