package com.example.gps_service;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class LocationDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "locationData.db";
    private static final int DATABASE_VERSION = 1;

    // tabela locations
    public static final String TABLE_NAME = "locations";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_STEPS = "steps";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    // tworzenie tabeli locations
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_LATITUDE + " DOUBLE, " +
                    COLUMN_LONGITUDE + " DOUBLE, " +
                    COLUMN_STEPS + " DOUBLE, " +
                    COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" + ");";

    public LocationDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addLocation(double latitude, double longitude, double steps) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LATITUDE, latitude);
        values.put(COLUMN_LONGITUDE, longitude);
        values.put(COLUMN_STEPS, steps);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public List<LatLng> getAllLocations() {
        List<LatLng> locations = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT latitude, longitude FROM locations", null);

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


}
