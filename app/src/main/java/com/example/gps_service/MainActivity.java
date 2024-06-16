package com.example.gps_service;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    TextView tVLocation, tVLatitude, tVLongitude, info;
    private GpsService gpsService;
    private boolean isTracking = false;
    private Intent gpsServiceIntent;
    private boolean bound;
    private Intent intent;
    private Handler handler;
    private LocationDatabaseHelper dbHelper; // baza danych

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            Log.v("saved", String.valueOf(savedInstanceState.getDouble("distance")));
        }

        gpsServiceIntent = new Intent(this, GpsService.class);

        setContentView(R.layout.activity_main);
        tVLocation = findViewById(R.id.tVLocation);
        tVLatitude = findViewById(R.id.tVLatitude);
        tVLongitude = findViewById(R.id.tVLongitude);
        info = findViewById(R.id.info);

        handler = new Handler(Looper.getMainLooper());

        dbHelper = new LocationDatabaseHelper(this); // incjacja obiektu bazy

        IntentFilter filter = new IntentFilter("com.example.labuslugionly.UPDATE_DISTANCE");
        registerReceiver(updateDistanceReceiver, filter, RECEIVER_EXPORTED);

        // routes buttons
        Button startTrackingButton = findViewById(R.id.startTrackingButton);
        Button stopTrackingButton = findViewById(R.id.stopTrackingButton);
        Button showMapButton = findViewById(R.id.showMapButton);

        startTrackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isTracking) {
                    startService(gpsServiceIntent);
                    isTracking = true;
                }
            }
        });

        stopTrackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTracking) {
                    stopService(gpsServiceIntent);
                    isTracking = false;
                }
            }
        });

        showMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RoutesListActivity.class);
                startActivity(intent);
            }
        });
    }

//    private ServiceConnection connection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName comName, IBinder binder) {
//            GpsService.Connection con = (GpsService.Connection) binder;
//            gpsService = con.getGps();
//            gpsService.setHandler(handler); // ustawiam handlera teraz nie będzie !null wewnątrz gps clss
//            bound = true;
//            Log.v("onServiceConnected", "Service connected");
//        }
//        @Override
//        public void onServiceDisconnected(ComponentName comName){
//            bound = false;
//            Log.v("onServiceDisconnected", "Service disconnected");
//        }
//    };

    private BroadcastReceiver updateDistanceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            double latitude = intent.getDoubleExtra("latitude", 0);
            double longitude = intent.getDoubleExtra("longitude", 0);
            double steps = intent.getDoubleExtra("steps", 0);
            String distance = intent.getStringExtra("distance");

            Log.v("BroadcastReceiver", "Received update: latitude=" + latitude + ", longitude=" + longitude + ", distance=" + distance);

            tVLatitude.setText("Latitude: " + String.valueOf(latitude));
            tVLongitude.setText("Longitude: " + String.valueOf(longitude));
            tVLocation.setText(distance);
        }
    };

    public void stop(View v){
        intent = new Intent(this, GpsService.class);
        stopService(intent);
    }
    public void test(View v) {
        String running = "";
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            running += service.service.getClassName();
        }

        if(!running.isEmpty() && !bound)  // run but not bound
            running = "Service disconnected but still working";
        else if(running.isEmpty())
            running = "Service disconnected and not working";


        Toast.makeText(this, running, Toast.LENGTH_LONG).show();
        info.setText(running);
    }
    public void showLocations(View v)
    {
    }
}