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

    public static void saveSavedLocations(ArrayList<String> names, ArrayList<String> addresses, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPreferences", MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();

        JSONArray namesData = new JSONArray(names);
        String namesString = namesData.toString();
        System.out.println(namesString + " before");
        edit.putString("savedNames", namesString);

        JSONArray addressesData = new JSONArray(addresses);
        String addressesString = addressesData.toString();
        System.out.println(addressesString + " before");
        edit.putString("savedAddresses", addressesString);

        edit.apply();
    }
    // returns savedNames[], savedAddresses[]
    public static ArrayList<String>[] loadSavedLocations(Context context) throws JSONException {
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPreferences", MODE_PRIVATE);

            String namesString = sharedPreferences.getString("savedNames", null);
            JSONArray namesData = new JSONArray(namesString);
            ArrayList<String> namesList = new ArrayList<>();
            for (int i = 0; i < namesData.length(); i++){
                namesList.add((String) namesData.get(i));
            }

            String addressesString = sharedPreferences.getString("savedAddresses", null);
            JSONArray addressesData = new JSONArray(addressesString);
            ArrayList<String> addressesList = new ArrayList<>();
            for (int i = 0; i < addressesData.length(); i++){
                addressesList.add((String) addressesData.get(i));
            }

            return new ArrayList[] {namesList, addressesList};
        } catch (NullPointerException e) { return null; }
    }

    public static void makeSnackBar(String text, View anyView) {
        View view = anyView.getRootView();
        Snackbar mySnackbar = Snackbar.make(view, text, 1500);
        mySnackbar.show();
    }
}
