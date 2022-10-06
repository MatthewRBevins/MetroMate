package com.example.busapp;

import android.content.Context;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


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

    private static Object[] findCommonElements(String[] arr1, String[] arr2) {
        Set<String> set = new HashSet<>();
        for (int i = 0; i < arr1.length; i++) {
            for (int j = 0; j < arr2.length; j++) {
                if (arr1[i] == arr2[j]) {
                    set.add(arr1[i]);
                    break;
                }
            }
        }
        return set.toArray();
    }

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
                    map.put("formatted_address", (String) results.get("formatted_address"));
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

    public static String findNearestBusStop(double currentLat, double currentLng) {
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

    public static String[] findRoutesBetweenBusStops(String stopID, String stopID2) {
        try {
            JSONObject json = Web.readJSON(new InputStreamReader(context.getAssets().open("stops.json")));
            JSONObject firstStop = (JSONObject) json.get(stopID);
            JSONObject lastStop = (JSONObject) json.get(stopID2);

            JSONArray firstTripIDs = (JSONArray) firstStop.get("trip_ids");
            JSONArray lastTripIDs = (JSONArray) lastStop.get("trip_ids");

            String[] firstTripIDs2 = new String[firstTripIDs.size()];
            for(int i = 0; i < firstTripIDs.size(); i++){
                firstTripIDs2[i] = (String) firstTripIDs.get(i);
            }

            String[] lastTripIDs2 = new String[lastTripIDs.size()];
            for(int i = 0; i < lastTripIDs.size(); i++){
                lastTripIDs2[i] = (String) lastTripIDs.get(i);
            }

            String[] commonElements = (String[]) findCommonElements(firstTripIDs2, lastTripIDs2);
            System.out.println(commonElements);
        } catch (IOException | ParseException e) {
            return null;
        }
        return null;
    }
}
