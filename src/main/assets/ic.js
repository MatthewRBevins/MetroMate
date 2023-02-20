const newTrips = require('./fawe.json');
const fs = require('fs');
for (let i of Object.keys(newTrips)){
    let jj = 0;
    newTrips[i] = [
        newTrips[i].regions,
        newTrips[i].stops
    ]
    for (let j of newTrips[i][1]) {
        console.log([parseInt(j.stop_id),parseInt(j.time.substr(0,5).replaceAll(":",""))]);
        newTrips[i][1][jj] = [parseInt(j.stop_id),parseInt(j.time.substr(0,5).replaceAll(":",""))];
        jj++;
    }
}
fs.writeFileSync('newTrips.json',JSON.stringify(newTrips));