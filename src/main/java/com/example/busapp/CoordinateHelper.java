package com.example.busapp;

import android.content.Context;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class CoordinateHelper {

    private static Context context;
    public CoordinateHelper(Context context){
        this.context=context;
    }

    public static double distance(double lat1, double lng1, double lat2, double lng2) {
        double x = Math.pow(lat2 - lat1, 2);
        double y = Math.pow(lng2 - lng1, 2);
        return Math.sqrt(x + y);
    }

    public String getRouteID(String routeNum) throws IOException, ParseException {
        JSONObject r = Web.readJSON(new InputStreamReader(context.getAssets().open("routes.json")));
        Object[] keys = r.keySet().toArray();
        for (Object i : keys) {
            JSONObject ii = (JSONObject) r.get(i.toString());
            if (ii.get("short_name").toString().equals(routeNum)) {
                return i.toString();
            }
        }
        return null;
    }
    public String getStopAddr(String stopID) throws IOException, ParseException {
        JSONObject s = Web.readJSON(new InputStreamReader(context.getAssets().open("newStops.json")));
        JSONObject ss = (JSONObject) s.get(stopID);
        if (ss == null) return null;
        return ss.get("stop_name").toString();
    }
    public String getRouteNum(String routeID) throws IOException, ParseException {
        JSONObject r = Web.readJSON(new InputStreamReader(context.getAssets().open("routes.json")));
        JSONObject rr = (JSONObject) r.get(routeID);
        if (rr == null) return null;
        return rr.get("short_name").toString();
    }
    public static Object[] textToCoordinatesAndAddress(String text) {
        String textFormatted = text.replace(" ","+");
        String URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=47.571811,-122.141054" +
                "&radius=50000" +
                "&keyword=" + textFormatted +
                "&key=AIzaSyBqVEGyYI0c1i49h9bv8LT7riQ1dg6vCNE";
        System.out.println(URL);
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
                    map.put("longitude", (double) location.get("lng"));
                    map.put("name", (String) results.get("name"));
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

    public static Object[] coordinatesToAddress(double latitude, double longitude, int resultCount) {
        String latlng = String.valueOf(latitude) + ", " + String.valueOf(longitude);
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

    public String findNearestBusStop(double currentLat, double currentLng) {
        try {
            JSONObject json = Web.readJSON(new InputStreamReader(context.getAssets().open("stops.json")));
            double distance = Double.POSITIVE_INFINITY;
            String stopID = "";
            for (Object key: json.keySet()) {
                Map<String, Object> data = (Map<String, Object>) json.get(key);
                double lat = Double.valueOf((String) data.get("latitude"));
                double lng = Double.valueOf((String) data.get("longitude"));
                double d = distance(lat, lng, currentLat, currentLng);
                if (d < distance) {
                    distance = d;
                    stopID = (String) key;
                }
            }
            return stopID;
        } catch (IOException | ParseException e) {
            return null;
        }
    }
}
