const fs = require('fs');
const prompt = require('prompt-sync')()
let folder = prompt('Folder: ')
const trips = fs.readFileSync('./' + folder + '-Raw/stops.txt').toString();
let str = "";
let obj = {};
for (let i of trips) {
    if (i == "\n") {
        let o = str.split(",");
        if (o[2] != "stop_name") {
            str = "";
            obj[o[0]] = {
                stop_name: o[2],
                latitude: o[4],
                longitude: o[5]
            }
        }
    }
    else {
        str += i;
    }
}
fs.writeFileSync(folder + '-Output/stops.json', JSON.stringify(obj))
//stop_id,stop_code,stop_name,stop_desc,stop_lat,stop_lon,zone_id,stop_url