package com.beppeben.cook4.utils;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.beppeben.cook4.R;
import com.beppeben.cook4.domain.C4User;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

public class LocationUtils {

    private static final String LOG_TAG = "GetAddressDetails";
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_DETAILS = "/details";
    private static final String OUT_JSON = "/json";


    public static Float getDistance(Double lat1, Double long1, Double lat2, Double long2) {
        if (lat1 == null || lat2 == null || long1 == null || long2 == null) return null;
        Location loc1 = new Location("test");
        loc1.setLatitude(lat1);
        loc1.setLongitude(long1);
        Location loc2 = new Location("test");
        loc2.setLatitude(lat2);
        loc2.setLongitude(long2);
        return loc1.distanceTo(loc2);
    }

    public static Location getBetterLocation(Location newLocation, Location currentBestLocation) {
        final long TIME = 1000 * 60 * 5;

        if (currentBestLocation == null) {
            return newLocation;
        }

        if (newLocation == null) {
            return currentBestLocation;
        }

        long timeDelta = newLocation.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TIME;
        boolean isSignificantlyOlder = timeDelta < -TIME;
        boolean isNewer = timeDelta > 0;

        if (isSignificantlyNewer) {
            return newLocation;
        } else if (isSignificantlyOlder) {
            return currentBestLocation;
        }

        int accuracyDelta = (int) (newLocation.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        boolean isFromSameProvider = newLocation.getProvider().equals(currentBestLocation.getProvider());

        if (isMoreAccurate) {
            return newLocation;
        } else if (isNewer && !isLessAccurate) {
            return newLocation;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return newLocation;
        }
        return currentBestLocation;
    }

    public static String[] getFromLocation(double lat, double lng) {
        String address = String.format(Locale.US,
                "https://maps.googleapis.com/maps/api/geocode/json?latlng=%1$f,%2$f&language="
                        + Locale.getDefault(), lat, lng);

        String[] res = new String[3];
        String streetNumber = "";
        String route = "";
        String addressLine = "";
        String locality = "";
        String country = "";
        String json = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            URL url = new URL(address);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate");

            InputStream is = conn.getInputStream();
            if ("gzip".equals(conn.getContentEncoding())) {
                is = new GZIPInputStream(is);
            }
            InputStreamReader in = new InputStreamReader(is);

            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
            json = jsonResults.toString();
            JSONObject jsonObject = new JSONObject(json);

            if ("OK".equalsIgnoreCase(jsonObject.getString("status"))) {
                JSONArray results = jsonObject.getJSONArray("results");
                if (results.length() > 0) {
                    int i = 0;
                    while (locality.equals("") && i < results.length()) {
                        JSONObject result = results.getJSONObject(i);
                        JSONArray components = result.getJSONArray("address_components");
                        for (int a = 0; a < components.length(); a++) {
                            JSONObject component = components.getJSONObject(a);
                            JSONArray types = component.getJSONArray("types");
                            for (int j = 0; j < types.length(); j++) {
                                String type = types.getString(j);
                                if (type.equals("locality")) {
                                    locality = component.getString("long_name");
                                } else if (type.equals("street_number")) {
                                    streetNumber = component.getString("long_name");
                                } else if (type.equals("route")) {
                                    route = component.getString("long_name");
                                } else if (type.equals("country")) {
                                    country = component.getString("long_name");
                                }
                            }
                        }
                        i++;
                    }
                    addressLine = route + " " + streetNumber;
                }
            }
            if (locality.equals("")) {
                LogsToServer.send("invalid city. Request: " + address + ". response: " + json);
            }

        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception:", e);
            LogsToServer.send(e);
            if (json != null) LogsToServer.send("address: " + address + ". json: " + json);
        }

        res[0] = addressLine;
        res[1] = locality;
        res[2] = country;

        return res;
    }


    public static List<Double> getCoordinates(Context ctx, String id, int retries) {
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        String API_KEY = ctx.getResources().getString(R.string.places_api_key);
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_DETAILS + OUT_JSON);
            sb.append("?sensor=false&key=" + API_KEY);
            sb.append("&placeid=" + URLEncoder.encode(id, "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception:", e);
            LogsToServer.send(e);
            if (retries > 0) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                return getCoordinates(ctx, id, retries - 1);
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        Double lat = null;
        Double lng = null;
        try {
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONObject locJson = (JSONObject) jsonObj.getJSONObject("result").getJSONObject("geometry").getJSONObject("location");
            lat = locJson.getDouble("lat");
            lng = locJson.getDouble("lng");

        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception:", e);
            LogsToServer.send(e);
            return null;
        }
        List<Double> result = new ArrayList<Double>();
        result.add(lat);
        result.add(lng);
        return result;
    }


    public static void getLocationInfo(Context ctx) {
        C4User me = Globals.getMe(ctx);
        if (me.isUnlocated()) return;
        Double latitude = me.getLatitude();
        Double longitude = me.getLongitude();
        if (latitude == null || longitude == null || ctx == null) return;
        String[] result = getAddress(ctx, latitude, longitude);
        if (result != null) {
            String addrCity = result[0];
            if (!result[1].isEmpty()) {
                if (!addrCity.isEmpty()) addrCity += ", ";
                addrCity += result[1];
            }
            me.setAddress(addrCity);
            me.setCity(result[1]);
            me.setCountry(result[2]);
        }
        Globals.updateLocInfo = false;
    }


    public static String[] getAddress(Context ctx, Double latitude, Double longitude) {
        if (ctx == null) return null;
        return getFromLocation(latitude, longitude);
    }

    public static String getMapsApiDirectionsUrl(Double mylat, Double mylong, Double latitude, Double longitude) {
        String endpoints = "origin=" + mylat + "," + mylong
                + "&destination=" + latitude + "," + longitude;
        String mode = "mode=walking";
        String language = "language=" + Locale.getDefault();
        String sensor = "sensor=false";
        String params = endpoints + "&" + mode + "&" + language + "&" + sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + params;
        return url;
    }

}
