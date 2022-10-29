package com.example.busapp;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Routing {
    private JSONObject routesToStops;
    private JSONObject stops;
    private Context context;

    public Routing(Context c) throws IOException, ParseException {
        this.context = c;
        this.stops = readJSON("newStops.json");
        this.routesToStops = readJSON("routesToStops.json");
    }

    /**
     * Generates route based on start stop and end position
     * @param endPos latitude and longitude of end position
     * @param startStopStr ID of start stop
     * @return ArrayList of each part of route, each index containing stop ID and route ID
     */
    public ArrayList<Object[]> genRoute(LatLng endPos, String startStopStr) {
        //Get information about start stop
        JSONObject startStop = (JSONObject) stops.get(startStopStr);
        //Make sure start stop exists
        assert startStop != null;

        //Use find route algorithm to map out a route based on stops closest to end position based on aboslute distance
        ArrayList<Object[]> route = findRoute(startStop, 200, false, startStopStr, endPos, false);
        //Get final stop information
        String finalStop = route.get(route.size()-1)[0].toString();
        JSONObject finalStopObj = (JSONObject) stops.get(finalStop);
        LatLng finalStopLatLng = new LatLng(Double.parseDouble(finalStopObj.get("latitude").toString()), Double.parseDouble(finalStopObj.get("longitude").toString()));
        //If previous algorithm did not find a sufficient route
        if (distance(finalStopLatLng, endPos) >= 0.02) {
            //Use find route algorithm to map out a route based on stops closest to end position based on LONGITUDE
            route = findRoute(startStop, 200, true, startStopStr, endPos, false);
            //Get final stop information
            finalStop = route.get(route.size() - 1)[0].toString();
            finalStopObj = (JSONObject) stops.get(finalStop);
            finalStopLatLng = new LatLng(Double.parseDouble(finalStopObj.get("latitude").toString()), Double.parseDouble(finalStopObj.get("longitude").toString()));
            //If longitude algorithm did not find a sufficent route
            if (distance(finalStopLatLng, endPos) >= 0.01) {
                //Use find route algorithm to continue route using closest possible absolute distance
                ArrayList<Object[]> route2 = findRoute(finalStopObj, distance(finalStopLatLng, endPos), false, finalStop, endPos, true);
                //Add results from latest algorithm to original route
                route.addAll(route2);
            }
        }
        //Loop through all elements in the generated route
        for (int i = 0; i < route.size(); i++) {
            //Make sure there aren't any redundant routes
            if (route.get(i).length > 1 && i > 1) {
                if (route.get(i)[1].equals(route.get(i - 1)[1])) {
                    route.remove(i - 1);
                }
            }
        }
        //Return final route list
        return route;
    }

    /**
     * Algorithmically generates the best possible route from a given starting stop to a given end position
     * @param startStop Start stop information
     * @param absBestDis Current distance from end position (needed in case of running algorithm multiple times)
     * @param latDisMethod Whether to check for absolute distance or longitudinal distance (true for longitudinal)
     * @param startStopID Start stop ID
     * @param endPos End position
     * @param suppressStart If algorithm is running multiple times, remove the start stop from beginning of ArrayList
     * @return ArrayList of generated route, each index containing stop ID and route ID
     */
    public ArrayList<Object[]> findRoute(JSONObject startStop, double absBestDis, boolean latDisMethod, String startStopID, LatLng endPos, boolean suppressStart) {
        //Initialize route plan ArrayList
        ArrayList<Object[]> bestTripPlan = new ArrayList();
        //Get all route IDS that leave from current stop
        JSONArray jA = (JSONArray) startStop.get("route_ids");
        String absBestStop = "";
        //Loop through all route IDS that leave from start stop
        for (Object j : jA) {
            //If route ID exists
            if (j != null) {
                //Loop through all stops that the current route goes to
                JSONObject toStopsI = (JSONObject) routesToStops.get(j);
                Object[] toStops = toStopsI.keySet().toArray();
                for (Object i : toStops) {
                    //Get stop information
                    JSONObject currentStop = (JSONObject) stops.get(i);
                    String currentStopId = i.toString();
                    String prevStop = "";
                    //Initialize trip plan
                    ArrayList<Object[]> tripPlan = new ArrayList();
                    if (! suppressStart) {
                        tripPlan.add(new Object[]{startStopID});
                    }
                    //Add current stop to trip ID
                    tripPlan.add(new Object[]{i.toString(),j});
                    double bestDis = 200;
                    String bestStop = "";
                    //Loop until closest possible stop reached
                    while (true) {
                        bestDis = 200;
                        bestStop = "";
                        String bestRoute = "";
                        if (currentStop == null) {
                            break;
                        }
                        //Loop through routes that leave from current stop
                        JSONArray jjA = (JSONArray) currentStop.get("route_ids");
                        for (Object jj : jjA) {
                            //If route exists
                            if (jj != null) {
                                //Get information about route
                                JSONObject ttoStopsI = (JSONObject) routesToStops.get(jj);
                                Object[] ttoStops = ttoStopsI.keySet().toArray();
                                //Loop through stops that current route goes to
                                for (Object ii : ttoStops) {
                                    //Check whether distance between current stop and end position is better than previous
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
                        //Make sure duplicates aren't added
                        boolean canAdd = true;
                        for (Object[] m : tripPlan) {
                            if (m[0].equals(bestStop)) {
                                canAdd = false;
                            }
                        }
                        //Add best stop found to trip plan
                        if (canAdd) {
                            tripPlan.add(new Object[]{bestStop, bestRoute});
                        }
                        if (bestStop.equals(prevStop)) {
                            break;
                        }
                        prevStop = currentStopId;
                        currentStop = (JSONObject) stops.get(bestStop);
                        currentStopId = bestStop;
                    }
                    //If current best distance is better than absolute best distance, update absolute best trip plan
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

    /**
     * Calculates distance between two LatLng objects
     * @param pos1 LatLng of first position
     * @param pos2 LatLng of second position
     * @return Distance between both points
     */
    public double distance(LatLng pos1, LatLng pos2) {
        return Math.sqrt(Math.pow((pos1.latitude - pos2.latitude),2) + Math.pow((pos1.longitude - pos2.longitude),2));
    }

    /**
     * Calculates distance between two longitudes
     * @param pos1 LatLng of first position
     * @param pos2 LatLng of second position
     * @return Distance between longitudes of both points
     */
    public double latDistance(LatLng pos1,LatLng pos2) {
        return Math.abs(pos2.longitude - pos1.longitude);
    }

    /**
     * Read JSON based on file
     * @param path File to read
     * @return JSONObject of file read
     * @throws IOException Opening file
     * @throws ParseException Parsing JSON
     */
    public JSONObject readJSON(String path) throws IOException, ParseException {
        JSONParser p = new JSONParser();
        return (JSONObject) p.parse(new InputStreamReader(context.getAssets().open(path)));
    }
}