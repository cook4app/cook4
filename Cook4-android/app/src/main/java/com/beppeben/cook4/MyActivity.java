package com.beppeben.cook4;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;


public class MyActivity extends ActionBarActivity {

    public Fragment frag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myactivity_layout);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.frag_container, frag)
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        if (onBackPressed(fm, null)) {
            return;
        }
        super.onBackPressed();
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
                fm.popBackStackImmediate();
                return true;
            }
            List<Fragment> fragList = fm.getFragments();
            if (fragList != null && fragList.size() > 0) {
                for (Fragment frag : fragList) {
                    if (frag == null) continue;
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

}
