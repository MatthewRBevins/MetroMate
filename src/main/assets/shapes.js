const shapes = require('./shapes.json');
for (let i of Object.keys(shapes)){
    let jj = 0;
    let arr = [];
    for (let j of shapes[i]) {
        if (jj % 2 == 0) {
            arr.push({
                la: j.latitude,
                lo: j.longitude
            })
        }
        jj++;
    }
    shapes[i] = arr;
}
const fs = require('fs');
fs.writeFileSync('shapes.json',JSON.stringify(shapes));