const newStops = require('./newStops.json');
let sl = Object.keys(newStops);
let obj = {};
let stopIDs = [];
for (let i in sl) {
    let len = newStops[sl[i]].route_ids.length;
    if (obj[len] == null) {
        obj[len] = 1;
    }
    else {
        obj[len]++;
    }
    if (len > 10) {
        stopIDs.push(sl[i]);
    }
}
console.log(obj);
console.log(stopIDs);