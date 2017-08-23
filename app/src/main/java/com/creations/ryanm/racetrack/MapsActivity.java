package com.creations.ryanm.racetrack;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.location.Location;
import android.location.LocationListener;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import java.util.Timer;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    GoogleMap mMap;
    private LocationListener locationListener;
    private LocationManager locationManager;
    private boolean readyForMaps;
    private PolylineOptions rectOptions;
    private Chronometer stopwatch;
    private boolean sessionRunning = false;
    private boolean resetButton = true;
    private long timeWhenStopped = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationListener = new MyLocationListener();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},10);
        }
        rectOptions = new PolylineOptions();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        readyForMaps = true;
        startLocationServices();
    }

    @Override
    public void onConnectionSuspended(int i) {
        readyForMaps = false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { readyForMaps = false; }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationServices();
        enableButton(false);
        sessionRunning = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        startLocationServices();
        stopwatch = (Chronometer) findViewById(R.id.stopwatch);
        stopwatch.setBase(SystemClock.elapsedRealtime());
        timeWhenStopped = 0;
        stopwatch.setVisibility(View.INVISIBLE);
        resetButton = true;
    }

    private void startLocationServices() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            System.out.println("starting location updates");
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }

    private void stopLocationServices() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            System.out.println("stopping location updates");
            locationManager.removeUpdates(locationListener);
        }
    }

    public void startSession(View view){
        stopwatch = (Chronometer) findViewById(R.id.stopwatch);
        if(!sessionRunning){
            stopwatch.setVisibility(View.VISIBLE);
            stopwatch.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
            stopwatch.start();
            sessionRunning = true;
            toggleButton(true);
        }
        else{
            timeWhenStopped = stopwatch.getBase() - SystemClock.elapsedRealtime();
            stopwatch.stop();
            sessionRunning = false;
            toggleButton(false);
        }
    }

    private void toggleButton(boolean running){
        Button startStop = (Button) findViewById(R.id.startstop);
        if(running){
            startStop.setText("Stop Session");
            startStop.setBackgroundColor(getResources().getColor(R.color.stop));
        }
        else{
            startStop.setText("Start Session");
            startStop.setBackgroundColor(getResources().getColor(R.color.start));
        }
    }

    private void enableButton(boolean enabled) {
        Button startStop = (Button) findViewById(R.id.startstop);
        startStop.setEnabled(enabled);
        if(!enabled) {
            startStop.setBackgroundColor(getResources().getColor(R.color.disabled_gray));
            startStop.setText("Getting location...");
        }
        else{
            startStop.setBackgroundColor(getResources().getColor(R.color.start));
            startStop.setText("Start Session");
        }
    }


    private class MyLocationListener implements LocationListener {
        boolean centered = false;
        LatLng lastRecorded;
        @Override
        public void onLocationChanged(Location loc) {
            double longitude = loc.getLongitude();
            double lat = loc.getLatitude();
            LatLng current = new LatLng(lat, longitude);
            mMap.animateCamera(CameraUpdateFactory.newLatLng(current));
            if(!centered) {
                mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
                lastRecorded = current;
                rectOptions.add(current);
                mMap.addPolyline(rectOptions);
                centered = true;
            }
            if(resetButton){
                enableButton(true);
                resetButton = false;
            }
            double distance = SphericalUtil.computeDistanceBetween(lastRecorded,current);
            System.out.println(distance);
            if(distance > 15){
                lastRecorded = current;
                rectOptions.add(current);
                mMap.addPolyline(rectOptions);
            }
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) { }
        @Override
        public void onProviderEnabled(String provider) { }
        @Override
        public void onProviderDisabled(String provider) { }
    }
}
