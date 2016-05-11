package com.beppeben.cook4.ui;


import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.beppeben.cook4.MainActivity;
import com.beppeben.cook4.R;
import com.beppeben.cook4.domain.C4Rating;
import com.beppeben.cook4.domain.C4Transaction;
import com.beppeben.cook4.utils.LogsToServer;
import com.beppeben.cook4.utils.net.HttpContext;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;


public class RatingFragment extends Fragment {

    private C4Transaction trans;
    private Boolean buy;
    private EditText genComments;
    private EditText foodComments;
    private RatingBar foodRatingBar;
    private RatingBar genRatingBar;
    private CheckBox checkNotReceived;
    private TextView foodRatingText;
    private LinearLayout foodPanel;
    private boolean fromsummary;

    public RatingFragment() {
    }

    public static RatingFragment newInstance(C4Transaction trans, Boolean buy, boolean fromsummary) {
        RatingFragment frag = new RatingFragment();
        frag.setTrans(trans);
        frag.setBuy(buy);
        frag.setFromsummary(fromsummary);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        RelativeLayout root = (RelativeLayout) inflater.inflate(R.layout.fragment_rating, container, false);

        TextView summary = (TextView) root.findViewById(R.id.summary);
        genComments = (EditText) root.findViewById(R.id.generalcomments);
        foodComments = (EditText) root.findViewById(R.id.foodcomments);
        foodPanel = (LinearLayout) root.findViewById(R.id.foodpanel);
        foodRatingText = (TextView) root.findViewById(R.id.foodratingtext);
        foodRatingBar = (RatingBar) root.findViewById(R.id.foodrating);
        genRatingBar = (RatingBar) root.findViewById(R.id.genrating);

        checkNotReceived = (CheckBox) root.findViewById(R.id.checknotreceived);
        checkNotReceived.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean check) {
                if (check) {
                    foodPanel.setVisibility(View.GONE);
                } else {
                    foodPanel.setVisibility(View.VISIBLE);
                }
            }

        });

        String counterpart = "";
        if (buy) {
            counterpart = trans.getCookName();
            foodRatingText.setText(getString(R.string.food_quality_rating) + " (" + trans.getDishName() + "):");

        } else {
            counterpart = trans.getFoodieName();
            foodPanel.setVisibility(View.GONE);
            checkNotReceived.setVisibility(View.GONE);
        }

        String desc = getString(R.string.evaluate) + " <b>" + counterpart + "</b> " + getString(R.string.for_recent_transaction);
        summary.setText(Html.fromHtml(desc));

        Button voteButton = (Button) root.findViewById(R.id.votebutton);
        voteButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                new SendRating().execute();
            }

        });

        return root;
    }

    public void setTrans(C4Transaction trans) {
        this.trans = trans;
    }

    public void setBuy(boolean buy) {
        this.buy = buy;
    }


    class SendRating extends AsyncTask<String, Void, String> {
        private static final String LOG_TAG = "ConfirmTransactionAsync";

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), getString(R.string.wait), getString(R.string.sending_rating) + "...");
        }

        protected String doInBackground(String... urls) {

            C4Rating rating = new C4Rating();
            rating.setGeneralRating(genRatingBar.getRating());
            rating.setGeneralComment(genComments.getText().toString());
            rating.setDishId(trans.getDishId());
            rating.setDishName(trans.getDishName());
            rating.setTransactionId(trans.getId());
            rating.setBuy(buy);
            if (buy) {
                rating.setFromUserId(trans.getFoodieId());
                rating.setFromUserName(trans.getFoodieName());
                rating.setToUserId(trans.getCookId());
                rating.setToUserName(trans.getCookName());
                if (!checkNotReceived.isChecked()) {
                    rating.setFoodRating(foodRatingBar.getRating());
                    rating.setFoodComment(foodComments.getText().toString());
                }

            } else {
                rating.setFromUserId(trans.getCookId());
                rating.setFromUserName(trans.getCookName());
                rating.setToUserId(trans.getFoodieId());
                rating.setToUserName(trans.getFoodieName());
            }

            HttpContext context = HttpContext.getInstance();
            String id = null;
            try {
                RestTemplate restTemplate = context.getDefaultRestTemplate();
                String path = "rating";
                ResponseEntity<String> responseEntity = restTemplate.exchange(context.getBaseUrl() + path, HttpMethod.POST,
                        new HttpEntity<C4Rating>(rating, context.getDefaultHeaders()), String.class);
                id = responseEntity.getBody();
                Log.d(LOG_TAG, "Uploading rating: " + id);
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getLocalizedMessage(), e);
                LogsToServer.send(e);
                return null;
            }

            return id;
        }

        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            if (result == null) {
                Toast.makeText(getActivity(), getString(R.string.problems_uploading_rating), Toast.LENGTH_LONG).show();
                return;
            }
            if (result.equals("OK")) {
                Toast.makeText(getActivity(), getString(R.string.rating_registered_successfully), Toast.LENGTH_LONG).show();
                MainActivity.refresh(false, false, false, true);
                getParentFragment().getChildFragmentManager().popBackStack();
                if (fromsummary)
                    getParentFragment().getParentFragment().getChildFragmentManager().popBackStack();
            }
        }
    }

    public void setFromsummary(boolean fromsummary) {
        this.fromsummary = fromsummary;
    }
}

