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

    public static void saveString(String key, String val, Context context) {
        try {
            //Open local storage
            SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPreferences", MODE_PRIVATE);
            SharedPreferences.Editor myEdit = sharedPreferences.edit();
            //Add boolean to local storage
            myEdit.putString(key, val);
            myEdit.commit();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save boolean to local storage
     * @param key Key to save boolean to
     * @param bool Boolean to save
     * @param context Application context
     */
    public static void saveBoolean(String key, boolean bool, Context context) {
        try {
            //Open local storage
            SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPreferences", MODE_PRIVATE);
            SharedPreferences.Editor myEdit = sharedPreferences.edit();
            //Add boolean to local storage
            myEdit.putBoolean(key, bool);
            myEdit.commit();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save locations for use in saved locations menu
     * @param names Names of saved locations
     * @param addresses Addresses of saved locations
     * @param context Application context
     */
    public static void saveSavedLocations(ArrayList<String> names, ArrayList<String> addresses, Context context) {
        //Open local storage
        SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPreferences", MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();

        //Add names data to savedNames key
        JSONArray namesData = new JSONArray(names);
        String namesString = namesData.toString();
        edit.putString("savedNames", namesString);

        //Add addresses data to savedAddresses key
        JSONArray addressesData = new JSONArray(addresses);
        String addressesString = addressesData.toString();
        edit.putString("savedAddresses", addressesString);

        edit.apply();
    }

    /**
     * Gets saved locations
     * @param context Application context
     * @return ArrayList of all saved locations
     * @throws JSONException Reading JSON
     */
    public static ArrayList<String>[] loadSavedLocations(Context context) throws JSONException {
        try {
            //Open local storage
            SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPreferences", MODE_PRIVATE);

            //Add all names to list
            String namesString = sharedPreferences.getString("savedNames", null);
            JSONArray namesData = new JSONArray(namesString);
            ArrayList<String> namesList = new ArrayList<>();
            for (int i = 0; i < namesData.length(); i++){
                namesList.add((String) namesData.get(i));
            }

            //Add all adresses to list
            String addressesString = sharedPreferences.getString("savedAddresses", null);
            JSONArray addressesData = new JSONArray(addressesString);
            ArrayList<String> addressesList = new ArrayList<>();
            for (int i = 0; i < addressesData.length(); i++) {
                if (addressesData.get(i).equals(null)) {
                    return null;
                }
                addressesList.add((String) addressesData.get(i));
            }

            //Return names nad addresses list
            if (namesList.size() > 0) {
                return new ArrayList[] {namesList, addressesList};
            } else {
                return null;
            }
        } catch (NullPointerException e) { return null; }
    }

    /**
     * Creates snackbar message on bottom of screen
     * @param text Snackbar text
     * @param anyView Application view
     */
    public static void makeSnackBar(String text, View anyView) {
        View view = anyView.getRootView();
        Snackbar mySnackbar = Snackbar.make(view, text, 1500);
        mySnackbar.show();
    }
}
