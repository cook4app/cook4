package com.beppeben.cook4.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.beppeben.cook4.MainActivity;
import com.beppeben.cook4.R;
import com.beppeben.cook4.domain.C4AuthRequest;
import com.beppeben.cook4.domain.C4Dish;
import com.beppeben.cook4.domain.C4Item;
import com.beppeben.cook4.domain.C4User;
import com.beppeben.cook4.utils.Globals;
import com.beppeben.cook4.utils.LogsToServer;
import com.beppeben.cook4.utils.PhotoUtils;
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
import java.util.List;


public class Cook4YouFragment extends MyFragment implements OnClickListener {

    private C4User me;
    private List<C4Dish> dishes;
    private List<C4Dish> olddishes;
    private List<C4Item> offers;
    private Boolean dishesdownloaded = false;
    private LinearLayout offersView;
    private LinearLayout dishesView;
    private Button addDish, authButton, okButton, becomeCookButton;
    private View mainPanel, authPanel, paypalPanel, infoPanel;
    private EditText paypalText;
    private TextView pendingApprovalText;
    private CheckBox cook4Myself, cook4Industry, cook4Diploma, amateurCourses;
    private RadioGroup experience;
    private RadioButton firstExp, secondExp, thirdExp;
    private EditText diplomaDetails, extraAuth, amateurDetails, introducer;

    public Cook4YouFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        me = Globals.getMe(getActivity());
    }

    public static Cook4YouFragment newInstance(List<C4Dish> dishes, List<C4Item> offers, Boolean dishesdownloaded) {
        Cook4YouFragment fragment = new Cook4YouFragment();
        fragment.setDishes(dishes);
        fragment.setOffers(offers);
        fragment.setDishesdownloaded(dishesdownloaded);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        registerTimer(false);
        me = Globals.getMe(getActivity());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        RelativeLayout root = (RelativeLayout) inflater.inflate(R.layout.fragment_cook4you, container, false);

        addDish = (Button) root.findViewById(R.id.add_dish_button);
        offersView = (LinearLayout) root.findViewById(R.id.offers);
        dishesView = (LinearLayout) root.findViewById(R.id.dishes);
        mainPanel = root.findViewById(R.id.main_panel);
        authPanel = root.findViewById(R.id.auth_panel);
        paypalPanel = root.findViewById(R.id.paypal_card);
        infoPanel = root.findViewById(R.id.info_card);
        authButton = (Button) root.findViewById(R.id.auth_button);
        okButton = (Button) root.findViewById(R.id.ok);
        becomeCookButton = (Button) root.findViewById(R.id.become_cook_button);
        paypalText = (EditText) root.findViewById(R.id.paypal_text);
        pendingApprovalText = (TextView) root.findViewById(R.id.pending_approval);
        cook4Myself = (CheckBox) root.findViewById(R.id.cook4_myself);
        cook4Industry = (CheckBox) root.findViewById(R.id.cook4_industry);
        cook4Diploma = (CheckBox) root.findViewById(R.id.cook4_diploma);
        amateurCourses = (CheckBox) root.findViewById(R.id.amateur_courses);
        experience = (RadioGroup) root.findViewById(R.id.experience);
        firstExp = (RadioButton) root.findViewById(R.id.first_exp);
        secondExp = (RadioButton) root.findViewById(R.id.second_exp);
        thirdExp = (RadioButton) root.findViewById(R.id.third_exp);
        diplomaDetails = (EditText) root.findViewById(R.id.diploma_details);
        amateurDetails = (EditText) root.findViewById(R.id.amateur_details);
        extraAuth = (EditText) root.findViewById(R.id.extra);
        introducer = (EditText) root.findViewById(R.id.introducer);

        addDish.setOnClickListener(this);
        authButton.setOnClickListener(this);
        okButton.setOnClickListener(this);
        becomeCookButton.setOnClickListener(this);

        if (isAdded()) {
            updateLayout();
            updateAuthLayout();
        }

        return root;
    }

    private void updateAuthLayout() {
        cook4Industry.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    experience.setVisibility(View.VISIBLE);
                } else {
                    experience.setVisibility(View.GONE);
                }
            }
        });

        cook4Diploma.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    diplomaDetails.setVisibility(View.VISIBLE);
                } else {
                    diplomaDetails.setVisibility(View.GONE);
                }
            }
        });

        amateurCourses.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    amateurDetails.setVisibility(View.VISIBLE);
                } else {
                    amateurDetails.setVisibility(View.GONE);
                }
            }
        });
    }

    private void processAuthRequest() {
        StringBuilder sb = new StringBuilder();
        if (cook4Myself.isChecked()) {
            sb.append("I cook for myself and my friends\n");
        }
        if (cook4Industry.isChecked()) {
            sb.append("I worked in the food industry for ");
            if (firstExp.isChecked()) {
                sb.append("1-2 years");
            } else if (secondExp.isChecked()) {
                sb.append("2-5 years");
            } else {
                sb.append("more than 5 years");
            }
        }
        if (cook4Diploma.isChecked()) {
            String diploma = diplomaDetails.getText().toString();
            if (diploma == null || diploma.isEmpty()) {
                Toast.makeText(getActivity(), getString(R.string.diploma_details), Toast.LENGTH_LONG).show();
                return;
            }
            sb.append("\nI hold a cooking diploma: " + diploma);
        }
        if (amateurCourses.isChecked()) {
            String amat = amateurDetails.getText().toString();
            if (amat == null || amat.isEmpty()) {
                Toast.makeText(getActivity(), getString(R.string.specify_courses), Toast.LENGTH_LONG).show();
                return;
            }
            sb.append("\nI attended amatorial cooking classes: " + amat);
        }
        String introducedby = introducer.getText().toString();
        if (introducedby != null) {
            sb.append("\nIntroduced by: " + introducedby);
        }
        String extra = extraAuth.getText().toString();
        if (extra != null) {
            sb.append("\nExtra info: " + extra);
        }
        final C4AuthRequest req = new C4AuthRequest(me.getId(), sb.toString(), cook4Diploma.isChecked());
        if (!cook4Myself.isChecked() && !cook4Industry.isChecked() && !cook4Diploma.isChecked()
                && !amateurCourses.isChecked() && extraAuth.getText().toString().isEmpty()) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setTitle(getString(R.string.empty_form));
            alertDialogBuilder
                    .setMessage(getString(R.string.empty_form_confirm))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            new SendAuthRequestTask(req).execute();
                        }
                    })
                    .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            alertDialogBuilder.create().show();
            return;
        }
        new SendAuthRequestTask(req).execute();
    }


    private void addOfferLayout(final C4Item item, LayoutInflater inflater, ViewGroup container, boolean first) {
        View uiItem = inflater.inflate(R.layout.offer_layout, container, false);
        TextView dishText = (TextView) uiItem.findViewById(R.id.dish);
        dishText.setText(item.getDish().getName());

        if (first) uiItem.findViewById(R.id.bar).setVisibility(View.INVISIBLE);

        LinearLayout content = (LinearLayout) uiItem.findViewById(R.id.content);
        content.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.addToBackStack(null);
                transaction.replace(R.id.cook4you_root, MakeOfferFragment.newInstance(item));
                transaction.commit();
            }

        });

        TextView timeText = (TextView) uiItem.findViewById(R.id.when);
        TextView portionsText = (TextView) uiItem.findViewById(R.id.portions);
        ImageButton shareButton = (ImageButton) uiItem.findViewById(R.id.shareButton);

        DateTime today = new DateTime();
        DateTime next = item.nextOffer(today);

        if (next == null) {
            return;
        }

        String time = StringUtils.format(getResources(), next);
        final String fbdesc = getString(R.string.locality) + ": " + item.getCity();

        if (!item.getOneoff()) {
            time += " (" + getResources().getString(R.string.periodic) + ")";
        }
        timeText.setText(time);

        int portTotal = item.getPortions();
        int portSold = portTotal - item.portionsLeft(next);

        portionsText.setText(portSold + "/" + portTotal + " " + getString(R.string.servings_sold));

        shareButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.shareFbStory(Cook4YouFragment.this, item.getDish().getName(), fbdesc, item.getDish().getCoverId());
            }
        });

        final Long id = item.getId();
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
                                new DeleteOfferTask(id).execute();
                        }
                        return false;
                    }
                });
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.offeritem, popup.getMenu());
                popup.show();
            }

        });

        offersView.addView(uiItem);
    }

    private void addDishLayout(C4Dish dish, LayoutInflater inflater, ViewGroup container, boolean first) {

        View uiDish = inflater.inflate(R.layout.dish_layout, container, false);
        TextView dishName = (TextView) uiDish.findViewById(R.id.dish);
        dishName.setText(dish.getName());

        if (first) uiDish.findViewById(R.id.bar).setVisibility(View.INVISIBLE);

        RatingBar rating = (RatingBar) uiDish.findViewById(R.id.ratingbar);
        if (dish.getRating() != null) rating.setRating(dish.getRating());
        TextView orders = (TextView) uiDish.findViewById(R.id.totorders);
        orders.setText("(" + dish.getOrders() + ")");

        ImageView photoView = (ImageView) uiDish.findViewById(R.id.photo);
        PhotoUtils.setPhoto(getActivity(), photoView, dish, null);

        View content = uiDish.findViewById(R.id.content);

        final C4Dish dishToPass = dish;

        content.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.addToBackStack(null);
                transaction.replace(R.id.cook4you_root, NewDishFragment.newInstance(dishToPass));
                transaction.commit();
            }
        });

        final Long id = dish.getId();

        ImageView menu = (ImageView) uiDish.findViewById(R.id.menuview);
        menu.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(getActivity(), v);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem arg0) {
                        switch (arg0.getItemId()) {

                            case R.id.action_delete:
                                deleteDishDialog(id);
                                break;

                            case R.id.action_makeoffer:
                                if (me.getPrivilege() == null || me.getPrivilege().isEmpty() || me.getPrivilege().equals("pending")) {
                                    Toast.makeText(getActivity(), getString(R.string.not_allowed_sell_short), Toast.LENGTH_LONG).show();
                                    break;
                                } else if (me.getPayEmail() == null) {
                                    Toast.makeText(getActivity(), getString(R.string.set_paypal_before_selling), Toast.LENGTH_LONG).show();
                                    break;
                                }
                                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                                transaction.addToBackStack(null);
                                transaction.replace(R.id.cook4you_root, MakeOfferFragment.newInstance(dishToPass));
                                transaction.commit();
                        }

                        return false;
                    }
                });
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.dishitem, popup.getMenu());
                popup.show();
            }

        });

        dishesView.addView(uiDish);
    }

    private void deleteDishDialog(final Long dish_id) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(getString(R.string.confirm_delete_dish));
        alertDialogBuilder
                .setMessage(getString(R.string.delete_dish_hint))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        new DeleteDishTask(dish_id).execute();
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        alertDialogBuilder.create().show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.add_dish_button:
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.addToBackStack(null);
                transaction.replace(R.id.cook4you_root, NewDishFragment.newInstance());
                transaction.commit();
                break;

            case R.id.auth_button:
                processAuthRequest();
                break;

            case R.id.ok:
                Utils.hideKeyboard(getActivity());
                String email = paypalText.getText().toString();
                new Utils.SetPaypalTask(getActivity(), me, email, new Utils.MyCallback() {
                    @Override
                    public void callback() {
                        updateLayout();
                    }
                }).execute();
                break;

            case R.id.become_cook_button:
                authPanel.setVisibility(View.VISIBLE);
                infoPanel.setVisibility(View.GONE);
                break;
        }
    }

    public class GetDishesTask extends AsyncTask<Void, Void, List<List>> {

        private final String LOG_TAG = GetDishesTask.class.getName();

        public GetDishesTask() {
            super();
        }

        @Override
        protected List<List> doInBackground(Void... params) {

            if (!Globals.registered) return null;

            HttpContext context = HttpContext.getInstance();
            List<C4Dish> dishes = null;
            List<C4Item> offers = null;
            try {
                Log.d(LOG_TAG, "Sending c4you query...");
                RestTemplate restTemplate = context.getDefaultRestTemplate();

                //get dishes
                String path = "dish/" + me.getId();
                long startTime = System.currentTimeMillis();
                ResponseEntity<C4Dish[]> responseEntityDish = restTemplate.exchange(context.getBaseUrl() + path, HttpMethod.GET,
                        new HttpEntity<C4Dish>(context.getDefaultHeaders()), C4Dish[].class);
                C4Dish[] arrDishes = responseEntityDish.getBody();
                dishes = new ArrayList<C4Dish>(Arrays.asList(arrDishes));
                me.setDishes(dishes);

                Log.d(LOG_TAG, "got " + dishes.size() + "dishes");

                //get offers
                path = "offer/" + me.getId();
                ResponseEntity<C4Item[]> responseEntityItem = restTemplate.exchange(context.getBaseUrl() + path, HttpMethod.GET,
                        new HttpEntity<C4Item>(context.getDefaultHeaders()), C4Item[].class);
                C4Item[] arrItems = responseEntityItem.getBody();
                offers = new ArrayList<C4Item>(Arrays.asList(arrItems));

                long estimatedTime = System.currentTimeMillis() - startTime;
                Log.d(LOG_TAG, "c4you queries took " + estimatedTime + " milliseconds");

            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception while getting dish/offer data", e);
                Log.e(LOG_TAG, e.getLocalizedMessage(), e);
                LogsToServer.send(e);
            }

            List<List> result = new ArrayList<List>(2);
            result.add(0, dishes);
            result.add(1, offers);

            return result;
        }

        @Override
        protected void onPostExecute(List<List> result) {
            if (result != null) {
                List<C4Dish> dishes = result.get(0);
                List<C4Item> offers = result.get(1);
                setDishes(dishes);
                setOffers(offers);
                dishesdownloaded = true;
                if (getActivity() != null) updateLayout();
            }
        }
    }

    public class DeleteDishTask extends AsyncTask<Void, Void, String> {

        private final String LOG_TAG = DeleteDishTask.class.getName();
        private Long id;
        ProgressDialog progressDialog;

        public DeleteDishTask(Long id) {
            super();
            this.id = id;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), getString(R.string.wait), getString(R.string.deleting_dish) + "...");
        }

        @Override
        protected String doInBackground(Void... params) {

            if (!Globals.registered) return null;

            HttpContext context = HttpContext.getInstance();
            String response = "";
            try {
                RestTemplate restTemplate = context.getDefaultRestTemplate();
                String path = "dish/" + id;
                ResponseEntity<String> responseEntity = restTemplate.exchange(context.getBaseUrl() + path, HttpMethod.DELETE,
                        new HttpEntity<>(context.getDefaultHeaders()), String.class);
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
                Toast.makeText(getActivity(), getString(R.string.dish_deleted), Toast.LENGTH_LONG).show();
                ((MainActivity) getActivity()).refresh(true, true, false, false);
            } else if (response.equals("ERROR_PENDING_TRANSACTION")) {
                Toast.makeText(getActivity(), getString(R.string.cant_delete_dish_transactions), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), getString(R.string.problems_deleting_dish), Toast.LENGTH_LONG).show();
            }
        }
    }

    public class SendAuthRequestTask extends AsyncTask<Void, Void, String> {

        private final String LOG_TAG = SendAuthRequestTask.class.getName();
        private C4AuthRequest req;
        private ProgressDialog progressDialog;

        public SendAuthRequestTask(C4AuthRequest req) {
            super();
            this.req = req;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), getString(R.string.wait), getString(R.string.sending_auth_request) + "...");
        }

        @Override
        protected String doInBackground(Void... params) {

            if (!Globals.registered) return null;

            HttpContext context = HttpContext.getInstance();
            String response = "";
            try {
                RestTemplate restTemplate = context.getDefaultRestTemplate();
                String path = "cookauth";
                ResponseEntity<String> responseEntity = restTemplate.exchange(context.getBaseUrl() + path, HttpMethod.POST,
                        new HttpEntity<C4AuthRequest>(req, context.getDefaultHeaders()), String.class);
                response = responseEntity.getBody();
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getLocalizedMessage(), e);
                LogsToServer.send(e);
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            progressDialog.dismiss();
            if (response == null) {
                Toast.makeText(getActivity(), getString(R.string.problems_sending_request), Toast.LENGTH_LONG).show();
            } else if (response.equals("OK")) {
                Toast.makeText(getActivity(), getString(R.string.auth_request_sent), Toast.LENGTH_LONG).show();
                me.setPrivilege("pending");
                updateLayout();
            }
        }
    }


    public class DeleteOfferTask extends AsyncTask<Void, Void, String> {

        private final String LOG_TAG = DeleteOfferTask.class.getName();
        private Long id;
        ProgressDialog progressDialog;

        public DeleteOfferTask(Long id) {
            super();
            this.id = id;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), getString(R.string.wait), getString(R.string.deleting_offer) + "...");
        }

        @Override
        protected String doInBackground(Void... params) {

            if (!Globals.registered) return null;

            HttpContext context = HttpContext.getInstance();
            String response = "";
            try {
                RestTemplate restTemplate = context.getDefaultRestTemplate();
                String path = "offer/" + id;
                ResponseEntity<String> responseEntity = restTemplate.exchange(context.getBaseUrl() + path, HttpMethod.DELETE,
                        new HttpEntity<>(context.getDefaultHeaders()), String.class);
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
                Toast.makeText(getActivity(), getString(R.string.offer_deleted), Toast.LENGTH_LONG).show();
                ((MainActivity) getActivity()).refresh(true, true, false, false);
            } else {
                Toast.makeText(getActivity(), getString(R.string.problems_deleting_offer), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void setDishes(List<C4Dish> dishes) {
        this.dishes = dishes;
    }

    public void setDishesdownloaded(Boolean dishesdownloaded) {
        this.dishesdownloaded = dishesdownloaded;
    }

    public void setOffers(List<C4Item> offers) {
        this.offers = offers;
    }

    @Override
    public void update(boolean redownload) {
        if (getActivity() == null || me == null) return;
        if (redownload /*&& me.getPrivilege() != null*/) {
            if (Globals.registered) {
                new GetDishesTask().execute();
            }
            olddishes = dishes;
        } else {
            if (me.getPrivilege() != null) updateLayout();
        }
    }

    public void updateLayout() {
        if (!Globals.registered) return;
        String privilege = me.getPrivilege();
        if (privilege == null || privilege.isEmpty()) {
            infoPanel.setVisibility(View.VISIBLE);
            becomeCookButton.setVisibility(View.VISIBLE);
            pendingApprovalText.setVisibility(View.GONE);
            authPanel.setVisibility(View.GONE);
            mainPanel.setVisibility(View.GONE);
        } else if (privilege.equals("pending")) {
            infoPanel.setVisibility(View.VISIBLE);
            becomeCookButton.setVisibility(View.GONE);
            pendingApprovalText.setVisibility(View.VISIBLE);
            authPanel.setVisibility(View.GONE);
            mainPanel.setVisibility(View.GONE);
        } else if (me.getPayEmail() == null) {
            infoPanel.setVisibility(View.GONE);
            paypalPanel.setVisibility(View.VISIBLE);
            authPanel.setVisibility(View.GONE);
            mainPanel.setVisibility(View.GONE);
        } else if (me.getPayEmail() != null) {
            infoPanel.setVisibility(View.GONE);
            paypalPanel.setVisibility(View.GONE);
            authPanel.setVisibility(View.GONE);
            mainPanel.setVisibility(View.VISIBLE);
        }

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        offersView.removeAllViews();
        dishesView.removeAllViews();
        if (offers != null && offers.size() != 0) {
            for (int i = 0; i < offers.size(); i++) {
                addOfferLayout(offers.get(i), inflater, null, i == 0);
            }
        } else if (dishesdownloaded) {
            LinearLayout noOffers = (LinearLayout) inflater.inflate(R.layout.simpleitem_layout, null, false);
            ((TextView) noOffers.findViewById(R.id.text)).setText(getString(R.string.no_offers_made));
            offersView.addView(noOffers);
        }

        Integer askoffer = null;
        if (dishes != null && dishes.size() != 0) {
            for (int i = 0; i < dishes.size(); i++) {
                addDishLayout(dishes.get(i), inflater, null, i == 0);
                if (olddishes == null) continue;
                boolean oldfound = false;
                for (C4Dish d : olddishes) {
                    if (d.getId().equals(dishes.get(i).getId())) oldfound = true;
                }
                if (!oldfound) askoffer = i;
            }
        } else if (dishesdownloaded) {
            LinearLayout noDishes = (LinearLayout) inflater.inflate(R.layout.simpleitem_layout, null, false);
            ((TextView) noDishes.findViewById(R.id.text)).setText("No dishes registered yet");
            dishesView.addView(noDishes);
        }

        olddishes = dishes;
        if (askoffer != null && (me.getPrivilege() != null && !me.getPrivilege().isEmpty())) {
            final C4Dish dish = dishes.get(askoffer);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setTitle(getString(R.string.new_dish_added));
            alertDialogBuilder
                    .setMessage(getString(R.string.want_make_offer) + " " + dish.getName() + "?")
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                            transaction.addToBackStack(null);
                            transaction.replace(R.id.cook4you_root, MakeOfferFragment.newInstance(dish));
                            transaction.commit();
                            dialog.cancel();
                        }
                    })
                    .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            alertDialogBuilder.create().show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

}