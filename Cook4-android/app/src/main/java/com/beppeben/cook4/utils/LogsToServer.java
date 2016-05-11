package com.beppeben.cook4.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.beppeben.cook4.utils.net.HttpContext;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.PrintWriter;
import java.io.StringWriter;

public class LogsToServer {

    private static boolean ACTIVE = true;

    private static void send(Long id, Throwable e) {
        if (ACTIVE) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            new SendLogTask(id, exceptionAsString).execute();
        }
    }

    private static void send(Long id, String message) {
        if (ACTIVE) {
            new SendLogTask(id, message).execute();
        }
    }

    public static void send(Throwable e) {
        Long id = Globals.getMe(null).getId();
        if (id == null) id = -1L;
        LogsToServer.send(id, e);
    }

    public static void send(String message) {
        Long id = Globals.getMe(null).getId();
        if (id == null) id = -1L;
        LogsToServer.send(id, message);
    }


    public static class SendLogTask extends AsyncTask<Void, Void, String> {

        private final String LOG_TAG = SendLogTask.class.getName();
        private Long id;
        private String msg;

        public SendLogTask(Long id, String msg) {
            super();
            this.id = id;
            this.msg = msg;
        }

        @Override
        protected String doInBackground(Void... params) {
            HttpContext context = HttpContext.getInstance();
            String response = "";
            try {
                RestTemplate restTemplate = context.getDefaultRestTemplate();
                restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

                String path = "clientlogs/" + id;

                ResponseEntity<String> responseEntity = restTemplate.exchange(context.getBaseUrl() + path, HttpMethod.POST,
                        new HttpEntity<String>(msg, context.getDefaultHeaders()), String.class);

                response = responseEntity.getBody();
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getLocalizedMessage(), e);
            }

            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            if (response.equals("OK")) {
                Log.d(LOG_TAG, "Logs sent successfully");
            }
        }
    }

}
