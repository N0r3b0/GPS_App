package com.example.gps_service;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    TextView tVLocation, tVLatitude, tVLongitude, info;
    private Gps gps;
    private boolean bound;
    private Intent intent;
    private Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            Log.v("saved", String.valueOf(savedInstanceState.getDouble("distance")));
        }
        setContentView(R.layout.activity_main);
        tVLocation = findViewById(R.id.tVLocation);
        tVLatitude = findViewById(R.id.tVLatitude);
        tVLongitude = findViewById(R.id.tVLongitude);
        info = findViewById(R.id.info);

        handler = new Handler(Looper.getMainLooper());

        IntentFilter filter = new IntentFilter("com.example.labuslugionly.UPDATE_DISTANCE");
        registerReceiver(updateDistanceReceiver, filter, RECEIVER_EXPORTED);
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName comName, IBinder binder) {
            Gps.Connection con = (Gps.Connection) binder;
            gps = con.getGps();
            gps.setHandler(handler); // ustawiam handlera teraz nie będzie !null wewnątrz gps clss
            bound = true;
            Log.v("onServiceConnected", "Service connected");
        }
        @Override
        public void onServiceDisconnected(ComponentName comName){
            bound = false;
            Log.v("onServiceDisconnected", "Service disconnected");
        }
    };

    private BroadcastReceiver updateDistanceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            double latitude = intent.getDoubleExtra("latitude", 0);
            double longitude = intent.getDoubleExtra("longitude", 0);
            String distance = intent.getStringExtra("distance");

            Log.v("BroadcastReceiver", "Received update: latitude=" + latitude + ", longitude=" + longitude + ", distance=" + distance);

            tVLatitude.setText("Latitude: " + String.valueOf(latitude));
            tVLongitude.setText("Longitude: " + String.valueOf(longitude));
            tVLocation.setText(distance);
        }
    };

    public void bind(View v){
        intent = new Intent(this, Gps.class);
        startService(intent);
        bindService(intent, connection, Context.BIND_NOT_FOREGROUND);
        bound = true;
    }
    public void unbind(View v){
        if (bound) {
            unbindService(connection);
            bound = false;
            Log.v("unbind", "Service disconnected");
        }
        else{
            Log.v("unbind", "No service connected");
        }
    }
    public void stop(View v){
        intent = new Intent(this, Gps.class);
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
}