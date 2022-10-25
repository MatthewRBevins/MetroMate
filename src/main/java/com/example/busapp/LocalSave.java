package com.example.busapp;
import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;

public class LocalSave {

    public static void saveBoolean(String key, boolean bool, Context context) {
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPreferences", MODE_PRIVATE);
            SharedPreferences.Editor myEdit = sharedPreferences.edit();
            myEdit.putBoolean(key, bool);
            myEdit.commit();
        } catch (NullPointerException e) {
            System.out.println(e);
        }
    }

    // {name, latitude, longitude}

    public static void saveSavedLocations(ArrayList<String[]> list, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPreferences", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        JSONArray data = new JSONArray(list);
        String dataString = data.toString();
        myEdit.putString("savedLocations", dataString);
        myEdit.apply();
    }

    public static ArrayList<String[]> loadSavedLocations(Context context) throws JSONException {
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPreferences", MODE_PRIVATE);
            String dataString = sharedPreferences.getString("savedLocations", null);
            JSONArray data = new JSONArray(dataString);
            ArrayList<String[]> list = new ArrayList<>();
            for (int i = 0; i < data.length(); i++){
                JSONArray locJSON = (JSONArray) data.get(i);
                ArrayList location = new ArrayList();
                if (locJSON != null) {
                    for (int j = 0; j < locJSON.length(); j++){
                        location.add(locJSON.getString(i));
                    }
                }
                String[] stringLocation = {(String) location.get(0), (String) location.get(1), (String) location.get(2)};
                list.add(stringLocation);
            }
            return list;
        } catch (NullPointerException e) { return null; }
    }

    public static void makeSnackBar(String text, View anyView) {
        View view = anyView.getRootView();
        Snackbar mySnackbar = Snackbar.make(view, text, 1500);
        mySnackbar.show();
    }
}
