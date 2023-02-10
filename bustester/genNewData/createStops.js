const fs = require('fs');
const trips = fs.readFileSync('stops.txt').toString();
let str = "";
let obj = {};
for (let i of trips) {
    if (i == "\n") {
        let o = str.split(",");
        str = "";
        obj[o[0]] = {
            stop_name: o[2],
            latitude: o[4],
            longitude: o[5]
        }
    }
    else {
        str += i;
    }
}
fs.writeFileSync('stops.json', JSON.stringify(obj))
//stop_id,stop_code,stop_name,stop_desc,stop_lat,stop_lon,zone_id,stop_url,location_type,parent_station,stop_timezone