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

    private static final int DATABASE_VERSION = 2;
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
                "end_time INTEGER," +
                "city_name TEXT" +
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

    public void updateRouteCity(long routeId, String cityName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("city_name", cityName);
        db.update("routes", values, "id = ?", new String[]{String.valueOf(routeId)});
        db.close();
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
        Cursor cursor = db.rawQuery("SELECT start_time, end_time, city_name FROM routes WHERE id = ?", new String[]{String.valueOf(routeId)});

        if (cursor.moveToFirst()) {
            @SuppressLint("Range") long startTime = cursor.getLong(cursor.getColumnIndex("start_time"));
            @SuppressLint("Range") long endTime = cursor.getLong(cursor.getColumnIndex("end_time"));
            @SuppressLint("Range") String cityName = cursor.getString(cursor.getColumnIndex("city_name"));
            cursor.close();

            // Formatowanie daty i godziny
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String formattedStartDate = sdf.format(new Date(startTime));
            String formattedEndDate = sdf.format(new Date(endTime));

            // Zwróć informacje o trasie, w tym nazwę miasta
            return "Rozpoczęta: " + formattedStartDate + "\nZakończona: " + formattedEndDate
                    + "\nMiejscowość: " + (cityName != null ? cityName : "Nieznana");
        }

        cursor.close();
        return "Nieznana trasa";
    }


}
