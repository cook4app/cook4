package com.beppeben.cook4.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Point;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.beppeben.cook4.R;
import com.beppeben.cook4.domain.C4Report;
import com.beppeben.cook4.domain.C4User;
import com.beppeben.cook4.utils.net.HttpContext;
import com.facebook.share.model.ShareOpenGraphAction;
import com.facebook.share.model.ShareOpenGraphContent;
import com.facebook.share.model.ShareOpenGraphObject;
import com.facebook.share.widget.ShareDialog;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;


public class Utils {

    public static float distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371; //kilometers
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        float dist = (float) (earthRadius * c);
        return dist;
    }

    public static void setExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(null);
        Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler());
    }

    @SuppressLint("NewApi")
    public static int getScreenWidth(Context ctx) {
        return getScreenDim(ctx, true);
    }

    @SuppressLint("NewApi")
    public static int getScreenDim(Context ctx, boolean width) {
        int columnWidth;
        if (ctx == null) return 0;
        WindowManager wm = (WindowManager) ctx.getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        final Point point = new Point();
        try {
            display.getSize(point);
        } catch (java.lang.NoSuchMethodError ignore) { // Older device
            point.x = display.getWidth();
            point.y = display.getHeight();
        }
        columnWidth = point.x;
        if (!width) columnWidth = point.y;
        return columnWidth;
    }

    public static void hideKeyboard(Activity act) {
        if (act == null) return;
        View view = act.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void populateTagWidgets(LinearLayout ll, List<View> views, Context mContext) {
        ll.removeAllViews();
        int maxWidth = getScreenWidth(mContext) - 100;
        LinearLayout.LayoutParams params;
        LinearLayout newLL = new LinearLayout(mContext);
        newLL.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
        newLL.setGravity(Gravity.LEFT);
        newLL.setOrientation(LinearLayout.HORIZONTAL);
        int widthSoFar = 10;
        for (int i = 0; i < views.size(); i++) {
            LinearLayout LL = new LinearLayout(mContext);
            LL.setOrientation(LinearLayout.HORIZONTAL);
            LL.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
            LL.setLayoutParams(new LinearLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

            views.get(i).setLayoutParams(new ViewGroup.LayoutParams(0, 0));
            views.get(i).measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            params = new LinearLayout.LayoutParams(views.get(i).getMeasuredWidth(),
                    LayoutParams.WRAP_CONTENT);
            params.setMargins(5, 0, 5, 0);
            LL.addView(views.get(i), params);
            LL.measure(0, 0);
            widthSoFar += views.get(i).getMeasuredWidth() + 10;
            if (widthSoFar >= maxWidth) {
                ll.addView(newLL);
                newLL = new LinearLayout(mContext);
                newLL.setLayoutParams(new LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT));
                newLL.setOrientation(LinearLayout.HORIZONTAL);
                newLL.setGravity(Gravity.LEFT);
                params = new LinearLayout.LayoutParams(LL
                        .getMeasuredWidth(), LL.getMeasuredHeight());
                newLL.addView(LL, params);
                widthSoFar = LL.getMeasuredWidth();
            } else {
                newLL.addView(LL);
            }
        }
        ll.addView(newLL);
    }

    public static interface MyCallback {
        void callback();
    }

    public static class SetPaypalTask extends AsyncTask<Void, Void, String> {

        private final String LOG_TAG = SetPaypalTask.class.getName();
        private String email;
        private C4User me;
        private Context ctx;
        private MyCallback func;

        public SetPaypalTask(Context ctx, C4User me, String email, MyCallback func) {
            super();
            this.email = email;
            this.ctx = ctx;
            this.me = me;
            this.func = func;
        }

        @Override
        protected String doInBackground(Void... params) {
            if (!EmailValidator.getInstance().isValid(email)) {
                return "INVALID";
            }
            if (!Globals.registered) return null;

            HttpContext context = HttpContext.getInstance();
            try {
                RestTemplate restTemplate = context.getDefaultRestTemplate();
                String path = "setpayemail/id=" + me.getId() + "&email=" + email;
                ResponseEntity<String> responseEntityDish = restTemplate.exchange(context.getBaseUrl() + path, HttpMethod.GET,
                        new HttpEntity<String>(context.getDefaultHeaders()), String.class);
                String response = responseEntityDish.getBody();
                return response;
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getLocalizedMessage(), e);
                LogsToServer.send(e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            if (response != null) {
                if (response.equals("OK")) {
                    me.setPayEmail(email);
                    if (ctx != null) {
                        Toast.makeText(ctx, ctx.getString(R.string.paypal_set), Toast.LENGTH_SHORT).show();
                        if (func != null) func.callback();
                    }
                } else if (response.equals("INVALID")) {
                    Toast.makeText(ctx, ctx.getString(R.string.insert_valid_email), Toast.LENGTH_SHORT).show();
                } else if (response.equals("ERROR_ACCOUNT")) {
                    Toast.makeText(ctx, ctx.getString(R.string.error_paypal_invalid), Toast.LENGTH_SHORT).show();
                }
            } else if (ctx != null) {
                Toast.makeText(ctx, ctx.getString(R.string.error_paypal_account), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static class ReportTask extends AsyncTask<Void, Void, String> {

        private final String LOG_TAG = ReportTask.class.getName();
        private ProgressDialog progressDialog;
        private Context context;
        private C4Report report;

        public ReportTask(Context context, C4Report report) {
            super();
            this.context = context;
            this.report = report;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(context, context.getString(R.string.wait), context.getString(R.string.sending_report) + "...");
        }

        @Override
        protected String doInBackground(Void... params) {

            HttpContext context = HttpContext.getInstance();
            String response = null;
            try {
                RestTemplate restTemplate = context.getDefaultRestTemplate();
                String path = "report";
                ResponseEntity<String> userEntity = restTemplate.exchange(context.getBaseUrl() + path, HttpMethod.POST,
                        new HttpEntity<C4Report>(report, context.getDefaultHeaders()), String.class);
                response = userEntity.getBody();
                Log.d(LOG_TAG, "Sending report from user id " + report.getFromUserId() + " to user id " + report.getToUserId() + " for dish id " +
                        report.getToDishId());
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception while sending report", e);
                Log.e(LOG_TAG, e.getLocalizedMessage(), e);
                LogsToServer.send(e);
            }

            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            progressDialog.dismiss();
            if (response != null && response.equals("OK"))
                Toast.makeText(context, context.getString(R.string.report_sent), Toast.LENGTH_LONG).show();
            else
                Toast.makeText(context, context.getString(R.string.problems_report), Toast.LENGTH_LONG).show();
        }
    }

    public static Float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    public static class MyFocusChangeListener implements View.OnFocusChangeListener {

        Activity act;

        public MyFocusChangeListener(Activity act) {
            this.act = act;
        }

        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus && act != null) {
                hideKeyboard(act);
            }
        }
    }

    public static void showPrivilege(C4User user, TextView txt, boolean reduced, Context ctx) {
        String privilege = user.getPrivilege();
        if (privilege != null) {
            if (privilege.equals("certified_cook")) {
                txt.setTextColor(ctx.getResources().getColor(R.color.DarkGreen));
                if (reduced) {
                    txt.setText(ctx.getString(R.string.certified));
                } else {
                    txt.setText(ctx.getString(R.string.certified_cook));
                }
            } else if (privilege.equals("amateur")) {
                txt.setTextColor(ctx.getResources().getColor(R.color.Orange));
                if (reduced) {
                    txt.setText(ctx.getString(R.string.amateur));
                } else {
                    txt.setText(ctx.getString(R.string.amateur_cook));
                }
            }
        }
    }

    public static void shareFbStory(Fragment frag, String dishname, String description, Long photoId) {
        ShareOpenGraphObject.Builder ob = new ShareOpenGraphObject.Builder()
                .putString("og:type", "cook_for_app:offer")
                .putString("og:title", dishname)
                .putString("cook_for_app:dish", dishname)
                .putString("og:description", description)
                .putString("og:url", "https://play.google.com/store/apps/details?id=com.beppeben.cook4");
        if (photoId != null) {
            ob.putString("og:image", "http://cook4.ddns.net/services/image-" + photoId);
        }
        ShareOpenGraphObject object = ob.build();
        ShareOpenGraphAction action = new ShareOpenGraphAction.Builder()
                .setActionType("cook_for_app:post")
                .putObject("offer", object)
                .build();
        ShareOpenGraphContent content = new ShareOpenGraphContent.Builder()
                .setPreviewPropertyName("offer")
                .setAction(action)
                .build();
        ShareDialog.show(frag, content);
    }

    public static int longToInt(long value) {
        while (value > Integer.MAX_VALUE) {
            value -= Integer.MAX_VALUE;
        }
        return (int) value;
    }
}
