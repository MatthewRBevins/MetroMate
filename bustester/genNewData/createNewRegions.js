const fs = require('fs')
let folder = require('./folder.json').folder
const regions = require('./' + folder + '-Output/fullRegions.json').regions;
const routes = require('./' + folder + '-Output/routes.json')
const newTrips = require('./' + folder + '-Output/newTrips.json')
let obj = {}
for (let i in regions) {
    if (i % 100 == 0) {
        console.log(i + " / " + regions.length)
    }
    let curRegion = regions[i]
    let routesIncluded = []
    for (let j of Object.keys(routes)) {
        for (let k of routes[j].trip_ids) {
            if (newTrips[k].regions.includes(parseInt(i))) {
                routesIncluded.push(j)
                break
            }
        }
    }
    if (routesIncluded.length > 0) {
        obj[i] = {
            bounds: curRegion,
            routes: routesIncluded
        }
    }
}
fs.writeFileSync(folder + '-Output/newRegions.json', JSON.stringify(obj))