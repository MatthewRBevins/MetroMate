const fs = require('fs')
let folder = require('./folder.json').folder
const stops = require('./' + folder + '-Output/stops.json');
let arr = [];
for (let v of Object.keys(stops)) {
    arr.push(`${stops[v].latitude},${stops[v].longitude}`)
}
fs.writeFileSync('./' + folder + '-Output/geoguessr.csv', arr.join("\n"))