package com.example.gps_service;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private LocationDatabaseHelper dbHelper;
    private long routeId;
    private boolean isTracking = false;
    private Polyline currentPolyline;
    private List<LatLng> currentRoute = new ArrayList<>();
    private Button stopTrackingButton;
    private Handler handler = new Handler();
    private Runnable updateRouteRunnable;
    private int zoom = 0;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        dbHelper = new LocationDatabaseHelper(this);

        routeId = getIntent().getLongExtra("routeId", -1);
        isTracking = getIntent().getBooleanExtra("isTracking", false);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        stopTrackingButton = findViewById(R.id.stopTrackingButton);
        if (isTracking) {
            stopTrackingButton.setVisibility(View.VISIBLE);
            stopTrackingButton.setOnClickListener(v -> stopTracking());
        } else {
            stopTrackingButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d("MapsActivity", "Map is ready");

        if (routeId != -1 && !isTracking) {
            List<LatLng> locations = dbHelper.getLocationsForRoute(routeId);
            if (locations.size() > 0) {
                PolylineOptions polylineOptions = new PolylineOptions()
                        .addAll(locations)
                        .color(Color.RED)
                        .width(25);
                mMap.addPolyline(polylineOptions);

                // Przesuń kamerę, aby pokazać całą trasę
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (LatLng latLng : locations) {
                    builder.include(latLng);
                }
                LatLngBounds bounds = builder.build();
                int padding = 100; // Padding w pikselach
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                mMap.moveCamera(cu);
            } else {
                Toast.makeText(this, "Brak danych lokalizacyjnych dla tej trasy", Toast.LENGTH_SHORT).show();
            }
        } else if (isTracking) {
            startTracking();
        }
    }

    private void startTracking() {
        currentPolyline = mMap.addPolyline(new PolylineOptions().color(Color.RED).width(25));

        // Uruchom okresowe odświeżanie trasy
        updateRouteRunnable = new Runnable() {
            @Override
            public void run() {
                List<LatLng> locations = dbHelper.getLocationsForRoute(routeId);
                if (locations.size() > 0) {
                    currentPolyline.setPoints(locations);

                    // Przesuń kamerę do ostatniej lokalizacji
                    LatLng lastLocation = locations.get(locations.size() - 1);
                    if (zoom == 0) {
                        float zoomLevel = 15.0f;
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, zoomLevel));
                        zoom++;
                    }
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(lastLocation));
                }

                handler.postDelayed(this, 50);
            }
        };

        handler.post(updateRouteRunnable);
    }

    private void stopTracking() {
        if (isTracking) {
            handler.removeCallbacks(updateRouteRunnable);

            // Zakończenie śledzenia i zapisanie trasy do bazy danych
            dbHelper.endRoute(routeId);
            Log.d("MapsActivity", "Route ended with ID: " + routeId);
            isTracking = false;
            stopTrackingButton.setVisibility(View.GONE);

            Intent serviceIntent = new Intent(this, GpsService.class);
            stopService(serviceIntent);

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}