package com.example.gps_service;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.List;

public class RoutesListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private LocationDatabaseHelper dbHelper;
    private ListView routesListView;
    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        dbHelper = new LocationDatabaseHelper(this);
        routesListView = findViewById(R.id.routesListView);

        List<Long> routes = dbHelper.getAllRoutes();
        ArrayAdapter<Long> adapter = new ArrayAdapter<Long>(this, R.layout.list_item_route, routes) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.list_item_route, parent, false);
                }

                TextView routeInfoTextView = convertView.findViewById(R.id.routeInfoTextView);
                Button deleteButton = convertView.findViewById(R.id.deleteButton);

                long routeId = getItem(position);
                String routeInfo = dbHelper.getRouteInfo(routeId); // Metoda do pobrania informacji o trasie
                routeInfoTextView.setText(routeInfo);

                deleteButton.setOnClickListener(v -> {
                    dbHelper.deleteRoute(routeId);
                    routes.remove(position);
                    notifyDataSetChanged();
                    Toast.makeText(RoutesListActivity.this, "Trasa usunięta", Toast.LENGTH_SHORT).show();
                });

                return convertView;
            }
        };
        routesListView.setAdapter(adapter);

        routesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                long routeId = (long) parent.getItemAtPosition(position);
                Intent intent = new Intent(RoutesListActivity.this, MapsActivity.class);
                intent.putExtra("routeId", routeId);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            startActivity(new Intent(this, MainActivity.class));
        } else if (id == R.id.nav_routes) {
            // Jesteśmy już w RoutesListActivity, nie trzeba nic robić
        } else if (id == R.id.nav_settings) {
            // Dodaj obsługę ustawień
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
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