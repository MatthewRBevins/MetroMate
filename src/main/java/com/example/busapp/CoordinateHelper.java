package com.example.busapp;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import java.io.IOException;
import java.io.StringReader;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

public class CoordinateHelper {
    public static Object[] textToCoordinatesAndAddress(String text) {
        String addressURLFormatted = text.replace(" ","+");
        String URL = "https://maps.googleapis.com/maps/api/geocode/json?address="
                + addressURLFormatted
                + "&key=" + BuildConfig.MAPS_API_KEY;
        try {
            String data = Web.readFromWeb(URL);
            JSONObject json = Web.readJSON(new StringReader(data));
            JSONArray resultsArr = (JSONArray) json.get("results");
            if (! resultsArr.isEmpty()) {
                Map<String, Object>[] arr = new HashMap[resultsArr.size()];
                for (int i = 0; i < resultsArr.size(); i++) {
                    JSONObject results = (JSONObject) resultsArr.get(i);
                    JSONObject geometry = (JSONObject) results.get("geometry");
                    JSONObject location = (JSONObject) geometry.get("location");
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("latitude", (double) location.get("lat"));
                    map.put("longitude", (double) location.get("longitude"));
                    map.put("formatted_address", (String) results.get("formatted_address"))
                    arr[i] = map;
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
