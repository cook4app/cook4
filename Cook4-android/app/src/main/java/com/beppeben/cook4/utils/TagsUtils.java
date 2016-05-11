package com.beppeben.cook4.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beppeben.cook4.R;
import com.beppeben.cook4.domain.C4Tag;
import com.beppeben.cook4.domain.C4TagList;
import com.beppeben.cook4.utils.net.HttpContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TagsUtils {

    private static List<C4Tag> tags;
    private static Map<String, C4Tag> defaulttagmap;
    private static Map<String, C4Tag> localtagmap;


    public static List<C4Tag> toDefaultTags(List<C4Tag> tags) {
        List<C4Tag> result = new ArrayList<C4Tag>();
        for (C4Tag t : tags) result.add(new C4Tag(t.getTag()));
        return result;
    }

    public static C4Tag getTag(String key, boolean local) {
        if (local) return localtagmap.get(key);
        else return defaulttagmap.get(key);
    }

    public static List<C4Tag> getTags(List<String> keys, boolean local) {
        List<C4Tag> result = new ArrayList<C4Tag>();
        for (String key : keys) {
            C4Tag t = getTag(key, local);
            if (t != null) result.add(t);
        }
        return result;
    }

    private static void createMaps() {
        defaulttagmap = new HashMap<String, C4Tag>();
        localtagmap = new HashMap<String, C4Tag>();
        for (C4Tag t : tags) {
            defaulttagmap.put(t.getTag(), t);
            localtagmap.put(t.getLocalTag(), t);
        }
    }

    public static void refreshTags(Context ctx, boolean forcedownload) {
        if (forcedownload || !loadTags(ctx)) {
            new GetTagsTask(ctx).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            return;
        }
    }

    public static List<C4Tag> getTags() {
        return tags;
    }


    public static class GetTagsTask extends AsyncTask<Void, Void, List<C4Tag>> {

        private final String LOG_TAG = GetTagsTask.class.getName();
        private Context ctx;

        public GetTagsTask(Context ctx) {
            super();
            this.ctx = ctx;
        }

        @Override
        protected List<C4Tag> doInBackground(Void... params) {
            HttpContext context = HttpContext.getInstance();
            C4Tag[] tags = null;
            List<C4Tag> tagList = null;
            try {
                RestTemplate restTemplate = context.getDefaultRestTemplate();
                String path = "tags/id=" + Globals.getMe(ctx).getId();
                ResponseEntity<C4Tag[]> responseEntity = restTemplate.exchange(context.getBaseUrl() + path, HttpMethod.GET,
                        new HttpEntity<C4Tag>(context.getDefaultHeaders()), C4Tag[].class);
                tags = responseEntity.getBody();
                tagList = Arrays.asList(tags);
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getLocalizedMessage(), e);
            }
            return tagList;
        }

        @Override
        protected void onPostExecute(List<C4Tag> tgs) {
            if (tgs != null) {
                tags = tgs;
                Collections.sort(tags);
                storeTags(ctx);
                createMaps();
            }
        }

    }


    public static void refreshTagViews(final LinearLayout tagContainer,
                                       final List<C4Tag> chosenTags, final Context ctx, final boolean big) {
        if (ctx == null) return;
        if (chosenTags.size() == 0) {
            tagContainer.setVisibility(View.GONE);
            return;
        }
        tagContainer.setVisibility(View.VISIBLE);
        List<View> tagViews = new ArrayList<View>();
        for (final C4Tag tag : chosenTags) {
            final View v = LayoutInflater.from(ctx).inflate(R.layout.tag_layout, null);
            tagViews.add(v);
            TextView t = (TextView) v.findViewById(R.id.tagname);
            t.setText(tag.getLocalTag());
            View c = v.findViewById(R.id.cross);
            if (!big) {
                t.setTextSize(12);
                c.setVisibility(View.GONE);
            }

            c.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    chosenTags.remove(tag);
                    refreshTagViews(tagContainer, chosenTags, ctx, big);
                }

            });
        }
        Utils.populateTagWidgets(tagContainer, tagViews, ctx);
    }


    public static class WaitForTags extends AsyncTask<String, Void, List<C4Tag>> {

        private List<C4Tag> tags;
        private AutoCompleteTextView tagText;
        private Context ctx;

        public WaitForTags(Context ctx, List<C4Tag> tags, AutoCompleteTextView tagText) {
            this.tags = tags;
            this.tagText = tagText;
            this.ctx = ctx;
        }

        protected List<C4Tag> doInBackground(String... urls) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            List<C4Tag> dtags = TagsUtils.getTags();
            return dtags;
        }

        protected void onPostExecute(List<C4Tag> dtags) {
            if (dtags != null) {
                tags = dtags;
                ArrayAdapter<C4Tag> tagsAdapter =
                        new ArrayAdapter<C4Tag>(ctx, android.R.layout.simple_list_item_1, tags);
                tagText.setAdapter(tagsAdapter);
            } else new WaitForTags(ctx, tags, tagText).execute();
        }
    }


    public static void storeTags(Context context) {
        if (tags == null) return;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = preferences.edit();
        ObjectMapper objectMapper = new ObjectMapper();
        String json = null;
        try {
            C4TagList list = new C4TagList(tags);
            json = objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        if (json != null) {
            prefsEditor.putString("tags", json);
            prefsEditor.commit();
        }
    }

    public static boolean loadTags(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = preferences.getString("tags", null);
        if (json == null) return false;
        try {
            C4TagList list = (C4TagList)
                    objectMapper.readValue(json, C4TagList.class);
            tags = list.getTags();
            createMaps();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
