package com.beppeben.cook4;

import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import com.beppeben.cook4.utils.LocationUtils;
import com.beppeben.cook4.utils.PathJSONParser;
import com.beppeben.cook4.utils.net.HttpConnection;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MapActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, OnMyLocationButtonClickListener {

    private GoogleMap mMap;
    private String dishName;
    private LatLng dishPosition;
    private LatLng myPosition;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Bundle extras = getIntent().getExtras();
        Double dishlat = extras.getDouble("dishlat");
        Double dishlong = extras.getDouble("dishlong");
        Double mylat = extras.getDouble("mylat");
        Double mylong = extras.getDouble("mylong");
        dishName = extras.getString("dishname");
        dishPosition = new LatLng(dishlat, dishlong);
        myPosition = new LatLng(mylat, mylong);
        setUpMapIfNeeded();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mMap.addMarker(new MarkerOptions().position(dishPosition).title(dishName));
        new ReadDirectionsTask().execute(getMapsApiDirectionsUrl());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
                mMap.setOnMyLocationButtonClickListener(this);
                final LatLngBounds bounds = new LatLngBounds.Builder()
                        .include(dishPosition)
                        .include(myPosition)
                        .build();
                mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
                        CameraPosition pos = mMap.getCameraPosition();
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos.target, pos.zoom - 0.5F));
                    }
                });
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Double newlat = location.getLatitude();
        Double newlong = location.getLongitude();
        if (myPosition == null || LocationUtils.getDistance(newlat, newlong,
                myPosition.latitude, myPosition.longitude) > 30) {
            myPosition = new LatLng(newlat, newlong);
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(dishPosition).title(dishName));
            new ReadDirectionsTask().execute(getMapsApiDirectionsUrl());
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000); // Update location every second
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("MapActivity", "GoogleApiClient connection has been suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i("MapActivity", "GoogleApiClient connection has failed");
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    private String getMapsApiDirectionsUrl() {
        String endpoints = "origin=" + myPosition.latitude + "," + myPosition.longitude
                + "&destination=" + dishPosition.latitude + "," + dishPosition.longitude;
        String mode = "mode=walking";
        String sensor = "sensor=false";
        String params = endpoints + "&" + mode + "&" + sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + params;
        return url;
    }


    private class ReadDirectionsTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                HttpConnection http = new HttpConnection();
                data = http.readUrl(url[0]);
            } catch (Exception e) {
                Log.d("Directions API Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(result);
        }
    }

    private class ParserTask extends
            AsyncTask<String, Integer, PolylineOptions> {

        @Override
        protected PolylineOptions doInBackground(String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(jsonData[0]);
                PathJSONParser parser = new PathJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            ArrayList<LatLng> points;
            PolylineOptions polyLineOptions = null;
            for (int i = 0; i < routes.size(); i++) {
                points = new ArrayList<>();
                polyLineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = routes.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }

                polyLineOptions.addAll(points);
            }

            return polyLineOptions;
        }

        @Override
        protected void onPostExecute(PolylineOptions polyLineOptions) {
            if (polyLineOptions != null) {
                polyLineOptions.width(4);
                polyLineOptions.color(Color.BLUE);
                mMap.addPolyline(polyLineOptions);
            }
        }
    }

}
