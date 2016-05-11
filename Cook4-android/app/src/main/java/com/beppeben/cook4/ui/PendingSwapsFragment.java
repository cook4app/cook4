package com.beppeben.cook4.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.beppeben.cook4.R;
import com.beppeben.cook4.domain.C4SwapProposal;
import com.beppeben.cook4.domain.C4Transaction;
import com.beppeben.cook4.domain.C4User;
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
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;


public class PendingSwapsFragment extends MyFragment {

    private C4User me;
    private List<C4SwapProposal> swapsToConsider = new ArrayList<C4SwapProposal>();
    ;
    private List<C4SwapProposal> swapsProposed = new ArrayList<C4SwapProposal>();
    ;
    //private ListView toConsiderList;
    //private ListView proposedList;
    private LinearLayout toConsiderView;
    private LinearLayout proposedView;
    //private MyBaseAdapter toConsiderAdapter;
    //private MyBaseAdapter proposedAdapter;
    private boolean swapsdownloaded = false;

    public PendingSwapsFragment() {
    }

    public static PendingSwapsFragment newInstance() {
        PendingSwapsFragment fragment = new PendingSwapsFragment();
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //new QuerySwapsTask().execute();
        setRetainInstance(true);
        setHasOptionsMenu(true);
        me = Globals.getMe(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        me = Globals.getMe(getActivity());
    }

    //public void update(){
    //	new QuerySwapsTask().execute();
    //}

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public void update(boolean redownload) {
        if (!isAdded()) return;
        //me = act.me;
        if (redownload) {
            if (Globals.registered) {
                new QuerySwapsTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
            }
        } else updateLayout();
        //else{
        //	toConsiderAdapter.notifyDataSetChanged();
        //	proposedAdapter.notifyDataSetChanged();
        //}

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        RelativeLayout root = (RelativeLayout) inflater.inflate(R.layout.fragment_pendingswaps, container, false);

        toConsiderView = (LinearLayout) root.findViewById(R.id.swapsToConsiderList);
        proposedView = (LinearLayout) root.findViewById(R.id.proposedSwapsList);

        //toConsiderAdapter = new MyBaseAdapter(getActivity(), swapsToConsider, false);
        //proposedAdapter = new MyBaseAdapter(getActivity(), swapsProposed, true);
        //toConsiderList.setAdapter(toConsiderAdapter);
        //proposedList.setAdapter(proposedAdapter);
        updateLayout();

        return root;
    }

    private void updateLayout() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());

        toConsiderView.removeAllViews();
        proposedView.removeAllViews();


        if (swapsToConsider.size() != 0) {
            for (int i = 0; i < swapsToConsider.size(); i++) {
                addSwapLayout(swapsToConsider.get(i), false, inflater, null, i == 0);
            }

        } else if (swapsdownloaded) {
            LinearLayout noReceive = (LinearLayout) inflater.inflate(R.layout.simpleitem_layout, null, false);
            ((TextView) noReceive.findViewById(R.id.text)).setText(getString(R.string.no_swaps_consider));
            toConsiderView.addView(noReceive);
        }


        if (swapsProposed.size() != 0) {
            for (int i = 0; i < swapsProposed.size(); i++) {
                addSwapLayout(swapsProposed.get(i), true, inflater, null, i == 0);
            }

        } else if (swapsdownloaded) {
            LinearLayout noPrepare = (LinearLayout) inflater.inflate(R.layout.simpleitem_layout, null, false);
            ((TextView) noPrepare.findViewById(R.id.text)).setText(getString(R.string.no_swaps_proposed));
            proposedView.addView(noPrepare);
        }
    }


    private void addSwapLayout(C4SwapProposal swap, boolean proposed, LayoutInflater inflater, ViewGroup container, boolean first) {
        View uiItem = inflater.inflate(R.layout.fragment_pendingswap, container, false);
        TextView titleText = (TextView) uiItem.findViewById(R.id.title);
        TextView proposedText = (TextView) uiItem.findViewById(R.id.proposed);
        TextView dateText = (TextView) uiItem.findViewById(R.id.date);

        if (first) {
            uiItem.findViewById(R.id.bar).setVisibility(View.INVISIBLE);
        }

        String rewName = swap.getRewardDishName();
        int rewPort = swap.getRewardDishPortions();
        String tarName = swap.getTargetDishName();
        int tarPort = swap.getTargetDishPortions();

        String title;
        String prop;
        if (proposed) {
            title = getString(R.string.get) + " " + tarPort + " " + tarName + " " + getString(R.string.in_exchange_for) + " " + rewPort + " " + rewName;
            prop = getResources().getString(R.string.proposed_to) + " " + swap.getToCookName();
        } else {
            title = getString(R.string.get) + " " + rewPort + " " + rewName + " " + getString(R.string.in_exchange_for) + " " + tarPort + " " + tarName;
            prop = getResources().getString(R.string.proposed_by) + " " + swap.getFromCookName();
        }

        proposedText.setText(prop);
        titleText.setText(title);

        DateTime next = new DateTime(swap.getDate());
        //Period p = new Period(new DateTime(), new DateTime(swap.getDate()));
        //dateText.setText(StringUtils.periodString(getResources(), p, true));
        dateText.setText(StringUtils.format(getResources(), next));


        View content = uiItem.findViewById(R.id.content);

        final C4Transaction mytrans = C4Transaction.swapToTrans(swap, proposed);
        final C4Transaction theirtrans = C4Transaction.swapToTrans(swap, !proposed);
        final Date validUntil = swap.getValidUntil();
        Long swapId = swap.getId();
        if (!proposed) swapId = -swapId;
        final Long finSwapId = swapId;

        content.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.addToBackStack(null);
                transaction.replace(R.id.pendingswaps_root, BuyFragment.newInstance(mytrans, theirtrans, finSwapId,
                        validUntil));
                transaction.commit();
            }
        });

        if (proposed) proposedView.addView(uiItem);
        else toConsiderView.addView(uiItem);

    }


    public class QuerySwapsTask extends AsyncTask<Void, Void, List<C4SwapProposal>> {

        private final String LOG_TAG = QuerySwapsTask.class.getName();

        public QuerySwapsTask() {
            super();
        }

        @Override
        protected List<C4SwapProposal> doInBackground(Void... params) {

            HttpContext context = HttpContext.getInstance();
            List<C4SwapProposal> swaps = null;
            try {
                RestTemplate restTemplate = context.getDefaultRestTemplate();
                Log.d(LOG_TAG, "Querying swaps...");
                String path = "pendingswaps/" + me.getId();

                ResponseEntity<C4SwapProposal[]> responseEntity = restTemplate.exchange(context.getBaseUrl() + path, HttpMethod.GET,
                        new HttpEntity<>(context.getDefaultHeaders()), C4SwapProposal[].class);

                C4SwapProposal[] arrSwaps = responseEntity.getBody();
                swaps = new ArrayList<C4SwapProposal>(Arrays.asList(arrSwaps));


            } catch (Exception e) {
                Log.e(LOG_TAG, e.getLocalizedMessage(), e);
                LogsToServer.send(e);
            }


            return swaps;
        }

        @Override
        protected void onPostExecute(List<C4SwapProposal> swaps) {
            if (swaps != null) {
                Log.d(LOG_TAG, "Got " + swaps.size() + " swaps");

                swapsToConsider = new ArrayList<C4SwapProposal>();
                swapsProposed = new ArrayList<C4SwapProposal>();

                for (C4SwapProposal swap : swaps) {
                    if (swap.getFromCookId().equals(me.getId()) && !contains(swapsProposed, swap.getId()))
                        swapsProposed.add(swap);
                    if (swap.getToCookId().equals(me.getId()) && !contains(swapsToConsider, swap.getId()))
                        swapsToConsider.add(swap);
                }

                swapsToConsider = new ArrayList<C4SwapProposal>(new HashSet<C4SwapProposal>(swapsToConsider));
                swapsProposed = new ArrayList<C4SwapProposal>(new HashSet<C4SwapProposal>(swapsProposed));

                swapsdownloaded = true;

                if (getActivity() != null) updateLayout();

            }
        }

    }

    private boolean contains(List<C4SwapProposal> swaps, Long id) {
        for (C4SwapProposal swap : swaps) {
            if (swap.getId().equals(id)) return true;
        }
        return false;
    }

    /*
    public class MyBaseAdapter extends BaseAdapter {
        
        List<Cook4SwapProposal> swapList;
        LayoutInflater inflater;
        Context context;
        boolean proposed;
        
        
        public MyBaseAdapter(Context context, List<Cook4SwapProposal> swapList, boolean proposed) {
        	this.swapList = swapList;
        	this.context = context;
        	this.proposed = proposed;
        	inflater = LayoutInflater.from(this.context);      
        }
        
        public void setSwaps(List<Cook4SwapProposal> swapList){
        	this.swapList = swapList;
        }
        
        public int numSwaps(){
        	if (swapList != null) return swapList.size();
        	else return 0;
        }
 
        @Override
        public int getCount() {
        	if (swapList != null)
        		return Math.max(numSwaps(), 1);
        	else return numSwaps();
        }
 
        @Override
        public Cook4SwapProposal getItem(int position) {
                return swapList.get(position);
        }
 
        @Override
        public long getItemId(int position) {
                return 0;
        }
 
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
        	
        	if (swapList != null && numSwaps() == 0){
        		    		
        		LinearLayout noOffers = (LinearLayout) 
        				inflater.inflate(R.layout.simpleitem_layout, null);
        		((TextView)noOffers.findViewById(R.id.text))
        				.setText("No swaps found");
        		return noOffers;	
    		
        	}	
        	
            if(convertView == null || numSwaps() == 1) {
            	convertView = inflater.inflate(R.layout.fragment_pendingswap, null);
                        
            } 
                
            convertView = setSwapLayout(getItem(position), proposed, convertView, position == 0);
                
            return convertView;
        }
        
      
 
	}

    */


}

