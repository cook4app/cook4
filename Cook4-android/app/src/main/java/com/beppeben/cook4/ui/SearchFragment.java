package com.beppeben.cook4.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.beppeben.cook4.MainActivity;
import com.beppeben.cook4.R;
import com.beppeben.cook4.domain.C4Query;
import com.beppeben.cook4.domain.C4Tag;
import com.beppeben.cook4.domain.C4User;
import com.beppeben.cook4.utils.DateTimeHelper;
import com.beppeben.cook4.utils.Globals;
import com.beppeben.cook4.utils.LocationUtils;
import com.beppeben.cook4.utils.PlacesAutoCompleteAdapter;
import com.beppeben.cook4.utils.StringUtils;
import com.beppeben.cook4.utils.TagsUtils;
import com.beppeben.cook4.utils.Utils;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class SearchFragment extends MyFragment implements DateTimeHelper, OnClickListener, OnItemClickListener {

    private List<C4Tag> tags;
    private AutoCompleteTextView tagText, addressView;
    private List<C4Tag> chosenTags = new ArrayList<C4Tag>();
    private LinearLayout tagContainer;
    private PlacesAutoCompleteAdapter addressAdapter;
    private String addressId;
    private Calendar cal;
    private Integer hour, min, day, month, year;
    private EditText timeText, dateText, dishNameText;

    public SearchFragment() {

    }

    public static SearchFragment newInstance() {
        SearchFragment fragment = new SearchFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tags = TagsUtils.getTags();

        cal = Calendar.getInstance();
        hour = cal.get(Calendar.HOUR_OF_DAY);
        min = cal.get(Calendar.MINUTE);
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH) + 1;
        day = cal.get(Calendar.DAY_OF_MONTH);
    }

    @Override
    public void update(boolean redownload) {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout root = (RelativeLayout) inflater.inflate(R.layout.fragment_search, container, false);

        addressView = (AutoCompleteTextView) root.findViewById(R.id.address);
        addressAdapter = new PlacesAutoCompleteAdapter(getActivity(), android.R.layout.simple_list_item_1);
        addressView.setAdapter(addressAdapter);
        timeText = (EditText) root.findViewById(R.id.timeText);
        timeText.setOnClickListener(this);
        dateText = (EditText) root.findViewById(R.id.dateText);
        dishNameText = (EditText) root.findViewById(R.id.dishName);
        dateText.setOnClickListener(this);
        tagContainer = (LinearLayout) root.findViewById(R.id.tagContainer);
        tagText = (AutoCompleteTextView) root.findViewById(R.id.autocompleteTags);
        if (tags != null) {
            ArrayAdapter<C4Tag> tagsAdapter =
                    new ArrayAdapter<C4Tag>(getActivity(), android.R.layout.simple_list_item_1, tags);
            tagText.setAdapter(tagsAdapter);
        } else new TagsUtils.WaitForTags(getActivity(), tags, tagText).execute();
        tagText.setThreshold(1);
        tagText.setOnItemClickListener(this);
        TagsUtils.refreshTagViews(tagContainer, chosenTags, getActivity(), true);
        Button submit = (Button) root.findViewById(R.id.submit);
        submit.setOnClickListener(this);

        return root;
    }

    @Override
    public void onClick(View arg0) {

        final Fragment thisFrag = this;
        switch (arg0.getId()) {

            case R.id.timeText:
                TimeDialog.newInstance(thisFrag, hour, min).show(getFragmentManager(), "time");
                break;

            case R.id.dateText:
                DateDialog.newInstance(thisFrag, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH),
                        cal.get(Calendar.YEAR), "").show(getFragmentManager(), "date");
                break;

            case R.id.submit:
                Utils.hideKeyboard(getActivity());
                addressId = addressAdapter.placeIds.get(addressView.getText().toString());
                if (!addressView.getText().toString().equals("") && addressId == null) {
                    Toast.makeText(getActivity(), getString(R.string.error_choose_from_list), Toast.LENGTH_LONG).show();
                    break;
                }
                new SendQuery().execute();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        String tagString = ((TextView) arg1).getText().toString();
        C4Tag tag = TagsUtils.getTag(tagString, true);
        if (chosenTags.size() >= 3) {
            Toast.makeText(getActivity(), getString(R.string.too_many_tags), Toast.LENGTH_LONG).show();
            tagText.setText("");
            return;
        }
        if (chosenTags.contains(tag)) {
            Toast.makeText(getActivity(), getString(R.string.tag_chosen_already), Toast.LENGTH_LONG).show();
            tagText.setText("");
        } else {
            chosenTags.add(tag);
            TagsUtils.refreshTagViews(tagContainer, chosenTags, getActivity(), true);
            tagText.setText("");
        }
    }

    @Override
    public void registerTime(int hour, int minute) {
        timeText.setText(StringUtils.formatTime(hour, minute));
        this.hour = hour;
        this.min = minute;
    }

    @Override
    public void registerDate(int day, int month, int year, String type) {
        this.day = day;
        this.month = month;
        this.year = year;
        dateText.setText(StringUtils.formatDate(getResources(), day, month, year, true));

    }

    class SendQuery extends AsyncTask<String, Void, C4Query> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected C4Query doInBackground(String... urls) {

            C4Query query = new C4Query();
            C4User me = Globals.getMe(getActivity());
            Double lat = me.getAppLatitude();
            Double lng = me.getAppLongitude();

            if (addressId != null) {
                List<Double> coordinates = LocationUtils.getCoordinates(getActivity(), addressId, 4);
                if (coordinates == null) return null;
                lat = coordinates.get(0);
                lng = coordinates.get(1);
                String[] address = LocationUtils.getAddress(getActivity(), lat, lng);
                query.setAddress(addressView.getText().toString());
                if (address != null) query.setCity(address[1]);
            } else {
                query.setAddress(me.getAppAddress());
                query.setCity(me.getCity());
            }
            query.setLatitude(lat);
            query.setLongitude(lng);
            query.setTags(TagsUtils.toDefaultTags(chosenTags));
            DateTime queryDate = new DateTime(year, month, day, hour, min);
            query.setDate(queryDate.toDate());
            query.setDishName(dishNameText.getText().toString());

            return query;

        }

        protected void onPostExecute(C4Query query) {
            if (getActivity() == null) return;
            if (query == null) {
                Toast.makeText(getActivity(), getString(R.string.geolocation_problem), Toast.LENGTH_LONG).show();
            }
            if (query.getLatitude() == null) {
                Toast.makeText(getActivity(), getString(R.string.insert_address), Toast.LENGTH_LONG).show();
                return;
            }
            Cook4MeFragment parent = (Cook4MeFragment) getParentFragment();
            MainActivity act = (MainActivity) getActivity();
            if (query != null && parent != null && act != null) {
                act.launchQuery(query);
            }
        }
    }

}

