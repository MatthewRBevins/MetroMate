const stops = require('./busdata/stops.json');
const fs = require('fs');
const trips = require('./busdata/trips.json');
const stopsList = Object.keys(stops);
//TEST 1: 30 seconds
let finalObj = {};
let finalObj2 = {};
for (let i in stopsList) {
    let tripIDs = stops[stopsList[i]].trip_ids;
    let arr = [];
    let arr2 = [];
    for (let j in tripIDs) {
        for (let k in trips[tripIDs[j]]) {
            let currentTrip = trips[tripIDs[j]][k].stop_id;
            if (! arr.includes(currentTrip)) {
                arr.push(currentTrip)
                arr2.push([tripIDs[j]])
            }
            else {
                arr2[arr.indexOf(currentTrip)].push(tripIDs[j]);
            }
        }
    }
    finalObj[stopsList[i]] = arr;
    finalObj2[stopsList[i]] = arr2;
}
fs.writeFileSync('test.json',JSON.stringify(finalObj));
fs.writeFileSync('test2.json',JSON.stringify(finalObj2));