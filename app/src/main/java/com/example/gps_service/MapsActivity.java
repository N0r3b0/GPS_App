package com.example.gps_service;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
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

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        createLocationCallback();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d("MapsActivity", "Map is ready");

        if (routeId != -1 && !isTracking) {
            // Wyświetlanie historii trasy
            List<LatLng> locations = dbHelper.getLocationsForRoute(routeId);
            if (locations.size() > 0) {
                PolylineOptions polylineOptions = new PolylineOptions().addAll(locations).color(Color.RED).width(25);
                mMap.addPolyline(polylineOptions);

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (LatLng latLng : locations) {
                    builder.include(latLng);
                }
                LatLngBounds bounds = builder.build();
                int padding = 100;
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                mMap.moveCamera(cu);
            }
        } else if (isTracking) {
            // Rozpoczęcie śledzenia nowej trasy
            currentPolyline = mMap.addPolyline(new PolylineOptions().color(Color.RED).width(25));
            Log.d("MapsActivity", "Polyline initialized: " + (currentPolyline != null));
            startTracking();
        }
    }

    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(100); // 10 sekund
        locationRequest.setFastestInterval(50); // 5 sekund
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    LatLng newLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    updateRoute(newLocation);
                }
            }
        };
    }

    private void startTracking() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startTracking();
            } else {
                // Obsługa przypadku, gdy użytkownik nie udzielił uprawnień
                Toast.makeText(this, "Uprawnienia do lokalizacji są wymagane do śledzenia trasy", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateRoute(LatLng newLocation) {
        currentRoute.add(newLocation);
        currentPolyline.setPoints(currentRoute);

        // Przesuń kamerę do nowej lokalizacji
        if (zoom == 0) {
            float zoomLevel = 15.0f;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLocation, zoomLevel));
            zoom++;
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(newLocation));

        // Zapisz nową lokalizację do bazy danych
        dbHelper.addLocation(routeId, newLocation.latitude, newLocation.longitude);
    }

    private void stopTracking() {
        if (isTracking) {
            // Zakończenie śledzenia i zapisanie trasy do bazy danych
            fusedLocationClient.removeLocationUpdates(locationCallback);
            dbHelper.endRoute(routeId);
            Log.d("MapsActivity", "Route ended with ID: " + routeId);
            isTracking = false;
            stopTrackingButton.setVisibility(View.GONE);

            // Zatrzymaj GpsService
            Intent serviceIntent = new Intent(this, GpsService.class);
            stopService(serviceIntent);

            // Przejdź z powrotem do MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isTracking) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}