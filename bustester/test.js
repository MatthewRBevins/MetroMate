const routes = require('./busdata/routes.json');
const trips = require('./busdata/trips.json');

let origLen = 0;
let actualTripsLen = 0;
for (let k of Object.keys(routes)) {
    let route = routes[k];
    origLen += route.trip_ids.length;
    for (let i of route.trip_ids) {
        for (let j of trips[i]) {
            if (j.time.substr(0,2) == "11" || j.time.substr(0,2) == "12") {
                actualTripsLen++;
                break;
            }
        }
    }
}
console.log(origLen);
console.log(actualTripsLen);