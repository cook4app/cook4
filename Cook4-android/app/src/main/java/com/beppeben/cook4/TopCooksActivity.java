package com.beppeben.cook4;

import android.os.Bundle;

import com.beppeben.cook4.ui.TopCooksFragment;


public class TopCooksActivity extends MyActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        frag = new TopCooksFragment();
        super.onCreate(savedInstanceState);
    }

}
