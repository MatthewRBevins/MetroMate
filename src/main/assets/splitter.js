const newTrips = require('./newTrips.json');
const fs = require('fs');
let num = 0;
let obj1 = {};
let obj2 = {};
let obj3 = {};
let ii = 0;
for (let i of Object.keys(newTrips)) {
    if (ii < 18500) {
        obj1[i] = newTrips[i]
    }
    else if (ii < 37000) {
        obj2[i] = newTrips[i]
    }
    else {
        obj3[i] = newTrips[i]
    }
    ii++;
}
fs.writeFileSync('newTrips1.json',JSON.stringify(obj1));
fs.writeFileSync('newTrips2.json',JSON.stringify(obj2));
fs.writeFileSync('newTrips3.json',JSON.stringify(obj3));