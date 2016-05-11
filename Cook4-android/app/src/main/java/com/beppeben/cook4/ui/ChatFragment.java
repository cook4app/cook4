package com.beppeben.cook4.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.beppeben.cook4.R;
import com.beppeben.cook4.domain.C4Conversation;
import com.beppeben.cook4.utils.ChatUtils;
import com.beppeben.cook4.utils.Globals;
import com.beppeben.cook4.utils.LogsToServer;
import com.beppeben.cook4.utils.StringUtils;
import com.beppeben.cook4.utils.net.HttpContext;

import org.joda.time.DateTime;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Date;


public class ChatFragment extends MyFragment implements OnClickListener {

    private RelativeLayout chatLayout;
    private ScrollView scrollView;
    private ChatUtils chatUtils;
    private Long user_id;
    private Long my_id;
    private String user_name;
    private C4Conversation conversation;
    private ArrayList<C4Conversation.Message> messages;
    public static Boolean activityVisible;
    private BroadcastReceiver receiver;
    private EditText msgText;
    private ImageButton sendButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        RelativeLayout root = (RelativeLayout) inflater.inflate(R.layout.fragment_chat, container, false);

        msgText = (EditText) root.findViewById(R.id.chat_message);
        sendButton = (ImageButton) root.findViewById(R.id.chat_send);
        sendButton.setOnClickListener(this);
        msgText = (EditText) root.findViewById(R.id.chat_message);
        sendButton = (ImageButton) root.findViewById(R.id.chat_send);
        sendButton.setOnClickListener(this);
        chatLayout = (RelativeLayout) root.findViewById(R.id.chat_text);
        scrollView = (ScrollView) root.findViewById(R.id.chat_container);

        chatLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                scrollDown();
            }

        });

        if (isAdded()) refreshChat();

        return root;
    }

    public static ChatFragment newInstance(String user, Long id) {
        ChatFragment frag = new ChatFragment();
        frag.user_name = user;
        frag.user_id = id;
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        chatUtils = new ChatUtils(getActivity().getApplicationContext());
        my_id = Globals.getMe(getActivity()).getId();
    }

    private void scrollDown() {
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);

            }
        });
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            refreshChat();
        }
    }

    public void refreshChat() {
        conversation = chatUtils.getConversation(user_id);
        chatLayout.removeAllViews();
        if (conversation != null && conversation.messages != null) {
            messages = conversation.messages;
            for (C4Conversation.Message msg : messages) {
                addMessage(msg.message, new DateTime(msg.date), !msg.me);
            }
        }
        chatUtils.storeConversation(user_id, conversation, true);
        getActivity().sendBroadcast(new Intent("com.beppeben.cook4.CONVSUMMARY_REFRESH"));
    }

    public int addMessage(String message, DateTime date, boolean toRight) {

        int numMessages = chatLayout.getChildCount();

        RelativeLayout msgLayout = (RelativeLayout)
                LayoutInflater.from(getActivity()).inflate(R.layout.chatmsg_layout, null);
        TextView msgTextView = (TextView) msgLayout.findViewById(R.id.message);
        TextView dateTextView = (TextView) msgLayout.findViewById(R.id.date);

        msgTextView.setText(message);
        dateTextView.setText(StringUtils.format(getResources(), date));
        msgLayout.setId(numMessages + 1);

        RelativeLayout.LayoutParams params =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        if (numMessages > 0) {
            params.addRule(RelativeLayout.BELOW, numMessages);
        }
        if (toRight) {
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            msgLayout.setBackgroundColor(Color.parseColor("#A9BFFF"));
        } else {
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            msgLayout.setBackgroundColor(Color.parseColor("#80CC80"));
        }
        params.setMargins(0, 10, 0, 0);

        msgLayout.setLayoutParams(params);
        chatLayout.addView(msgLayout);

        scrollDown();

        return chatLayout.indexOfChild(msgLayout);
    }

    @Override
    public void onResume() {
        super.onResume();
        activityVisible = true;
        receiver = new MyBroadcastReceiver();
        getActivity().registerReceiver(receiver, new IntentFilter("com.beppeben.cook4.CONV_REFRESH"));
    }

    @Override
    public void onPause() {
        super.onPause();
        activityVisible = false;
        chatUtils.setAsRead(user_id);
        getActivity().unregisterReceiver(receiver);
    }

    @Override
    public void update(boolean redownload) {
    }

    private class SendMessageTask extends AsyncTask<Void, Void, String> {

        private final String LOG_TAG = SendMessageTask.class.getName();
        private String msg;

        public SendMessageTask(String msg) {
            super();
            this.msg = msg;
        }

        @Override
        protected String doInBackground(Void... params) {
            HttpContext context = HttpContext.getInstance();
            String response = null;
            try {
                RestTemplate restTemplate = context.getDefaultRestTemplate();
                String path = "chat/from=" + my_id + "&to=" + user_id;
                ResponseEntity<String> responseEntity = restTemplate.exchange(context.getBaseUrl() + path, HttpMethod.POST,
                        new HttpEntity<String>(msg, context.getDefaultHeaders()), String.class);
                response = responseEntity.getBody();
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getLocalizedMessage(), e);
                LogsToServer.send(e);
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            if (response == null) return;
            if (response.equals("OK")) {
                msgText.setText("");
                saveMyMessage(msg);
                if (getActivity() != null)
                    refreshChat();
            } else if (response.equals("ERROR_NOUSER")) {
                Toast.makeText(getActivity(), getString(R.string.chat_error), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onClick(View arg0) {
        sendMsg();
    }

    private void sendMsg() {
        String msg = msgText.getText().toString();
        if (msg == null || msg.equals("")) {
            Toast.makeText(getActivity(), getString(R.string.type_message_first), Toast.LENGTH_LONG).show();
            return;
        } else {
            int index = addMessage(msg, new DateTime(), false);
            chatLayout.getChildAt(index).setBackgroundColor(Color.LTGRAY);
            new SendMessageTask(msg).execute();
        }
    }

    public void saveMyMessage(String message) {
        if (conversation == null) {
            conversation = new C4Conversation();
            conversation.messages = new ArrayList<C4Conversation.Message>();
            conversation.username = user_name;
        }
        C4Conversation.Message msg = new C4Conversation.Message(true, message, new Date());
        conversation.messages.add(msg);

        while (conversation.messages.size() > 30) {
            conversation.messages.remove(0);
        }
        chatUtils.storeConversation(user_id, conversation, true);
    }
}