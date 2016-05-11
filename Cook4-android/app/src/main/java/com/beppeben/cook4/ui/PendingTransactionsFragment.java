package com.beppeben.cook4.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.beppeben.cook4.MainActivity;
import com.beppeben.cook4.R;
import com.beppeben.cook4.domain.C4Dish;
import com.beppeben.cook4.domain.C4Transaction;
import com.beppeben.cook4.domain.C4User;
import com.beppeben.cook4.utils.CurrencyUtils;
import com.beppeben.cook4.utils.Globals;
import com.beppeben.cook4.utils.LocationUtils;
import com.beppeben.cook4.utils.LogsToServer;
import com.beppeben.cook4.utils.StringUtils;
import com.beppeben.cook4.utils.Utils;
import com.beppeben.cook4.utils.net.HttpContext;

import org.joda.time.DateTime;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class PendingTransactionsFragment extends MyFragment {

    private C4User me;
    private List<C4Transaction> transactions;
    private Boolean transactionsdownloaded = false;
    private LinearLayout receiveView;
    private LinearLayout prepareView;

    public PendingTransactionsFragment() {
    }

    public static PendingTransactionsFragment newInstance(List<C4Transaction> transactions, Boolean transactionsdownloaded) {
        PendingTransactionsFragment fragment = new PendingTransactionsFragment();
        fragment.setTransactions(transactions);
        fragment.setTransactionsdownloaded(transactionsdownloaded);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        me = Globals.getMe(getActivity());
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        me = Globals.getMe(getActivity());
        registerTimer(false);
        updateLayout();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout root = (RelativeLayout) inflater.inflate(R.layout.fragment_pending, container, false);
        receiveView = (LinearLayout) root.findViewById(R.id.receive);
        prepareView = (LinearLayout) root.findViewById(R.id.prepare);

        updateLayout();

        return root;
    }


    private void updateLayout() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        List<C4Transaction> toReceive = new ArrayList<C4Transaction>();
        List<C4Transaction> toPrepare = new ArrayList<C4Transaction>();
        receiveView.removeAllViews();
        prepareView.removeAllViews();

        if (transactions != null) {
            for (C4Transaction transaction : transactions) {
                if (transaction.getCookId().equals(me.getId())) toPrepare.add(transaction);
                if (transaction.getFoodieId().equals(me.getId())) toReceive.add(transaction);
            }
        }

        if (toReceive.size() != 0) {
            Collections.sort(toReceive, C4Transaction.compareByDatePosition(new DateTime(),
                    me.getAppLatitude(), me.getAppLongitude()));
            for (int i = 0; i < toReceive.size(); i++) {
                addTransactionLayout(true, toReceive.get(i), inflater, null, i == 0);
            }
        } else if (transactionsdownloaded) {
            LinearLayout noReceive = (LinearLayout) inflater.inflate(R.layout.simpleitem_layout, null, false);
            ((TextView) noReceive.findViewById(R.id.text)).setText(getString(R.string.no_dishes_to_receive));
            receiveView.addView(noReceive);
        }

        if (toPrepare.size() != 0) {
            Collections.sort(toPrepare, C4Transaction.compareByDatePosition(new DateTime(),
                    me.getAppLatitude(), me.getAppLongitude()));
            for (int i = 0; i < toPrepare.size(); i++) {
                addTransactionLayout(false, toPrepare.get(i), inflater, null, i == 0);
            }
        } else if (transactionsdownloaded) {
            LinearLayout noPrepare = (LinearLayout) inflater.inflate(R.layout.simpleitem_layout, null, false);
            ((TextView) noPrepare.findViewById(R.id.text)).setText(getString(R.string.no_dishes_to_prepare));
            prepareView.addView(noPrepare);
        }
    }

    private void addTransactionLayout(final Boolean buy, final C4Transaction trans,
                                      LayoutInflater inflater, ViewGroup container, boolean first) {
        View uiTransaction = inflater.inflate(R.layout.transaction_layout, container, false);

        TextView dishText = (TextView) uiTransaction.findViewById(R.id.dish);
        TextView swapText = (TextView) uiTransaction.findViewById(R.id.swaplabel);
        TextView voteText = (TextView) uiTransaction.findViewById(R.id.votelabel);
        TextView timeText = (TextView) uiTransaction.findViewById(R.id.when);
        TextView distText = (TextView) uiTransaction.findViewById(R.id.distance);
        TextView portionsText = (TextView) uiTransaction.findViewById(R.id.portions);
        TextView priceText = (TextView) uiTransaction.findViewById(R.id.price);

        if (first) {
            uiTransaction.findViewById(R.id.bar).setVisibility(View.INVISIBLE);
        }

        int portions = trans.getPortions();
        portionsText.setText("" + portions);

        String currency = CurrencyUtils.getSymbolFromCode(trans.getCurrency());
        Float price = trans.getTotalPrice();
        if (price == null) {
            price = trans.getPrice() * portions;
        }
        priceText.setText(StringUtils.formatFloat(price) + " " + currency);
        dishText.setText(trans.getDishName());

        final DateTime today = new DateTime();
        final DateTime next = new DateTime(trans.getDate());

        timeText.setText(StringUtils.format(getResources(), next));

        Float dist = LocationUtils.getDistance(me.getAppLatitude(), me.getAppLongitude(), trans.getLatitude(), trans.getLongitude());
        if (dist != null) {
            String distString = Utils.round(dist / 1000, 2).toString();
            String asterisk = "";
            if (me.isModData()) asterisk = "*";
            distText.setText(distString + " Km" + asterisk);
        }

        if (next.isBefore(today)) {
            voteText.setVisibility(View.VISIBLE);
        }

        ImageView menu = (ImageView) uiTransaction.findViewById(R.id.menuview);
        menu.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(getActivity(), v);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem arg0) {
                        switch (arg0.getItemId()) {

                            case R.id.action_vote:
                                if (next.isBefore(today)) {
                                    if (trans.getTwinTransId() != null && !buy) {
                                        Toast.makeText(getActivity(), getString(R.string.only_vote_buy_swap), Toast.LENGTH_LONG).show();
                                        break;
                                    }
                                    FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                                    transaction.addToBackStack(null);
                                    transaction.replace(R.id.pending_root, RatingFragment.newInstance(trans, buy, false));
                                    transaction.commit();
                                } else {
                                    Toast.makeText(getActivity(), getString(R.string.only_vote_past), Toast.LENGTH_LONG).show();
                                }
                                break;

                            case R.id.action_delete:
                                cancelTransactionDialog(trans, buy);
                                break;
                        }
                        return false;
                    }
                });
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.transaction, popup.getMenu());

                popup.show();
            }

        });

        RelativeLayout content = (RelativeLayout) uiTransaction.findViewById(R.id.content);
        content.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.addToBackStack(null);
                transaction.replace(R.id.pending_root, BuyFragment.newInstance(trans, buy));
                transaction.commit();
            }
        });

        if (trans.getTwinTransId() != null) {
            swapText.setVisibility(View.VISIBLE);
        }

        if (buy) receiveView.addView(uiTransaction);
        else prepareView.addView(uiTransaction);
    }

    private void cancelTransactionDialog(final C4Transaction trans, final boolean buy) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(getString(R.string.cancel_transaction));
        String message;
        if (!buy) message = getString(R.string.canceltrans_cook_warning);
        else {
            if (new DateTime(trans.getDate()).minusHours(24).isBefore(new DateTime())) {
                message = getString(R.string.canceltrans_norefund_warning);
            } else {
                message = getString(R.string.canceltrans_warning);
            }
        }
        alertDialogBuilder
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg) {
                        new DeleteTransactionTask(trans.getId(), buy).execute();
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        alertDialogBuilder.create().show();
    }

    public class GetTransactionsTask extends AsyncTask<Void, Void, List<C4Transaction>> {

        private final String LOG_TAG = GetTransactionsTask.class.getName();

        public GetTransactionsTask() {
            super();
        }

        @Override
        protected List<C4Transaction> doInBackground(Void... params) {

            if (!Globals.registered) return null;

            HttpContext context = HttpContext.getInstance();
            List<C4Transaction> transactions = null;
            try {
                RestTemplate restTemplate = context.getDefaultRestTemplate();
                String path = "transaction/" + me.getId();
                ResponseEntity<C4Transaction[]> responseEntity = restTemplate.exchange(context.getBaseUrl() + path, HttpMethod.GET,
                        new HttpEntity<C4Dish>(context.getDefaultHeaders()), C4Transaction[].class);
                C4Transaction[] arrTransaction = responseEntity.getBody();
                transactions = new ArrayList<C4Transaction>(Arrays.asList(arrTransaction));
                Log.d(LOG_TAG, "Getting transactions for  " + me.getEmail());
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception while getting transactions", e);
                Log.e(LOG_TAG, e.getLocalizedMessage(), e);
                LogsToServer.send(e);
            }

            return transactions;
        }

        @Override
        protected void onPostExecute(List<C4Transaction> transactions) {
            if (transactions != null) {
                addTwins(transactions);
                setTransactions(transactions);
                transactionsdownloaded = true;
                if (getActivity() != null) updateLayout();
            }
        }
    }

    private void addTwins(List<C4Transaction> transactions) {
        for (C4Transaction trans : transactions) {
            Long twinId = trans.getTwinTransId();
            if (twinId != null) {
                for (C4Transaction tr : transactions) {
                    if (tr.getId().equals(twinId)) {
                        trans.setTwinTransaction(tr);
                        tr.setTwinTransaction(trans);
                    }
                }
            }
        }
    }


    public class DeleteTransactionTask extends AsyncTask<Void, Void, String> {

        private final String LOG_TAG = DeleteTransactionTask.class.getName();
        private Long id;
        private boolean buy;
        private ProgressDialog progressDialog;

        public DeleteTransactionTask(Long id, boolean buy) {
            super();
            this.id = id;
            this.buy = buy;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), getString(R.string.wait), getString(R.string.deleting_transaction) + "...");
        }

        @Override
        protected String doInBackground(Void... params) {

            if (!Globals.registered) return null;

            HttpContext context = HttpContext.getInstance();
            String response = "";
            try {
                RestTemplate restTemplate = context.getDefaultRestTemplate();
                String path = "transaction/id=" + id + "&buy=" + buy;
                ResponseEntity<String> responseEntity = restTemplate.exchange(context.getBaseUrl() + path, HttpMethod.DELETE,
                        new HttpEntity<C4Dish>(context.getDefaultHeaders()), String.class);
                response = responseEntity.getBody();
                Log.d(LOG_TAG, "Deleting  " + id + ": " + response);
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getLocalizedMessage(), e);
                LogsToServer.send(e);
            }

            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            progressDialog.dismiss();
            if (response.equals("OK")) {
                Toast.makeText(getActivity(), getString(R.string.transaction_deleted), Toast.LENGTH_LONG).show();
                update(true);
            } else {
                Toast.makeText(getActivity(), getString(R.string.problems_del_trans), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void setTransactions(List<C4Transaction> transactions) {
        this.transactions = transactions;
    }

    public void setTransactionsdownloaded(Boolean transactionsdownloaded) {
        this.transactionsdownloaded = transactionsdownloaded;
    }

    @Override
    public void update(boolean redownload) {
        MainActivity act = (MainActivity) getActivity();
        if (act == null || me == null) return;
        if (redownload) {
            if (Globals.registered)
                new GetTransactionsTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        } else {
            updateLayout();
        }
    }
}

