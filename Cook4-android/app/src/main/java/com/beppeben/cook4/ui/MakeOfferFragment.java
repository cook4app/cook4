package com.beppeben.cook4.ui;


import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.beppeben.cook4.MainActivity;
import com.beppeben.cook4.R;
import com.beppeben.cook4.domain.C4Dish;
import com.beppeben.cook4.domain.C4Item;
import com.beppeben.cook4.domain.C4User;
import com.beppeben.cook4.utils.CurrencyUtils;
import com.beppeben.cook4.utils.DateTimeHelper;
import com.beppeben.cook4.utils.Globals;
import com.beppeben.cook4.utils.LocationUtils;
import com.beppeben.cook4.utils.LogsToServer;
import com.beppeben.cook4.utils.PlacesAutoCompleteAdapter;
import com.beppeben.cook4.utils.PriceDialog;
import com.beppeben.cook4.utils.PriceHelper;
import com.beppeben.cook4.utils.StringUtils;
import com.beppeben.cook4.utils.Utils;
import com.beppeben.cook4.utils.net.HttpContext;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;


public class MakeOfferFragment extends Fragment
        implements DateTimeHelper, OnClickListener, OnItemSelectedListener, OnCheckedChangeListener, PriceHelper {

    private C4Dish dish;
    private C4Item item;
    private Calendar cal;
    private Integer hour, min, startDay, startMonth, startYear, endDay, endMonth, endYear, oneOffDay, oneOffMonth, oneOffYear;
    private EditText timeText, startDateText, endDateText, oneOffDateText, addressCompl;
    private Spinner maxDist, minNoticeText, maxPortionsText;
    private Button submit;
    private CheckBox youGo, theyCome, periodicCheckBox;
    private List<Boolean> daysAvailable;
    private List<ToggleButton> dayToggles;
    private AutoCompleteTextView addressView;
    private PlacesAutoCompleteAdapter addressAdapter;
    private String addressId, defCurr;
    private DateTime startDate, endDate, oneOffDate;
    private int[] dists, minnotices;
    private Integer maxDistMeters, maxPortions, minNotice;
    private TextView dishname, currencyTheyComeText, currencyYouGoText, priceYouGo, priceTheyCome, youGoText, theyComeText;
    private List<Integer> ports;
    private TableRow addressRow, maxDistRow, periodicRow;
    private C4User me;

    public MakeOfferFragment() {
    }

    public static MakeOfferFragment newInstance(C4Dish dish) {
        MakeOfferFragment fragment = new MakeOfferFragment();
        fragment.setDish(dish);
        return fragment;
    }

    public static MakeOfferFragment newInstance(C4Item item) {
        MakeOfferFragment fragment = new MakeOfferFragment();
        fragment.setItem(item);
        fragment.setDish(item.getDish());
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        me = Globals.getMe(getActivity());
        dists = getResources().getIntArray(R.array.dist_meters);
        maxDistMeters = dists[0];
        minnotices = getResources().getIntArray(R.array.min_notices);
        minNotice = minnotices[0];

        ports = new ArrayList<Integer>();
        for (int i = 1; i <= 30; i++) {
            ports.add(i);
        }
        maxPortions = ports.get(0);

        cal = Calendar.getInstance();
        hour = cal.get(Calendar.HOUR_OF_DAY);
        min = cal.get(Calendar.MINUTE);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        defCurr = sharedPref.getString("defCurrency", null);
    }

    @Override
    public void onResume() {
        super.onResume();
        me = Globals.getMe(getActivity());
        //hack to make days show on rotation (new version with checkbox)
        if (periodicCheckBox.isChecked()) {
            periodicCheckBox.setChecked(true);
        }
        youGo.setOnCheckedChangeListener(this);
        theyCome.setOnCheckedChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        youGo.setOnCheckedChangeListener(null);
        theyCome.setOnCheckedChangeListener(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout root = (RelativeLayout) inflater.inflate(R.layout.fragment_makeoffer, container, false);

        dishname = (TextView) root.findViewById(R.id.dishname);
        dishname.setText(getString(R.string.make_offer_for) + " " + dish.getName() + "!");

        currencyTheyComeText = (TextView) root.findViewById(R.id.currency_theycome_text);
        currencyYouGoText = (TextView) root.findViewById(R.id.currency_yougo_text);

        addressView = (AutoCompleteTextView) root.findViewById(R.id.address);
        addressAdapter = new PlacesAutoCompleteAdapter(getActivity(), android.R.layout.simple_list_item_1);
        addressView.setAdapter(addressAdapter);
        addressView.setOnFocusChangeListener(new Utils.MyFocusChangeListener(getActivity()));

        periodicCheckBox = (CheckBox) root.findViewById(R.id.periodic_checkbox);
        periodicCheckBox.setOnCheckedChangeListener(this);

        periodicRow = (TableRow) root.findViewById(R.id.periodic_layout);
        addressRow = (TableRow) root.findViewById(R.id.address_row);
        maxDistRow = (TableRow) root.findViewById(R.id.maxdist_row);

        youGoText = (TextView) root.findViewById(R.id.youGoText);
        theyComeText = (TextView) root.findViewById(R.id.theyComeText);
        youGoText.setOnClickListener(this);
        theyComeText.setOnClickListener(this);

        timeText = (EditText) root.findViewById(R.id.timeText);
        startDateText = (EditText) root.findViewById(R.id.startDateText);
        endDateText = (EditText) root.findViewById(R.id.endDateText);
        oneOffDateText = (EditText) root.findViewById(R.id.oneoffDateText);
        timeText.setOnClickListener(this);
        startDateText.setOnClickListener(this);
        endDateText.setOnClickListener(this);
        oneOffDateText.setOnClickListener(this);
        maxDist = (Spinner) root.findViewById(R.id.max_distance);

        ArrayAdapter<CharSequence> distAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.dist_array, R.layout.spinner_item_layout);
        distAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        maxDist.setAdapter(distAdapter);
        maxDist.setOnItemSelectedListener(this);
        submit = (Button) root.findViewById(R.id.submit_offer);
        submit.setOnClickListener(this);

        youGo = (CheckBox) root.findViewById(R.id.youGo);
        theyCome = (CheckBox) root.findViewById(R.id.theyCome);
        priceYouGo = (TextView) root.findViewById(R.id.price_yougo);
        priceTheyCome = (TextView) root.findViewById(R.id.price_theycome);
        priceYouGo.setOnClickListener(this);
        priceTheyCome.setOnClickListener(this);

        addressCompl = (EditText) root.findViewById(R.id.address_complement);
        addressCompl.setOnFocusChangeListener(new Utils.MyFocusChangeListener(getActivity()));

        maxPortionsText = (Spinner) root.findViewById(R.id.max_portions);
        ArrayAdapter<Integer> portAdapter = new ArrayAdapter<Integer>(getActivity(),
                R.layout.spinner_item_layout, ports);
        portAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        maxPortionsText.setAdapter(portAdapter);
        maxPortionsText.setOnItemSelectedListener(this);

        minNoticeText = (Spinner) root.findViewById(R.id.minNoticeText);
        ArrayAdapter<CharSequence> noticeAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.minnotice_array, R.layout.spinner_item_layout);
        noticeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        minNoticeText.setAdapter(noticeAdapter);
        minNoticeText.setOnItemSelectedListener(this);

        List<Integer> dayIds = Arrays.asList(R.id.mon, R.id.tue, R.id.wed, R.id.thu, R.id.fri, R.id.sat, R.id.sun);
        daysAvailable = Arrays.asList(new Boolean[dayIds.size()]);
        dayToggles = Arrays.asList(new ToggleButton[dayIds.size()]);
        for (int i = 0; i < dayToggles.size(); i++) {
            ToggleButton toggle = (ToggleButton) root.findViewById(dayIds.get(i));
            dayToggles.set(i, toggle);
        }

        if (item != null) updateLayout();
        else {
            //set defaults
            DateTime ref = new DateTime().plusHours(3);
            registerDate(ref.getDayOfMonth(), ref.getMonthOfYear(), ref.getYear(), "oneoff");
            registerTime(ref.getHourOfDay(), 0);
            addressView.setText(me.getAppAddress());
        }

        return root;
    }

    private void updateLayout() {
        dishname.setText(getString(R.string.your_offer_for) + " " + item.getDish().getName());
        if (item.getPriceDel() != null) {
            String temp = item.getPriceDel().toString();
            String[] parts = temp.split("\\.");
            if (parts.length == 2 && (parts[1].equals("") || parts[1].equals("0"))) temp = parts[0];
            priceYouGo.setText(temp);
            youGo.setChecked(true);
            currencyYouGoText.setText(CurrencyUtils.getSymbolFromCode(item.getDelCurrency()));
            maxDistRow.setVisibility(View.VISIBLE);
        }
        if (item.getPriceNoDel() != null) {
            String temp = item.getPriceNoDel().toString();
            String[] parts = temp.split("\\.");
            if (parts.length == 2 && (parts[1].equals("") || parts[1].equals("0"))) temp = parts[0];
            priceTheyCome.setText(temp);
            theyCome.setChecked(true);
            currencyTheyComeText.setText(CurrencyUtils.getSymbolFromCode(item.getNoDelCurrency()));
            addressCompl.setVisibility(View.VISIBLE);
        }
        if (item.getAddress() != null) {
            addressView.setText(item.getAddress());
            addressView.dismissDropDown();
            startDateText.requestFocus();
        }
        if (item.getAddressDetails() != null) addressCompl.setText(item.getAddressDetails());
        if (item.getStartDate() != null) {
            if (!item.getOneoff()) {
                startDate = new DateTime(item.getStartDate());
                registerDate(startDate.getDayOfMonth(), startDate.getMonthOfYear(), startDate.getYear(), "start");
                registerTime(startDate.getHourOfDay(), startDate.getMinuteOfHour());
            } else {
                oneOffDate = new DateTime(item.getStartDate());
                registerDate(oneOffDate.getDayOfMonth(), oneOffDate.getMonthOfYear(), oneOffDate.getYear(), "oneoff");
                registerTime(oneOffDate.getHourOfDay(), oneOffDate.getMinuteOfHour());
            }
        }
        if (item.getEndDate() != null) {
            endDate = new DateTime(item.getEndDate());
            registerDate(endDate.getDayOfMonth(), endDate.getMonthOfYear(), endDate.getYear(), "end");
        }
        if (item.getDaysAvailable() != null) {
            daysAvailable = item.getDaysAvailable();
            for (int i = 0; i < daysAvailable.size(); i++) {
                if (daysAvailable.get(i) != null)
                    dayToggles.get(i).setChecked(daysAvailable.get(i));
            }
        }
        periodicCheckBox.setChecked(!item.getOneoff());
        if (!item.getOneoff()) {
            periodicRow.setVisibility(View.VISIBLE);
        }
        if (item.getPortions() != null)
            maxPortionsText.setSelection(item.getPortions() - 1);

        maxDistMeters = item.getMaxDist();
        int temp = 0;
        for (int i = 0; i < dists.length; i++) {
            if (dists[i] == maxDistMeters) temp = i;
        }
        maxDist.setSelection(temp);

        minNotice = item.getMinNotice();
        temp = 0;
        for (int i = 0; i < minnotices.length; i++) {
            if (minnotices[i] == minNotice) temp = i;
        }
        minNoticeText.setSelection(temp);
    }

    public C4Dish getDish() {
        return dish;
    }

    public void setDish(C4Dish dish) {
        this.dish = dish;
    }

    @Override
    public void registerTime(int hour, int minute) {
        timeText.setText(StringUtils.formatTime(hour, minute));
        this.hour = hour;
        this.min = minute;
    }

    @Override
    public void registerDate(int day, int month, int year, String type) {
        if (type.equals("start")) {
            startDay = day;
            startMonth = month;
            startYear = year;
            startDateText.setText(StringUtils.formatDate(getResources(), day, month, year, true));
        } else if (type.equals("end")) {
            endDay = day;
            endMonth = month;
            endYear = year;
            endDateText.setText(StringUtils.formatDate(getResources(), day, month, year, true));
        } else if (type.equals("oneoff")) {
            oneOffDay = day;
            oneOffMonth = month;
            oneOffYear = year;
            oneOffDateText.setText(StringUtils.formatDate(getResources(), day, month, year, true));
        }
    }

    @Override
    public void onClick(View arg0) {
        final Fragment thisFrag = this;
        switch (arg0.getId()) {

            case R.id.youGoText:
                youGo.setChecked(true);
                break;

            case R.id.theyComeText:
                theyCome.setChecked(true);
                break;

            case R.id.timeText:
                TimeDialog.newInstance(thisFrag, hour, min).show(getFragmentManager(),
                        "time");
                break;

            case R.id.oneoffDateText:
                if (oneOffDay == null) {
                    DateDialog.newInstance(thisFrag, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH),
                            cal.get(Calendar.YEAR), "oneoff").show(getFragmentManager(), "date");
                } else {
                    DateDialog.newInstance(thisFrag, oneOffDay, oneOffMonth - 1,
                            oneOffYear, "oneoff").show(getFragmentManager(), "date");
                }
                break;

            case R.id.startDateText:
                if (startDay == null) {
                    DateDialog.newInstance(thisFrag, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH),
                            cal.get(Calendar.YEAR), "start").show(getFragmentManager(), "date");
                } else {
                    DateDialog.newInstance(thisFrag, startDay, startMonth - 1,
                            startYear, "start").show(getFragmentManager(), "date");
                }
                break;

            case R.id.endDateText:
                if (endDay == null) {
                    DateDialog.newInstance(thisFrag, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH),
                            cal.get(Calendar.YEAR), "end").show(getFragmentManager(), "date");
                } else {
                    DateDialog.newInstance(thisFrag, endDay, endMonth - 1,
                            endYear, "end").show(getFragmentManager(), "date");
                }
                break;

            case R.id.price_yougo:
                if (priceYouGo.getText() != null && currencyYouGoText.getText() != null) {
                    String currcode = CurrencyUtils.getCodeFromSymbol((String) currencyYouGoText.getText());
                    Float price = null;
                    String ps = priceYouGo.getText().toString();
                    if (!ps.isEmpty()) {
                        if (ps.equals(getString(R.string.free))) {
                            price = 0F;
                        } else {
                            price = Float.parseFloat(priceYouGo.getText().toString());
                        }
                    }
                    PriceDialog.newInstance(this, price, currcode, "yougo").show(getFragmentManager(), "price");
                } else {
                    PriceDialog.newInstance(this, null, defCurr, "yougo").show(getFragmentManager(), "price");
                }
                break;

            case R.id.price_theycome:
                if (priceTheyCome.getText() != null && currencyTheyComeText.getText() != null) {
                    String currcode = CurrencyUtils.getCodeFromSymbol((String) currencyTheyComeText.getText());
                    Float price = null;
                    String ps = priceTheyCome.getText().toString();
                    if (!ps.isEmpty()) {
                        if (ps.equals(getString(R.string.free))) {
                            price = 0F;
                        } else {
                            price = Float.parseFloat(ps);
                        }
                    }
                    PriceDialog.newInstance(this, price, currcode, "theycome").show(getFragmentManager(), "price");
                } else {
                    PriceDialog.newInstance(this, null, defCurr, "theycome").show(getFragmentManager(), "price");
                }
                break;

            case R.id.submit_offer:
                if (!youGo.isChecked() && !theyCome.isChecked()) {
                    Toast.makeText(getActivity(), getString(R.string.choose_delivery_option), Toast.LENGTH_LONG).show();
                    break;
                }
                if (youGo.isChecked() && priceYouGo.getText().toString().equals("") || theyCome.isChecked() && priceTheyCome.getText().toString().equals("")) {
                    Toast.makeText(getActivity(), getString(R.string.prices_not_complete), Toast.LENGTH_LONG).show();
                    break;
                }
                if (timeText.getText().toString().equals("")) {
                    Toast.makeText(getActivity(), getString(R.string.insert_time_availability), Toast.LENGTH_LONG).show();
                    break;
                }
                DateTime today = new DateTime();
                if (periodicCheckBox.isChecked()) {
                    for (int i = 0; i < dayToggles.size(); i++) {
                        daysAvailable.set(i, dayToggles.get(i).isChecked());
                    }
                    if (startDay == null) {
                        Toast.makeText(getActivity(), getString(R.string.choose_start_date), Toast.LENGTH_LONG).show();
                        break;
                    } else {
                        startDate = new DateTime(startYear, startMonth, startDay, hour, min);
                        if (startDate.isBefore(today) && !periodicCheckBox.isChecked()) {
                            Toast.makeText(getActivity(), getString(R.string.offerdate_past), Toast.LENGTH_LONG).show();
                            break;
                        }
                    }
                    if (endDay == null) {
                        Toast.makeText(getActivity(), getString(R.string.choose_end_date), Toast.LENGTH_LONG).show();
                        break;
                    } else {
                        endDate = new DateTime(endYear, endMonth, endDay, hour, min);
                        if (startDate.isAfter(endDate)) {
                            Toast.makeText(getActivity(), getString(R.string.end_date_before_start), Toast.LENGTH_LONG).show();
                            break;
                        }
                        if (endDate.isBefore(today)) {
                            Toast.makeText(getActivity(), getString(R.string.enddate_past), Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    if (oneOffDay == null) {
                        Toast.makeText(getActivity(), getString(R.string.choose_date), Toast.LENGTH_LONG).show();
                        break;
                    } else {
                        oneOffDate = new DateTime(oneOffYear, oneOffMonth, oneOffDay, hour, min);
                        if (oneOffDate.isBefore(today)) {
                            Toast.makeText(getActivity(), getString(R.string.date_past), Toast.LENGTH_LONG).show();
                        }
                    }
                }
                String address = addressView.getText().toString();
                addressId = addressAdapter.placeIds.get(address);
                if (address.equals("") || address.equals(me.getAddress())
                        || (item != null && address.equals(item.getAddress()))) {
                    //if (theyCome.isChecked()){
                    //	Toast.makeText(getActivity(), "You must insert an address" , Toast.LENGTH_LONG).show();
                    //	break;
                    //}
                } else if (addressId == null) {
                    Toast.makeText(getActivity(), getString(R.string.error_choose_from_list), Toast.LENGTH_LONG).show();
                    break;
                }
                if (maxPortions == null) {
                    Toast.makeText(getActivity(), getString(R.string.insert_portions), Toast.LENGTH_LONG).show();
                    break;
                } else if (maxPortions == 0) {
                    Toast.makeText(getActivity(), getString(R.string.error_portions_positive), Toast.LENGTH_LONG).show();
                    break;
                }
                Utils.hideKeyboard(getActivity());
                new SendOffer().execute();
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        switch (parent.getId()) {
            case R.id.max_distance:
                maxDistMeters = dists[pos];
                break;

            case R.id.minNoticeText:
                minNotice = minnotices[pos];
                break;

            case R.id.max_portions:
                maxPortions = ports.get(pos);
                break;
        }
    }


    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }

    @Override
    public void onCheckedChanged(CompoundButton arg0, boolean checked) {
        switch (arg0.getId()) {

            case R.id.youGo:
                if (arg0.isChecked()) {
                    maxDistRow.setVisibility(View.VISIBLE);
                    if (priceYouGo.getText() == null || priceYouGo.getText().toString().isEmpty()) {
                        PriceDialog.newInstance(this, null, defCurr, "yougo").show(getFragmentManager(), "price");
                    }
                } else {
                    maxDistRow.setVisibility(View.GONE);
                    currencyYouGoText.setText("");
                    priceYouGo.setText("");
                }
                break;

            case R.id.theyCome:
                if (arg0.isChecked()) {
                    addressCompl.setVisibility(View.VISIBLE);
                    if (priceTheyCome.getText() == null || priceTheyCome.getText().toString().isEmpty()) {
                        PriceDialog.newInstance(this, null, defCurr, "theycome").show(getFragmentManager(), "price");
                    }
                } else {
                    addressCompl.setVisibility(View.GONE);
                    priceTheyCome.setText("");
                    currencyTheyComeText.setText("");
                }
                break;

            case R.id.periodic_checkbox:
                if (arg0.isChecked()) {
                    periodicRow.setVisibility(View.VISIBLE);
                    oneOffDateText.setVisibility(View.GONE);
                } else {
                    periodicRow.setVisibility(View.GONE);
                    oneOffDateText.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @Override
    public void registerPrice(Float price, String type) {
        if (type.equals("theycome")) {
            if (price == 0F) {
                priceTheyCome.setText(getString(R.string.free));
                currencyTheyComeText.setVisibility(View.INVISIBLE);
            } else {
                priceTheyCome.setText(price.toString());
                currencyTheyComeText.setVisibility(View.VISIBLE);
            }

        } else if (type.equals("yougo")) {
            if (price == 0F) {
                priceYouGo.setText(getString(R.string.free));
                currencyYouGoText.setVisibility(View.INVISIBLE);
            } else {
                priceYouGo.setText(price.toString());
                currencyYouGoText.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void registerCurrency(String currency, String type) {
        if (type.equals("theycome")) {
            currencyTheyComeText.setText(CurrencyUtils.getSymbolFromCode(currency));
        } else if (type.equals("yougo")) {
            currencyYouGoText.setText(CurrencyUtils.getSymbolFromCode(currency));
        }
    }

    @Override
    public void unCheckOption(String type) {
        if (type.equals("theycome")) {
            theyCome.setChecked(false);
        } else if (type.equals("yougo")) {
            youGo.setChecked(false);
        }
    }

    class SendOffer extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        private static final String LOG_TAG = "SendOfferAsync";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), getString(R.string.wait), getString(R.string.uploading_offer) + "...");
        }

        protected String doInBackground(String... urls) {

            String response = createItem();
            if (response.equals("NO_OFFERS")) return response;
            if (item.getLatitude() == null) return "NO_LOCATION";

            //upload offer
            HttpContext context = HttpContext.getInstance();
            try {
                RestTemplate restTemplate = context.getDefaultRestTemplate();
                String path = "offer";
                ResponseEntity<String> responseEntity = restTemplate.exchange(context.getBaseUrl() + path, HttpMethod.POST,
                        new HttpEntity<C4Item>(item, context.getDefaultHeaders()), String.class);
                response = responseEntity.getBody();
                Log.d(LOG_TAG, "Registered item " + response);
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getLocalizedMessage(), e);
                LogsToServer.send(e);
                return null;
            }

            return response;
        }

        protected void onPostExecute(String response) {
            if (!isAdded()) return;
            progressDialog.dismiss();
            if (response == null) {
                Toast.makeText(getActivity(), getString(R.string.problems_offer_upload), Toast.LENGTH_LONG).show();
                return;
            }
            if (response.equals("NO_OFFERS")) {
                Toast.makeText(getActivity(), getString(R.string.no_future_offers), Toast.LENGTH_LONG).show();
            } else if (response.equals("NO_LOCATION")) {
                Toast.makeText(getActivity(), getString(R.string.insert_address), Toast.LENGTH_LONG).show();
            } else if (response.equals("OK")) {
                DateTime today = new DateTime();
                DateTime next = item.nextOffer(today);
                Period period = new Period(today, next);
                String deltatime = StringUtils.periodString(getResources(), period, false);
                if (deltatime.equals("")) deltatime = getString(R.string.now) + ".";
                else deltatime += getString(R.string.from_now) + ".";
                Toast.makeText(getActivity(), getString(R.string.next_offer) + ": " + deltatime, Toast.LENGTH_LONG).show();
                ((MainActivity) getActivity()).refresh(true, true, false, false);
                getParentFragment().getChildFragmentManager().popBackStack();
            }
        }
    }

    private String createItem() {
        Double lat = me.getAppLatitude();
        Double lng = me.getAppLongitude();
        String[] address = {me.getAppAddress(), me.getAppCity()};

        if (addressId != null) {
            List<Double> coordinates = LocationUtils.getCoordinates(getActivity(), addressId, 4);
            if (coordinates != null) {
                lat = coordinates.get(0);
                lng = coordinates.get(1);
                address = LocationUtils.getAddress(getActivity(), lat, lng);
            }
        } else if (item != null) {
            lat = item.getLatitude();
            lng = item.getLongitude();
            String[] address1 = {item.getAddress(), item.getCity()};
            address = address1;
        }

        if (item == null) item = new C4Item();

        item.setCook(new C4User(me.getId()));
        item.setDish(new C4Dish(dish.getId()));
        String price = priceYouGo.getText().toString();
        Float priceDel = getPriceFromView(priceYouGo);
        item.setPriceDel(priceDel);
        item.setDelCurrency(price.equals("") ? null : CurrencyUtils.getCodeFromSymbol(currencyYouGoText.getText().toString()));
        price = priceTheyCome.getText().toString();
        Float priceNoDel = getPriceFromView(priceTheyCome);
        item.setPriceNoDel(priceNoDel);
        item.setNoDelCurrency(price.equals("") ? null : CurrencyUtils.getCodeFromSymbol(currencyTheyComeText.getText().toString()));
        item.setLatitude(lat);
        item.setLongitude(lng);
        item.setCity(address[1]);
        item.setTimeZone(DateTimeZone.getDefault().getID());

        if (!addressView.getText().toString().equals(""))
            item.setAddress(addressView.getText().toString());
        else item.setAddress(address[0]);
        item.setAddressDetails(addressCompl.getText().toString());
        item.setOneoff(!periodicCheckBox.isChecked());
        if (item.getOneoff()) item.setStartDate(oneOffDate.toDate());
        else item.setStartDate(startDate.toDate());
        if (endDate != null) item.setEndDate(endDate.toDate());
        item.setDaysAvailable(daysAvailable);

        item.setPortionsOrdered(null);
        item.setPortions(maxPortions);
        item.setMinNotice(minNotice);
        item.setMaxDist(maxDistMeters);

        SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit();
        prefsEditor.putString("defCurrency", item.getNoDelCurrency());
        prefsEditor.commit();

        if (item.nextOffer(new DateTime()) == null) return "NO_OFFERS";
        else return "OK";
    }

    private Float getPriceFromView(TextView view) {
        String price = view.getText().toString();
        Float p = null;
        if (!price.equals("")) {
            if (price.equals(getString(R.string.free))) {
                p = 0F;
            } else {
                p = Float.valueOf(price);
            }
        }
        return p;
    }

    public C4Item getItem() {
        return item;
    }

    public void setItem(C4Item item) {
        this.item = item;
    }

}

