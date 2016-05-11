package com.beppeben.cook4.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class ImageCache {

    static final int CACHE_SIZE = 20;
    private static SharedPreferences preferences;
    private static ObjectMapper objectMapper;
    private static SyncLruCache cache;

    public static void initialize(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        objectMapper = new ObjectMapper();
        String json = preferences.getString("cache", null);
        if (json != null) {
            try {
                cache = (SyncLruCache) objectMapper.readValue(json, SyncLruCache.class);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (cache != null) cache.makeSync();
        else cache = new SyncLruCache(CACHE_SIZE);
    }

    public static void save() {
        if (cache == null) return;
        Editor prefsEditor = preferences.edit();
        String json = null;
        try {
            json = objectMapper.writeValueAsString(cache);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        if (json != null) {
            prefsEditor.putString("cache", json);
            prefsEditor.commit();
        }
    }

    public static byte[] get(Long id) {
        if (cache == null) return null;
        return cache.syncImages.get(id);
    }

    public static void put(Long id, byte[] image) {
        if (cache == null) return;
        cache.syncImages.put(id, image);
    }


    private static class LruCache extends LinkedHashMap<Long, byte[]> {
        public int maxEntries;

        public LruCache(final int maxEntries) {
            super(maxEntries + 1, 1.0f, true);
            this.maxEntries = maxEntries;
        }

        public LruCache(Map<Long, byte[]> images, final int maxEntries) {
            super(maxEntries + 1, 1.0f, true);
            this.maxEntries = maxEntries;

            Iterator entries = images.entrySet().iterator();
            while (entries.hasNext()) {
                Entry thisEntry = (Entry) entries.next();
                Long key = (Long) thisEntry.getKey();
                byte[] value = (byte[]) thisEntry.getValue();
                put(key, value);
            }
        }

        @Override
        protected boolean removeEldestEntry(final Map.Entry<Long, byte[]> eldest) {
            return super.size() > maxEntries;
        }
    }

    private static class SyncLruCache {
        public Map<Long, byte[]> syncImages;
        public int maxEntries;

        public SyncLruCache(final int maxEntries) {
            syncImages = Collections.synchronizedMap(new LruCache(maxEntries));
            this.maxEntries = maxEntries;
        }

        public SyncLruCache() {
        }

        public void makeSync() {
            if (syncImages == null) return;
            syncImages = Collections.synchronizedMap(new LruCache(syncImages, maxEntries));
        }
    }

}
