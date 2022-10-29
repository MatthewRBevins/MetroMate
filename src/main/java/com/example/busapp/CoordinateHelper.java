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

    /**
     * Calculate the distance between two points
     * @param lat1 Latitude of first point
     * @param lng1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lng2 Longitude of second point
     * @return Distance between two points
     */
    public static double distance(double lat1, double lng1, double lat2, double lng2) {
        double x = Math.pow(lat2 - lat1, 2);
        double y = Math.pow(lng2 - lng1, 2);
        return Math.sqrt(x + y);
    }

    /**
     * Converts route short name to route ID
     * @param routeNum Bus route short name
     * @return Bus route ID
     * @throws IOException Opening JSON file
     * @throws ParseException Parsing JSON file
     */
    public String getRouteID(String routeNum) throws IOException, ParseException {
        JSONObject r = Web.readJSON(new InputStreamReader(context.getAssets().open("routes.json")));
        Object[] keys = r.keySet().toArray();
        //Loop through all routes
        for (Object i : keys) {
            JSONObject ii = (JSONObject) r.get(i.toString());
            //If the current route corresponds to the given route num, return route ID
            if (ii.get("short_name").toString().equals(routeNum)) {
                return i.toString();
            }
        }
        return null;
    }

    /**
     * Converts stop ID to stop address
     * @param stopID Stop ID
     * @return Stop address
     * @throws IOException Opening JSON file
     * @throws ParseException Parsing JSON file
     */
    public String getStopAddr(String stopID) throws IOException, ParseException {
        JSONObject s = Web.readJSON(new InputStreamReader(context.getAssets().open("newStops.json")));
        //Get information about given stop ID
        JSONObject ss = (JSONObject) s.get(stopID);
        if (ss == null) return null;
        //Return stop address
        return ss.get("stop_name").toString();
    }

    /**
     * Converts route ID to route short name
     * @param routeID Bus route ID
     * @return Bus route short name
     * @throws IOException Opening JSON file
     * @throws ParseException Parsing JSON file
     */
    public String getRouteNum(String routeID) throws IOException, ParseException {
        JSONObject r = Web.readJSON(new InputStreamReader(context.getAssets().open("routes.json")));
        //Get information about given route ID
        JSONObject rr = (JSONObject) r.get(routeID);
        if (rr == null) return null;
        //Return route short name
        return rr.get("short_name").toString();
    }

    /**
     * Searches for locations based on user query
     * @param text User query
     * @return Information about locations found
     */
    public static Object[] textToCoordinatesAndAddress(String text) {
        String textFormatted = text.replace(" ","+");
        //Make API call to find locations in the Seattle area
        String URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=47.571811,-122.141054" +
                "&radius=50000" +
                "&keyword=" + textFormatted +
                "&key=" + BuildConfig.PLACES_API_KEY;
        try {
            //Fetch data from URL
            String data = Web.readFromWeb(URL);
            JSONObject json = Web.readJSON(new StringReader(data));
            JSONArray resultsArr = (JSONArray) json.get("results");
            if (! resultsArr.isEmpty()) {
                Map<String, Object>[] arr = new HashMap[resultsArr.size()];
                //Loop through all places
                for (int i = 0; i < resultsArr.size(); i++) {
                    //Add place information to HashMap
                    JSONObject results = (JSONObject) resultsArr.get(i);
                    JSONObject geometry = (JSONObject) results.get("geometry");
                    JSONObject location = (JSONObject) geometry.get("location");
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("latitude", (double) location.get("lat"));
                    map.put("longitude", (double) location.get("lng"));
                    map.put("name", (String) results.get("name"));
                    arr[i] = map;
                }
                //Return information about places
                return arr;
            } else {
                return null;
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Finds closest bus stop to given location
     * @param currentLat Latitude of given location
     * @param currentLng Longitude of given location
     * @return Bus stop ID
     */
    public String findNearestBusStop(double currentLat, double currentLng) {
        try {
            JSONObject json = Web.readJSON(new InputStreamReader(context.getAssets().open("stops.json")));
            double distance = Double.POSITIVE_INFINITY;
            String stopID = "";
            //Loop through all stops
            for (Object key: json.keySet()) {
                //Check distance between current location and stop location
                Map<String, Object> data = (Map<String, Object>) json.get(key);
                double lat = Double.valueOf((String) data.get("latitude"));
                double lng = Double.valueOf((String) data.get("longitude"));
                double d = distance(lat, lng, currentLat, currentLng);
                //If stop is closer than previous best, set previous best to current stop
                if (d < distance) {
                    distance = d;
                    stopID = (String) key;
                }
            }
            //Return closest stop ID
            return stopID;
        } catch (IOException | ParseException e) {
            return null;
        }
    }
}
