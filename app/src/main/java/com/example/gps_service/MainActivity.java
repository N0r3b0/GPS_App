package com.example.gps_service;

import com.example.gps_service.R;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    TextView tVLocation, tVLatitude, tVLongitude, info;
    private GpsService gpsService;
    private boolean isTracking = false;
    private Intent gpsServiceIntent;
    private boolean bound;
    private Intent intent;
    private Handler handler;
    private LocationDatabaseHelper dbHelper; // baza danych
    private DrawerLayout drawer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Ustawienie layoutu tutaj

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        if (navigationView == null) {
            Log.e("MainActivity", "NavigationView is null");
        } else {
            Log.d("MainActivity", "NavigationView initialized successfully");
        }

        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            navigationView.setCheckedItem(R.id.nav_home);
        }

        gpsServiceIntent = new Intent(this, GpsService.class);

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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
        } else if (id == R.id.nav_routes) {
            startActivity(new Intent(this, RoutesListActivity.class));
        } else if (id == R.id.nav_settings) {
            // Add your settings activity or fragment here
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}