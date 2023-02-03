const routes = require('./newRoutes.json');
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
  for (let j of routes[i].trips) {
    j.regions = [];
    for (let k of trips[j.id]) {
      let lat = stops[k.stop_id].latitude;
      let long = stops[k.stop_id].longitude;
      let reg = checkRegion(lat, long);
      if (! Object.keys(j.regions).includes(reg)) {
        j.regions[reg] = [];
      }
      else {
        j.regions[reg].push()
      }
    }
  }
}

fs.writeFileSync('newerRoutes.json',JSON.stringify(routes));