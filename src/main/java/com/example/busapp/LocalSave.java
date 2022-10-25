package com.example.busapp;
import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;

public class LocalSave {

    private static Context context;
    public LocalSave(){
        this.context=context;
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
        try {
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
        } catch (NullPointerException e) {
            return null;
        }
    }

    public void makeSnackBar(String text) {
        View view = view.findViewById().getRootView();
        //View view = ((Activity) context).findViewById(R.id.coordinatorLayout);
        Snackbar mySnackbar = Snackbar.make(view, text, 1500);
        mySnackbar.show();
    }
}
