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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.Spinner;
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
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

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
    private Snackbar loadingSnackbar;

    public Object[][] cities = new Object[][]{
            new Object[]{"seattle", new LatLng(47.606471, -122.334604), true, "https://s3.amazonaws.com/kcm-alerts-realtime-prod/vehiclepositions_pb.json"},
            new Object[]{"washington", new LatLng(38.892958, -77.036163), false, "https://api.wmata.com/Bus.svc/json/jBusPositions?api_key=42604b5787594cd8b5360df332607b8b"}
    };
    public Object[] city;

    /**
     * Method to run when app is opened
     * @param savedInstanceState
     */
    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        //Update UI elements
        Spinner spinner = (Spinner) findViewById(R.id.CitySpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.city_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    /**
     * Adds marker to map based on given location, title, and color
     * @param latitude Latitude of marker position
     * @param longitude Longitude of marker position
     * @param title Title of marker
     * @param color Color of marker
     * @return Data of created marker
     */
    public void createMapMarker(Double latitude, Double longitude, String title, String color, String type) {
        LatLng pos = new LatLng(latitude, longitude);
        MarkerOptions marker = new MarkerOptions();
        marker.position(pos);
        float[] hsv = new float[3];
        Color.colorToHSV(Color.parseColor(color), hsv);
        switch(type) {
            case "bus":
                marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.busicon));
                break;
            case "home":
                marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.house));
                break;
            default:
                marker.icon(BitmapDescriptorFactory.defaultMarker());
                break;
        }
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
            String data = Web.readFromWeb(city[3].toString());
            System.out.println("got data from web after " + (System.currentTimeMillis() - startTime) + " ms");
            JSONObject o = Web.readJSON(new StringReader(data));
            System.out.println("parsed data to json after " + (System.currentTimeMillis() - startTime) + " ms");
            ArrayList<LatLng> positions = null;
            if (city[0] == "washington") {
                JSONArray a = (JSONArray) o.get("BusPositions");
                Iterator<JSONObject> iterator = a.iterator();
                positions = new ArrayList<>();
                //Loop through all running buses
                while (iterator.hasNext()) {
                    JSONObject jj = iterator.next();
                    //If route ID is correct, add bus to positions
                    if (jj.get("RouteID").equals(busID)) {
                        double lat = (double) jj.get("Lat");
                        double lon = (double) jj.get("Lon");
                        LatLng latlng = new LatLng(lat, lon);
                        positions.add(latlng);
                    }
                }
            }
            else if (city[0] == "seattle") {
                JSONArray a = (JSONArray) o.get("entity");
                Iterator<JSONObject> iterator = a.iterator();
                positions = new ArrayList<>();
                while (iterator.hasNext()) {
                    JSONObject jj = iterator.next();
                    JSONObject vehicle = (JSONObject) jj.get("vehicle");
                    JSONObject trip = (JSONObject) vehicle.get("trip");
                    if (trip.get("route_id").equals(busID)) {
                        JSONObject position = (JSONObject) vehicle.get("position");
                        double lat = (double) position.get("latitude");
                        double lon = (double) position.get("longitude");
                        LatLng latlng = new LatLng(lat, lon);
                        positions.add(latlng);
                    }
                }
            }
            //Return positions
            if (positions.size() > 0) {
                System.out.println("found valid positions in json after " + (System.currentTimeMillis() - startTime) + " ms");
                return positions;
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
                loadingSnackbar = Snackbar.make(getWindow().getDecorView().getRootView(), "Loading Route Map...", 1000000);
                loadingSnackbar.show();
                JSONObject r = null;
                try {
                    r = Web.readJSON(new InputStreamReader(getAssets().open(city[0].toString() + "/routes.json")));
                } catch (Exception e){}///*ParseException | IOException ignored*/) {}
                System.out.println("DAAAAAAAAAAAAAVEY!!!!!!");
                JSONObject item;
                item = (JSONObject) r.get(routeID);
                if (item == null) {
                    loadingSnackbar.dismiss();
                    Snackbar ns = Snackbar.make(getWindow().getDecorView().getRootView(), "Route " + routeID + " does not exist", 1500);
                    ns.show();
                }
                else {
                    System.out.println("AMMMMMMMMMMMMMMONGS");
                    JSONArray shapeIDss = (JSONArray) item.get("shape_ids");
                    assert shapeIDss != null;
                    Object[] shapeIDs = shapeIDss.toArray();
                    //Loop through all shapes in current route
                    System.out.println("DONNNNNNNNNNNNNNNNNE");
                    JSONObject o = null;
                    try {
                        o = Web.readJSON(new InputStreamReader(getAssets().open(city[0].toString() + "/shapes.json")));
                    } catch (Exception e) {
                    }
                    for (Object ii : shapeIDs) {
                        System.out.println("IN LOOP");
                        JSONArray locations = (JSONArray) o.get(ii.toString());
                        Iterator<JSONObject> i = locations.iterator();
                        PolylineOptions polyline = new PolylineOptions();
                        //Add position of shape to polyline
                        while (i.hasNext()) {
                            JSONObject currentObject = i.next();
                            LatLng hii = new LatLng(Double.parseDouble((String) Objects.requireNonNull(currentObject.get("latitude"))), Double.parseDouble((String) Objects.requireNonNull(currentObject.get("longitude"))));
                            polyline.add(hii);
                            try {
                                i.next();
                                i.next();
                            } catch (NoSuchElementException ignored) {
                            }
                        }
                        polylineList.add(polyline);
                    }
                    routeMapHandler.post(() -> {
                        loadingSnackbar.dismiss();
                        if (! Thread.currentThread().isInterrupted()) {
                            for (PolylineOptions pl : polylineList) {
                                mMap.addPolyline(pl);
                            }
                        }
                    });
                }
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
        Spinner spinner = (Spinner) findViewById(R.id.CitySpinner);
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPreferences", MODE_PRIVATE);
        if (sharedPreferences.contains("city")) {
            int i = 0;
            for (Object[] c : cities) {
                if (c[0].toString().equals(sharedPreferences.getString("city", "seattle").toLowerCase())) {
                    city = cities[i];
                    spinner.setSelection(i);
                    break;
                }
                i++;
            }
        }
        else {
            city = cities[0];
            LocalSave.saveString("city", city[0].toString().toLowerCase(), this);
        }
        Button saveDestination = (Button) findViewById(R.id.saveDestination);
        //When "Show Route" button is pressed
        Button submitDirections = (Button) findViewById(R.id.submitDirections);
        //Initialize starting and ending points for use in routing
        final LatLng[] currentDestination = {(LatLng) city[1]};
        final LatLng[] currentStartingPoint = {(LatLng) city[1]};

        //Initialize Google map and set camera to selected city location
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom((LatLng) city[1], 12));

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
                //SharedPreferences sharedPreferences = getSharedPreferences("sharedPreferences", MODE_PRIVATE);
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
        Button finalSaveDestination = saveDestination;
        mBottomNavigationView.setOnItemSelectedListener(item -> {
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
                System.out.println("AAAAAAAAAAAAAAAAAA CLOSE THE THING");
                Button showDirections = (Button) findViewById(R.id.showDirections);
                System.out.println("--HIDE1");
                showDirections.setVisibility(View.INVISIBLE);
                stopAllThreads();
                menu[0].setVisibility(View.INVISIBLE);
                mMap.clear();
                mBottomNavigationView.getMenu().setGroupCheckable(0,false,true);
            });

            spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    System.out.println("SPINNER SELECTION");
                    System.out.println(spinner.getSelectedItem().toString());
                    for (Object[] c : cities) {
                        if (c[0].toString().equals(spinner.getSelectedItem().toString().toLowerCase())) {
                            city = c;
                            currentDestination[0] = (LatLng) city[1];
                            currentStartingPoint[0] = (LatLng) city[1];
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom((LatLng) city[1], 12));
                            break;
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
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
                                loadingSnackbar = Snackbar.make(getWindow().getDecorView().getRootView(), "Loading Bus Locations...", 1000000);
                                loadingSnackbar.show();
                                CoordinateHelper ch = new CoordinateHelper(getApplicationContext());
                                ArrayList<LatLng> positions = null;
                                try {
                                    positions = getBusLocation(ch.getRouteID(city[0].toString(), query));
                                } catch (IOException | ParseException e) {
                                    e.printStackTrace();
                                }
                                final ArrayList<LatLng> finalPositions = positions;
                                mapHandler.post(() -> {
                                    loadingSnackbar.dismiss();
                                    if (! Thread.currentThread().isInterrupted()) {
                                        if (finalPositions == null || finalPositions.size() == 0) {
                                            LocalSave.makeSnackBar("No Active Buses of Route " + query, getWindow().getDecorView().getRootView());
                                        }
                                        //Create map markers for each running bus
                                        else {
                                            for (int i = 0; i < finalPositions.size(); i++) {
                                                LatLng pos = finalPositions.get(i);
                                                createMapMarker(pos.latitude, pos.longitude, "", "#f91504", "bus");
                                            }
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
                                if (Boolean.parseBoolean(city[2].toString())) {
                                    JSONObject obj = Web.readJSON(new InputStreamReader(getAssets().open(city[0].toString() + "/displayNameToRouteID.json")));
                                    String routeID = (String) obj.get(query);
                                    showRouteMap(routeID, query);
                                }
                                else {
                                    showRouteMap(query, query);
                                }
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
                                    currentStartingPoint[0] = (LatLng) city[1];
                                }
                                currentDestination[0] = new LatLng(lat,lng);
                                finalSaveDestination.setVisibility(View.VISIBLE);
                                submitDirections.setVisibility(View.VISIBLE);
                                createMapMarker(currentStartingPoint[0].latitude, currentStartingPoint[0].longitude, "Start", "#f91104", "home");
                                createMapMarker(currentDestination[0].latitude, currentDestination[0].longitude, "Destination", "#f91104", "");
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
                stopAllThreads();
                Button showDirections = (Button) findViewById(R.id.showDirections);
                showDirections.setVisibility(View.INVISIBLE);
                System.out.println("--HIDE7");

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
        saveDestination = (Button) findViewById(R.id.saveDestination);
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
                    Map<String, Object> map = (HashMap) CoordinateHelper.textToCoordinatesAndAddress(query, Double.toString(((LatLng) city[1]).latitude), Double.toString(((LatLng) city[1]).longitude))[0];
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
                    Map<String, Object> map = (HashMap) CoordinateHelper.textToCoordinatesAndAddress(query, Double.toString(((LatLng) city[1]).latitude), Double.toString(((LatLng) city[1]).longitude))[0];
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
        Button finalSaveDestination1 = saveDestination;
        resourceFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Search for food banks and return results
                Object[] places = CoordinateHelper.textToCoordinatesAndAddress("food bank", Double.toString(((LatLng) city[1]).latitude), Double.toString(((LatLng) city[1]).longitude));
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
                                currentStartingPoint[0] = (LatLng) city[1];
                            }
                            currentDestination[0] = new LatLng((Double) map.get("latitude"), (Double) map.get("longitude"));
                            finalSaveDestination1.setVisibility(View.VISIBLE);
                            submitDirections.setVisibility(View.VISIBLE);
                            createMapMarker(currentStartingPoint[0].latitude, currentStartingPoint[0].longitude, "Start", "#f91104", "home");
                            createMapMarker(currentDestination[0].latitude, currentDestination[0].longitude, "Destination", "#f91104", "");
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
        Button finalSaveDestination2 = saveDestination;
        resourceShelter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Object[] places = CoordinateHelper.textToCoordinatesAndAddress("shelter", Double.toString(((LatLng) city[1]).latitude), Double.toString(((LatLng) city[1]).longitude));
                System.out.println(places);
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
                                currentStartingPoint[0] = (LatLng) city[1];
                            }
                            currentDestination[0] = new LatLng((Double) map.get("latitude"), (Double) map.get("longitude"));
                            finalSaveDestination2.setVisibility(View.VISIBLE);
                            submitDirections.setVisibility(View.VISIBLE);
                            createMapMarker(currentStartingPoint[0].latitude, currentStartingPoint[0].longitude, "Start", "#f91104", "home");
                            createMapMarker(currentDestination[0].latitude, currentDestination[0].longitude, "Destination", "#f91104", "");
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
        Button finalSaveDestination3 = saveDestination;
        resourcePolice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Object[] places = CoordinateHelper.textToCoordinatesAndAddress("police station", Double.toString(((LatLng) city[1]).latitude), Double.toString(((LatLng) city[1]).longitude));
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
                                currentStartingPoint[0] = (LatLng) city[1];
                            }
                            currentDestination[0] = new LatLng((Double) map.get("latitude"), (Double) map.get("longitude"));
                            finalSaveDestination3.setVisibility(View.VISIBLE);
                            submitDirections.setVisibility(View.VISIBLE);
                            createMapMarker(currentStartingPoint[0].latitude, currentStartingPoint[0].longitude, "Start", "#f91104", "home");
                            createMapMarker(currentDestination[0].latitude, currentDestination[0].longitude, "Destination", "#f91104", "");
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

        //Initialize routing object
        r = new Routing();
        Routing finalR = r;
        System.out.println("!@#$%^&*(*&^%$#$%^&*()(*&^%");
        System.out.println(r);
        submitDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Handler routeHandler = new Handler();
                Runnable routeRunnable = () -> {
                    loadingSnackbar = Snackbar.make(getWindow().getDecorView().getRootView(), "Loading Route...", 1000000);
                    loadingSnackbar.show();
                    CoordinateHelper ch = new CoordinateHelper(getApplicationContext());
                    String nearestBusStop = ch.findNearestBusStop(city[0].toString(), currentStartingPoint[0].latitude, currentStartingPoint[0].longitude);
                    System.out.println("YOUR DESTINATION:");
                    System.out.println(currentDestination[0]);
                    final JSONObject[] stops = {null};
                    try {
                        stops[0] = Web.readJSON(new InputStreamReader(getAssets().open(city[0].toString() + "/stops.json")));
                    } catch (Exception e){}//ParseException | IOException ignored) {}
                    System.out.println("NEAREST STOP: " + nearestBusStop);
                    LatLng nearestStop = new LatLng(Double.parseDouble(((JSONObject) stops[0].get(nearestBusStop)).get("latitude").toString()), Double.parseDouble(((JSONObject) stops[0].get(nearestBusStop)).get("longitude").toString()));
                    //Generate route between starting and ending position
                    List<Routing.RouteItem> route = null;
                    try {
                        System.out.println("HHHHHHHHHHHHHHHHHHHHHHHHH");
                        route = finalR.genRoute(LocalTime.now(), nearestStop, currentDestination[0], (String) city[0]);
                    } catch (IOException | ParseException e) {
                        e.printStackTrace();
                    }
                    List<Routing.RouteItem> finalRoute = route;
                    routeHandler.post(() -> {
                        loadingSnackbar.dismiss();
                        if (! Thread.currentThread().isInterrupted()) {
                            System.out.println("--SHOW2");
                            showDirections.setVisibility(View.VISIBLE);
                            //Create polyline to show route on map
                            RelativeLayout directionLayout = (RelativeLayout) findViewById(R.id.directionLayout);
                            directionLayout.removeViews(1, directionLayout.getChildCount() - 1);
                            PolylineOptions walk = new PolylineOptions();
                            List<PatternItem> pattern = Arrays.<PatternItem>asList(new Dot(), new Gap(20), new Dash(30), new Gap(20));
                            walk.pattern(pattern);
                            if (finalRoute == null || finalRoute.size() == 0) {
                                LocalSave.makeSnackBar("Could Not Find Route", getWindow().getDecorView().getRootView());
                            }
                            else {
                                for (int i = 0; i < finalRoute.size(); i++) {
                                    JSONObject prevStop = (JSONObject) stops[0].get(finalRoute.get(i).startStop);
                                    if (walk.getPoints().size() != 0) {
                                        System.out.println("WALK");
                                        walk.add(new LatLng(Double.parseDouble(prevStop.get("latitude").toString()), Double.parseDouble(prevStop.get("longitude").toString())));
                                        mMap.addPolyline(walk);
                                        walk = new PolylineOptions();
                                        walk.pattern(pattern);
                                    }
                                    //Generate directions list and add to directions layout
                                    TextView tv = new TextView(getApplicationContext());
                                    RelativeLayout.LayoutParams llp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                                    llp.setMargins(0, (i + 1) * 80, 0, 0);
                                    tv.setLayoutParams(llp);
                                    assert stops[0] != null;
                                    System.out.println("SSSSSTOP:" + finalRoute.get(i).stop);
                                    try {
                                        tv.setText("AT " + finalRoute.get(i).startTime + " TAKE ROUTE " + ch.getRouteNum(city[0].toString(), finalRoute.get(i).route) + " FROM " + ch.getStopAddr(city[0].toString(), finalRoute.get(i).startStop) + " TO " + ch.getStopAddr(city[0].toString(), finalRoute.get(i).stop));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }

                                    PolylineOptions po = new PolylineOptions();
                                    JSONObject currentStop = (JSONObject) stops[0].get(finalRoute.get(i).stop);
                                    createMapMarker(Double.parseDouble(prevStop.get("latitude").toString()), Double.parseDouble(prevStop.get("longitude").toString()), "Stop", "#0409f9", "bus");
                                    createMapMarker(Double.parseDouble(currentStop.get("latitude").toString()), Double.parseDouble(currentStop.get("longitude").toString()), "Stop", "#0409f9", "bus");
                                    directionLayout.addView(tv);
                                    po.add(new LatLng(Double.parseDouble(prevStop.get("latitude").toString()), Double.parseDouble(prevStop.get("longitude").toString())));
                                    po.add(new LatLng(Double.parseDouble(currentStop.get("latitude").toString()), Double.parseDouble(currentStop.get("longitude").toString())));
                                    mMap.addPolyline(po);
                                    walk.add(new LatLng(Double.parseDouble(currentStop.get("latitude").toString()), Double.parseDouble(currentStop.get("longitude").toString())));
                                }
                            }
                        }
                    });
                };
                System.out.println(routeRunnable);
                Thread routeThread = new Thread(routeRunnable);
                routeThread.start();
                currentThreads.add(routeThread);
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
        Button finalSaveDestination4 = saveDestination;
        toView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                System.out.println("--HIDE3");
                showDirections.setVisibility(View.INVISIBLE);
                submitDirections.setVisibility(View.INVISIBLE);
                finalSaveDestination4.setVisibility(View.INVISIBLE);
            }
        });

        //If typing in new starting point, remove buttons
        Button finalSaveDestination5 = saveDestination;
        fromView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                System.out.println("--HIDE4");
                showDirections.setVisibility(View.INVISIBLE);
                submitDirections.setVisibility(View.INVISIBLE);
                finalSaveDestination5.setVisibility(View.INVISIBLE);
            }
        });

        //When new destination is entered by user
        Button finalSaveDestination6 = saveDestination;
        toView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                //Set destination to query
                if (CoordinateHelper.textToCoordinatesAndAddress(query, Double.toString(((LatLng) city[1]).latitude), Double.toString(((LatLng) city[1]).longitude)) != null) {
                    Map<String, Object> map = (HashMap) CoordinateHelper.textToCoordinatesAndAddress(query, Double.toString(((LatLng) city[1]).latitude), Double.toString(((LatLng) city[1]).longitude))[0];
                    currentDestination[0] = new LatLng((Double) map.get("latitude"), (Double) map.get("longitude"));
                    mMap.clear();
                    createMapMarker((Double) map.get("latitude"), (Double) map.get("longitude"), "Destination", "#09f904", "");
                }
                //Create map marker for destination
                createMapMarker(currentStartingPoint[0].latitude, currentStartingPoint[0].longitude, "Start", "#f91104", "home");
                createMapMarker(currentDestination[0].latitude, currentDestination[0].longitude, "Destination", "#f91104", "");
                System.out.println("--HIDE5");
                showDirections.setVisibility(View.INVISIBLE);
                submitDirections.setVisibility(View.VISIBLE);
                finalSaveDestination6.setVisibility(View.VISIBLE);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        //When new starting point is entered by user
        Button finalSaveDestination7 = saveDestination;
        fromView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                //Set starting point to query
                if (CoordinateHelper.textToCoordinatesAndAddress(query, Double.toString(((LatLng) city[1]).latitude), Double.toString(((LatLng) city[1]).longitude)) != null) {
                    Map<String, Object> map = (HashMap) CoordinateHelper.textToCoordinatesAndAddress(query, Double.toString(((LatLng) city[1]).latitude), Double.toString(((LatLng) city[1]).longitude))[0];
                    currentStartingPoint[0] = new LatLng((Double) map.get("latitude"), (Double) map.get("longitude"));
                    mMap.clear();
                    createMapMarker((Double) map.get("latitude"), (Double) map.get("longitude"), "Start", "#f91104", "home");
                }
                //Create map marker for starting point
                createMapMarker(currentDestination[0].latitude, currentDestination[0].longitude, "Destination", "#09f904", "");
                System.out.println("--HIDE6");
                showDirections.setVisibility(View.INVISIBLE);
                submitDirections.setVisibility(View.VISIBLE);
                finalSaveDestination7.setVisibility(View.VISIBLE);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        //When default search view is submitted
        Button finalSaveDestination8 = saveDestination;
        locationSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {


            @Override
            public boolean onQueryTextSubmit(String query) {
                finalSaveDestination8.setVisibility(View.VISIBLE);
                submitDirections.setVisibility(View.VISIBLE);
                System.out.println("*******START");
                System.out.println(query);
                System.out.println(Arrays.toString(CoordinateHelper.textToCoordinatesAndAddress(query, Double.toString(((LatLng) city[1]).latitude), Double.toString(((LatLng) city[1]).longitude))));
                if (CoordinateHelper.textToCoordinatesAndAddress(query, Double.toString(((LatLng) city[1]).latitude), Double.toString(((LatLng) city[1]).longitude)) != null) {
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
                    Map<String, Object> map = (HashMap) CoordinateHelper.textToCoordinatesAndAddress(query, Double.toString(((LatLng) city[1]).latitude), Double.toString(((LatLng) city[1]).longitude))[0];
                    //Set starting point to user location if possible
                    if (checkLocationPermissions()) {
                        System.out.println("CHECK LOCATION PREMREIOERJP");
                        fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                try {
                                    currentStartingPoint[0] = new LatLng(task.getResult().getLatitude(), task.getResult().getLongitude());
                                    createMapMarker(currentStartingPoint[0].latitude, currentStartingPoint[0].longitude, "Start", "#f91104", "home");
                                } catch (NullPointerException e) {
                                    LocalSave.makeSnackBar("Unable to find location of user", getWindow().getDecorView().getRootView());
                                    currentStartingPoint[0] = (LatLng) city[1];
                                    createMapMarker(currentStartingPoint[0].latitude, currentStartingPoint[0].longitude, "Start", "#f91104", "home");
                                }
                            }
                        });
                    }
                    else {
                        currentStartingPoint[0] = (LatLng) city[1];
                        createMapMarker(currentStartingPoint[0].latitude, currentStartingPoint[0].longitude, "Start", "#f91104", "home");
                    }
                    currentDestination[0] = new LatLng((Double) map.get("latitude"), (Double) map.get("longitude"));
                }
                else {
                    LocalSave.makeSnackBar("Could Not Find Location", getWindow().getDecorView().getRootView());
                }
                //createMapMarker(currentStartingPoint[0].latitude, currentStartingPoint[0].longitude, "Start", "#f91104", "home");
                createMapMarker(currentDestination[0].latitude, currentDestination[0].longitude, "Destination", "#f91104", "");

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
        System.out.println("STOP ALL THREADS");
        if (loadingSnackbar != null) {
            loadingSnackbar.dismiss();
        }
        //Thread.currentThread().interrupt();
    }
}