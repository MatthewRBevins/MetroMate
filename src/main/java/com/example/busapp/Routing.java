package com.example.busapp;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Date;

public class Routing {
    JSONParser parser = new JSONParser();
    JSONObject routes;
    JSONObject shapes;
    JSONObject stops;
    JSONObject trips;
    JSONObject stopsToStops;
    JSONObject stopsToStopsTrips;
    Context context;

    public Routing(Context context) throws IOException, ParseException {
        this.context = context;
        routes = readJson("routes.json");
        shapes = readJson("shapes.json");
        stops = readJson("stops.json");
        trips = readJson("trips.json");
        stopsToStops = readJson("stopsToStops.json");
        stopsToStopsTrips = readJson("test2.json");
    }

    public ArrayList<Object[]> findRoute(String startStop, LatLng endPos) {
        JSONArray toStops = (JSONArray) stopsToStops.get(startStop);
        Object[] absBest = new Object[]{100,"0","0"};
        ArrayList<Object[]> bestTripPlan = new ArrayList();
        Iterator<Object> ii = toStops.iterator();
        while(ii.hasNext()) {
            String i = ii.next().toString();
            ArrayList<Object[]> tripPlan = new ArrayList();
            tripPlan.add(new Object[]{i});
            Object[] best = getClosest(i,endPos);
            ArrayList<String> alreadyFound = new ArrayList();
            while (! alreadyFound.contains(best[1])) {
                alreadyFound.add(best[1].toString());
                Object[] gc = getClosest(best[1].toString(),endPos);
                best = gc;
                tripPlan.add(gc);
            }
            if (Double.parseDouble(best[0].toString()) < Double.parseDouble(absBest[0].toString())) {
                absBest[0] = best[0];
                absBest[1] = best[1];
                absBest[2] = best[2];
                bestTripPlan = tripPlan;
            }
        }
        System.out.println("ABSOLUTE BEST: " + Arrays.toString(absBest));
        return bestTripPlan;
    }

    public Object[] getClosest(String startStop, LatLng endPos) {
        JSONArray toStops = (JSONArray) stopsToStops.get(startStop);
        Object[] best = new Object[]{100,"0","0"};
        for (int i = 0; i < toStops.size(); i++) {
            JSONArray aTripss = (JSONArray) stopsToStopsTrips.get(startStop);
            JSONArray tripss = (JSONArray) aTripss.get(i);
            String tripID = "";
            int bestTime = 100;
            //Loop through all possible trips
            Iterator<Object> jj = tripss.iterator();
            int ji = 0;
            while (jj.hasNext()) {
                String j = jj.next().toString();
                //Find what time the trip gets to the target stop
                JSONArray currentTrip = (JSONArray) trips.get(j);
                int stopIndex = 0;
                Iterator<JSONObject> kk = currentTrip.iterator();
                int ki = 0;
                while (kk.hasNext()) {
                    JSONObject k = kk.next();
                    if (k.get("stop_id").equals(toStops.get(i))) {
                        stopIndex = ki;
                    }
                    ki++;
                }
                JSONArray tAS = (JSONArray) trips.get(tripss.get(ji));
                JSONObject tAS2 = (JSONObject) tAS.get(stopIndex);
                int timeAtStop = Integer.parseInt(tAS2.get("time").toString().substring(0,2));
                if (timeAtStop < new Date().getHours()) {
                    timeAtStop = 24 - (new Date().getHours() - timeAtStop);
                }
                if (timeAtStop - new Date().getHours() < bestTime) {
                    bestTime = timeAtStop - new Date().getHours();
                    tripID = tripss.get(ji).toString();
                }
                ji++;
            }
            JSONObject la = (JSONObject) stops.get(toStops.get(i));
            LatLng currentPos = new LatLng(Double.parseDouble(la.get("latitude").toString()), Double.parseDouble(la.get("longitude").toString()));
            Double dis = checkDistance(currentPos, endPos);
            if (dis < Double.parseDouble(best[0].toString())) {
                best[0] = dis;
                best[1] = toStops.get(i);
                best[2] = tripID;
            }
        }
        return best;
    }

    public double checkDistance(LatLng pos1, LatLng pos2) {
        double latDiff = Math.abs(pos1.latitude-pos2.latitude);
        double longDiff = Math.abs(pos1.longitude-pos2.longitude);
        return latDiff + longDiff;
    }

    public JSONObject readJson(String s) throws IOException, ParseException {
        return (JSONObject) parser.parse(new InputStreamReader(context.getAssets().open(s)));
    }

    public Polyline drawStopToStop(String stopID1, String stopID2, String viaRouteID, GoogleMap mMap) {
        JSONObject stopID1Data = (JSONObject) stops.get(stopID1);
        LatLng stopID1LatLng = new LatLng((double) stopID1Data.get("latitude"), (double) stopID1Data.get("longitude"));
        JSONObject stopID2Data = (JSONObject) stops.get(stopID2);
        LatLng stopID2LatLng = new LatLng((double) stopID2Data.get("latitude"), (double) stopID2Data.get(("longitude")));

        PolylineOptions pathLine = new PolylineOptions();

        JSONObject routeData = (JSONObject) routes.get(viaRouteID);
        JSONArray shape_ids = (JSONArray) routeData.get("shape_ids");
        boolean foundStart = false;
        boolean foundEnd = false;
        // iterate over every shape id
        for (int i = 0; i < shape_ids.size(); i++) {
            String shape_id = (String) shape_ids.get(i);
            JSONArray shape_coordinates_list = (JSONArray) shapes.get(shape_id);
            ArrayList<LatLng> passedCoordinates = new ArrayList<>();
            for (int j = 0; j < shape_coordinates_list.size(); j++) {
                JSONObject coord = (JSONObject) shape_coordinates_list.get(j);
                LatLng coordLatLng = new LatLng((double) coord.get("latitude"), (double) coord.get("longitude"));
                // check for a point close to the first stop
                if (!foundStart) {
                    if (checkDistance(stopID1LatLng, coordLatLng) < 0.01d) {
                        foundStart = true;
                        // add point to line
                        pathLine.add(coordLatLng);
                    }
                } else {
                    // add point to line
                    pathLine.add(coordLatLng);
                    // check if reached point near last stop
                    if (checkDistance(coordLatLng, stopID2LatLng) < 0.01d) {
                        foundEnd = true;
                        break;
                    }
                }
            }
            // check if should continue to next shape id
            if (foundEnd) {
                break;
            }
        }
        Polyline polyline = mMap.addPolyline(pathLine);
        return polyline;
    }
}
