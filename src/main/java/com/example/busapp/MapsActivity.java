package com.example.busapp;

import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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

    public MarkerOptions createMapMarker(Double latitude, Double longitude, String title) {
        LatLng pos = new LatLng(latitude, longitude);
        MarkerOptions marker = new MarkerOptions();
        marker.position(pos);
        //BusMarker.icon(BitmapDescriptorFactory.defaultMarker(new Random().nextInt(360)));
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
                            try {
                                for (int i = 0; i < positions.size(); i++) {
                                    LatLng pos = positions.get(i);
                                    createMapMarker(pos.latitude, pos.longitude, "");
                                }
                            } catch (NullPointerException e) {
                                System.out.println("No running buses with id " + query);
                            }
                        } else if (name.equals("Routes")) {
                            try {
                                JSONObject obj = Web.readJSON(new InputStreamReader(getAssets().open("displayNameToRouteID.json")));
                                String routeID = (String) obj.get(query);
                                showRouteMap(routeID);
                            } catch (IOException e) {
                            } catch (ParseException e) {
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
            return false;
        });

        //BUTTON EVENTS
        Button zoomOutButton = (Button) findViewById(R.id.zoomout);
        zoomOutButton.setOnClickListener(v -> mMap.moveCamera(CameraUpdateFactory.zoomOut()));

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

    public void settingsButtonClicked(View view) {
        ToggleButton button = (ToggleButton) view.findViewById(view.getId());
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPreferences", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        boolean isChecked = button.isChecked();
        myEdit.putBoolean(String.valueOf(view.getId()), isChecked);
        myEdit.commit();
    }
}