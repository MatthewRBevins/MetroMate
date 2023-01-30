const routes = require('./busdata/routes.json');
const shapes = require('./busdata/shapes.json');
const stops = require('./busdata/stops.json');
const trips = require('./busdata/trips.json');
const testRegions = require('./testRegions.json');
const testRoutes = require('./testRoutes.json');
const regions = require('./fullRegions.json').regions;
const fs = require('fs');
const prompt = require('prompt-sync')();

class LatLng {
    constructor(arr) {
        this.lat = parseFloat(arr[0]);
        this.lng = parseFloat(arr[1]);
        this.checkRegion = () => {
            let j = 0;
            for (let i of regions) {
                if (this.lat >= i[0].lat && this.lat <= i[2].lat && this.lng >= i[0].lng && this.lng <= i[2].lng) return j;
                j++;
            }
            return -1;
        }
    }
}

function calcDistance(pos1, pos2) {
    return Math.sqrt((pos1.lat-pos2.lat)**2 + (pos1.lng-pos2.lng)**2)
}

function checkRegion(lat, lng) {
    let j = 0;
    for (let i of regions) {
      if (lat >= i[0].lat && lat <= i[2].lat && lng >= i[0].lng && lng <= i[2].lng) return j;
      j++;
    }
    return -1;
}



//EASTGATE = (47.580883, -122.152551)
//LAKESIDE = (47.732595, -122.327477)

let pos1 = new LatLng([47.580883, -122.152551])//new LatLng(prompt('Enter position: ').replaceAll("(","").replaceAll(")","").split(","));
let pos2 = new LatLng([47.732595, -122.327477])//new LatLng(prompt('Enter to go: ').replaceAll("(","").replaceAll(")","").split(","))
let region1 = pos1.checkRegion().toString();
let region2 = pos2.checkRegion().toString();

function getRoute(region1, region2, depth, arr) {
    if (depth > 2) {
        return [Infinity, arr];
    }
    if (testRegions[region1].toRegions.includes(region2)) {
        return [depth, arr];
    }
    let bestDepth = Infinity;
    let best = arr;
    let cur = arr.slice();
    for (let i of testRegions[region1].toRegions) {
        cur.push(i);
        let thisTry = getRoute(i, region2, depth+1, cur);
        if (thisTry < bestDepth) {
            bestDepth = thisTry[0];
            best = thisTry[1];
        }
        cur = arr.slice();
    }
    return [bestDepth, best];
}
console.log(getRoute("14","647",0,[]))