package com.example.busapp;

import androidx.fragment.app.FragmentActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.busapp.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Iterator;

import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        //IINITIALIZE GOOGLE MAP
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(47.6122709,-122.3471455), 12));

        //BOTTOM NAVIGATION VIEW STYLES
        BottomNavigationView mBottomNavigationView=(BottomNavigationView)findViewById(R.id.nav_view);
        mBottomNavigationView.getMenu().setGroupCheckable(0,false,true);
        mBottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                String stuff = item.toString() + "Menu";
                LinearLayout menuToShow = (LinearLayout) findViewById(getResources().getIdentifier(stuff, "id", getPackageName()));
                menuToShow.setVisibility(View.VISIBLE);
                item.setCheckable(true);
                item.setChecked(true);
                return false;
            }
        });
        LinearLayout desperado = (LinearLayout) findViewById(R.id.desperado);
        desperado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("CLOSE THE FUKC ");
            }
        });

        //BUTTON EVENTS
        Button zoomOutButton = (Button) findViewById(R.id.zoomout);
        zoomOutButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mMap.moveCamera(CameraUpdateFactory.zoomOut());
            }
        });

        //TEXT BOX EVENTS
        SearchView locationSearch = (SearchView) findViewById(R.id.searchView);
        locationSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (Objects.requireNonNull(CoordinateHelper.textToCoordinatesAndAddress(query))[0] != null) {
                    Map<String, Object> map = (HashMap) CoordinateHelper.textToCoordinatesAndAddress(query)[0];
                    createMapMarker((Double) map.get("latitude"), (Double) map.get("longitude"), "Selected Location");
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