# GPS_Service
Android application built in Android Studio using Java that tracks GPS location, saves your routes 

## **Home view**  
![image](https://github.com/N0r3b0/GPS_App/assets/92164691/a927cf95-06b6-41f1-bc8a-f81c16857d0c)

The Start Tracking button starts the GPS service and creates a new route with a start time in the database.  
The Stop Tracking button stops the GPS service and puts endtime for the route from routes table in the database.  
The Show Map button is used to display all routes in the database

## **Routes view**  
![image](https://github.com/N0r3b0/GPS_App/assets/92164691/3f1f2c56-bf8f-4a33-81ce-a030207fb794)

Each item on the list is a route and after being clicked it displays its route map by starting MapsActivity with routeId inside Intent object. List uses ArrayAdapter 
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


### **Build**  
The app uses location, so it requires location permission  
To build this app, you need to provide your Google API key, preferably as a string called google_maps_key in your values/secrets.xml file

![image](https://github.com/N0r3b0/GPS_App/assets/92164691/12b13662-f64f-4133-aa64-a825ca8ac816)


minimum SDK version: API 31
