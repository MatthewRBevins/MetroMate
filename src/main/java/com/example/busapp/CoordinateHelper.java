package com.example.busapp;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import java.io.IOException;
import java.io.StringReader;

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
                arr[2] = (String) results.get("formatted_Address"); // formatted address
                return arr;
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return arr;
    }
}
