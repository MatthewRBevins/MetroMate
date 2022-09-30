package com.example.busapp;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;

public class CoordinateHelper {
    public static Object[] textToCoordinatesAndAddress(String address) {
        String addressURLFormatted = address.replace(" ","+");
        String URL = "https://maps.googleapis.com/maps/api/geocode/json?address="
                + addressURLFormatted
                + "&key=" + BuildConfig.MAPS_API_KEY;
        Object[] arr = new Object[3];
        try {
            String data = Web.readFromWeb(URL);
            JSONObject json = Web.readJSON(new StringReader(data));
            JSONArray resultsArr = (JSONArray) json.get("results");
            if (! resultsArr.isEmpty()) {
                JSONObject results = (JSONObject) resultsArr.get(0);
                JSONObject geometry = (JSONObject) results.get("geometry");
                JSONObject location = (JSONObject) geometry.get("location");
                arr[0] = (double) location.get("lat"); // latitude
                arr[1] = (double) location.get("lng"); // longitude
                arr[2] = (String) results.get("formatted_address"); // formatted address
                return arr;
            } else {
                return null;
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object[] coordinatesToAddress(double longitude, double latitude, int resultCount) {
        String latlng = String.valueOf(longitude) + ", " + String.valueOf(latitude);
        String latlngURLFormatted = latlng.replace(" ","+");
        String URL = "https://maps.googleapis.com/maps/api/geocode/json?latlng="
                + latlngURLFormatted
                + "&key=" + BuildConfig.MAPS_API_KEY;
        try {
            String data = Web.readFromWeb(URL);
            JSONObject json = Web.readJSON(new StringReader(data));
            JSONArray resultsArr = (JSONArray) json.get("results");
            if (! resultsArr.isEmpty()) {
                String[] arr = new String[resultsArr.size()];
                for (int i = 0; i < resultsArr.size(); i++) {
                    JSONObject results = (JSONObject) resultsArr.get(i);
                    arr[i] = (String) results.get("formatted_address");
                }
                return arr;
            } else {
                return null;
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
