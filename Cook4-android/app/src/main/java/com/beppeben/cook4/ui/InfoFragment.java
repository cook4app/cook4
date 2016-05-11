package com.beppeben.cook4.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.beppeben.cook4.R;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class InfoFragment extends Fragment {

    public InfoFragment() {
    }

    private String filename;

    public static InfoFragment newInstance(String filename) {
        InfoFragment frag = new InfoFragment();
        frag.filename = filename;
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_info, container, false);
        WebView webView = (WebView) rootView.findViewById(R.id.webview);
        Set<String> languages = getLanguages();
        String lang = Locale.getDefault().getDisplayLanguage().substring(0, 2).toLowerCase();
        if (filename == null) filename = "info";
        if (languages.contains(lang)) {
            webView.loadUrl("file:///android_asset/" + filename + "-" + lang + ".html");
        } else {
            webView.loadUrl("file:///android_asset/" + filename + ".html");
        }

        return rootView;
    }

    private Set<String> getLanguages() {
        android.content.res.Resources res = getResources();
        android.content.res.TypedArray lev = res.obtainTypedArray(R.array.languages);
        Set languages = new HashSet<String>();
        for (int i = 0; i < lev.length(); i++) {
            languages.add(lev.getString(i));
        }
        lev.recycle();
        return languages;
    }
}