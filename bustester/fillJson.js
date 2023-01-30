const routes = require('./busdata/routes.json');
const trips = require('./busdata/trips.json');
const stops = require('./busdata/stops.json');
const fs = require('fs');
const regions = require('./fullRegions.json').regions;

function checkRegion(lat, lng) {
    let j = 0;
    for (let i of regions) {
        if (lat >= i[0].lat && lat <= i[2].lat && lng >= i[0].lng && lng <= i[2].lng) return j;
        j++;
    }
    return -1;
}

for (let i of Object.keys(routes)) {
    console.log(i);
    routes[i].times_available = [];
    routes[i].regions = {};
    for (let j of routes[i].trip_ids) {
        routes[i].times_available.push({
            "from": trips[j][0].time,
            "to": trips[j][trips[j].length-1].time
        });
        for (let k of trips[j]) {
            let r = checkRegion(parseFloat(stops[k.stop_id].latitude), parseFloat(stops[k.stop_id].longitude));
            if (routes[i].regions[r] == null) {
                routes[i].regions[r] = {
                    "times": [
                        {
                            "time": k.time,
                            "stop_id": k.stop_id
                        }
                    ]
                }
            }
            else {
                routes[i].regions[r].times.push({
                    "time": k.time,
                    "stop_id": k.stop_id
                });
            }
        }
    }
}
fs.writeFileSync('newRoutes.json',JSON.stringify(routes));