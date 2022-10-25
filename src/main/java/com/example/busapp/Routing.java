package com.example.busapp;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

public class Routing {
    private JSONObject routesToStops;
    private JSONObject stops;
    private Context context;
    public Routing(Context c) throws IOException, ParseException {
        this.context = c;
        this.stops = readJSON("newStops.json");
        this.routesToStops = readJSON("routesToStops.json");
    }
    public ArrayList<Object[]> genRoute(LatLng endPos, String startStopStr) {
        JSONObject startStop = (JSONObject) stops.get(startStopStr);
        assert startStop != null;
        ArrayList<Object[]> route = findRoute(startStop, 200, true, startStopStr, endPos);
        String finalStop = route.get(route.size()-1)[0].toString();
        JSONObject finalStopObj = (JSONObject) stops.get(finalStop);
        LatLng finalStopLatLng = new LatLng(Double.parseDouble(finalStopObj.get("latitude").toString()), Double.parseDouble(finalStopObj.get("longitude").toString()));
        if (distance(finalStopLatLng, endPos) >= 0.01) {
            ArrayList<Object[]> route2 = findRoute(finalStopObj, distance(finalStopLatLng, endPos), false, finalStop, endPos);
            route.addAll(route2);
        }
        return route;
    }
    public ArrayList<Object[]> findRoute(JSONObject startStop, double absBestDis, boolean latDisMethod, String startStopID, LatLng endPos) {
        ArrayList<Object[]> bestTripPlan = new ArrayList();
        JSONArray jA = (JSONArray) startStop.get("route_ids");
        String absBestStop = "";
        for (Object j : jA) {
            if (j != null) {
                JSONObject toStopsI = (JSONObject) routesToStops.get(j);
                Object[] toStops = toStopsI.keySet().toArray();
                for (Object i : toStops) {
                    JSONObject currentStop = (JSONObject) stops.get(i);
                    String currentStopId = i.toString();
                    String prevStop = "";
                    ArrayList<Object[]> tripPlan = new ArrayList();
                    if (latDisMethod) {
                        tripPlan.add(new Object[]{startStopID});
                    }
                    tripPlan.add(new Object[]{i.toString(),j});
                    double bestDis = 200;
                    String bestStop = "";
                    while (true) {
                        bestDis = 200;
                        bestStop = "";
                        String bestRoute = "";
                        if (currentStop == null) {
                            break;
                        }
                        JSONArray jjA = (JSONArray) currentStop.get("route_ids");
                        for (Object jj : jjA) {
                            if (jj != null) {
                                JSONObject ttoStopsI = (JSONObject) routesToStops.get(jj);
                                Object[] ttoStops = ttoStopsI.keySet().toArray();
                                for (Object ii : ttoStops) {
                                    JSONObject iii = (JSONObject) stops.get(ii);
                                    LatLng stopLatLng = new LatLng(Double.parseDouble(iii.get("latitude").toString()), Double.parseDouble(iii.get("longitude").toString()));
                                    double dis;
                                    if (latDisMethod) {
                                        dis = latDistance(stopLatLng, endPos);
                                    }
                                    else {
                                        dis = distance(stopLatLng, endPos);
                                    }
                                    if (dis < bestDis) {
                                        bestDis = dis;
                                        bestStop = ii.toString();
                                        bestRoute = jj.toString();
                                    }
                                }
                            }
                        }
                        boolean canAdd = true;
                        for (Object[] m : tripPlan) {
                            if (m[0].equals(bestStop)) {
                                canAdd = false;
                            }
                        }
                        if (canAdd) {
                            if (bestRoute.equals(tripPlan.get(tripPlan.size()-1)[1].toString())) {
                                tripPlan.remove(tripPlan.size()-1);
                                tripPlan.add(new Object[]{bestStop, bestRoute});
                            }
                        }
                        if (bestStop.equals(prevStop)) {
                            break;
                        }
                        prevStop = currentStopId;
                        currentStop = (JSONObject) stops.get(bestStop);
                        currentStopId = bestStop;
                    }
                    if (bestDis < absBestDis) {
                        absBestDis = bestDis;
                        bestTripPlan = tripPlan;
                        absBestStop = bestStop;
                    }
                }
            }
        }
        return bestTripPlan;
    }
    public double distance(LatLng pos1, LatLng pos2) {
        return Math.sqrt(Math.pow((pos1.latitude - pos2.latitude),2) + Math.pow((pos1.longitude - pos2.longitude),2));
    }
    public double latDistance(LatLng pos1,LatLng pos2) {
        return Math.abs(pos2.longitude - pos1.longitude);
    }
    public JSONObject readJSON(String path) throws IOException, ParseException {
        JSONParser p = new JSONParser();
        return (JSONObject) p.parse(new InputStreamReader(context.getAssets().open(path)));
    }
    /*static class LatLng {
        double latitude;
        double longitude;
        LatLng(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }*/
}