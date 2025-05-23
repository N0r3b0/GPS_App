# GPS_App  
Android app built in Android Studio using Java. Tracks GPS location, allows you to save and view routes

## **Home view**  
![image](https://github.com/user-attachments/assets/2fe5fa41-9126-4933-89ca-55c71ff23c3b)

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
![image](https://github.com/user-attachments/assets/d1ef080d-a8ef-420a-a30e-347343cbc961)


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
![image](https://github.com/user-attachments/assets/d01c62bc-1693-4b8d-9bce-434615d3f3ac)


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
![image](https://github.com/user-attachments/assets/815ff63d-63c0-459b-b1cc-d8e45b590f88)


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
![image](https://github.com/user-attachments/assets/dc75853c-66cc-4059-8f2b-858da529f865)


**Routes** table has 3 fields {id, start_time, end_time, city_name}  
**Routes table**  
![image](https://github.com/user-attachments/assets/aaf1b2ab-ecd1-49f7-a874-961be42b3e0a)


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
