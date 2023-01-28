const stops = require('./busdata/stops.json');
const fs = require('fs');
let arr = [];
for (let i of Object.keys(stops)) {
  if (stops[i].stop_name.toLowerCase().includes("ride") || stops[i].stop_name.toLowerCase().includes("transit center")) {
    arr.push([{lat: stops[i].latitude, lng: stops[i].longitude}, stops[i].trip_ids.length])
  }
}
let str = '';
for (let i of arr) {
  str += JSON.stringify(i).replaceAll("\"","") + ",";
}
fs.writeFileSync('t.json',str);