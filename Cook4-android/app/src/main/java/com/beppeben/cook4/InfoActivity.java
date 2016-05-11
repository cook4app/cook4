package com.beppeben.cook4;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;

import com.beppeben.cook4.ui.InfoFragment;

public class InfoActivity extends ActionBarActivity {

    protected Fragment frag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (frag == null) frag = new InfoFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, frag).commit();
    }

}
