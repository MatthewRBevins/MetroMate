const routes = require('./busdata/routes.json');
const shapes = require('./busdata/shapes.json');
const stops = require('./busdata/stops.json');
const trips = require('./busdata/trips.json');
const stopsToStops = require('./stopsToStops.json');
const stopsToStopsTrips = require('./test2.json');
const fs = require('fs');
const { listeners } = require('process');

class LatLng{
    constructor(lat, long) {
        this.lat = lat;
        this.long = long;
    }
}

const startStop = "260"
const endStop = "260"
const endPos = new LatLng(47.481230,-122.216501);
let toStops = stopsToStops[startStop];
let absBest = [100,"0","0"];
let bestTripPlan = [];
for (let i in toStops) {
    let tripPlan = [toStops[i]];
    let best = getClosest(toStops[i],endPos);
    let alreadyFound = [];
    while (! alreadyFound.includes(best[1])) {
        alreadyFound.push(best[1]);
        let gc = getClosest(best[1],endPos);
        best = gc;
        tripPlan.push(gc)
    }
    if (best[0] < absBest[0]) {
        absBest[0] = best[0];
        absBest[1] = best[1];
        absBest[2] = best[2];
        bestTripPlan = [];
        for (let i in tripPlan) {
            bestTripPlan.push(tripPlan[i]);
        }
    }
}
console.log("ABSOLUTE BEST: " + absBest);
console.log(bestTripPlan);
//TODO: shrink test2.json
function getClosest(startStop, endPos) {
    let toStops = stopsToStops[startStop];
    let best = [100,"0","0"]
    for (let i in toStops) {
        let tripss = stopsToStopsTrips[startStop][i];
        let tripID = "";
        let bestTime = 100;
        //Loop through all possible trips
        for (let j in tripss) {
            //Find what time the trip gets to the target stop
            let currentTrip = trips[tripss[j]];
            let stopIndex = 0;
            for (let k in currentTrip) {
                if (currentTrip[k].stop_id == toStops[i]) {
                    stopIndex = k;
                    break;
                }
            }
            let timeAtStop = parseInt(trips[tripss[j]][stopIndex].time.substr(0,2));
            if (timeAtStop < parseInt(new Date().getHours())) {
                timeAtStop = 24 - (parseInt(new Date().getHours()) - timeAtStop) 
            }
            if (timeAtStop - parseInt(new Date().getHours()) < bestTime) {
                bestTime = timeAtStop - parseInt(new Date().getHours());
                tripID = tripss[j];
            }
        }
        currentPos = new LatLng(parseFloat(stops[toStops[i]].latitude), parseFloat(stops[toStops[i]].longitude));
        let dis = checkDistance(currentPos,endPos);
        if (dis < best[0]) {
            best[0] = dis;
            best[1] = toStops[i];
            best[2] = tripID;
        }
    }
    return best;
}
//10 secs

function checkDistance(pos1, pos2) {
    let latDiff = Math.abs(pos1.lat-pos2.lat);
    let longDiff = Math.abs(pos1.long-pos2.long);
    return latDiff + longDiff;
}