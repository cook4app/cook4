package com.beppeben.cook4server.utils;

import static com.beppeben.cook4server.utils.GCMConstants.GCM_SEND_ENDPOINT;
import static com.beppeben.cook4server.utils.GCMConstants.PARAM_COLLAPSE_KEY;
import static com.beppeben.cook4server.utils.GCMConstants.PARAM_DELAY_WHILE_IDLE;
import static com.beppeben.cook4server.utils.GCMConstants.PARAM_DRY_RUN;
import static com.beppeben.cook4server.utils.GCMConstants.PARAM_PAYLOAD_PREFIX;
import static com.beppeben.cook4server.utils.GCMConstants.PARAM_REGISTRATION_ID;
import static com.beppeben.cook4server.utils.GCMConstants.PARAM_RESTRICTED_PACKAGE_NAME;
import static com.beppeben.cook4server.utils.GCMConstants.PARAM_TIME_TO_LIVE;
import static com.beppeben.cook4server.utils.GCMConstants.TOKEN_CANONICAL_REG_ID;
import static com.beppeben.cook4server.utils.GCMConstants.TOKEN_ERROR;
import static com.beppeben.cook4server.utils.GCMConstants.TOKEN_MESSAGE_ID;
import com.beppeben.cook4server.utils.GCMResult.Builder;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper class to send messages to the GCM service using an API Key.
 */
public class GCMSender {

    protected static final String UTF8 = "UTF-8";

    protected static final int BACKOFF_INITIAL_DELAY = 1000;

    protected static final int MAX_BACKOFF_DELAY = 1024000;

    protected final Random random = new Random();
    protected static final Logger logger
            = Logger.getLogger(GCMSender.class.getName());

    public GCMSender() {
    }

    public String send(GCMMessage message, String registrationId) {
        try {
            send(message, registrationId, 3);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
            return null;
        }
        return "OK";
    }

    public GCMResult send(GCMMessage message, String registrationId, int retries)
            throws IOException {
        int attempt = 0;
        GCMResult result = null;
        int backoff = BACKOFF_INITIAL_DELAY;

        boolean tryAgain;
        do {
            attempt++;
            if (logger.isLoggable(Level.INFO)) {
                logger.info("Attempt #" + attempt + " to send message "
                        + message + " to regIds " + registrationId);
            }
            result = sendNoRetry(message, registrationId);
            tryAgain = result == null && attempt <= retries;
            if (tryAgain) {
                int sleepTime = backoff / 2 + random.nextInt(backoff);
                sleep(sleepTime);
                if (2 * backoff < MAX_BACKOFF_DELAY) {
                    backoff *= 2;
                }
            }
        } while (tryAgain);
        if (result == null) {
            throw new IOException("Could not send message after " + attempt
                    + " attempts");
        }
        return result;
    }

    public GCMResult sendNoRetry(GCMMessage message, String registrationId)
            throws IOException {
        StringBuilder body = newBody(PARAM_REGISTRATION_ID, registrationId);
        Boolean delayWhileIdle = message.isDelayWhileIdle();
        if (delayWhileIdle != null) {
            addParameter(body, PARAM_DELAY_WHILE_IDLE, delayWhileIdle ? "1" : "0");
        }
        Boolean dryRun = message.isDryRun();
        if (dryRun != null) {
            addParameter(body, PARAM_DRY_RUN, dryRun ? "1" : "0");
        }
        String collapseKey = message.getCollapseKey();
        if (collapseKey != null) {
            addParameter(body, PARAM_COLLAPSE_KEY, collapseKey);
        }
        String restrictedPackageName = message.getRestrictedPackageName();
        if (restrictedPackageName != null) {
            addParameter(body, PARAM_RESTRICTED_PACKAGE_NAME, restrictedPackageName);
        }
        Integer timeToLive = message.getTimeToLive();
        if (timeToLive != null) {
            addParameter(body, PARAM_TIME_TO_LIVE, Integer.toString(timeToLive));
        }
        for (Entry<String, String> entry : message.getData().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key == null || value == null) {
                logger.warning("Ignoring payload entry thas has null: " + entry);
            } else {
                key = PARAM_PAYLOAD_PREFIX + key;
                addParameter(body, key, URLEncoder.encode(value, UTF8));
            }
        }
        String requestBody = body.toString();
        logger.finest("Request body: " + requestBody);
        HttpURLConnection conn;
        int status;
        try {
            conn = post(GCM_SEND_ENDPOINT, requestBody);
            status = conn.getResponseCode();
        } catch (IOException e) {
            logger.log(Level.INFO, "IOException posting to GCM", e);
            return null;
        }
        if (status / 100 == 5) {
            logger.info("GCM service is unavailable (status " + status + ")");
            return null;
        }
        String responseBody;
        if (status != 200) {
            try {
                responseBody = getAndClose(conn.getErrorStream());
                logger.info("Plain post error response: " + responseBody);
            } catch (IOException e) {
                // ignore the exception since it will thrown an InvalidRequestException
                // anyways
                responseBody = "N/A";
                logger.log(Level.INFO, "Exception reading response: ", e);
            }
            throw new InvalidRequestException(status, responseBody);
        } else {
            try {
                responseBody = getAndClose(conn.getInputStream());
            } catch (IOException e) {
                logger.log(Level.WARNING, "Exception reading response: ", e);
                // return null so it can retry
                return null;
            }
        }
        String[] lines = responseBody.split("\n");
        if (lines.length == 0 || lines[0].equals("")) {
            throw new IOException("Received empty response from GCM service.");
        }
        String firstLine = lines[0];
        String[] responseParts = split(firstLine);
        String token = responseParts[0];
        String value = responseParts[1];
        if (token.equals(TOKEN_MESSAGE_ID)) {
            Builder builder = new GCMResult.Builder().messageId(value);
            // check for canonical registration id
            if (lines.length > 1) {
                String secondLine = lines[1];
                responseParts = split(secondLine);
                token = responseParts[0];
                value = responseParts[1];
                if (token.equals(TOKEN_CANONICAL_REG_ID)) {
                    builder.canonicalRegistrationId(value);
                } else {
                    logger.warning("Invalid response from GCM: " + responseBody);
                }
            }
            GCMResult result = builder.build();
            if (logger.isLoggable(Level.INFO)) {
                logger.info("Message created succesfully (" + result + ")");
            }
            return result;
        } else if (token.equals(TOKEN_ERROR)) {
            return new GCMResult.Builder().errorCode(value).build();
        } else {
            throw new IOException("Invalid response from GCM: " + responseBody);
        }
    }

    private IOException newIoException(String responseBody, Exception e) {
        // log exception, as IOException constructor that takes a message and cause
        // is only available on Java 6
        String msg = "Error parsing JSON response (" + responseBody + ")";
        logger.log(Level.WARNING, msg, e);
        return new IOException(msg + ":" + e);
    }

    private static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                // ignore error
                logger.log(Level.FINEST, "IOException closing stream", e);
            }
        }
    }

    private void setJsonField(Map<Object, Object> json, String field,
            Object value) {
        if (value != null) {
            json.put(field, value);
        }
    }

    private Number getNumber(Map<?, ?> json, String field) {
        Object value = json.get(field);
        if (value == null) {
            throw new CustomParserException("Missing field: " + field);
        }
        if (!(value instanceof Number)) {
            throw new CustomParserException("Field " + field
                    + " does not contain a number: " + value);
        }
        return (Number) value;
    }

    class CustomParserException extends RuntimeException {

        CustomParserException(String message) {
            super(message);
        }
    }

    private String[] split(String line) throws IOException {
        String[] split = line.split("=", 2);
        if (split.length != 2) {
            throw new IOException("Received invalid response line from GCM: " + line);
        }
        return split;
    }

    protected HttpURLConnection post(String url, String body)
            throws IOException {
        return post(url, "application/x-www-form-urlencoded;charset=UTF-8", body);
    }

    protected HttpURLConnection post(String url, String contentType, String body)
            throws IOException {
        if (url == null || body == null) {
            throw new IllegalArgumentException("arguments cannot be null");
        }
        if (!url.startsWith("https://")) {
            logger.warning("URL does not use https: " + url);
        }
        logger.fine("Sending POST to " + url);
        logger.finest("POST body: " + body);
        byte[] bytes = body.getBytes();
        HttpURLConnection conn = getConnection(url);
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setFixedLengthStreamingMode(bytes.length);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", contentType);
        conn.setRequestProperty("Authorization", "key=" + Configs.GCM_KEY);
        OutputStream out = conn.getOutputStream();
        try {
            out.write(bytes);
        } finally {
            close(out);
        }
        return conn;
    }

    protected static final Map<String, String> newKeyValues(String key,
            String value) {
        Map<String, String> keyValues = new HashMap<String, String>(1);
        keyValues.put(nonNull(key), nonNull(value));
        return keyValues;
    }

    protected static StringBuilder newBody(String name, String value) {
        return new StringBuilder(nonNull(name)).append('=').append(nonNull(value));
    }

    protected static void addParameter(StringBuilder body, String name,
            String value) {
        nonNull(body).append('&')
                .append(nonNull(name)).append('=').append(nonNull(value));
    }

    protected HttpURLConnection getConnection(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        return conn;
    }

    protected static String getString(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        BufferedReader reader
                = new BufferedReader(new InputStreamReader(stream));
        StringBuilder content = new StringBuilder();
        String newLine;
        do {
            newLine = reader.readLine();
            if (newLine != null) {
                content.append(newLine).append('\n');
            }
        } while (newLine != null);
        if (content.length() > 0) {
            // strip last newline
            content.setLength(content.length() - 1);
        }
        return content.toString();
    }

    private static String getAndClose(InputStream stream) throws IOException {
        try {
            return getString(stream);
        } finally {
            if (stream != null) {
                close(stream);
            }
        }
    }

    static <T> T nonNull(T argument) {
        if (argument == null) {
            throw new IllegalArgumentException("argument cannot be null");
        }
        return argument;
    }

    void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
