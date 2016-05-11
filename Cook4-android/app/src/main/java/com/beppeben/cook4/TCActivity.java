package com.beppeben.cook4;

import android.os.Bundle;

import com.beppeben.cook4.ui.InfoFragment;


public class TCActivity extends InfoActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        frag = InfoFragment.newInstance("tc");
        super.onCreate(savedInstanceState);
    }

}