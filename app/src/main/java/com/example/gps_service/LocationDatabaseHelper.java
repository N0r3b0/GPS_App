package com.example.gps_service;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.icu.text.SimpleDateFormat;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocationDatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "locations.db";
    private final Context context;

    public LocationDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_ROUTES_TABLE = "CREATE TABLE routes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "start_time INTEGER," +
                "end_time INTEGER" +
                ")";
        db.execSQL(CREATE_ROUTES_TABLE);

        String CREATE_LOCATIONS_TABLE = "CREATE TABLE locations (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "route_id INTEGER," +
                "latitude REAL," +
                "longitude REAL," +
                "FOREIGN KEY(route_id) REFERENCES routes(id)" +
                ")";
        db.execSQL(CREATE_LOCATIONS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS locations");
        db.execSQL("DROP TABLE IF EXISTS routes");
        onCreate(db);
    }

    // Metody do zarządzania danymi w bazie danych...
    public long startNewRoute() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("start_time", System.currentTimeMillis());
        long routeId = db.insert("routes", null, values);
        db.close();
        return routeId;
    }

    public void endRoute(long routeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("end_time", System.currentTimeMillis());
        db.update("routes", values, "id = ?", new String[]{String.valueOf(routeId)});
        db.close();
    }

    public void addLocation(long routeId, double latitude, double longitude) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("route_id", routeId);
        values.put("latitude", latitude);
        values.put("longitude", longitude);
        db.insert("locations", null, values);
        db.close();
    }

    public List<Long> getAllRoutes() {
        List<Long> routes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM routes", null);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") long routeId = cursor.getLong(cursor.getColumnIndex("id"));
                routes.add(routeId);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return routes;
    }

    public List<LatLng> getLocationsForRoute(long routeId) {
        List<LatLng> locations = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT latitude, longitude FROM locations WHERE route_id = ?", new String[]{String.valueOf(routeId)});

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") double latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
                @SuppressLint("Range") double longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));
                locations.add(new LatLng(latitude, longitude));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return locations;
    }

    public void deleteRoute(long routeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("routes", "id = ?", new String[]{String.valueOf(routeId)});
        db.delete("locations", "route_id = ?", new String[]{String.valueOf(routeId)});
        db.close();
    }

    public String getRouteInfo(long routeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT start_time FROM routes WHERE id = ?", new String[]{String.valueOf(routeId)});

        if (cursor.moveToFirst()) {
            @SuppressLint("Range") long startTime = cursor.getLong(cursor.getColumnIndex("start_time"));
            cursor.close();

            // Przykład formatowania daty i godziny
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String formattedDate = sdf.format(new Date(startTime));

            // Pobierz miejscowość (możesz użyć Geocoder, aby uzyskać nazwę miejscowości na podstawie współrzędnych)
            // Tutaj zakładam, że masz metodę getFirstLocationForRoute, która zwraca pierwszą lokalizację dla trasy
            LatLng firstLocation = getFirstLocationForRoute(routeId);
            String locationName = "Unknown Location"; // Domyślna wartość
            if (firstLocation != null) {
                locationName = getLocationName(firstLocation.latitude, firstLocation.longitude);
            }

            return routeId + ", " + formattedDate + ", " + locationName;
        }

        cursor.close();
        return "Unknown Route";
    }

    private LatLng getFirstLocationForRoute(long routeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT latitude, longitude FROM locations WHERE route_id = ? LIMIT 1", new String[]{String.valueOf(routeId)});

        if (cursor.moveToFirst()) {
            @SuppressLint("Range") double latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
            @SuppressLint("Range") double longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));
            cursor.close();
            return new LatLng(latitude, longitude);
        }

        cursor.close();
        return null;
    }

    private String getLocationName(double latitude, double longitude) {
        // Użyj Geocoder, aby uzyskać nazwę miejscowości na podstawie współrzędnych
        // To wymaga połączenia z internetem
        Geocoder geocoder = new Geocoder(context, Locale.getDefault()); // Use stored context
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0).getLocality(); // Zwróć nazwę miejscowości
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Unknown Location";
    }

}
