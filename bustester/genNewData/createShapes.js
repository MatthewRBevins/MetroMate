const fs = require('fs')
let folder = require('./folder.json').folder
let fData = require('./folder.json')
const shapes = fs.readFileSync('./' + folder + '-Raw/shapes.txt').toString();
let obj = {};
let str = "";
for (let i of shapes) {
    if (i == "\n") {
        let o = str.split(",");
        if (o[fData.SHAPE_ID] != "shape_id") {
            if (obj[o[fData.SHAPE_ID]] == null) {
                obj[o[fData.SHAPE_ID]] = [{
                    latitude: o[fData.SHAPE_LAT],
                    longitude: o[fData.SHAPE_LNG]
                }];
            }
            else {
                obj[o[fData.SHAPE_ID]].push({
                    latitude: o[fData.SHAPE_LAT],
                    longitude: o[fData.SHAPE_LNG]
                });
            }
        }
        str = "";
    }
    else {
        str += i;
    }
}
fs.writeFileSync(folder + '-Output/shapes.json', JSON.stringify(obj))
//shape_id,shape_pt_lat,shape_pt_lon,shape_pt_sequence,shape_dist_traveled