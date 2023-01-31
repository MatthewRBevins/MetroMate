const stops = require('./busdata/stops.json');
const regions = require('./fullRegions.json').regions;
const fs = require('fs');

function checkRegion(lat, lng) {
    let j = 0;
    for (let i of regions) {
      if (lat >= i[0].lat && lat <= i[2].lat && lng >= i[0].lng && lng <= i[2].lng) return j;
      j++;
    }
    return -1;
}

for (let i of Object.keys(stops)) {
    stops[i].region = checkRegion(parseFloat(stops[i].latitude), parseFloat(stops[i].longitude));
    stops[i].trip_ids = null;
}
fs.writeFileSync('newStops.json',JSON.stringify(stops));