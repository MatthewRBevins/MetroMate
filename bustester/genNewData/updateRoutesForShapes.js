const routes = require('./Washington-Output/routes.json');
const shapes = require("./Washington-Output/shapes.json");

for (let i of Object.keys(shapes)) {
    let routeOfShapes = i.split(":")[0]
    if (routes[routeOfShapes].shape_ids == null) {
        routes[routeOfShapes].shape_ids = [i]
    }
    else {
        routes[routeOfShapes].shape_ids.push(i)
    }
}

const fs = require('fs');
fs.writeFileSync('routes.json', JSON.stringify(routes))