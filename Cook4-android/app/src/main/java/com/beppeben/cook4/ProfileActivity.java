package com.beppeben.cook4;

import android.os.Bundle;

import com.beppeben.cook4.ui.ProfileFragment;


public class ProfileActivity extends MyActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        frag = new ProfileFragment();
        super.onCreate(savedInstanceState);
    }

}
