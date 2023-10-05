package com.metromate.busapp;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;

import java.io.FileReader;
import java.time.LocalTime;
import java.util.*;

public class Routing {

    public Routing() {

    }

    public List<RouteItem> genRoute(LocalTime time, LatLng pos1, LatLng pos2, String city) throws IOException, ParseException {
        List<RouteItem> l = new ArrayList<RouteItem>();
        System.out.println("https://matthewrbevins.com/metromate/?city=" + city + "&time=" + deformatTime(time) + "&pos1=" + pos1.latitude + "," + pos1.longitude + "&pos2=" + pos2.latitude + "," + pos2.longitude);
        String data = Web.readFromWeb("https://matthewrbevins.com/metromate/?city=" + city + "&time=" + deformatTime(time) + "&pos1=" + pos1.latitude + "," + pos1.longitude + "&pos2=" + pos2.latitude + "," + pos2.longitude);//Web.readFromWeb("https://matthewrbevins.com/metromate/?time=09:00:00&pos1=47.545130,-122.137246&pos2=47.609165,-122.339078");
        System.out.println("DONE GETTING DATA");
        JSONObject o = Web.readJSON(new StringReader(data));
        System.out.println(o);
        System.out.println(o.get("path"));
        JSONArray path = (JSONArray) o.get("path");
        Iterator it = path.iterator();
        while (it.hasNext()) {
            JSONObject i = (JSONObject) it.next();
            System.out.println(i);
            l.add(new RouteItem(formatTime(i.get("startTime").toString()), i.get("startStop").toString(), i.get("route").toString(), i.get("trip").toString(), formatTime(i.get("time").toString()), i.get("stop").toString(), Integer.parseInt(i.get("region").toString())));
        }
        return l;
    }
    private String deformatTime(LocalTime time) {
        return time.getHour() + ":" + time.getMinute() + ":" + time.getSecond();
    }
    private LocalTime formatTime(String time) {
        String[] t = time.split(":");
        if (Integer.parseInt(t[0]) > 23) {
            t[0] = String.valueOf(Integer.parseInt(t[0])-24);
        }
        LocalTime lt = LocalTime.of(Integer.parseInt(t[0]),Integer.parseInt(t[1]),Integer.parseInt(t[2]));
        return lt;
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
