package com.beppeben.cook4.ui;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
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
import com.beppeben.cook4.domain.C4Dish;
import com.beppeben.cook4.domain.C4Image;
import com.beppeben.cook4.domain.C4Tag;
import com.beppeben.cook4.domain.C4User;
import com.beppeben.cook4.utils.Globals;
import com.beppeben.cook4.utils.LogsToServer;
import com.beppeben.cook4.utils.TagsUtils;
import com.beppeben.cook4.utils.Utils;
import com.beppeben.cook4.utils.net.HttpContext;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;


public class NewDishFragment extends Fragment implements OnClickListener, OnItemClickListener {

    public static final int NUM_OF_COLUMNS = 3;
    public static final int GRID_PADDING = 8;

    private List<C4Tag> tags;
    private AutoCompleteTextView tagText;
    private List<C4Tag> chosenTags = new ArrayList<C4Tag>();
    private LinearLayout tagContainer;
    private String dishName;
    private EditText dishText;
    private EditText descText;
    private String description;
    public GridViewImageAdapter adapter;
    private ExpandableHeightGridView gridView;
    private int columnWidth;
    private Long dishId;
    private List<Long> picIds = new ArrayList<Long>();
    private Long coverId;

    public NewDishFragment() {

    }

    public static NewDishFragment newInstance() {
        NewDishFragment fragment = new NewDishFragment();
        return fragment;
    }

    public static NewDishFragment newInstance(C4Dish dish) {
        NewDishFragment fragment = new NewDishFragment();
        fragment.dishName = dish.getName();
        fragment.description = dish.getDescription();
        fragment.chosenTags = TagsUtils.getTags(dish.obtainStringTags(), false);
        fragment.dishId = dish.getId();
        fragment.picIds = dish.getPicIds();
        fragment.coverId = dish.getCoverId();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tags = TagsUtils.getTags();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout root = (RelativeLayout) inflater.inflate(R.layout.fragment_newdish, container, false);

        dishText = (EditText) root.findViewById(R.id.dishname);
        if (dishName != null) {
            dishText.setText(dishName);
            dishText.setClickable(false);
            dishText.setInputType(InputType.TYPE_NULL);
        }
        descText = (EditText) root.findViewById(R.id.description);
        if (description != null) {
            descText.setText(description);
        }
        tagContainer = (LinearLayout) root.findViewById(R.id.tagContainer);

        tagText = (AutoCompleteTextView) root.findViewById(R.id.autocompleteTags);

        if (tags != null) {
            ArrayAdapter<C4Tag> tagsAdapter =
                    new ArrayAdapter<C4Tag>(getActivity(), android.R.layout.simple_list_item_1, tags);
            tagText.setAdapter(tagsAdapter);
        } else new TagsUtils.WaitForTags(getActivity(), tags, tagText).execute();

        tagText.setThreshold(1);
        tagText.setOnItemClickListener(this);

        Button submit = (Button) root.findViewById(R.id.submit);
        submit.setOnClickListener(this);

        gridView = (ExpandableHeightGridView) root.findViewById(R.id.grid_view);
        gridView.setExpanded(true);
        InitializeGridLayout();
        boolean downloadimages = true;
        if (adapter == null)
            adapter = new GridViewImageAdapter(getActivity(), columnWidth, picIds, coverId);
        else {
            adapter.setImageWidth(columnWidth);
            downloadimages = false;
        }
        gridView.setAdapter(adapter);
        adapter.setAdapter(adapter, downloadimages);

        TagsUtils.refreshTagViews(tagContainer, chosenTags, getActivity(), true);

        return root;
    }

    private void InitializeGridLayout() {
        Resources r = getResources();
        float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, GRID_PADDING, r.getDisplayMetrics());
        float mainpadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 35, r.getDisplayMetrics());

        columnWidth = (int) Math.floor(((Utils.getScreenWidth(getActivity()) - ((NUM_OF_COLUMNS - 1) * padding)
                - mainpadding) / (double) NUM_OF_COLUMNS));

        gridView.setNumColumns(NUM_OF_COLUMNS);
        gridView.setColumnWidth(columnWidth);
        gridView.setPadding(0, (int) padding, 0, (int) padding);
        gridView.setHorizontalSpacing((int) padding);
        gridView.setVerticalSpacing((int) padding);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == 1 && null != data) {
            adapter.addImage(data.getData());
            adapter.notifyDataSetChanged();
        }
        if (resultCode == Activity.RESULT_OK && requestCode == 2) {
            try {
                Uri uri = Uri.fromFile(GridViewImageAdapter.photoFile);
                adapter.addImage(uri);
                adapter.notifyDataSetChanged();
            } catch (Exception ex) {
                Log.e("NewDishFragment", ex.getLocalizedMessage(), ex);
                LogsToServer.send(ex);
            }
        }
    }

    public class AddDishTask extends AsyncTask<Void, Void, String> {

        private final String LOG_TAG = AddDishTask.class.getName();

        C4Dish dish;
        ProgressDialog progressDialog;

        public AddDishTask(C4Dish dish) {
            super();
            this.dish = dish;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), getString(R.string.wait), getString(R.string.uploading_dish) + "...");
        }

        @Override
        protected String doInBackground(Void... params) {
            List<C4Image> imagesToUpload = adapter.getImagesToUpload();
            Long coverId = adapter.getCoverId();

            List<Long> idsToRemove = adapter.getIdsToRemove();
            String idsToRemoveString = "";
            for (int i = 0; i < idsToRemove.size(); i++) {
                idsToRemoveString += idsToRemove.get(i).toString();
                if (i != idsToRemove.size() - 1) idsToRemoveString += "-";
            }

            HttpContext context = HttpContext.getInstance();
            String response = "OK";
            try {
                RestTemplate restTemplate = context.getDefaultRestTemplate();

                //Upload/Modify dish info
                String path = "dish/cover=" + coverId;
                ResponseEntity<String> responseEntity = restTemplate.exchange(context.getBaseUrl() + path, HttpMethod.POST,
                        new HttpEntity<C4Dish>(dish, context.getDefaultHeaders()), String.class);
                String id = responseEntity.getBody();
                if (id == null) return null;

                //Upload new images and notify cover changes
                if (imagesToUpload.size() != 0) {
                    Log.d(LOG_TAG, "uploading " + imagesToUpload.size() + " images");
                    path = "image/dish=" + id + "/cover=" + coverId;
                    responseEntity = restTemplate.exchange(context.getBaseUrl() + path, HttpMethod.POST,
                            new HttpEntity<List<C4Image>>(imagesToUpload, context.getDefaultHeaders()), String.class);
                }

                //Delete images
                if (idsToRemove.size() != 0) {
                    path = "imgdel";
                    responseEntity = restTemplate.exchange(context.getBaseUrl() + path, HttpMethod.POST,
                            new HttpEntity<String>(idsToRemoveString, context.getDefaultHeaders()), String.class);
                }

                Log.d(LOG_TAG, "Dish registered successfully");
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getLocalizedMessage(), e);
                LogsToServer.send(e);
                response = null;
            }

            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            progressDialog.dismiss();
            if (!isAdded()) return;
            if (response != null)
                Toast.makeText(getActivity(), getString(R.string.dish_saved), Toast.LENGTH_LONG).show();

            else
                Toast.makeText(getActivity(), getString(R.string.problems_dish_registration), Toast.LENGTH_LONG).show();

            try {
                ((MainActivity) getActivity()).refresh(false, true, false, false);
                getParentFragment().getChildFragmentManager().popBackStack();
            } catch (Exception e) {
                //anything related to activity detached or onSaveInstanceState called, just avoids useless crash
            }
        }
    }

    @Override
    public void onClick(View arg0) {
        Utils.hideKeyboard(getActivity());
        C4Dish newdish = new C4Dish();
        dishName = dishText.getText().toString();
        description = descText.getText().toString();
        if (dishName == null || dishName.equals("")) {
            Toast.makeText(getActivity(), getString(R.string.insert_dish_name), Toast.LENGTH_LONG).show();
            return;
        }
        if (dishId == null) {
            newdish.setOrders(0);
        } else newdish.setId(dishId);
        newdish.setName(dishName);
        newdish.setDishtags(TagsUtils.toDefaultTags(chosenTags));
        newdish.setDescription(description);
        C4User user = new C4User();
        user.setId(Globals.getMe(getActivity()).getId());
        newdish.setUser(user);
        newdish.setPicIds(picIds);
        newdish.setCoverId(coverId);

        new AddDishTask(newdish).execute();
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
}