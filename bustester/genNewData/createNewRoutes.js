const fs = require('fs')
let folder = require('./folder.json').folder
const routes = require('./' + folder + '-Output/routes.json')
const trips = require('./' + folder + '-Output/trips.json')
let ii = 0;
for (let i of Object.keys(routes)) {
    ii++;
    routes[i].trips = routes[i].trip_ids.map(val => {
        let obj = {}
        obj.id = val
        obj.times = {
            "from": trips[val].stops[0].time,
            "to": trips[val].stops[trips[val].stops.length-1].time
        }
        return obj
    })
    routes[i] = {
        short_name: routes[i].short_name,
        description: routes[i].description,
        trips: routes[i].trips
    }
}
fs.writeFileSync(folder + '-Output/newRoutes.json', JSON.stringify(routes))