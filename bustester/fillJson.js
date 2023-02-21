const newTrips = require('./newTrips.json');
let obj = {};
for (let i of Object.keys(newTrips)) {
  obj[i] = newTrips[i];
  let jj = -1;
  for (let j of obj[i].stops) {
    jj++;
    obj[i].stops[jj] = {
      s: j.stop_id,
      t: j.time,
      r: j.region
    }
  }
}
const fs = require('fs');
fs.writeFileSync('newnewTrips.json', JSON.stringify(obj));