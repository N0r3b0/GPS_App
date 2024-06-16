package com.example.gps_service;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationDatabaseHelper dbHelper;
    private LineView lineView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Inicjalizacja bazy danych
        dbHelper = new LocationDatabaseHelper(this);

        // Inicjalizacja LineView
        lineView = findViewById(R.id.lineView); // Upewnij się, że w layout masz LineView z odpowiednim ID

        // Uzyskanie referencji do MapFragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Pobranie danych z bazy
        List<LatLng> locations = dbHelper.getAllLocations();

        // Rysowanie trasy
        if (locations.size() > 0) {
            PolylineOptions polylineOptions = new PolylineOptions().addAll(locations).color(Color.RED).width(5);
            mMap.addPolyline(polylineOptions);

            // Ustawienie widoku mapy tak, aby objął całą trasę
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng latLng : locations) {
                builder.include(latLng);
            }
            LatLngBounds bounds = builder.build();
            int padding = 100; // padding around start and end marker
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            mMap.moveCamera(cu);
        }

        // Przekazanie danych do LineView
        lineView.setLocations(locations);
    }
}
