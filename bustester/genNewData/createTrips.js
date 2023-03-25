const fs = require('fs')
let folder = require('./folder.json').folder
let fData = require('./folder.json')
const trips = fs.readFileSync('./' + folder + '-Raw/trips.txt').toString().split("\n");
const stop_times = fs.readFileSync('./' + folder + '-Raw/stop_times.txt').toString().split("\n");

let obj = {};
for (let i of trips) {
    let o = i.split(",");
    obj[o[fData.TRIP_ID]] = {
        "route_id": o[fData.TRIP_ROUTE_ID],
        "stops": []
    }
}

let ii = 0;
for (let i of stop_times) {
    if (ii % 100000 == 0) {
        console.log (ii + " / " + stop_times.length)
    }
    let o = i.split(",");
    if (obj[o[fData.STOP_TIMES_TRIP_ID]] != null) { 
        obj[o[fData.STOP_TIMES_TRIP_ID]].stops.push({
            stop_id: o[fData.TRIP_STOP_ID],
            time: o[fData.TRIP_TIME]
        })
    }
    ii++
}
fs.writeFileSync(folder + '-Output/trips.json', JSON.stringify(obj))
//trip_id,arrival_time,departure_time,stop_id,stop_sequence,stop_headsign,pickup_type,drop_off_type,shape_dist_traveled,timepoint