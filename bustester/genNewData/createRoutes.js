const fs = require('fs')
let folder = require('./folder.json').folder
let fData = require('./folder.json')
const routes = fs.readFileSync('./' + folder + '-Raw/routes.txt').toString().split("\n")
const trips = require('./' + folder + '-Output/trips.json')
let obj = {};
for (let i of routes) {
    let o = i.split(",")
    if (o[fData.ROUTE_ID] != "") {
        obj[o[fData.ROUTE_ID]] = {
            "short_name": o[fData.SHORT_NAME],
            "description": o[fData.DESCRIPTION],
            "trip_ids": []
        }
        for (let i of Object.keys(trips)) {
            if (trips[i].route_id == o[fData.ROUTE_ID]) {
                obj[o[fData.ROUTE_ID]].trip_ids.push(i)
            }
        }
    }
}
fs.writeFileSync(folder + '-Output/routes.json', JSON.stringify(obj))