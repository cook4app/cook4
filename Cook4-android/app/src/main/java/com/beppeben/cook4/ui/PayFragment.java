package com.beppeben.cook4.ui;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.beppeben.cook4.MainActivity;
import com.beppeben.cook4.R;
import com.beppeben.cook4.utils.LogsToServer;
import com.beppeben.cook4.utils.net.HttpContext;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;


public class PayFragment extends MyFragment {

    private String paykey;
    private String tempId;
    private boolean checking = false;

    public PayFragment() {
    }

    public static PayFragment newInstance(String tempId, String paykey) {
        PayFragment fragment = new PayFragment();
        fragment.tempId = tempId;
        fragment.paykey = paykey;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void update(boolean redownload) {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout root = (RelativeLayout) inflater.inflate(R.layout.fragment_pay, container, false);

        if (paykey.equals("0")) {
            //free dish
            new CheckTempTransaction().execute();
            return root;
        }

        final WebView webView = (WebView) root.findViewById(R.id.webview);
        final ProgressBar progressBar = (ProgressBar) root.findViewById(R.id.progressBar);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("https://www.paypal.com/webapps/adaptivepayment/flow/pay?paykey=" + paykey + "&expType=mini");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (!isAdded()) return;
                super.onPageFinished(view, url);
                if (url.contains("closewindow") && !checking) {
                    checking = true;
                    new CheckTempTransaction().execute();
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                System.out.println("overriding: " + url);
                view.loadUrl(url);
                return true;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                progressBar.setProgress(progress);
                if (progress == 100) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                }
            }
        });

        return root;
    }

    private void refreshAndBack() {
        ((MainActivity) getActivity()).refresh(true);
        ((MainActivity) getActivity()).backToRoot();
    }

    class CheckTempTransaction extends AsyncTask<String, Void, String> {
        private final String LOG_TAG = CheckTempTransaction.class.getName();

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), getString(R.string.wait), getString(R.string.confirming_transaction) + "...");
        }

        protected String doInBackground(String... urls) {
            HttpContext context = HttpContext.getInstance();
            String resp = null;
            try {
                RestTemplate restTemplate = context.getDefaultRestTemplate();
                restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
                String path = "paysuccess/id=" + tempId;
                ResponseEntity<String> responseEntity = restTemplate.exchange(context.getBaseUrl() + path, HttpMethod.GET,
                        new HttpEntity<>(context.getDefaultHeaders()), String.class);
                resp = responseEntity.getBody();
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getLocalizedMessage(), e);
                LogsToServer.send(e);
                return null;
            }
            return resp;
        }

        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            if (result == null || !result.equals("OK")) {
                Toast.makeText(getActivity(), getString(R.string.error_transaction), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), getActivity().getString(R.string.transaction_confirmed), Toast.LENGTH_SHORT).show();
            }
            refreshAndBack();
        }
    }

}

