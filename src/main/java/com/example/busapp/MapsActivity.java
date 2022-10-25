package com.example.busapp;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.ToggleButton;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.busapp.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Iterator;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FusedLocationProviderClient fusedLocationClient;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ArrayList<String[]> list = new ArrayList<String[]>();
        list.add(new String[]{"1", "2", "3"});
        list.add(new String[]{"4", "5", "6"});

        LocalSave.saveSavedLocations(list, MapsActivity.this);
        try {
            ArrayList<String[]> data = LocalSave.loadSavedLocations(MapsActivity.this);
            for (String[] a : data) {
                for (String b : a) {
                    System.out.println(b);
                }
            }
            System.out.println(data);
            System.out.println("AAAZZDWFGFWAFJFYUWAUKGYDUGKAWUKYGDUYKAWGKUYDUYGK");
        } catch (JSONException e) { e.printStackTrace(); }


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLocationPermissions();
        //Initialize app
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Start map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public MarkerOptions createMapMarker(Double latitude, Double longitude, String title, String color) {
        LatLng pos = new LatLng(latitude, longitude);
        MarkerOptions marker = new MarkerOptions();
        marker.position(pos);
        float[] hsv = new float[3];
        Color.colorToHSV(Color.parseColor(color), hsv);
        marker.icon(BitmapDescriptorFactory.defaultMarker(hsv[0]));
        marker.title(title);
        mMap.addMarker(marker);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
        return marker;
    }

    public ArrayList<LatLng> getBusLocation(String busID) {
        try {
            String data = Web.readFromWeb("https://s3.amazonaws.com/kcm-alerts-realtime-prod/vehiclepositions_pb.json");
            JSONObject o = Web.readJSON(new StringReader(data));
            JSONArray a = (JSONArray) o.get("entity");
            Iterator<JSONObject> iterator = a.iterator();
            ArrayList<LatLng> positions = new ArrayList<>();
            while (iterator.hasNext()) {
                JSONObject jj = iterator.next();
                JSONObject vehicle = (JSONObject) jj.get("vehicle");
                JSONObject v = (JSONObject) vehicle.get("vehicle");
                if (v.get("id").equals(busID)) {
                    JSONObject pos = (JSONObject) vehicle.get("position");
                    LatLng latlng = new LatLng((double) pos.get("latitude"), (double) pos.get("longitude"));
                    positions.add(latlng);
                }
            }
            ArrayList<LatLng> validPositions = new ArrayList<>();
            for (LatLng pos : positions) {
                if (pos.latitude != 0d && pos.longitude != 0d) {
                    validPositions.add(pos);
                }
            }
            if (validPositions.size() > 0) {
                return validPositions;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void showRouteMap(String routeID, String routeShortName) throws IOException, ParseException {
        try {
            JSONObject r = Web.readJSON(new InputStreamReader(getAssets().open("routes.json")));
            JSONObject item = (JSONObject) r.get(routeID);
            JSONArray shapeIDss = (JSONArray) item.get("shape_ids");
            Object[] shapeIDs = shapeIDss.toArray();
            for (Object ii : shapeIDs) {
                JSONObject o = Web.readJSON(new InputStreamReader(getAssets().open("shapes.json")));
                JSONArray locations = (JSONArray) o.get(ii.toString());
                Iterator<JSONObject> i = locations.iterator();
                PolylineOptions polyline = new PolylineOptions();
                while (i.hasNext()) {
                    JSONObject currentObject = i.next();
                    LatLng hii = new LatLng(Double.valueOf((String) currentObject.get("latitude")), Double.valueOf((String) currentObject.get("longitude")));
                    polyline.add(hii);
                }
                mMap.addPolyline(polyline);
            }
        } catch (NullPointerException _) {
            LocalSave.makeSnackBar("Unable to find route with ID " + routeShortName, getWindow().getDecorView().getRootView());
        }
    }

    public boolean checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    public void getLocationPermissions() {
        Activity a = this;
        String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        ActivityCompat.requestPermissions(a, permission, 1);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        //INITIALIZE ROUTING OBJECT
        Routing r = null;
        try {
            r = new Routing(getApplicationContext());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        final LatLng[] currentDestination = {new LatLng(47.606471, -122.334604)};
        final LatLng[] currentStartingPoint = {new LatLng(47.606471, -122.334604)};


        //IINITIALIZE GOOGLE MAP
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(47.6122709,-122.3471455), 12));

        //BOTTOM NAVIGATION VIEW STYLES
        final RelativeLayout[] menu = {null};
        BottomNavigationView mBottomNavigationView=(BottomNavigationView)findViewById(R.id.nav_view);

        //SETUP SETTINGS BUTTONS
        final int[] BUTTONS = {
                getResources().getIdentifier("setting1", "id", getPackageName()),
                getResources().getIdentifier("setting2", "id", getPackageName()),
                getResources().getIdentifier("setting3", "id", getPackageName())
        };
        for (int buttonID : BUTTONS) {
            try {
                SharedPreferences sharedPreferences = getSharedPreferences("sharedPreferences", MODE_PRIVATE);
                System.out.println(buttonID);
                ToggleButton button = findViewById(buttonID);
                boolean isChecked = sharedPreferences.getBoolean(String.valueOf(buttonID), false);
                System.out.println(isChecked);
                button.setChecked(isChecked);
            } catch (NullPointerException e) {}
        }

        //SETUP SAVED PLACES MENU
        /*try {
            ArrayList<String[]> savedPlaces = LocalSave.loadSavedLocations();
        } catch (JSONException e) {
            e.printStackTrace();
        }*/

        mBottomNavigationView.getMenu().setGroupCheckable(0,false,true);
        mBottomNavigationView.setOnItemSelectedListener(item -> {
            if (menu[0] != null) {
                menu[0].setVisibility(View.INVISIBLE);
            }
            String name = item.toString();
            menu[0] = (RelativeLayout) findViewById(getResources().getIdentifier(name + "Menu", "id", getPackageName()));
            menu[0].setVisibility(View.VISIBLE);
            item.setCheckable(true);
            item.setChecked(true);

            Button closeButton = (Button) menu[0].findViewById(getResources().getIdentifier(name + "Exit", "id", getPackageName()));
            closeButton.setOnClickListener(view -> {
                menu[0].setVisibility(View.INVISIBLE);
                mMap.clear();
                mBottomNavigationView.getMenu().setGroupCheckable(0,false,true);
            });
            SearchView searchView = (SearchView) menu[0].findViewById(getResources().getIdentifier(name + "SearchBar", "id", getPackageName()));
            if (searchView != null) {
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        if (name.equals("Buses")) {
                            ArrayList<LatLng> positions = getBusLocation(query);
                            if (positions == null || positions.size() == 0) {
                                LocalSave.makeSnackBar("No running buses with id " + query, getWindow().getDecorView().getRootView());
                            }
                            else {
                                for (int i = 0; i < positions.size(); i++) {
                                    LatLng pos = positions.get(i);
                                    createMapMarker(pos.latitude, pos.longitude, "","#f91504");
                                }
                            }
                        } else if (name.equals("Routes")) {
                            try {
                                JSONObject obj = Web.readJSON(new InputStreamReader(getAssets().open("displayNameToRouteID.json")));
                                String routeID = (String) obj.get(query);
                                showRouteMap(routeID, query);
                            } catch (IOException | ParseException _) {}
                        }
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                        return false;
                    }
                });
            }
            return false;
        });

        //BUTTON EVENTS
        Button closeDirections = (Button) findViewById(R.id.closeDirections);
        closeDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.clear();
                RelativeLayout defaultSearchView = (RelativeLayout) findViewById(R.id.defaultSearchLayout);
                defaultSearchView.setVisibility(View.VISIBLE);
                RelativeLayout newSearchView = (RelativeLayout) findViewById(R.id.newSearchLayout);
                newSearchView.setVisibility(View.INVISIBLE);
            }
        });
        SearchView locationSearch = (SearchView) findViewById(R.id.searchView);
        SearchView fromView = (SearchView) findViewById(R.id.searchView2);
        SearchView toView = (SearchView) findViewById(R.id.searchView3);


        Button showDirections = (Button) findViewById(R.id.showDirections);
        showDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RelativeLayout directionLayout = (RelativeLayout) findViewById(R.id.directionLayout);
                directionLayout.setVisibility(View.VISIBLE);
            }
        });
        Button DirectionsExit = (Button) findViewById(R.id.DirectionsExit);
        DirectionsExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RelativeLayout directionLayout = (RelativeLayout) findViewById(R.id.directionLayout);
                directionLayout.setVisibility(View.INVISIBLE);
            }
        });

        Button submitDirections = (Button) findViewById(R.id.submitDirections);
        Routing finalR = r;
        submitDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CoordinateHelper ch = new CoordinateHelper(getApplicationContext());
                String nearestBusStop = ch.findNearestBusStop(currentStartingPoint[0].latitude, currentStartingPoint[0].longitude);
                System.out.println("NEAR: " + nearestBusStop);
                ArrayList<Object[]> route = finalR.genRoute(currentDestination[0], nearestBusStop);
                JSONObject stops = null;
                PolylineOptions po = new PolylineOptions();
                try {
                    stops = Web.readJSON(new InputStreamReader(getAssets().open("newStops.json")));
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                for (int i = 0; i < route.size(); i++) {
                    System.out.println(Arrays.toString(route.get(i)));
                    JSONObject currentStop = (JSONObject) stops.get(route.get(i)[0].toString());
                    if (i == 0) {
                        createMapMarker(Double.parseDouble(currentStop.get("latitude").toString()), Double.parseDouble(currentStop.get("longitude").toString()), "Stop " + (i+1), "#f91504");
                    }
                    else {
                        createMapMarker(Double.parseDouble(currentStop.get("latitude").toString()), Double.parseDouble(currentStop.get("longitude").toString()), "Stop " + (i + 1), "#0409f9");
                    }
                    po.add(new LatLng(Double.parseDouble(currentStop.get("latitude").toString()), Double.parseDouble(currentStop.get("longitude").toString())));
                }
                mMap.addPolyline(po);
            }
        });




        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                locationSearch.clearFocus();
            }
        });

        final String[] amogus = {"2"};

        Routing finalR1 = r;

        toView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                System.out.println("CCCCC");
                showDirections.setVisibility(View.INVISIBLE);
                submitDirections.setVisibility(View.INVISIBLE);
            }
        });

        fromView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                System.out.println("CCCCC");
                showDirections.setVisibility(View.INVISIBLE);
                submitDirections.setVisibility(View.INVISIBLE);
            }
        });

        toView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                if (CoordinateHelper.textToCoordinatesAndAddress(query) != null) {
                    Map<String, Object> map = (HashMap) CoordinateHelper.textToCoordinatesAndAddress(query)[0];
                    currentDestination[0] = new LatLng((Double) map.get("latitude"), (Double) map.get("longitude"));
                    mMap.clear();
                    createMapMarker((Double) map.get("latitude"), (Double) map.get("longitude"), "Destination", "#09f904");
                }
                createMapMarker(currentStartingPoint[0].latitude, currentStartingPoint[0].longitude, "Start", "#f91104");
                showDirections.setVisibility(View.VISIBLE);
                submitDirections.setVisibility(View.VISIBLE);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        fromView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                if (CoordinateHelper.textToCoordinatesAndAddress(query) != null) {
                    Map<String, Object> map = (HashMap) CoordinateHelper.textToCoordinatesAndAddress(query)[0];
                    currentStartingPoint[0] = new LatLng((Double) map.get("latitude"), (Double) map.get("longitude"));
                    mMap.clear();
                    createMapMarker((Double) map.get("latitude"), (Double) map.get("longitude"), "Start", "#f91104");
                }
                else if (query.isEmpty()) {
                    if (checkLocationPermissions()) {
                        fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                currentStartingPoint[0] = new LatLng(task.getResult().getLatitude(), task.getResult().getLongitude());
                            }
                        });
                    }
                    else {
                        currentStartingPoint[0] = new LatLng(47.606470, -122.334289);
                    }
                    mMap.clear();
                    createMapMarker(currentStartingPoint[0].latitude, currentStartingPoint[0].longitude, "Start", "#f91104");
                }
                createMapMarker(currentDestination[0].latitude, currentDestination[0].longitude, "Destination", "#09f904");
                showDirections.setVisibility(View.VISIBLE);
                submitDirections.setVisibility(View.VISIBLE);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        //TEXT BOX EVENTS
        locationSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (CoordinateHelper.textToCoordinatesAndAddress(query) != null) {
                    RelativeLayout defaultSearchView = (RelativeLayout) findViewById(R.id.defaultSearchLayout);
                    defaultSearchView.setVisibility(View.INVISIBLE);
                    RelativeLayout newSearchView = (RelativeLayout) findViewById(R.id.newSearchLayout);
                    newSearchView.setVisibility(View.VISIBLE);
                    CharSequence fromText = "CURRENT LOCATION";
                    CharSequence toText = query;
                    fromView.setQuery(fromText, true);
                    toView.setQuery(toText, true);
                    Map<String, Object> map = (HashMap) CoordinateHelper.textToCoordinatesAndAddress(query)[0];
                    if (checkLocationPermissions()) {
                        fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                try {
                                    currentStartingPoint[0] = new LatLng(task.getResult().getLatitude(), task.getResult().getLongitude());
                                } catch (NullPointerException _) { LocalSave.makeSnackBar("Unable to find location of user", getWindow().getDecorView().getRootView()); }
                            }
                            //override methods
                        });
                    }
                    else {
                        currentStartingPoint[0] = new LatLng(47.606470, -122.334289);
                    }
                    currentDestination[0] = new LatLng((Double) map.get("latitude"), (Double) map.get("longitude"));
                    createMapMarker((Double) map.get("latitude"), (Double) map.get("longitude"), "Destination", "#f90404");
                }
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }
    public void settingsButtonClicked(View view) {
        ToggleButton button = (ToggleButton) view.findViewById(view.getId());
        boolean isChecked = button.isChecked();
        System.out.println(isChecked);
        LocalSave.saveBoolean(String.valueOf(view.getId()), isChecked, MapsActivity.this);
    }
}