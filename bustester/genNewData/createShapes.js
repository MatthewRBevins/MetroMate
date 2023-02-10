const fs = require('fs');
const shapes = fs.readFileSync('shapes.txt').toString();
let obj = {};
let str = "";
for (let i of shapes) {
    if (i == "\n") {
        let o = str.split(",");
        if (obj[o[0]] == null) {
            obj[o[0]] = [{
                latitude: o[1],
                longitude: o[2]
            }];
        }
        else {
            obj[o[0]].push({
                latitude: o[1],
                longitude: o[2]
            });
        }
        str = "";
    }
    else {
        str += i;
    }
}
fs.writeFileSync('shapes.json', JSON.stringify(obj))
//shape_id,shape_pt_lat,shape_pt_lon,shape_pt_sequence,shape_dist_traveled