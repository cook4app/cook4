package com.beppeben.cook4;

import android.os.Bundle;

import com.beppeben.cook4.ui.ChatFragment;

public class ChatActivity extends MyActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Bundle extras = getIntent().getExtras();
        String user_name = extras.getString("user_name");
        Long user_id = extras.getLong("user_id");
        setTitle(user_name);

        frag = ChatFragment.newInstance(user_name, user_id);
        super.onCreate(savedInstanceState);
    }

}
