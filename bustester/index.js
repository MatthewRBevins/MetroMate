//NEXT THING TO DO:








const routes = require('./busdata/routes.json');
const shapes = require('./busdata/shapes.json');
const stops = require('./busdata/stops.json');
const trips = require('./busdata/trips.json');
const newRegions = require('./newRegions.json');
const newRoutes = require('./newRoutes.json');
const newTrips = require('./newTrips.json');
const regions = require('./fullRegions.json').regions;
const fs = require('fs');
const prompt = require('prompt-sync')();
const regionSide = 67;

function getClosestRegions(region) {
    //bottom, top, left, right
    let boundary = [region % 67 == 0, (region+1) % 67 == 0, region < 68, region > (regions.length-67)];
    let closest = [[region-1, [0]],[region+1,[1]],[region-67,[2]],[region+67,[3]],[ (region-1)+67,[0,3]],[ (region+1)+67,[1,3]],[ (region-1)-67,[0,2]],[ (region+1)-67,[1,2]]];
    return closest.filter(val => {
        for (let i of val[1]) {
            if (boundary[i]) return false;
        }
        return true;
    }).map(val => val[0].toString());
}

function getFormattedTime() {
    let d = new Date();
    return d.getHours() + ":" + d.getMinutes() + ":" + d.getSeconds();
}

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
    return true;//(parseInt(start.substr(0,2)) == parseInt(time.substr(0,2)) || parseInt(start.substr(0,2)) == parseInt(time.substr(0,2)-1)) 
}

//EASTGATE = (47.580883, -122.152551)
//LAKESIDE = (47.732595, -122.327477)

function arrIncludesKey(arr, key, val) {
    for (let i of arr) {
        if (i[key] == val) {
            return true;
        }
    }
    return false;
}

function getPossibleRegions(time, startingRegion, closestRegions) {
    let arr = [];
    let regionsToCheck = [startingRegion.toString()];
    if (closestRegions) {
        regionsToCheck = regionsToCheck.concat(getClosestRegions(parseInt(startingRegion)))
    }
    for (let startingRegion of regionsToCheck) {
        //Loop through all of the routes that go through starting regions
        if (newRegions[startingRegion] != null) {
            for (let i of newRegions[startingRegion].routes) {
                //Loop through every trip of current route
                for (let j of newRoutes[i].trips) {
                    //If the current trip takes place at the current time
                    if (isInTimeFrame(time, j.times.from, j.times.start)) {
                        let regionsArr = newTrips[j.id].regions
                        if (regionsArr.includes(parseInt(startingRegion))) {
                            console.log('jiowaefoi')
                            //let startTime = newTrips[j.id][startingRegion][0].time;
                            //Loop through all regions that the trip goes to starting from the current region
                            for (let k = regionsArr.indexOf(startingRegion); k < regionsArr.length; k++) {
                                if (! arrIncludesKey(arr, "region", regionsArr[k]) && regionsArr[k] != null) {
                                    arr.push({
                                        //TIME BUS REACHES REGION
                                        startTime: 'idkyet',//startTime,
                                        route: i,
                                        trip: j.id,
                                        time: "idkyet",
                                        region: regionsArr[k]
                                    });
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    return arr;
}

let pos1 = new LatLng([47.580814, -122.153245])//new LatLng(prompt('Enter position: ').replaceAll("(","").replaceAll(")","").split(","));
let pos2 = new LatLng([47.732234, -122.328510])//new LatLng(prompt('Enter to go: ').replaceAll("(","").replaceAll(")","").split(","))
let region1 = pos1.checkRegion() //1125
let region2 = pos2.checkRegion()
let time = getFormattedTime();
console.log(region1 + " TO " + region2);
let r = getPossibleRegions(time, region1, false);
let rs = [];
let low = Infinity;
let path = [];
let bestPath = [];
for (let i of r) {
    //path = [i];
    /*if (checkRegionDistance(i.region,region2) < low) {
        low = checkRegionDistance(i.region,region2);
        //bestPath = path;
    }*/
    if (! rs.includes(i.region)) {
        rs.push(i.region);
    }
    //for (let j of getPossibleRegions(time, i.region, true)) {
        /*path.push(j);
        if (checkRegionDistance(j.region,region2) < low) {
            bestPath = path;
            low = checkRegionDistance(j.region,region2);
        }
        path.pop();*/
        //if (! rs.includes(j.region)) {
        //    rs.push(j.region);
        //}
    //}
}
fs.writeFileSync('dataDump.json', JSON.stringify(rs));