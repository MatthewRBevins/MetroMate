//NEXT THING TO DO:








const routes = require('./busdata/routes.json');
const shapes = require('./busdata/shapes.json');
const stops = require('./busdata/stops.json');
const trips = require('./busdata/trips.json');
const newRegions = require('./newRegions.json');
const newRoutes = require('./newRoutes.json');
const regions = require('./fullRegions.json').regions;
const fs = require('fs');
const prompt = require('prompt-sync')();
const regionSide = 67;

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

function calcDistance(x1,y1,x2,y2) {
    return Math.sqrt((x1-x2)**2 + (y1-y2)**2)
}

function checkRegionDistance(region1, region2) {
    return calcDistance(Math.ceil(region1/regionSide), region1 - (regionSide*Math.floor(region1/regionSide)), Math.ceil(region2/regionSide), region2 - (regionSide*Math.floor(region2/regionSide)))
}

function checkRegion(lat, lng) {
    let j = 0;
    for (let i of regions) {
      if (lat >= i[0].lat && lat <= i[2].lat && lng >= i[0].lng && lng <= i[2].lng) return j;
      j++;
    }
    return -1;
}


function isInTimeFrame(time, start, end) {
    return (parseInt(start.substr(0,2)) == parseInt(time.substr(0,2))) 
}

//EASTGATE = (47.580883, -122.152551)
//LAKESIDE = (47.732595, -122.327477)

function getPossibleRegions(time, startingRegion) {
    let arr = [];
    //Loop through all of the routes that go through starting regions
    for (let i of newRegions[startingRegion].routes) {
        //Loop through every trip of current route
        for (let j of newRoutes[i].trips) {
            //If the current trip takes place at the current time
            if (isInTimeFrame(time, j.times.from, j.times.start)) {
                //Loop through all regions that the trip goes to starting from the current region
                for (let k = j.regions.indexOf(startingRegion); k < j.regions.length; k++) {
                    if (! arr.includes(j.regions[k]) && j.regions[k] != null) {
                        arr.push({
                            route: i,
                            time: "00:00:00",
                            region: j.regions[k]
                        });
                    }
                }
            }
        }
    }
    return arr;
}

let pos1 = new LatLng([47.732017, -122.326533])//new LatLng(prompt('Enter position: ').replaceAll("(","").replaceAll(")","").split(","));
let pos2 = new LatLng([47.407566, -122.254934])//new LatLng(prompt('Enter to go: ').replaceAll("(","").replaceAll(")","").split(","))
let region1 = pos1.checkRegion()
let region2 = pos2.checkRegion()
let time = "12:00:00"
let r = getPossibleRegions(time, region1);

let low = Infinity;
let l = 'hi';
for (let i of r) {
    let cd = checkRegionDistance(i.region, region2);
    if (cd < low) {
        low = cd;
        l = i.region;
    }
    for (let j of getPossibleRegions("15:00:00", i.region)) {
        let cd = checkRegionDistance(j.region, region2);
        if (cd < low) {
            low = cd;
            l = j.region;
        }
    }
}
console.log(low);
console.log(l);