const fs = require('fs')
let folder = require('./folder.json').folder
const trips = require('./' + folder + '-Output/trips.json')
const stops = require('./' + folder + '-Output/stops.json')
const regions = require('./' + folder + '-Output/fullRegions.json').regions
function checkRegion(lat, lng) {
    let j = 0
    for (let i of regions) {
      if (lat >= i[0].lat && lat <= i[2].lat && lng >= i[0].lng && lng <= i[2].lng) return j
      j++
    }
    return -1
}

let obj = {};
let ii = 0;
let ll = Object.keys(trips).length
for (let i of Object.keys(trips)) {
    ii++;
    if (ii % 10000 == 0) {
        console.log(ii + " / " + ll)
    }
    obj[i] = {"regions": [], "stops": []}
    for (let j of trips[i].stops) {
        let s = j.stop_id
        let t = j.time
        if (stops[s] != null) {
            let r = checkRegion(parseFloat(stops[s].latitude), parseFloat(stops[s].longitude))
            if (! obj[i].regions.includes(r)) {
                obj[i].regions.push(r)
            }
            obj[i].stops.push({
                "s": s,
                "t": t,
                "r": r
            })
        }
    }
}
fs.writeFileSync(folder + '-Output/newTrips.json', JSON.stringify(obj))
