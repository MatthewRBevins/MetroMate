for (let j in startStop.route_ids) {
    if (startStop.route_ids[j] != null) {
        var toStops = Object.keys(routesToStops[startStop.route_ids[j]]);
        for (let i in toStops) {
            let currentStop = stops[toStops[i]];
            let currentStopId = toStops[i];
            let prevStop = "";
            var tripPlan = ["1000",[toStops[i],startStop.route_ids[j]]];
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
                            let dis = latDistance(endPos,stopLatLng);
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
                //console.log("Take route " + bestRoute + " to stop " + bestStop + " which is " + bestDis + " away.");
            }
            if (bestDis < absBestDis) {
                absBestDis = bestDis;
                bestTripPlan = tripPlan;
                var absBestRoute = bestStop;
            }
        }
    }
}