package com.example.busapp;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.os.StrictMode;
import android.util.JsonReader;
import android.view.View;
import android.widget.Button;

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

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    public void jsonToObj(String json) {
        String[] l = json.split(Pattern.quote("{"));
        System.out.println(Arrays.toString(l));
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
                    LatLng bus = new LatLng((double) pos.get("latitude"), (double) pos.get("longitude"));
                    MarkerOptions BusMarker = new MarkerOptions();
                    BusMarker.position(bus);
                    Random r = new Random();
                    BusMarker.icon(BitmapDescriptorFactory.defaultMarker(new Random().nextInt(360)));
                    BusMarker.title("Route " + trip.get("route_id"));
                    mMap.addMarker(BusMarker);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //showBusLocations();
        Button b = (Button) findViewById(R.id.button1);
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                System.out.println("*********************************************************************");
                //showBusLocations();
                for (String i : Crap.stopsKeys) {
                    try {
                        JSONObject o = Web.readJSON(new InputStreamReader(getAssets().open("stops.json")));
                        JSONObject os = (JSONObject) o.get(i);
                        MarkerOptions stopMarker = new MarkerOptions();
                        stopMarker.position(new LatLng(Double.valueOf(os.get("latitude").toString()), Double.valueOf(os.get("longitude").toString())));
                        stopMarker.title("BUS STOP");
                        mMap.addMarker(stopMarker);
                    } catch (ParseException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(47.7318, -122.3274);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}