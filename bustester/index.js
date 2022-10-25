const stops = require('./newStops.json');
const routesToStops = require('./routesToStops.json');
class LatLng {
    constructor(lat, lng) {
        this.latitude = lat;
        this.longitude = lng;
    }
}

let startStop = stops["67014"];
const endPos = new LatLng(47.617111, -122.143067);
var route = findRoute(startStop, 200, distance, "1000");
/*let dd = new LatLng(stops[route[1]].latitude, stops[route[1]].longitude);
var route2 = findRoute(stops[route[1]], distance(dd,endPos), distance, route[1]);
for (let i in route2[2]) {
    route[2].push(route2[2][i]);
}*/
console.log(route[2]);
console.log(route[1])


function distance(pos1, pos2) {
    return Math.sqrt(Math.pow((pos1.latitude - pos2.latitude),2) + Math.pow((pos1.longitude - pos2.longitude),2));
}
function latDistance(pos1,pos2) {
    return Math.abs(pos2.longitude - pos1.longitude);
}

function findRoute(startStop, absBestDis, latDis, startStopID) {
    var bestTripPlan = [];
    for (let j in startStop.route_ids) {
        if (startStop.route_ids[j] != null) {
            var toStops = Object.keys(routesToStops[startStop.route_ids[j]]);
            for (let i in toStops) {
                let currentStop = stops[toStops[i]];
                let currentStopId = toStops[i];
                let prevStop = "";
                var tripPlan = [startStopID,[toStops[i],startStop.route_ids[j]]];
                while (true) {
                    var bestDis = 200;
                    var bestStop = "";
                    var bestRoute = "";
                    if (currentStop == null) {
                        break;
                    }
                    for (let j in currentStop.route_ids) {
                        if (currentStop.route_ids[j] != null) {
                            var toStops = Object.keys(routesToStops[currentStop.route_ids[j]]);
                            for (let i in toStops) {
                                let stopLatLng = new LatLng(parseFloat(stops[toStops[i]].latitude), parseFloat(stops[toStops[i]].longitude));
                                let dis = latDis(endPos,stopLatLng);
                                if (dis < bestDis) {
                                    bestDis = dis;
                                    bestStop = toStops[i];
                                    bestRoute = currentStop.route_ids[j];
                                }
                            }
                        }
                    }
                    let canAdd = true;
                    for (let m in tripPlan) {
                        if (tripPlan[m][0] == bestStop) {
                            canAdd = false;
                        }
                    }
                    if (canAdd) {
                        tripPlan.push([bestStop, bestRoute]);
                    }
                    if (bestStop == prevStop) {
                        break;
                    }
                    prevStop = currentStopId;
                    currentStop = stops[bestStop];
                    currentStopId = bestStop;
                }
                if (bestDis < absBestDis) {
                    absBestDis = bestDis;
                    bestTripPlan = tripPlan;
                    var absBestStop = bestStop;
                }
            }
        }
    }
    return [absBestDis,absBestStop,bestTripPlan];
}