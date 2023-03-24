const fs = require('fs')
const prompt = require('prompt-sync')()
let folder = prompt('Folder: ')
const stops = require('./' + folder + '-Output/stops.json');
let arr = [];
for (let v of Object.keys(stops)) {
    arr.push(`${stops[v].latitude},${stops[v].longitude}`)
}
fs.writeFileSync('./' + folder + '-Output/geoguessr.csv', arr.join("\n"))