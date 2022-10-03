package com.example.busapp;

import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.JsonReader;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
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
    public void showRouteMap(String shapeID) throws IOException, ParseException {
        JSONObject o = Web.readJSON(new InputStreamReader(getAssets().open("shapes.json")));
        JSONArray locations = (JSONArray) o.get(shapeID);
        Iterator<JSONObject> i = locations.iterator();
        PolylineOptions polyline = new PolylineOptions();
        int index = 0;
        String[] hi = new String[1000];
        String[] hi2 = new String[1000];
        while (i.hasNext()) {
            JSONObject currentObject = i.next();
            hi[index] = (String) currentObject.get("latitude");
            hi2[index] = (String) currentObject.get("longitude");
            index++;
        }

        System.out.println(Arrays.toString(hi));
        System.out.println(Arrays.toString(hi2));
        mMap.addPolyline(polyline);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            showRouteMap("10002005");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //showBusLocations();
        Button b = (Button) findViewById(R.id.button1);
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                mMap.moveCamera(CameraUpdateFactory.zoomOut());
            }
        });
        SearchView v = (SearchView) findViewById(R.id.searchView);
        v.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (CoordinateHelper.textToCoordinatesAndAddress(query)[0] != null) {
                    createMapMarker((Double) CoordinateHelper.textToCoordinatesAndAddress(query)[0], (Double) CoordinateHelper.textToCoordinatesAndAddress(query)[1], "Selected Location");
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