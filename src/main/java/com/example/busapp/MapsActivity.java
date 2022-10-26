package com.example.busapp;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
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
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
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

    @SuppressLint({"SetTextI18n", "MissingPermission"})
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

        SearchView locationSearch = (SearchView) findViewById(R.id.searchView);
        SearchView fromView = (SearchView) findViewById(R.id.searchView2);
        SearchView toView = (SearchView) findViewById(R.id.searchView3);

        //BOTTOM NAVIGATION VIEW STYLES
        final RelativeLayout[] menu = {null};
        BottomNavigationView mBottomNavigationView=(BottomNavigationView)findViewById(R.id.nav_view);

        //SETUP SETTINGS BUTTONS
        final int[] BUTTONS = {
                getResources().getIdentifier("setting1", "id", getPackageName()),
                getResources().getIdentifier("setting2", "id", getPackageName()),
                getResources().getIdentifier("setting3", "id", getPackageName())
        };
        final boolean[] defaultVals = {true,false,false};
        int bi = -1;
        for (int buttonID : BUTTONS) {
            bi++;
            try {
                SharedPreferences sharedPreferences = getSharedPreferences("sharedPreferences", MODE_PRIVATE);
                System.out.println(buttonID);
                ToggleButton button = findViewById(buttonID);
                if (! sharedPreferences.contains(String.valueOf(buttonID))) {
                    LocalSave.saveBoolean(String.valueOf(buttonID), defaultVals[bi], MapsActivity.this);
                }
                boolean isChecked = sharedPreferences.getBoolean(String.valueOf(buttonID), false);
                System.out.println(isChecked);
                button.setChecked(isChecked);
            } catch (NullPointerException e) {}
        }

        mBottomNavigationView.getMenu().setGroupCheckable(0,false,true);
        mBottomNavigationView.setOnItemSelectedListener(item -> {
            if (menu[0] != null) {
                menu[0].setVisibility(View.INVISIBLE);
            }
            String name = item.toString();
            if (name.equals("Resources")) {
                RelativeLayout resourceSelector = (RelativeLayout) findViewById(R.id.resourceSelector);
                ScrollView resourceViewer = (ScrollView) findViewById(R.id.resourceViewer);
                resourceSelector.setVisibility(View.VISIBLE);
                resourceViewer.setVisibility(View.INVISIBLE);
            }
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
            } else if (name.equals("Saved")) {
                System.out.println("saved menu");
                RelativeLayout relativeLayout = findViewById(R.id.SavedLayout);
                relativeLayout.removeAllViews();

                try {
                    ArrayList<String>[] savedLocations = LocalSave.loadSavedLocations(MapsActivity.this);
                    if (savedLocations != null) {
                        for (int i = 0; i < savedLocations[0].size(); i++) {
                            LinearLayout newLL = new LinearLayout(this);
                            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            llp.setMargins(0,i*150,0,0);
                            newLL.setLayoutParams(llp);
                            newLL.setOrientation(LinearLayout.HORIZONTAL);
                            Button button = new Button(this);
                            Button closeSaveButton = new Button(this);
                            closeSaveButton.setText("X");
                            newLL.addView(button);
                            newLL.addView(closeSaveButton);
                            String buttonText = savedLocations[0].get(i);
                            if (buttonText.length() > 41) {
                                buttonText = buttonText.substring(0,41);
                            }
                            button.setText(buttonText);
                            int finalI = i;
                            closeSaveButton.setOnClickListener(view -> {
                                ArrayList<String>[] previousSave = new ArrayList[0];
                                try {
                                    previousSave = LocalSave.loadSavedLocations(MapsActivity.this);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                ArrayList<String> previousNames = previousSave[0];
                                ArrayList<String> previousAddresses = previousSave[1];
                                relativeLayout.removeViews(previousNames.indexOf(savedLocations[0].get(finalI)),1);
                                previousAddresses.remove(previousNames.indexOf(savedLocations[0].get(finalI)));
                                previousNames.remove(previousNames.indexOf(savedLocations[0].get(finalI)));
                                LocalSave.saveSavedLocations(previousNames, previousAddresses, MapsActivity.this);
                            });
                            button.setOnClickListener(view -> {
                                String coordinates = button.getText().toString().split(": ")[1];
                                double lat = Double.parseDouble(coordinates.split(", ")[0]);
                                double lng = Double.parseDouble(coordinates.split(", ")[1]);
                                System.out.println("clicked on button with coords: " + lat + ", " + lng);
                                // TODO search functionality here
                                RelativeLayout defaultSearchView = (RelativeLayout) findViewById(R.id.defaultSearchLayout);
                                defaultSearchView.setVisibility(View.INVISIBLE);
                                RelativeLayout newSearchView = (RelativeLayout) findViewById(R.id.newSearchLayout);
                                newSearchView.setVisibility(View.VISIBLE);
                                CharSequence fromText = "CURRENT LOCATION";
                                CharSequence toText = savedLocations[0].get(finalI);
                                fromView.setQuery(fromText, false);
                                toView.setQuery(toText, false);
                                if (checkLocationPermissions()) {
                                    fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Location> task) {
                                            System.out.println("FJIEOOIJEWFJIOFEWJIOIOP");
                                            System.out.println(task.getResult());
                                            try {
                                                System.out.println("CHANGE CURRENT START");
                                                System.out.println("CHANGE4");
                                                currentStartingPoint[0] = new LatLng(task.getResult().getLatitude(), task.getResult().getLongitude());
                                            } catch (NullPointerException e) {
                                                System.out.println(e);
                                                LocalSave.makeSnackBar("Unable to find location of user", getWindow().getDecorView().getRootView());
                                            }
                                        }
                                        //override methods
                                    });
                                }
                                else {
                                    System.out.println("CHANGE5");
                                    currentStartingPoint[0] = new LatLng(47.606470, -122.334289);
                                }
                                currentDestination[0] = new LatLng(lat,lng);
                            });
                            relativeLayout.addView(newLL);
                        }
                    }
                } catch (JSONException | IndexOutOfBoundsException e) {
                    e.printStackTrace();
                    System.out.println("saved menu failure");
                }
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
                    ArrayList<String> previousNames = previousSave[0];
                    ArrayList<String> previousAddresses = previousSave[1];
                    SearchView toView = (SearchView) findViewById(R.id.searchView3);

                    String query = toView.getQuery().toString();

                    Map<String, Object> map = (HashMap) CoordinateHelper.textToCoordinatesAndAddress(query)[0];
                    String lat = String.valueOf(map.get("latitude"));
                    String lng = String.valueOf(map.get("longitude"));
                    String latlng = lat + ", " + lng;

                    if (!previousAddresses.contains(latlng)) {

                        previousNames.add(query);
                        previousAddresses.add(latlng);

                        System.out.println("query: " + query);
                        System.out.println("coords: " + latlng);

                        LocalSave.saveSavedLocations(previousNames, previousAddresses, MapsActivity.this);
                        System.out.println("saved locations on top of previous ones");

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

                    System.out.println("query: " + query);
                    System.out.println("coords: " + latlng);

                    ArrayList<String> names = new ArrayList<>();
                    names.add(query);
                    ArrayList<String> addresses = new ArrayList<>();
                    addresses.add(latlng);
                    LocalSave.saveSavedLocations(names, addresses, MapsActivity.this);
                    System.out.println("saved locations fresh");
                }
            }
        });

        Button resourceFood = (Button) findViewById(R.id.resourceFood);
        resourceFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Object[] places = CoordinateHelper.textToCoordinatesAndAddress("food bank");
                int oi = 0;
                LinearLayout resourceViewerLayout = (LinearLayout) findViewById(R.id.resourceViewerLayout);
                resourceViewerLayout.removeAllViews();
                for (Object o : places) {
                    oi++;
                    Map<String, Object> map = (Map<String, Object>) o;
                    Button currentB = new Button(getApplicationContext());
                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    currentB.setLayoutParams(llp);
                    currentB.setText(map.get("name").toString());






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
                                        System.out.println("FJIEOOIJEWFJIOFEWJIOIOP");
                                        System.out.println(task.getResult());
                                        try {
                                            System.out.println("CHANGE CURRENT START");
                                            System.out.println("CHANGE4");
                                            currentStartingPoint[0] = new LatLng(task.getResult().getLatitude(), task.getResult().getLongitude());
                                        } catch (NullPointerException e) {
                                            System.out.println(e);
                                            LocalSave.makeSnackBar("Unable to find location of user", getWindow().getDecorView().getRootView());
                                        }
                                    }
                                    //override methods
                                });
                            }
                            else {
                                System.out.println("CHANGE5");
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

        Button resourceShelter = (Button) findViewById(R.id.resourceShelter);
        resourceShelter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Object[] places = CoordinateHelper.textToCoordinatesAndAddress("shelter");
                int oi = 0;
                LinearLayout resourceViewerLayout = (LinearLayout) findViewById(R.id.resourceViewerLayout);
                resourceViewerLayout.removeAllViews();
                for (Object o : places) {
                    oi++;
                    Map<String, Object> map = (Map<String, Object>) o;
                    Button currentB = new Button(getApplicationContext());
                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    currentB.setLayoutParams(llp);
                    currentB.setText(map.get("name").toString());
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
                                        System.out.println("FJIEOOIJEWFJIOFEWJIOIOP");
                                        System.out.println(task.getResult());
                                        try {
                                            System.out.println("CHANGE CURRENT START");
                                            System.out.println("CHANGE4");
                                            currentStartingPoint[0] = new LatLng(task.getResult().getLatitude(), task.getResult().getLongitude());
                                        } catch (NullPointerException e) {
                                            System.out.println(e);
                                            LocalSave.makeSnackBar("Unable to find location of user", getWindow().getDecorView().getRootView());
                                        }
                                    }
                                    //override methods
                                });
                            }
                            else {
                                System.out.println("CHANGE5");
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

        Button resourcePolice = (Button) findViewById(R.id.resourcePolice);
        resourcePolice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Object[] places = CoordinateHelper.textToCoordinatesAndAddress("police station");
                int oi = 0;
                LinearLayout resourceViewerLayout = (LinearLayout) findViewById(R.id.resourceViewerLayout);
                resourceViewerLayout.removeAllViews();
                for (Object o : places) {
                    oi++;
                    Map<String, Object> map = (Map<String, Object>) o;
                    Button currentB = new Button(getApplicationContext());
                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    currentB.setLayoutParams(llp);
                    currentB.setText(map.get("name").toString());
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
                                        System.out.println("FJIEOOIJEWFJIOFEWJIOIOP");
                                        System.out.println(task.getResult());
                                        try {
                                            System.out.println("CHANGE CURRENT START");
                                            System.out.println("CHANGE4");
                                            currentStartingPoint[0] = new LatLng(task.getResult().getLatitude(), task.getResult().getLongitude());
                                        } catch (NullPointerException e) {
                                            System.out.println(e);
                                            LocalSave.makeSnackBar("Unable to find location of user", getWindow().getDecorView().getRootView());
                                        }
                                    }
                                    //override methods
                                });
                            }
                            else {
                                System.out.println("CHANGE5");
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

        Button submitDirections = (Button) findViewById(R.id.submitDirections);
        Routing finalR = r;
        submitDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDirections.setVisibility(View.VISIBLE);
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
                RelativeLayout directionLayout = (RelativeLayout) findViewById(R.id.directionLayout);
                directionLayout.removeViews(1,directionLayout.getChildCount()-1);
                for (int i = 0; i < route.size(); i++) {
                    TextView tv = new TextView(getApplicationContext());
                    RelativeLayout.LayoutParams llp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    llp.setMargins(0,(i+1)*80,0,0);
                    tv.setLayoutParams(llp);
                    System.out.println(Arrays.toString(route.get(i)));
                    JSONObject currentStop = (JSONObject) stops.get(route.get(i)[0].toString());
                    if (i == 0) {
                        try {
                            tv.setText("START AT " + ch.getStopAddr(route.get(i)[0].toString()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        createMapMarker(Double.parseDouble(currentStop.get("latitude").toString()), Double.parseDouble(currentStop.get("longitude").toString()), "Stop " + (i+1), "#f91504");
                    }
                    else {
                        try {
                            tv.setText("TAKE ROUTE " + ch.getRouteNum(route.get(i)[1].toString()) + " TO " + ch.getStopAddr(route.get(i)[0].toString()));
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
                saveDestination.setVisibility(View.INVISIBLE);
            }
        });

        fromView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                System.out.println("CCCCC");
                showDirections.setVisibility(View.INVISIBLE);
                submitDirections.setVisibility(View.INVISIBLE);
                saveDestination.setVisibility(View.INVISIBLE);
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
                System.out.println("S1");
                System.out.println(currentStartingPoint[0]);
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

        fromView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                System.out.println(Arrays.toString(CoordinateHelper.textToCoordinatesAndAddress(query)));
                if (CoordinateHelper.textToCoordinatesAndAddress(query) != null) {
                    System.out.println("CHANGE1");
                    Map<String, Object> map = (HashMap) CoordinateHelper.textToCoordinatesAndAddress(query)[0];
                    currentStartingPoint[0] = new LatLng((Double) map.get("latitude"), (Double) map.get("longitude"));
                    mMap.clear();
                    System.out.println("S2");
                    createMapMarker((Double) map.get("latitude"), (Double) map.get("longitude"), "Start", "#f91104");
                }
                else if (query.isEmpty()) {
                    if (checkLocationPermissions()) {
                        fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                System.out.println("LOCATIONTNION");
                                System.out.println("CHANGE2");
                                currentStartingPoint[0] = new LatLng(task.getResult().getLatitude(), task.getResult().getLongitude());
                            }
                        });
                    }
                    else {
                        System.out.println("CHANGE3");
                        currentStartingPoint[0] = new LatLng(47.606470, -122.334289);
                    }
                    mMap.clear();
                    System.out.println("S3");
                    createMapMarker(currentStartingPoint[0].latitude, currentStartingPoint[0].longitude, "Start", "#f91104");
                }
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
                    fromView.setQuery(fromText, false);
                    toView.setQuery(toText, false);
                    Map<String, Object> map = (HashMap) CoordinateHelper.textToCoordinatesAndAddress(query)[0];
                    if (checkLocationPermissions()) {
                        fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                System.out.println("FJIEOOIJEWFJIOFEWJIOIOP");
                                System.out.println(task.getResult());
                                try {
                                    System.out.println("CHANGE CURRENT START");
                                    System.out.println("CHANGE4");
                                    currentStartingPoint[0] = new LatLng(task.getResult().getLatitude(), task.getResult().getLongitude());
                                } catch (NullPointerException e) {
                                    System.out.println(e);
                                    LocalSave.makeSnackBar("Unable to find location of user", getWindow().getDecorView().getRootView());
                                }
                            }
                            //override methods
                        });
                    }
                    else {
                        System.out.println("CHANGE5");
                        currentStartingPoint[0] = new LatLng(47.606470, -122.334289);
                    }
                    currentDestination[0] = new LatLng((Double) map.get("latitude"), (Double) map.get("longitude"));
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