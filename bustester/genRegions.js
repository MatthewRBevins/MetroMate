const stops = require('./busdata/stops');
let extremeties = [47.8710632, 47.1891174, -121.785835, -122.506622];
let latDiff = extremeties[0] - extremeties[1];
let longDiff = extremeties[2] - extremeties[3];
let regions = 25;
let latB = latDiff / Math.sqrt(regions);
let longB = longDiff / Math.sqrt(regions);

let currentLat = extremeties[1];
let currentLong = extremeties[3];

let polygons = [];
for (let i = 0; i < Math.sqrt(regions); i++) {
    for (let j = 0; j < Math.sqrt(regions); j++) {
        polygons.push([{lat: currentLat, lng: currentLong},{lat: currentLat, lng: currentLong + longB},{lat: currentLat + latB, lng: currentLong + longB},{lat: currentLat + latB, lng: currentLong}]);
        currentLat += latB;
    }
    currentLat = extremeties[1];
    currentLong += longB;
}

console.log(polygons);