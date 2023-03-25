const fs = require('fs')
let folder = require('./folder.json').folder
let fData = require('./folder.json')
const stops = fs.readFileSync('./' + folder + '-Raw/stops.txt').toString().split("\n");
let str = "";
let obj = {};
let ii = 0;
for (let i of stops) {
    let o = i.split(",");
    ii++
    if (o[fData.STOP_NAME] != "stop_name") {
        str = "";
        let latIn = fData.STOP_LAT
        while (parseFloat(o[latIn]) != o[latIn]) {
            latIn++
        }
        obj[o[fData.STOP_ID]] = {
            stop_name: o[fData.STOP_NAME],
            latitude: o[latIn],
            longitude: o[latIn+1]
        }
    }
}
fs.writeFileSync(folder + '-Output/stops.json', JSON.stringify(obj))
//stop_id,stop_code,stop_name,stop_desc,stop_lat,stop_lon,zone_id,stop_url