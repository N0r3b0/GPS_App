# GPS_App
### Android app built in Android Studio using Java. Tracks GPS location, allows you to save and view routes

## **Home view**  
![image](https://github.com/N0r3b0/GPS_App/assets/92164691/a927cf95-06b6-41f1-bc8a-f81c16857d0c)

**Start Tracking** button starts the GPS service and creates a new route with a start time in the database.  
**Stop Tracking** button stops the GPS service and puts route's endtime in the database.  
**Show Map** button is used to display all routes in the database

```java
startTrackingButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        if (!isTracking) {
            startService(gpsServiceIntent);
            isTracking = true;
        }
    }
});

// GpsService class function connected with startTracking button:
@Override
public int onStartCommand(Intent intent, int flags, int startId) {
    currentRouteId = dbHelper.startNewRoute();
    try {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 2, this);
    } catch (SecurityException e) {
        e.printStackTrace();
    }
    return START_STICKY;
}
```
```java
stopTrackingButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        if (isTracking) {
            stopService(gpsServiceIntent);
            isTracking = false;
        }
    }
});

// GpsService class function connected with stopTracking button:
@Override
public void onDestroy() {
    super.onDestroy();
    locationManager.removeUpdates(this);
    dbHelper.endRoute(currentRouteId);
}
```
```java
showMapButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(MainActivity.this, RoutesListActivity.class);
        startActivity(intent);
    }
});
```

## **Routes view**  
![image](https://github.com/N0r3b0/GPS_App/assets/92164691/3f1f2c56-bf8f-4a33-81ce-a030207fb794)

Each item on the list is a route and after being clicked it displays its map by starting MapsActivity with routeId inside Intent object. List uses ArrayAdapter 
```java
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
```

## **Route map**
![image](https://github.com/N0r3b0/GPS_App/assets/92164691/02267bdc-0690-4a4a-ba8f-f6707ef8f451)

Route map is displayed through MapsActivity class which impements OnMapReadyCallback google interface. Function onMapReady is called before opening the map and allows to configure the route polyline, camera focus and other settings.
```java
@Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (routeId != -1) {
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
        }
    }
```

## **Navigation panel**  
![image](https://github.com/N0r3b0/GPS_App/assets/92164691/b32ad8e7-059b-43fb-b81b-753fa9396731)

Navigation panel was made with NavigationView.OnNavigationItemSelectedListener interface and it uses nav_header.xml layout and drawer_menu.xml menu.  
Toolbar item is used to open navigation panel
```java
setSupportActionBar(toolbar);
navigationView.setNavigationItemSelectedListener(this);
ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
drawer.addDrawerListener(toggle);
toggle.syncState();
```

## **SQLite Database**  
**Locations table** is used to store coordinates and the route they belong to. It contains 4 fields {id, route_id, latitude, longitude}   
**Locations table:**    
![image](https://github.com/N0r3b0/GPS_App/assets/92164691/75679634-6a6b-4925-b27b-4fcec889818f)


**Routes** table has 3 fields {id, start_time, end_time}  
**Routes table**  
![image](https://github.com/N0r3b0/GPS_App/assets/92164691/3d89c153-935b-4006-aff8-c431a7067c0f)


### Funtion that retrieves the coordinates of the selected route
```java
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
```



### **Build**  
The app uses location, so it requires location permission  
To build this app, you need to provide your Google API key, preferably as a string called google_maps_key in your values/secrets.xml file

![image](https://github.com/N0r3b0/GPS_App/assets/92164691/12b13662-f64f-4133-aa64-a825ca8ac816)


minimum SDK version: API 31
