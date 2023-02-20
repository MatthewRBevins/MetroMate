const newStops = require('./newStops.json');
const newTrips = require('./newTrips.json');
const fs = require('fs');
let stopObj = {};
let ii = 0;
for (let i of Object.keys(newTrips)){
    let arr = [];
    for (let j of newTrips[i][1]) {
        for (let k of j) {
            arr.push(k);
        }
    }
    newTrips[i][1] = arr;
}
fs.writeFileSync('newTrips.json',JSON.stringify(newTrips));