package com.example.busapp;
import static android.content.Context.MODE_PRIVATE;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;

public class LocalSave {
    static Context context;
    public LocalSave(Context c) {
        this.context = c;
    }
    public static void saveBoolean(String key, boolean bool) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPreferences", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putBoolean(key, bool);
        myEdit.commit();
    }

    // {name, latitude, longitude}

    public void saveSavedLocations(ArrayList<String[]> list) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPreferences", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        JSONArray data = new JSONArray(list);
        String dataString = data.toString();
        myEdit.putString("savedLocations", dataString);
        myEdit.apply();
    }

    public static ArrayList<String[]> loadSavedLocations() throws JSONException {
        SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPreferences", MODE_PRIVATE);
        String dataString = sharedPreferences.getString("savedLocations", null);
        JSONArray data = new JSONArray(dataString);
        ArrayList<String[]> list = new ArrayList<>();
        for (int i = 0; i < data.length(); i++){
            ArrayList location = (ArrayList) data.get(i);
            String[] stringLocation = {(String) location.get(0), (String) location.get(1), (String) location.get(2)};
            list.add(stringLocation);
        }
        return list;
    }

}
