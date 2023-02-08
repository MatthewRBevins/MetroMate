const fs = require('fs');
const routes = require('./newRoutes.json');
const stops = require('./newStops.json');
const trips = require('./busdata/trips.json');
const regions = require('./fullRegions.json').regions;
const newRegions = require('./newRegions.json');

function checkRegion(lat, lng) {
  let j = 0;
  for (let i of regions) {
    if (lat >= i[0].lat && lat <= i[2].lat && lng >= i[0].lng && lng <= i[2].lng) return j;
    j++;
  }
  return -1;
}

for (let i of Object.keys(trips)) {
  let arr = trips[i];
  trips[i] = {
    regions: [],
    stops: []
  }
  for (let j of arr) {
    trips[i].stops.push(j);
    let r = stops[j.stop_id].region;
    trips[i].stops[trips[i].stops.length-1].region = r;
    if (! trips[i].regions.includes(r)) {
      trips[i].regions.push(r);
    }
  }
}

fs.writeFileSync('newTrips.json', JSON.stringify(trips));