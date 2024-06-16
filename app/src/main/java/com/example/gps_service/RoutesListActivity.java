package com.example.gps_service;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class RoutesListActivity extends AppCompatActivity {
    private LocationDatabaseHelper dbHelper;
    private ListView routesListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes_list);

        dbHelper = new LocationDatabaseHelper(this);
        routesListView = findViewById(R.id.routesListView);

        List<Long> routes = dbHelper.getAllRoutes();
        ArrayAdapter<Long> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, routes);
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
}