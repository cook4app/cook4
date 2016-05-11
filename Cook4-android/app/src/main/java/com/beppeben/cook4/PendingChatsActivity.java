package com.beppeben.cook4;

import android.os.Bundle;

import com.beppeben.cook4.ui.PendingChatsFragment;


public class PendingChatsActivity extends MyActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        frag = new PendingChatsFragment();
        super.onCreate(savedInstanceState);
    }

}
