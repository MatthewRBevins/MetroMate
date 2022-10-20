package com.example.busapp;

import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.JsonReader;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SearchView;

import com.google.android.gms.dynamic.IObjectWrapper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.busapp.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.Random;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static Context context;
    public MapsActivity(Context context) throws IOException, ParseException {
        this.context=context;
    }

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    FusedLocationProviderClient mFusedLocationClient;
    final int PERMISSION_ID = 44;
    private Routing routing = new Routing(context);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @SuppressLint("MissingPermission")
    public boolean checkLocationAvailable() {
        // check if permissions are given
        if (checkPermissions()) {

            // check if location is enabled
            if (isLocationEnabled()) {

                return true;
            }
            else {
                Toast.makeText(this, "Please turn on" + " your location...", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            // if permissions aren't available,
            // request for permissions
            requestPermissions();
        }
        return false;
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {

        // Initializing LocationRequest
        // object with appropriate methods
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        // setting LocationRequest
        // on FusedLocationClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
        }
    };

    // method to check for permissions
    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;// && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        // If we want background location
        // on Android 10.0 and higher,
        // use:
        // ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // method to request for permissions
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    // method to check
    // if location is enabled
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    // If everything is alright then
    @Override
    public void
    onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationAvailable();
            }
        }
    }

    public void createMapMarker(Double latitude, Double longitude, String title) {
        LatLng pos = new LatLng(latitude, longitude);
        MarkerOptions marker = new MarkerOptions();
        marker.position(pos);
        //BusMarker.icon(BitmapDescriptorFactory.defaultMarker(new Random().nextInt(360)));
        marker.title(title);
        mMap.addMarker(marker);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
    }
    public void showBusLocations() {
        try {
            String data = Web.readFromWeb("https://s3.amazonaws.com/kcm-alerts-realtime-prod/vehiclepositions_pb.json");
            JSONObject o = Web.readJSON(new StringReader(data));
            JSONArray a = (JSONArray) o.get("entity");
            Iterator<JSONObject> iterator = a.iterator();
            while (iterator.hasNext()) {
                JSONObject jj = iterator.next();
                JSONObject vehicle = (JSONObject) jj.get("vehicle");
                JSONObject pos = (JSONObject) vehicle.get("position");
                JSONObject trip = (JSONObject) vehicle.get("trip");
                if (pos.get("latitude") != null) {
                    createMapMarker((double) pos.get("latitude"), (double) pos.get("longitude"), "Bus");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    public String getRouteID(String routeNum) throws IOException, ParseException {
        JSONObject r = Web.readJSON(new InputStreamReader(getAssets().open("routes.json")));
        Object[] keys = r.keySet().toArray();
        for (Object i : keys) {
            JSONObject ii = (JSONObject) r.get(i.toString());
            if (ii.get("short_name").toString().equals(routeNum)) {
                return i.toString();
            }
        }
        return null;
    }
    public void showRouteMap(String routeID) throws IOException, ParseException {
        JSONObject r = Web.readJSON(new InputStreamReader(getAssets().open("routes.json")));
        JSONObject item = (JSONObject) r.get(routeID);
        JSONArray shapeIDss = (JSONArray) item.get("shape_ids");
        Object[] shapeIDs = shapeIDss.toArray();
        for (Object ii : shapeIDs) {
            JSONObject o = Web.readJSON(new InputStreamReader(getAssets().open("shapes.json")));
            JSONArray locations = (JSONArray) o.get(ii.toString());
            Iterator<JSONObject> i = locations.iterator();
            PolylineOptions polyline = new PolylineOptions();
            int index = 0;
            String[] hi = new String[1000];
            String[] hi2 = new String[1000];
            while (i.hasNext()) {
                JSONObject currentObject = i.next();
                hi[index] = (String) currentObject.get("latitude");
                hi2[index] = (String) currentObject.get("longitude");
                LatLng hii = new LatLng(Double.valueOf((String) currentObject.get("latitude")), Double.valueOf((String) currentObject.get("longitude")));
                polyline.add(hii);
                index++;
            }
            mMap.addPolyline(polyline);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {


        BottomNavigationView mBottomNavigationView=(BottomNavigationView)findViewById(R.id.nav_view);
        mBottomNavigationView.getMenu().setGroupCheckable(0,false,true);
        mBottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                String stuff = item.toString() + "Menu";
                mBottomNavigationView.getMenu().setGroupCheckable(0,false,true);
                LinearLayout menuToShow = (LinearLayout) findViewById(getResources().getIdentifier(stuff, "id", getPackageName()));
                menuToShow.setVisibility(View.VISIBLE);
                item.setCheckable(true);
                item.setChecked(true);
                return false;
            }
        });

        LinearLayout[] EmptyTouch = new LinearLayout[]{(LinearLayout) findViewById(R.id.BusesEmptyTouch), (LinearLayout) findViewById(R.id.RoutesEmptyTouch), (LinearLayout) findViewById(R.id.ResourcesEmptyTouch), (LinearLayout) findViewById(R.id.SavedEmptyTouch), (LinearLayout) findViewById(R.id.SettingsEmptyTouch)};
        for (LinearLayout i : EmptyTouch) {
            i.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    System.out.println("CLOSE THE FUKC ");
                }
            });
        }




        mMap = googleMap;



        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(47.6122709,-122.3471455), 12));
        //showBusLocations();
        Button b = (Button) findViewById(R.id.zoomout);
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mMap.moveCamera(CameraUpdateFactory.zoomOut());
            }
        });
                /*mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (!Object.equals(location.getLatitude(),null)) {
                            createMapMarker(location.getLatitude(), location.getLongitude(), "Your Location");
                        }
                    }
                });*/
        SearchView v = (SearchView) findViewById(R.id.searchView);
        v.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (CoordinateHelper.textToCoordinatesAndAddress(query)[0] != null) {
                    Map<String, Object> map = (HashMap) CoordinateHelper.textToCoordinatesAndAddress(query)[0];
                    createMapMarker((Double) map.get("latitude"), (Double) map.get("longitude"), "Selected Location");

                    // uses [[distance, stop_id to go to, trip_id to get there], [""]]
                    String startStop = "260";
                    String endStop = "260";
                    LatLng endPos = new LatLng(47.481230,-122.216501);

                    ArrayList<Object[]> route = routing.findRoute(startStop, endPos);
                    for (int i = 0; i < route.size(); i++) {
                        Object[] data = route.get(i);
                        if (i == 0) {
                            // first adventure
                            routing.drawStopToStop(startStop, data[1], )
                        } else {
                            // last adventure
                        }
                    }

                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }
}