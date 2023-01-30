const testRegions = require('./testRegions.json');
const testRoutes = require('./testRoutes.json');
const routes = require('./busdata/routes.json');
const trips = require('./busdata/trips.json');
var obj = {};
for (let i of Object.keys(testRegions)) {
    obj[i] = {
        "short_name":testRoutes[i].short_name,
        "description":testRoutes[i].description,
        "url":testRoutes[i].url,
        "shape_ids": testRoutes[i].shape_ids
    }
}