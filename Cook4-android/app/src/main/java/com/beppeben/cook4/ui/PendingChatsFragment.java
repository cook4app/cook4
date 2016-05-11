package com.beppeben.cook4.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.beppeben.cook4.ChatActivity;
import com.beppeben.cook4.R;
import com.beppeben.cook4.domain.C4ConversationSummary;
import com.beppeben.cook4.domain.C4User;
import com.beppeben.cook4.utils.ChatUtils;
import com.beppeben.cook4.utils.Globals;
import com.beppeben.cook4.utils.StringUtils;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;


public class PendingChatsFragment extends MyFragment {

    private C4User me;
    private List<C4ConversationSummary> convSummaries;
    private ListView convList;
    private MyBaseAdapter adapter;
    private ChatUtils chatUtils;
    private BroadcastReceiver receiver;

    public PendingChatsFragment() {
    }

    public static PendingChatsFragment newInstance() {
        PendingChatsFragment fragment = new PendingChatsFragment();
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        chatUtils = new ChatUtils(getActivity());
        convSummaries = chatUtils.getConversationSummaries();
        me = Globals.getMe(getActivity());
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            refreshConversations();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        me = Globals.getMe(getActivity());
        refreshConversations();
        receiver = new MyBroadcastReceiver();
        getActivity().registerReceiver(receiver, new IntentFilter("com.beppeben.cook4.CONVSUMMARY_REFRESH"));
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(receiver);
    }

    @Override
    public void update(boolean redownload) {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout root = (RelativeLayout) inflater.inflate(R.layout.fragment_pendingchats, container, false);
        convList = (ListView) root.findViewById(R.id.convList);
        adapter = new MyBaseAdapter(getActivity(), convSummaries);
        convList.setAdapter(adapter);
        return root;
    }

    private void refreshConversations() {
        convSummaries = chatUtils.getConversationSummaries();
        adapter.setItems(convSummaries);
        adapter.notifyDataSetChanged();
    }


    private View setItemLayout(final C4ConversationSummary conv, View uiItem, boolean first) {

        TextView userText = (TextView) uiItem.findViewById(R.id.user);
        userText.setText(StringUtils.formatName(conv.getUsername()));

        if (first) {
            uiItem.findViewById(R.id.bar).setVisibility(View.INVISIBLE);
        }

        TextView dateText = (TextView) uiItem.findViewById(R.id.lastdate);
        DateTime last = new DateTime(conv.getLast());
        dateText.setText(StringUtils.formatDate(last.getDayOfMonth(), last.getMonthOfYear()));

        if (!conv.isRead()) {
            TextView commentText = (TextView) uiItem.findViewById(R.id.comments);
            commentText.setText(getString(R.string.unread_messages));
            commentText.setVisibility(View.VISIBLE);
        }

        View content = uiItem.findViewById(R.id.content);
        content.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
                chatIntent.putExtra("user_name", conv.getUsername());
                chatIntent.putExtra("user_id", conv.getId());
                chatIntent.putExtra("my_id", me.getId());
                startActivity(chatIntent);
            }
        });

        ImageView menu = (ImageView) uiItem.findViewById(R.id.menuview);
        menu.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(getActivity(), v);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem arg0) {
                        switch (arg0.getItemId()) {
                            case R.id.action_delete:
                                chatUtils.deleteConversation(conv.getId());
                                convSummaries = new ArrayList<C4ConversationSummary>(chatUtils.getConversationSummaries());
                                adapter.setItems(convSummaries);
                                adapter.notifyDataSetChanged();
                                break;
                        }
                        return false;
                    }
                });
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.convsummary, popup.getMenu());

                popup.show();
            }

        });

        return uiItem;
    }


    public class MyBaseAdapter extends BaseAdapter {

        List<C4ConversationSummary> convList = new ArrayList<C4ConversationSummary>();
        LayoutInflater inflater;
        Context context;

        public MyBaseAdapter(Context context, List<C4ConversationSummary> convList) {
            if (convList != null)
                this.convList = convList;
            this.context = context;
            inflater = LayoutInflater.from(this.context);
        }

        public void setItems(List<C4ConversationSummary> itemList) {
            this.convList = convList;
        }

        public int numItems() {
            if (convList != null) return convList.size();
            else return 0;
        }

        @Override
        public int getCount() {
            return Math.max(numItems(), 1);
        }

        @Override
        public C4ConversationSummary getItem(int position) {
            return convList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (numItems() == 0) {
                LinearLayout noOffers = (LinearLayout)
                        inflater.inflate(R.layout.simpleitem_layout, null);
                ((TextView) noOffers.findViewById(R.id.text))
                        .setText(getString(R.string.no_active_chats));
                return noOffers;
            }
            if (convertView == null || numItems() == 1) {
                convertView = inflater.inflate(R.layout.conversation_layout, null);

            }
            convertView = setItemLayout(getItem(position), convertView, position == 0);
            return convertView;
        }
    }
}

