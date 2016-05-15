package com.beppeben.cook4.ui;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.beppeben.cook4.MainActivity;
import com.beppeben.cook4.R;
import com.beppeben.cook4.domain.C4Dish;
import com.beppeben.cook4.domain.C4SwapProposal;
import com.beppeben.cook4.domain.C4User;
import com.beppeben.cook4.utils.Globals;
import com.beppeben.cook4.utils.LocationUtils;
import com.beppeben.cook4.utils.LogsToServer;
import com.beppeben.cook4.utils.PathJSONParser;
import com.beppeben.cook4.utils.StringUtils;
import com.beppeben.cook4.utils.net.HttpConnection;
import com.beppeben.cook4.utils.net.HttpContext;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;


public class SwapFragment extends MyFragment implements OnClickListener {

    private C4User me;
    private List<C4Dish> dishes;
    private C4Dish dish;
    private Spinner dishSpin, portSpin;
    private Button submitButton;
    private TextView whereText, whenText, summaryText;
    private DateTime date;
    private Integer portions;
    private List<Integer> ports;
    private C4SwapProposal swap;
    private boolean directionsdownloaded = false;

    public SwapFragment() {
    }

    public static SwapFragment newInstance(C4SwapProposal swap) {
        SwapFragment fragment = new SwapFragment();
        fragment.setSwap(swap);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        me = Globals.getMe(getActivity());
        ports = new ArrayList<Integer>();
        for (int i = 1; i <= 30; i++) {
            ports.add(i);
        }
        portions = ports.get(0);
        dishes = me.getDishes();
        date = new DateTime(swap.getDate());
    }

    @Override
    public void update(boolean redownload) {
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void setSwap(C4SwapProposal swap) {
        this.swap = swap;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout root = (RelativeLayout) inflater.inflate(R.layout.fragment_swap, container, false);

        dishSpin = (Spinner) root.findViewById(R.id.itemSpin);
        ArrayAdapter<C4Dish> dishAdapter = new ArrayAdapter<C4Dish>(getActivity(),
                R.layout.spinner_item_layout, dishes);
        dishAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dishSpin.setAdapter(dishAdapter);
        dishSpin.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dish = dishes.get(position);
                updateSummary();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        portSpin = (Spinner) root.findViewById(R.id.portSpin);
        ArrayAdapter<Integer> portAdapter = new ArrayAdapter<Integer>(getActivity(),
                R.layout.spinner_item_layout, ports);
        portAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        portSpin.setAdapter(portAdapter);
        portSpin.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                portions = ports.get(position);
                updateSummary();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        submitButton = (Button) root.findViewById(R.id.submit);
        submitButton.setOnClickListener(this);
        whenText = (TextView) root.findViewById(R.id.when);
        whereText = (TextView) root.findViewById(R.id.where);
        summaryText = (TextView) root.findViewById(R.id.summary);

        if (isAdded()) updateDescription();

        updateSummary();

        return root;
    }

    private void updateDescription() {
        if (swap.getToCookDelivers()) whereText.setText(swap.getDeliveryAddress());
        else {
            whereText.setText(getString(R.string.swap_cooks_place) + " " + swap.getToCookName());
            new ReadDirectionsTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                    LocationUtils.getMapsApiDirectionsUrl(me.getLatitude(), me.getLongitude(), swap.getLatitude(), swap.getLongitude()));
        }
        whenText.setText(StringUtils.formatWithPrep(getResources(), date));
    }

    private void updateSummary() {
        if (dish != null) {
            int targetPortions = swap.getTargetDishPortions();
            String summ = portions + " " + ((portions > 1) ? getString(R.string.portions_pl) : getString(R.string.portion))
                    + " " + getString(R.string.of) + " " + dish.getName() + " " + getString(R.string.against) + " "
                    + targetPortions + " " + ((targetPortions > 1) ? getString(R.string.portions_pl) : getString(R.string.portion))
                    + " " + getString(R.string.of) + " " + swap.getTargetDishName() + ".";
            summaryText.setText(summ);
        }
    }

    @Override
    public void onClick(View arg0) {
        final Fragment thisFrag = this;
        switch (arg0.getId()) {

            case R.id.submit:
                DateTime validUntil = date;
                if (dish == null) {
                    Toast.makeText(getActivity(), getString(R.string.select_dish_to_exchange), Toast.LENGTH_LONG).show();
                    break;
                }
                swap.setRewardDishId(dish.getId());
                swap.setRewardDishName(dish.getName());
                swap.setRewardDishPortions(portions);
                swap.setValidUntil(validUntil.toDate());
                new SendProposal().execute();
        }
    }

    class SendProposal extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        private static final String LOG_TAG = "SendProposalAsync";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), getString(R.string.wait), getString(R.string.sending_swap_proposal) + "...");
        }

        protected String doInBackground(String... urls) {

            HttpContext context = HttpContext.getInstance();
            String response = null;
            try {
                RestTemplate restTemplate = context.getDefaultRestTemplate();
                String path = "proposeswap";
                ResponseEntity<String> responseEntity = restTemplate.exchange(context.getBaseUrl() + path, HttpMethod.POST,
                        new HttpEntity<C4SwapProposal>(swap, context.getDefaultHeaders()), String.class);
                response = responseEntity.getBody();

            } catch (Exception e) {
                Log.e(LOG_TAG, e.getLocalizedMessage(), e);
                LogsToServer.send(e);
            }

            return response;
        }

        protected void onPostExecute(String response) {
            progressDialog.dismiss();
            if (response == null) {
                Toast.makeText(getActivity(), getString(R.string.problems_uploading_proposal), Toast.LENGTH_LONG).show();
                return;
            }
            if (response.equals("OK")) {
                Toast.makeText(getActivity(), getString(R.string.proposal_uploaded_successfully), Toast.LENGTH_LONG).show();
                ((MainActivity) getActivity()).refresh(true, false, true, false);
                ((MainActivity) getActivity()).backToRoot();
                return;
            }
            if (response.equals("ERROR_FAR")) {
                Toast.makeText(getActivity(), getString(R.string.error_too_far), Toast.LENGTH_LONG).show();
                return;
            }
            if (response.equals("ERROR_DATE")) {
                Toast.makeText(getActivity(), getString(R.string.date_not_available), Toast.LENGTH_LONG).show();
                return;
            }
        }
    }

    private class ReadDirectionsTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                HttpConnection http = new HttpConnection();
                data = http.readUrl(url[0]);
            } catch (Exception e) {
                Log.d("Directions API Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            directionsdownloaded = true;
            if (!isAdded()) return;
            JSONObject jObject;
            try {
                jObject = new JSONObject(result);
                PathJSONParser parser = new PathJSONParser();
                String expTime = parser.getTime(jObject);
                if (expTime != null) {
                    whereText.setText(whereText.getText().toString() + " (" + expTime + " " + getString(R.string.walk) + ")");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}

