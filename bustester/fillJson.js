const routes = require('./newRoutes.json');
const trips = require('./busdata/trips.json');
const stops = require('./busdata/stops.json');
const fs = require('fs');
const regions = require('./fullRegions.json').regions;

for (let i of Object.keys(routes)) {
  for (let j of routes[i].trips) {
    j.regions = [];
  }
}

fs.writeFileSync('newRoutes.json',JSON.stringify(routes));