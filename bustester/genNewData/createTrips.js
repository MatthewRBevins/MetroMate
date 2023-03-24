const fs = require('fs');
const prompt = require('prompt-sync')()
let folder = prompt('Folder: ')
const trips = require('./trips.json')
const stop_times = fs.readFileSync('stop_times.txt').toString();

let str = "";
let ii = 0;
for (let i of stop_times) {
    if (i == "\n") {
        console.log(ii + " / 1963715");
        ii++;
        let o = str.split(",");
        trips[o[0]].push({
            stop_id: o[3],
            time: o[1]
        })
        str = "";
    }
    else {
        str += i;
    }
}
fs.writeFileSync('trips.json', JSON.stringify(trips))
//trip_id,arrival_time,departure_time,stop_id,stop_sequence,stop_headsign,pickup_type,drop_off_type,shape_dist_traveled,timepoint