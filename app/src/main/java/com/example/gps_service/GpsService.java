package com.example.gps_service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

public class GpsService extends Service
{
    // remember to enable location for the app in phone emulator
    private LocationDatabaseHelper dbHelper; // database
    public class Connection extends Binder {
        GpsService getGps() { return GpsService.this; }
    }
    private final IBinder binder = new GpsService.Connection();
    public LocationManager locationManager;
    public Location position = null;
    public Location previousPosition = null;
    public double latitude;
    public double longitude;
    double recentDistance;
    double fullDistance;
    double steps = 0;
    double stepLength = 0.7;
    String[] providers;
    String provider;

    private Handler handler;
    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    LocationListener listener = new LocationListener() {
        @Override
        public void onLocationChanged(Location newPosition) {
            Log.v("onLocationChanged", "Position changed");
            position = newPosition;
            if (previousPosition == null) {
                previousPosition = newPosition;
            }
            latitude = newPosition.getLatitude();
            longitude = newPosition.getLongitude();
            recentDistance = newPosition.distanceTo(previousPosition);
            fullDistance += recentDistance;
            steps += (int) (fullDistance / stepLength);
            String distance =  "Recent distance " + String.format("%.2f", recentDistance) + " Meters\n" +
                    "Full distance " + String.format("%.2f", fullDistance) + " Meters\n" +
                    "Number of steps " + steps;
            previousPosition = newPosition;

            // save location to database
            dbHelper.addLocation(latitude, longitude, steps);

            // send location to MainActivity
            if (handler != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent("com.example.labuslugionly.UPDATE_DISTANCE");
                        intent.putExtra("latitude", latitude);
                        intent.putExtra("longitude", longitude);
                        intent.putExtra("steps", steps);
                        intent.putExtra("distance", distance);
                        sendBroadcast(intent);
                    }
                });
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        dbHelper = new LocationDatabaseHelper(this);

        List<String> list = locationManager.getProviders(true);
        int numberOfProviders = list.size();
        providers = new String[numberOfProviders];
        Log.v("availableProviders", String.valueOf(numberOfProviders));
        int i = 0;
        for (String tempList : list) {
            Log.v("availableProviders", tempList);
            providers[i++] = tempList;
        }
        provider = "gps";
        try {
            locationManager.requestLocationUpdates(provider, 1000, 2, listener);
        } catch (SecurityException se) {
            Log.v("avilable", "Security violation");
        }
        return binder;
    }

    @Override
    public void onDestroy() {
        Log.v("onDestroy", "Object destroyed");
        super.onDestroy();
    }
}