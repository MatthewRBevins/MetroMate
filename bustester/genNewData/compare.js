const stops = require('./stops.json');
const fs = require('fs')
let arr = Object.keys(stops).map(val => [stops[val].latitude,stops[val].longitude]);
let str = "";
for (let i of arr) {
    str += i[0] + "," + i[1] + "\n"
}
fs.writeFileSync("fawe.csv",str)