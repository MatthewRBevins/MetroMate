/**
 * MetroMate (C) 2022
 * Authors: Matthew Bevins and Miles Schuler
 */

package com.example.busapp;

import com.example.busapp.databinding.ActivityMapsBinding;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    //Google map object
    private GoogleMap mMap;
    //Object for getting user location
    private FusedLocationProviderClient fusedLocationClient;
    private ArrayList<Thread> currentThreads = new ArrayList<>();
    Routing r = null;
    JSONObject mshapes;
    JSONObject mdisplayNameToRouteID;
    JSONObject mfullRegions;
    JSONObject mnewRegions;
    JSONObject mnewRoutes;
    JSONObject mnewStops;
    JSONObject mroutes;
    JSONObject mnewTrips;

    /**
     * Method to run when app is opened
     * @param savedInstanceState
     */
    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            System.out.println("OPENING JSON FILES...");
            mshapes = Web.readJSON(new InputStreamReader(getAssets().open("shit.json")));
            mdisplayNameToRouteID = Web.readJSON(new InputStreamReader(getAssets().open("displayNameToRouteID.json")));
            mfullRegions = Web.readJSON(new InputStreamReader(getAssets().open("fullRegions.json")));
            mnewRegions = Web.readJSON(new InputStreamReader(getAssets().open("newRegions.json")));
            mnewRoutes = Web.readJSON(new InputStreamReader(getAssets().open("newRoutes.json")));
            mnewStops = Web.readJSON(new InputStreamReader(getAssets().open("newStops.json")));
            mroutes = Web.readJSON(new InputStreamReader(getAssets().open("routes.json")));
            mnewTrips = Web.readJSON(new InputStreamReader(getAssets().open("ass.json")));
            System.out.println("OPENED JSON FILES");

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Get location permissions
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLocationPermissions();

        //Initialize app
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        com.example.busapp.databinding.ActivityMapsBinding binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Start map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Adds marker to map based on given location, title, and color
     * @param latitude Latitude of marker position
     * @param longitude Longitude of marker position
     * @param title Title of marker
     * @param color Color of marker
     * @return Data of created marker
     */
    public void createMapMarker(Double latitude, Double longitude, String title, String color) {
        LatLng pos = new LatLng(latitude, longitude);
        MarkerOptions marker = new MarkerOptions();
        marker.position(pos);
        float[] hsv = new float[3];
        Color.colorToHSV(Color.parseColor(color), hsv);
        marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.busicon));
        System.out.println("put icon on map");
        //marker.icon(BitmapDescriptorFactory.fromFile("res/drawable/busicon.png"));
        marker.title(title);
        mMap.addMarker(marker);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
        //return marker;
    }

    /**
     * Gets location of buses for a given route ID
     * @param busID route ID
     * @return List of buses running on busID
     */
    public ArrayList<LatLng> getBusLocation(String busID) {
        long startTime = System.currentTimeMillis();
        try {
            //Open location data for all buses
            String data = Web.readFromWeb("https://s3.amazonaws.com/kcm-alerts-realtime-prod/vehiclepositions_pb.json");
            System.out.println("got data from web after " + (System.currentTimeMillis() - startTime) + " ms");
            JSONObject o = null;//Web.readJSON(new StringReader(data));
            System.out.println("parsed data to json after " + (System.currentTimeMillis() - startTime) + " ms");
            JSONArray a = (JSONArray) o.get("entity");
            Iterator<JSONObject> iterator = a.iterator();
            ArrayList<LatLng> positions = new ArrayList<>();
            //Loop through all running buses
            while (iterator.hasNext()) {
                JSONObject jj = iterator.next();
                JSONObject vehicle = (JSONObject) jj.get("vehicle");
                JSONObject t = (JSONObject) vehicle.get("trip");
                //If route ID is correct, add bus to positions
                if (t.get("route_id").equals(busID)) {
                    JSONObject pos = (JSONObject) vehicle.get("position");
                    LatLng latlng = new LatLng((double) pos.get("latitude"), (double) pos.get("longitude"));
                    positions.add(latlng);
                }
            }
            //Check whether each position is valid
            ArrayList<LatLng> validPositions = new ArrayList<>();
            for (LatLng pos : positions) {
                if (pos.latitude != 0d && pos.longitude != 0d) {
                    validPositions.add(pos);
                }
            }
            //Return positions
            if (validPositions.size() > 0) {
                System.out.println("found valid positions in json after " + (System.currentTimeMillis() - startTime) + " ms");
                return validPositions;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Display map of given route
     * @param routeID ID of given route
     * @param routeShortName Short name of given route
     * @throws IOException Reading file
     * @throws ParseException Parsing JSON
     */
    public void showRouteMap(String routeID, String routeShortName) throws IOException, ParseException {
        try {
            ArrayList<PolylineOptions> polylineList = new ArrayList<>();
            Handler routeMapHandler = new Handler();
            Runnable routeMapRunnable = () -> {
                JSONObject r = null;
                try {
                    r = null; //Web.readJSON(new InputStreamReader(getAssets().open("routes.json")));
                } catch (Exception e){}///*ParseException | IOException ignored*/) {}
                JSONObject item = (JSONObject) r.get(routeID);
                assert item != null;
                JSONArray shapeIDss = (JSONArray) item.get("shape_ids");
                assert shapeIDss != null;
                Object[] shapeIDs = shapeIDss.toArray();
                //Loop through all shapes in current route
                for (Object ii : shapeIDs) {
                    JSONObject o = null;
                    try {
                        o = null;//Web.readJSON(new InputStreamReader(getAssets().open("shapes.json")));
                    } catch (Exception e) {}
                    JSONArray locations = (JSONArray) o.get(ii.toString());
                    Iterator<JSONObject> i = locations.iterator();
                    PolylineOptions polyline = new PolylineOptions();
                    //Add position of shape to polyline
                    while (i.hasNext()) {
                        JSONObject currentObject = i.next();
                        LatLng hii = new LatLng(Double.parseDouble((String) Objects.requireNonNull(currentObject.get("latitude"))), Double.parseDouble((String) Objects.requireNonNull(currentObject.get("longitude"))));
                        polyline.add(hii);
                        try {
                            i.next(); i.next();
                        } catch (NoSuchElementException ignored) {}
                    }
                    polylineList.add(polyline);
                }
                routeMapHandler.post(() -> {
                    System.out.println(Thread.currentThread().isInterrupted());
                    for (PolylineOptions pl : polylineList) {
                        mMap.addPolyline(pl);
                    }
                });
            };
            Thread busLocationThread = new Thread(routeMapRunnable);
            currentThreads.add(busLocationThread);
            busLocationThread.start();

        } catch (NullPointerException _) {
            LocalSave.makeSnackBar("Unable to find route with ID " + routeShortName, getWindow().getDecorView().getRootView());
        }
    }

    /**
     * Checks whether app can get user location
     * @return Whether location permissions are allowed
     */
    public boolean checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPreferences", MODE_PRIVATE);
        return sharedPreferences.getBoolean(String.valueOf(R.id.setting1), false);
    }

    /**
     * Requests location permissions from user
     */
    public void getLocationPermissions() {
        Activity a = this;
        String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        ActivityCompat.requestPermissions(a, permission, 1);
    }

    /**
     * Code to run when map is loaded
     * @param googleMap Map object
     */
    @SuppressLint({"SetTextI18n", "MissingPermission"})
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        //Initialize starting and ending points for use in routing
        final LatLng[] currentDestination = {new LatLng(47.606471, -122.334604)};
        final LatLng[] currentStartingPoint = {new LatLng(47.606471, -122.334604)};

        //Initialize Google map and set camera to Seattle
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(47.6122709,-122.3471455), 12));

        //Main search views for routing
        SearchView locationSearch = (SearchView) findViewById(R.id.searchView);
        SearchView fromView = (SearchView) findViewById(R.id.searchView2);
        SearchView toView = (SearchView) findViewById(R.id.searchView3);

        //Get bottom navigation view
        final RelativeLayout[] menu = {null};
        BottomNavigationView mBottomNavigationView=(BottomNavigationView)findViewById(R.id.nav_view);

        //Set up settings buttons
        final int[] BUTTONS = {
                getResources().getIdentifier("setting1", "id", getPackageName()),
                getResources().getIdentifier("setting2", "id", getPackageName()),
                getResources().getIdentifier("setting3", "id", getPackageName())
        };
        //Default setting values
        final boolean[] defaultVals = {true,false,false};
        int bi = -1;
        //Loop through all settings buttons
        for (int buttonID : BUTTONS) {
            bi++;
            try {
                //Set button to local storage value of setting
                SharedPreferences sharedPreferences = getSharedPreferences("sharedPreferences", MODE_PRIVATE);
                ToggleButton button = findViewById(buttonID);
                //If local storage value does not exist, set button to default value
                if (! sharedPreferences.contains(String.valueOf(buttonID))) {
                    LocalSave.saveBoolean(String.valueOf(buttonID), defaultVals[bi], MapsActivity.this);
                }
                boolean isChecked = sharedPreferences.getBoolean(String.valueOf(buttonID), false);
                button.setChecked(isChecked);
            } catch (NullPointerException ignored) {}
        }

        //Make sure bottom navigation button is not selected by default
        mBottomNavigationView.getMenu().setGroupCheckable(0,false,true);
        //Listeners for when item is selected by user
        mBottomNavigationView.setOnItemSelectedListener(item -> {
            stopAllThreads();
            mMap.clear();
            //Set visibility on current open menu to invisible
            if (menu[0] != null) {
                menu[0].setVisibility(View.INVISIBLE);
            }
            String name = item.toString();
            //Show resource viewer if resource button is selected
            if (name.equals("Resources")) {
                RelativeLayout resourceSelector = (RelativeLayout) findViewById(R.id.resourceSelector);
                ScrollView resourceViewer = (ScrollView) findViewById(R.id.resourceViewer);
                resourceSelector.setVisibility(View.VISIBLE);
                resourceViewer.setVisibility(View.INVISIBLE);
            }

            //Set currently open menu
            menu[0] = (RelativeLayout) findViewById(getResources().getIdentifier(name + "Menu", "id", getPackageName()));
            menu[0].setVisibility(View.VISIBLE);
            item.setCheckable(true);
            item.setChecked(true);

            //Show close button for shown menu
            Button closeButton = (Button) menu[0].findViewById(getResources().getIdentifier(name + "Exit", "id", getPackageName()));
            //Close currently open menu when close button is pressed
            closeButton.setOnClickListener(view -> {
                menu[0].setVisibility(View.INVISIBLE);
                mMap.clear();
                mBottomNavigationView.getMenu().setGroupCheckable(0,false,true);
                stopAllThreads();
            });

            //If current menu contains a search view (bus finder or route mapper), set onQueryTextListener
            SearchView searchView = (SearchView) menu[0].findViewById(getResources().getIdentifier(name + "SearchBar", "id", getPackageName()));
            if (searchView != null) {
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        //When searching for bus locations
                        if (name.equals("Buses")) {
                            mMap.clear();

                            Handler mapHandler = new Handler();
                            Runnable busLocationRunnable = () -> {
                                CoordinateHelper ch = new CoordinateHelper(getApplicationContext());
                                ArrayList<LatLng> positions = null;
                                try {
                                    positions = getBusLocation(ch.getRouteID(query));
                                } catch (IOException | ParseException e) {
                                    e.printStackTrace();
                                }

                                final ArrayList<LatLng> finalPositions = positions;
                                mapHandler.post(() -> {
                                    if (finalPositions == null || finalPositions.size() == 0) {
                                        LocalSave.makeSnackBar("Could not find buses with id " + query, getWindow().getDecorView().getRootView());
                                    }
                                    //Create map markers for each running bus
                                    else {
                                        for (int i = 0; i < finalPositions.size(); i++) {
                                            LatLng pos = finalPositions.get(i);
                                            createMapMarker(pos.latitude, pos.longitude, "","#f91504");
                                        }
                                    }
                                });
                            };
                            Thread busLocationThread = new Thread(busLocationRunnable);
                            busLocationThread.start();
                            currentThreads.add(busLocationThread);
                            //busLocationThread.interrupt();

                        //When viewing route map
                        } else if (name.equals("Routes")) {
                        mMap.clear();
                            try {
                                //Get route map of given query
                                JSONObject obj = null; //Web.readJSON(new InputStreamReader(getAssets().open("displayNameToRouteID.json")));
                                String routeID = (String) obj.get(query);
                                showRouteMap(routeID, query);
                            } catch (IOException | ParseException ignored) {}
                        }
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                        return false;
                    }
                });
            //If saved menu has been opened
            } else if (name.equals("Saved")) {
                //Remove current values from saved location viewer
                RelativeLayout relativeLayout = findViewById(R.id.SavedLayout);
                relativeLayout.removeAllViews();

                try {
                    //Get all saved locations
                    ArrayList<String>[] savedLocations = LocalSave.loadSavedLocations(MapsActivity.this);
                    if (savedLocations != null) {
                        //Loop through saved locations
                        for (int i = 0; i < savedLocations[0].size(); i++) {
                            //Create new linear layout for current saved location
                            LinearLayout newLL = new LinearLayout(this);
                            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            llp.setMargins(0,i*150,0,0);
                            newLL.setLayoutParams(llp);
                            newLL.setOrientation(LinearLayout.HORIZONTAL);
                            Button button = new Button(this);
                            Button closeSaveButton = new Button(this);
                            closeSaveButton.setText("X");
                            //Add location and remove location button to linear layout
                            newLL.addView(button);
                            newLL.addView(closeSaveButton);
                            String buttonText = savedLocations[0].get(i);
                            //Make sure button text is not too long
                            if (buttonText.length() > 17) {
                                buttonText = buttonText.substring(0, 17) + "...";
                            }
                            button.setText(buttonText + ": " + savedLocations[1].get(i));
                            int finalI = i;
                            //Remove saved location from local storage when close button is pressed
                            closeSaveButton.setOnClickListener(view -> {
                                ArrayList<String>[] previousSave = new ArrayList[0];
                                try {
                                    previousSave = LocalSave.loadSavedLocations(MapsActivity.this);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                assert previousSave != null;
                                ArrayList<String> previousNames = previousSave[0];
                                ArrayList<String> previousAddresses = previousSave[1];
                                relativeLayout.removeViews(previousNames.indexOf(savedLocations[0].get(finalI)),1);
                                previousAddresses.remove(previousNames.indexOf(savedLocations[0].get(finalI)));
                                previousNames.remove(savedLocations[0].get(finalI));
                                LocalSave.saveSavedLocations(previousNames, previousAddresses, MapsActivity.this);
                            });
                            //Load destination when saved location button pressed
                            button.setOnClickListener(view -> {
                                //Show route generator UI
                                String coordinates = button.getText().toString().split(": ")[1];
                                double lat = Double.parseDouble(coordinates.split(", ")[0]);
                                double lng = Double.parseDouble(coordinates.split(", ")[1]);
                                RelativeLayout defaultSearchView = (RelativeLayout) findViewById(R.id.defaultSearchLayout);
                                defaultSearchView.setVisibility(View.INVISIBLE);
                                RelativeLayout newSearchView = (RelativeLayout) findViewById(R.id.newSearchLayout);
                                newSearchView.setVisibility(View.VISIBLE);
                                CharSequence fromText = "CURRENT LOCATION";
                                CharSequence toText = savedLocations[0].get(finalI);
                                fromView.setQuery(fromText, false);
                                toView.setQuery(toText, false);
                                //Set starting point to user's location if possible
                                if (checkLocationPermissions()) {
                                    fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Location> task) {
                                            try {
                                                currentStartingPoint[0] = new LatLng(task.getResult().getLatitude(), task.getResult().getLongitude());
                                            } catch (NullPointerException e) {
                                                LocalSave.makeSnackBar("Unable to find location of user", getWindow().getDecorView().getRootView());
                                            }
                                        }
                                    });
                                }
                                else {
                                    currentStartingPoint[0] = new LatLng(47.606470, -122.334289);
                                }
                                currentDestination[0] = new LatLng(lat,lng);
                            });
                            relativeLayout.addView(newLL);
                        }
                    }
                } catch (JSONException | IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
            return false;
        });

        //Button to close route generation UI
        Button closeDirections = (Button) findViewById(R.id.closeDirections);
        closeDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.clear();
                //Remove all UI for directions
                RelativeLayout defaultSearchView = (RelativeLayout) findViewById(R.id.defaultSearchLayout);
                defaultSearchView.setVisibility(View.VISIBLE);
                RelativeLayout newSearchView = (RelativeLayout) findViewById(R.id.newSearchLayout);
                newSearchView.setVisibility(View.INVISIBLE);
            }
        });

        //Show direction list for generated route
        Button showDirections = (Button) findViewById(R.id.showDirections);
        showDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RelativeLayout directionLayout = (RelativeLayout) findViewById(R.id.directionLayout);
                directionLayout.setVisibility(View.VISIBLE);
            }
        });

        //Close directions list
        Button DirectionsExit = (Button) findViewById(R.id.DirectionsExit);
        DirectionsExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RelativeLayout directionLayout = (RelativeLayout) findViewById(R.id.directionLayout);
                directionLayout.setVisibility(View.INVISIBLE);
            }
        });

        //Save current destination
        Button saveDestination = (Button) findViewById(R.id.saveDestination);
        saveDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // placeholder
                ArrayList<String>[] previousSave = new ArrayList[0];
                try {
                    previousSave = LocalSave.loadSavedLocations(MapsActivity.this);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    //Add name and address of destination to saved locations
                    assert previousSave != null;
                    ArrayList<String> previousNames = previousSave[0];
                    ArrayList<String> previousAddresses = previousSave[1];
                    SearchView toView = (SearchView) findViewById(R.id.searchView3);

                    String query = toView.getQuery().toString();

                    Map<String, Object> map = (HashMap) CoordinateHelper.textToCoordinatesAndAddress(query)[0];
                    String lat = String.valueOf(map.get("latitude"));
                    String lng = String.valueOf(map.get("longitude"));
                    String latlng = lat + ", " + lng;

                    //Make sure destination is not already saved
                    if (!previousAddresses.contains(latlng)) {

                        previousNames.add(query);
                        previousAddresses.add(latlng);

                        LocalSave.saveSavedLocations(previousNames, previousAddresses, MapsActivity.this);

                        LocalSave.makeSnackBar(query + " has been added to saved places", view);
                    } else {
                        LocalSave.makeSnackBar("Location is already saved", view);
                    }
                } catch (NullPointerException | ArrayIndexOutOfBoundsException _) {
                    SearchView toView = (SearchView) findViewById(R.id.searchView3);
                    String query = toView.getQuery().toString();
                    Map<String, Object> map = (HashMap) CoordinateHelper.textToCoordinatesAndAddress(query)[0];
                    String lat = String.valueOf(map.get("latitude"));
                    String lng = String.valueOf(map.get("longitude"));
                    String latlng = lat + ", " + lng;

                    ArrayList<String> names = new ArrayList<>();
                    names.add(query);
                    ArrayList<String> addresses = new ArrayList<>();
                    addresses.add(latlng);
                    LocalSave.saveSavedLocations(names, addresses, MapsActivity.this);
                }
            }
        });

        //Display all food banks
        Button resourceFood = (Button) findViewById(R.id.resourceFood);
        resourceFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Search for food banks and return results
                Object[] places = CoordinateHelper.textToCoordinatesAndAddress("food bank");
                int oi = 0;
                LinearLayout resourceViewerLayout = (LinearLayout) findViewById(R.id.resourceViewerLayout);
                resourceViewerLayout.removeAllViews();
                assert places != null;
                for (Object o : places) {
                    //Add current food bank to resource viewer
                    oi++;
                    Map<String, Object> map = (Map<String, Object>) o;
                    Button currentB = new Button(getApplicationContext());
                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    currentB.setLayoutParams(llp);
                    currentB.setText(Objects.requireNonNull(map.get("name")).toString());

                    //When food bank button pressed
                    currentB.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //Show route generation UI
                            RelativeLayout defaultSearchView = (RelativeLayout) findViewById(R.id.defaultSearchLayout);
                            defaultSearchView.setVisibility(View.INVISIBLE);
                            RelativeLayout newSearchView = (RelativeLayout) findViewById(R.id.newSearchLayout);
                            newSearchView.setVisibility(View.VISIBLE);
                            CharSequence fromText = "CURRENT LOCATION";
                            CharSequence toText = Objects.requireNonNull(map.get("name")).toString();
                            fromView.setQuery(fromText, false);
                            toView.setQuery(toText, false);
                            if (checkLocationPermissions()) {
                                fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Location> task) {
                                        try {
                                            currentStartingPoint[0] = new LatLng(task.getResult().getLatitude(), task.getResult().getLongitude());
                                        } catch (NullPointerException e) {
                                            LocalSave.makeSnackBar("Unable to find location of user", getWindow().getDecorView().getRootView());
                                        }
                                    }
                                    //override methods
                                });
                            }
                            else {
                                currentStartingPoint[0] = new LatLng(47.606470, -122.334289);
                            }
                            currentDestination[0] = new LatLng((Double) map.get("latitude"), (Double) map.get("longitude"));
                        }
                    });

                    resourceViewerLayout.addView(currentB);
                }
                RelativeLayout resourceSelector = (RelativeLayout) findViewById(R.id.resourceSelector);
                ScrollView resourceViewer = (ScrollView) findViewById(R.id.resourceViewer);
                resourceSelector.setVisibility(View.INVISIBLE);
                resourceViewer.setVisibility(View.VISIBLE);
            }
        });

        //Same as food bank viewer but for shelters
        Button resourceShelter = (Button) findViewById(R.id.resourceShelter);
        resourceShelter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Object[] places = CoordinateHelper.textToCoordinatesAndAddress("shelter");
                int oi = 0;
                LinearLayout resourceViewerLayout = (LinearLayout) findViewById(R.id.resourceViewerLayout);
                resourceViewerLayout.removeAllViews();
                assert places != null;
                for (Object o : places) {
                    oi++;
                    Map<String, Object> map = (Map<String, Object>) o;
                    Button currentB = new Button(getApplicationContext());
                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    currentB.setLayoutParams(llp);
                    currentB.setText(Objects.requireNonNull(map.get("name")).toString());
                    currentB.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            RelativeLayout defaultSearchView = (RelativeLayout) findViewById(R.id.defaultSearchLayout);
                            defaultSearchView.setVisibility(View.INVISIBLE);
                            RelativeLayout newSearchView = (RelativeLayout) findViewById(R.id.newSearchLayout);
                            newSearchView.setVisibility(View.VISIBLE);
                            CharSequence fromText = "CURRENT LOCATION";
                            CharSequence toText = Objects.requireNonNull(map.get("name")).toString();
                            fromView.setQuery(fromText, false);
                            toView.setQuery(toText, false);
                            if (checkLocationPermissions()) {
                                fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Location> task) {
                                        try {
                                            currentStartingPoint[0] = new LatLng(task.getResult().getLatitude(), task.getResult().getLongitude());
                                        } catch (NullPointerException e) {
                                            LocalSave.makeSnackBar("Unable to find location of user", getWindow().getDecorView().getRootView());
                                        }
                                    }
                                });
                            }
                            else {
                                currentStartingPoint[0] = new LatLng(47.606470, -122.334289);
                            }
                            currentDestination[0] = new LatLng((Double) map.get("latitude"), (Double) map.get("longitude"));
                        }
                    });
                    resourceViewerLayout.addView(currentB);
                }
                RelativeLayout resourceSelector = (RelativeLayout) findViewById(R.id.resourceSelector);
                ScrollView resourceViewer = (ScrollView) findViewById(R.id.resourceViewer);
                resourceSelector.setVisibility(View.INVISIBLE);
                resourceViewer.setVisibility(View.VISIBLE);
            }
        });

        //Same as food bank viewer but for police stations
        Button resourcePolice = (Button) findViewById(R.id.resourcePolice);
        resourcePolice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Object[] places = CoordinateHelper.textToCoordinatesAndAddress("police station");
                int oi = 0;
                LinearLayout resourceViewerLayout = (LinearLayout) findViewById(R.id.resourceViewerLayout);
                resourceViewerLayout.removeAllViews();
                assert places != null;
                for (Object o : places) {
                    oi++;
                    Map<String, Object> map = (Map<String, Object>) o;
                    Button currentB = new Button(getApplicationContext());
                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    currentB.setLayoutParams(llp);
                    currentB.setText(Objects.requireNonNull(map.get("name")).toString());
                    currentB.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            RelativeLayout defaultSearchView = (RelativeLayout) findViewById(R.id.defaultSearchLayout);
                            defaultSearchView.setVisibility(View.INVISIBLE);
                            RelativeLayout newSearchView = (RelativeLayout) findViewById(R.id.newSearchLayout);
                            newSearchView.setVisibility(View.VISIBLE);
                            CharSequence fromText = "CURRENT LOCATION";
                            CharSequence toText = map.get("name").toString();
                            fromView.setQuery(fromText, false);
                            toView.setQuery(toText, false);
                            if (checkLocationPermissions()) {
                                fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Location> task) {
                                        try {
                                            currentStartingPoint[0] = new LatLng(task.getResult().getLatitude(), task.getResult().getLongitude());
                                        } catch (NullPointerException e) {
                                            LocalSave.makeSnackBar("Unable to find location of user", getWindow().getDecorView().getRootView());
                                        }
                                    }
                                });
                            }
                            else {
                                currentStartingPoint[0] = new LatLng(47.606470, -122.334289);
                            }
                            currentDestination[0] = new LatLng((Double) map.get("latitude"), (Double) map.get("longitude"));
                        }
                    });
                    resourceViewerLayout.addView(currentB);
                }
                RelativeLayout resourceSelector = (RelativeLayout) findViewById(R.id.resourceSelector);
                ScrollView resourceViewer = (ScrollView) findViewById(R.id.resourceViewer);
                resourceSelector.setVisibility(View.INVISIBLE);
                resourceViewer.setVisibility(View.VISIBLE);
            }
        });

        //When "Show Route" button is pressed
        Button submitDirections = (Button) findViewById(R.id.submitDirections);
        //Initialize routing object
        try {
            r = new Routing(getApplicationContext(), mnewRegions, mnewRoutes, mnewStops, mnewTrips, mfullRegions);
            System.out.println(r);
        } catch (IOException | ParseException i) {
            System.out.println(i);
        }
        Routing finalR = r;
        System.out.println("!@#$%^&*(*&^%$#$%^&*()(*&^%");
        System.out.println(r);
        submitDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDirections.setVisibility(View.VISIBLE);
                CoordinateHelper ch = new CoordinateHelper(getApplicationContext());
                String nearestBusStop = ch.findNearestBusStop(currentStartingPoint[0].latitude, currentStartingPoint[0].longitude);
                //Generate route between starting and ending position
                List<Routing.RouteItem> route = finalR.genRoute(LocalTime.of(9,0,0), new LatLng(47.545130, -122.137246), new LatLng(47.609165, -122.339078));
                JSONObject stops = null;
                //Create polyline to show route on map
                PolylineOptions po = new PolylineOptions();
                try {
                    stops = null; //Web.readJSON(new InputStreamReader(getAssets().open("newStops.json")));
                } catch (Exception e){}//ParseException | IOException ignored) {}
                RelativeLayout directionLayout = (RelativeLayout) findViewById(R.id.directionLayout);
                directionLayout.removeViews(1,directionLayout.getChildCount()-1);
                for (int i = 0; i < route.size(); i++) {
                    //Generate directions list and add to directions layout
                    TextView tv = new TextView(getApplicationContext());
                    RelativeLayout.LayoutParams llp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    llp.setMargins(0,(i+1)*80,0,0);
                    tv.setLayoutParams(llp);
                    assert stops != null;
                    JSONObject currentStop = (JSONObject) stops.get(route.get(i).stop);
                    if (i == 0) {
                        try {
                            tv.setText("START AT " + ch.getStopAddr(route.get(i).stop));
                        } catch (IOException | ParseException ignored) {}
                        assert currentStop != null;
                        createMapMarker(Double.parseDouble(Objects.requireNonNull(currentStop.get("latitude")).toString()), Double.parseDouble(currentStop.get("longitude").toString()), "Stop " + (i+1), "#f91504");
                    }
                    else {
                        try {
                            tv.setText("TAKE ROUTE " + ch.getRouteNum(route.get(i).route) + " TO " + ch.getStopAddr(route.get(i).stop));
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        createMapMarker(Double.parseDouble(currentStop.get("latitude").toString()), Double.parseDouble(currentStop.get("longitude").toString()), "Stop " + (i + 1), "#0409f9");
                    }
                    directionLayout.addView(tv);
                    po.add(new LatLng(Double.parseDouble(currentStop.get("latitude").toString()), Double.parseDouble(currentStop.get("longitude").toString())));
                }
                mMap.addPolyline(po);
            }
        });

        //If map is tapped, exit out of search view
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                locationSearch.clearFocus();
            }
        });

        //If typing in new destination, remove buttons
        toView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                showDirections.setVisibility(View.INVISIBLE);
                submitDirections.setVisibility(View.INVISIBLE);
                saveDestination.setVisibility(View.INVISIBLE);
            }
        });

        //If typing in new starting point, remove buttons
        fromView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                showDirections.setVisibility(View.INVISIBLE);
                submitDirections.setVisibility(View.INVISIBLE);
                saveDestination.setVisibility(View.INVISIBLE);
            }
        });

        //When new destination is entered by user
        toView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                //Set destination to query
                if (CoordinateHelper.textToCoordinatesAndAddress(query) != null) {
                    Map<String, Object> map = (HashMap) CoordinateHelper.textToCoordinatesAndAddress(query)[0];
                    currentDestination[0] = new LatLng((Double) map.get("latitude"), (Double) map.get("longitude"));
                    mMap.clear();
                    createMapMarker((Double) map.get("latitude"), (Double) map.get("longitude"), "Destination", "#09f904");
                }
                //Create map marker for destination
                createMapMarker(currentStartingPoint[0].latitude, currentStartingPoint[0].longitude, "Start", "#f91104");
                showDirections.setVisibility(View.INVISIBLE);
                submitDirections.setVisibility(View.VISIBLE);
                saveDestination.setVisibility(View.VISIBLE);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        //When new starting point is entered by user
        fromView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                //Set starting point to query
                if (CoordinateHelper.textToCoordinatesAndAddress(query) != null) {
                    Map<String, Object> map = (HashMap) CoordinateHelper.textToCoordinatesAndAddress(query)[0];
                    currentStartingPoint[0] = new LatLng((Double) map.get("latitude"), (Double) map.get("longitude"));
                    mMap.clear();
                    createMapMarker((Double) map.get("latitude"), (Double) map.get("longitude"), "Start", "#f91104");
                }
                //Create map marker for starting point
                createMapMarker(currentDestination[0].latitude, currentDestination[0].longitude, "Destination", "#09f904");
                showDirections.setVisibility(View.INVISIBLE);
                submitDirections.setVisibility(View.VISIBLE);
                saveDestination.setVisibility(View.VISIBLE);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        //When default search view is submitted
        locationSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                System.out.println("*******START");
                System.out.println(query);
                System.out.println(Arrays.toString(CoordinateHelper.textToCoordinatesAndAddress(query)));
                if (CoordinateHelper.textToCoordinatesAndAddress(query) != null) {
                    System.out.println("*******YES");
                    //Show route generation UI
                    RelativeLayout defaultSearchView = (RelativeLayout) findViewById(R.id.defaultSearchLayout);
                    defaultSearchView.setVisibility(View.INVISIBLE);
                    RelativeLayout newSearchView = (RelativeLayout) findViewById(R.id.newSearchLayout);
                    newSearchView.setVisibility(View.VISIBLE);
                    CharSequence fromText = "CURRENT LOCATION";
                    CharSequence toText = query;
                    fromView.setQuery(fromText, false);
                    toView.setQuery(toText, false);
                    Map<String, Object> map = (HashMap) CoordinateHelper.textToCoordinatesAndAddress(query)[0];
                    //Set starting point to user location if possible
                    if (checkLocationPermissions()) {
                        fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                try {
                                    currentStartingPoint[0] = new LatLng(task.getResult().getLatitude(), task.getResult().getLongitude());
                                } catch (NullPointerException e) {
                                    LocalSave.makeSnackBar("Unable to find location of user", getWindow().getDecorView().getRootView());
                                }
                            }
                        });
                    }
                    else {
                        currentStartingPoint[0] = new LatLng(47.606470, -122.334289);
                    }
                    currentDestination[0] = new LatLng((Double) map.get("latitude"), (Double) map.get("longitude"));
                }

                //If auto show route map setting is on, immediately show route map
                ToggleButton autoShowRouteMap = findViewById(R.id.setting2);
                Button showRouteButton = findViewById(R.id.submitDirections);
                if (autoShowRouteMap.isChecked()) {
                    showRouteButton.performClick();
                }

                //If auto save destination setting is on, immediately save destination
                ToggleButton autoSaveDestination = findViewById(R.id.setting3);
                Button saveDestinationButton = findViewById(R.id.saveDestination);
                if (autoSaveDestination.isChecked()) {
                    saveDestinationButton.performClick();
                }

                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }

    /**
     * Toggle settings by changing local storage value
     * @param view Application view
     */
    public void settingsButtonClicked(View view) {
        ToggleButton button = (ToggleButton) view.findViewById(view.getId());
        boolean isChecked = button.isChecked();
        LocalSave.saveBoolean(String.valueOf(view.getId()), isChecked, MapsActivity.this);
    }

    public void stopAllThreads() {
        for (Thread thread : currentThreads) {
            thread.interrupt();
        }
        currentThreads.clear();
    }
}