package com.beppeben.cook4.ui;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.beppeben.cook4.R;
import com.beppeben.cook4.domain.C4User;
import com.beppeben.cook4.utils.LogsToServer;
import com.beppeben.cook4.utils.net.HttpContext;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class TopCooksFragment extends MyFragment {

    private C4User me;
    private List<C4User> cooks;
    private Boolean cooksdownloaded = false;
    private ListView userList;
    private MyBaseAdapter adapter;

    public TopCooksFragment() {
    }

    public static TopCooksFragment newInstance() {
        TopCooksFragment fragment = new TopCooksFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        new QueryCooksTask().execute();
    }

    @Override
    public void update(boolean redownload) {

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        RelativeLayout root = (RelativeLayout) inflater.inflate(R.layout.fragment_topcooks, container, false);

        userList = (ListView) root.findViewById(R.id.itemList);
        adapter = new MyBaseAdapter(getActivity(), cooks);
        userList.setAdapter(adapter);

        return root;
    }


    private View setCookLayout(C4User cook, View uiItem, int position) {
        TextView cookNameText = (TextView) uiItem.findViewById(R.id.user);
        String cookText = cook.getName();
        if (cook.getCity() != null && !cook.getCity().equals(""))
            cookText += " (" + cook.getCity() + ")";
        cookNameText.setText(cookText);
        TextView scoreText = (TextView) uiItem.findViewById(R.id.score);
        String scoreString = ((Integer) Math.round(cook.foodScore())).toString();
        scoreText.setText(scoreString);
        TextView rankText = (TextView) uiItem.findViewById(R.id.rank);
        rankText.setText((position + 1) + ".");
        if (position == 0) {
            uiItem.findViewById(R.id.bar).setVisibility(View.GONE);
        } else {
            uiItem.findViewById(R.id.bar).setVisibility(View.VISIBLE);
        }

        View content = uiItem.findViewById(R.id.content);
        final Long id = cook.getId();
        content.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.addToBackStack(null);
                transaction.replace(R.id.topcooks_root, UserInfoFragment.newInstance(id));
                transaction.commit();
            }
        });

        return uiItem;
    }


    public class QueryCooksTask extends AsyncTask<Void, Void, List<C4User>> {

        private final String LOG_TAG = QueryCooksTask.class.getName();

        public QueryCooksTask() {
            super();
        }

        @Override
        protected List<C4User> doInBackground(Void... params) {

            HttpContext context = HttpContext.getInstance();
            List<C4User> users = null;
            try {
                RestTemplate restTemplate = context.getDefaultRestTemplate();
                Log.d(LOG_TAG, "Querying cooks...");
                String path = "topcooks";
                ResponseEntity<C4User[]> responseEntity = restTemplate.exchange(context.getBaseUrl() + path, HttpMethod.GET,
                        new HttpEntity<C4User>(context.getDefaultHeaders()), C4User[].class);
                C4User[] arrUsers = responseEntity.getBody();
                users = new ArrayList<C4User>(Arrays.asList(arrUsers));
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getLocalizedMessage(), e);
                LogsToServer.send(e);
            }

            return users;
        }

        @Override
        protected void onPostExecute(List<C4User> users) {
            if (users != null) {
                cooksdownloaded = true;
                cooks = users;
                adapter.setCooks(users);
                adapter.notifyDataSetChanged();
            }
        }
    }


    public class MyBaseAdapter extends BaseAdapter {

        List<C4User> userList = new ArrayList<C4User>();
        LayoutInflater inflater;
        Context context;

        public MyBaseAdapter(Context context, List<C4User> userList) {
            if (userList != null)
                this.userList = userList;
            this.context = context;
            inflater = LayoutInflater.from(this.context);
        }

        public void setCooks(List<C4User> userList) {
            this.userList = userList;
        }

        public int numCooks() {
            if (userList != null) return userList.size();
            else return 0;
        }

        @Override
        public int getCount() {
            if (cooksdownloaded)
                return Math.max(numCooks(), 1);
            else return numCooks();
        }

        @Override
        public C4User getItem(int position) {
            return userList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (cooksdownloaded && numCooks() == 0) {
                LinearLayout noCooks = (LinearLayout)
                        inflater.inflate(R.layout.simpleitem_layout, null);
                ((TextView) noCooks.findViewById(R.id.text))
                        .setText(getString(R.string.no_cooks_found));
                return noCooks;
            }
            if (convertView == null || numCooks() == 1) {
                convertView = inflater.inflate(R.layout.ranking_layout, null);

            }
            convertView = setCookLayout(getItem(position), convertView, position);
            return convertView;
        }
    }

}
	
