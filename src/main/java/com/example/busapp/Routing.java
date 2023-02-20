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

import java.io.FileReader;
import java.time.LocalTime;
import java.util.*;

public class Routing {
    private JSONObject newRegions;
    private JSONObject newRoutes;
    private JSONObject newStops;
    private JSONObject newTrips;
    private JSONObject fullRegions;
    private JSONArray regions;
    private Context c;

    public Routing(Context c, JSONObject newRegions, JSONObject newRoutes, JSONObject newStops, JSONObject newTrips, JSONObject fullRegions) throws IOException, ParseException {
        this.c = c;
        System.out.println("LOADING JSON FILES...");
        this.newRegions = newRegions;//Web.readJSON(new InputStreamReader(c.getAssets().open("newRegions.json")));
        //System.out.println("DONE 1");
        this.newRoutes = newRoutes;//Web.readJSON(new InputStreamReader(c.getAssets().open("newRoutes.json")));
        //System.out.println("DONE 2");
        this.newStops = newStops;//Web.readJSON(new InputStreamReader(c.getAssets().open("newStops.json")));
        //System.out.println("DONE 3");
        this.newTrips = newTrips;//Web.readJSON(new InputStreamReader(c.getAssets().open("newTrips.json")));
        //System.out.println("DONE 4");
        this.fullRegions = fullRegions;//Web.readJSON(new InputStreamReader(c.getAssets().open("fullRegions.json")));
        System.out.println(this.fullRegions);
        this.regions = (JSONArray) this.fullRegions.get("regions");
        System.out.println("DONE LOADING");
    }
    public List<RouteItem> genRoute(LocalTime time, LatLng pos1, LatLng pos2) {
        System.out.println("STARTING ROUTE GENERATION");
        int region1 = checkRegion(pos1);
        //System.outprintln(region1);
        int region2 = checkRegion(pos2);
        double low = 10000;
        //System.outprintln(Arrays.toString(getClosestRegions(1125, false)));
        int times = 0;
        List<RouteItem> path = new ArrayList<>();
        List<RouteItem> bestPath = new ArrayList<>();
        while (low > 5 && times < 3) {
            path = new ArrayList<>(bestPath);
            times++;
            List<RouteItem> r = getPossibleRegions(time, region1, true);
            //System.outprintln(rr.size());
            low = 10000;
            System.out.println("SIZE:" + r.size());
            for (RouteItem i : r) {
                path.add(i);
                if (checkRegionDistance(i.region, region2) <= low) {
                    low = checkRegionDistance(i.region, region2);
                    bestPath = new ArrayList<>(path);
                    region1 = i.region;
                }
                for (RouteItem j : getPossibleRegions(i.time, i.region, false)) {
                    System.out.println("AMOG");
                    path.add(j);
                    if (checkRegionDistance(j.region, region2) < low) {
                        bestPath = new ArrayList<>(path);
                        region1 = j.region;
                        low = checkRegionDistance(j.region, region2);
                    }
                    path.remove(path.size()-1);
                }
                path.remove(path.size()-1);
            }
            time = bestPath.get(bestPath.size()-1).time;
        }
        return bestPath;
    }
    private List<RouteItem> getPossibleRegions(LocalTime time, int startingRegion, boolean closestRegions) {
        List<RouteItem> arr = new ArrayList<>();
        List<Object> regionsToCheck = new ArrayList<>();
        regionsToCheck.add(String.valueOf(startingRegion));
        if (closestRegions) {
            regionsToCheck.addAll(Arrays.asList(getClosestRegions(startingRegion, false)));
        }
        for (Object currentRegion : regionsToCheck) {
            //Loop through all the routes that go through starting regions
            if (newRegions.get(String.valueOf(currentRegion)) != null) {
                for (Iterator iti = ((JSONArray) ((JSONObject) newRegions.get(String.valueOf(currentRegion))).get("routes")).iterator(); iti.hasNext(); ) {
                    Object i = iti.next();
                    //Loop through every trip of current route
                    for (Iterator itj = ((JSONArray) ((JSONObject)newRoutes.get(i)).get("trips")).iterator(); itj.hasNext(); ) {
                        JSONObject j = (JSONObject) itj.next();
                        //If the current trip takes place at the current time
                        if (isInTimeFrame(time, formatTime((String) ((JSONObject) j.get("times")).get("from")), formatTime((String) ((JSONObject) j.get("times")).get("to")))) {
                            JSONArray toRegions = (JSONArray) ((JSONArray) newTrips.get(j.get("id"))).get(0);
                            if (toRegions.toString().contains(currentRegion.toString())) {
                                boolean hasReached = false;
                                LocalTime startingTime = null;
                                String startingStop = null;
                                int kk = 0;
                                //stop, time
                                long[] curVal = new long[2];
                                for (Iterator itk = ((JSONArray) ((JSONArray) newTrips.get(j.get("id"))).get(1)).iterator(); itk.hasNext(); ) {
                                    if (kk % 2 == 0) {
                                        curVal[0] = (long) itk.next();
                                    }
                                    else {
                                        curVal[1] = (long) itk.next();
                                        String region = String.valueOf(((JSONObject) newStops.get(String.valueOf(curVal[0]))).get("region"));
                                        //System.out.println(k);
                                        if (Integer.parseInt(region) == Integer.parseInt(currentRegion.toString())) {
                                            startingTime = formatTime(intToStrTime(curVal[1]));
                                            startingStop = String.valueOf(curVal[0]);
                                            hasReached = true;
                                        }
                                        if (hasReached) {
                                            arr.add(new RouteItem(startingTime, startingStop, i.toString(), j.get("id").toString(), formatTime(intToStrTime(curVal[1])), String.valueOf(curVal[0]), Integer.parseInt(region)));
                                        }
                                    }
                                    kk++;
                                }
                            }
                        }
                    }
                }
            }
        }
        return arr;
    }
    private String intToStrTime(long time) {
        String st = String.valueOf(time);
        if (st.length() == 3) {
            st = "0" + st.charAt(0) + ":" + st.charAt(1) + st.charAt(2) + ":00";
        }
        else {
            st = st.charAt(0) +""+ st.charAt(1) + ":" + st.charAt(2) + st.charAt(3) + ":00";
        }
        return st;
    }
    private LocalTime formatTime(String time) {
        String[] t = time.split(":");
        if (Integer.parseInt(t[0]) > 23) {
            t[0] = String.valueOf(Integer.parseInt(t[0])-24);
        }
        LocalTime lt = LocalTime.of(Integer.parseInt(t[0]),Integer.parseInt(t[1]),Integer.parseInt(t[2]));
        return lt;
    }
    private Object[] getClosestRegions(int region, boolean immediateReturn) {
        boolean[] boundary = new boolean[]{region % 67 == 0, (region + 1) % 67 == 0, region < 68, region > (regions.size() - 67)};
        int[][][] closest = new int[][][]{{{region - 1}, {0}}, {{region + 1}, {1}}, {{region - 67}, {2}}, {{region + 67}, {3}}, {{(region - 1) + 67}, {0, 3}}, {{(region + 1) + 67}, {1, 3}}, {{(region - 1) - 67}, {0, 2}}, {{(region + 1) - 67}, {1, 2}}};
        List<Object> arr = Arrays.asList(Arrays.stream(closest).filter(x -> {
            for (int i : x[1]) {
                if (boundary[i]) {
                    return false;
                }
            }
            return true;
        }).map(x -> String.valueOf(x[0][0])).toArray());
        arr = new ArrayList<>(arr);
        if (immediateReturn) return Arrays.stream(arr.toArray()).filter(x -> newRegions.get(x) != null).toArray();
        boolean hasClosest = Arrays.stream(arr.toArray()).filter(x -> newRegions.get(x) != null).toArray().length != 0;
        while (!hasClosest) {
            ArrayList<Object> newClosest = new ArrayList<>();
            for (Object o : arr) {
                Object[] nextClosest = getClosestRegions(Integer.parseInt(o.toString()), true);
                newClosest.addAll(Arrays.asList(nextClosest));
            }
            arr.addAll(newClosest);
            hasClosest = Arrays.stream(arr.toArray()).filter(x -> newRegions.get(x) != null).toArray().length != 0;
        }
        return Arrays.stream(arr.toArray()).filter(x -> newRegions.get(x) != null).toArray();
    }
    private double calcDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x1-x2,2) + Math.pow(y1-y2,2));
    }
    private double checkRegionDistance(int region1, int region2) {
        return calcDistance(Math.ceil(region1/67), region1 - (67*Math.floor(region1/67)), Math.ceil(region2/67), region2 - (67*Math.floor(region2/67)));
    }
    private boolean isInTimeFrame(LocalTime time, LocalTime start, LocalTime end) {
        if ((time.getHour() == start.getHour() && start.getMinute() > time.getMinute()) || (start.getHour() == time.getHour()+1)) {
            if ((end.getHour() == time.getHour() && end.getMinute() > time.getMinute()) || (end.getHour() > time.getHour())) {
                return true;
            }
        }
        return false;
    }
    private int checkRegion(LatLng pos) {
        int j = 0;
        for (Iterator it = regions.iterator(); it.hasNext(); ) {
            JSONArray a = (JSONArray) it.next();
            if (pos.latitude >= Double.parseDouble((((JSONObject) a.get(0)).get("lat")).toString()) && pos.latitude <= Double.parseDouble((((JSONObject) a.get(2)).get("lat")).toString()) && pos.longitude >= Double.parseDouble((((JSONObject) a.get(0)).get("lng")).toString()) && pos.longitude <= Double.parseDouble((((JSONObject) a.get(2)).get("lng")).toString())) return j;
            j++;
        }
        return -1;
    }
    public class RouteItem {
        public LocalTime startTime;
        public String startStop;
        public String route;
        public String trip;
        public LocalTime time;
        public String stop;
        public int region;
        public RouteItem(LocalTime startTime, String startStop, String route, String trip, LocalTime time, String stop, int region) {
            this.startTime = startTime;
            this.startStop = startStop;
            this.route = route;
            this.trip = trip;
            this.time = time;
            this.stop = stop;
            this.region = region;
        }
        public String format() {
            return "{" +
                    "startTime: " + startTime +
                    "startStop: " + startStop +
                    "route: " + route +
                    "trip: " + trip +
                    "time: " + time +
                    "stop: " + stop +
                    "region: " + region +
                    "}";
        }
    }
}
