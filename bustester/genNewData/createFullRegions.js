const fs = require('fs')
const prompt = require('prompt-sync')()
let folder = prompt('Folder: ')
const stops = require('./' + folder + '-Output/stops.json');
let arr = [];
let lowLat = Infinity
let lowLng = Infinity
let highLat = -Infinity
let highLng = -Infinity
for (let i of Object.keys(stops)) {
    let lat = parseFloat(stops[i].latitude)
    let lng = parseFloat(stops[i].longitude)
    if (lat < lowLat) {
        lowLat = lat
    }
    if (lat > highLat) {
        highLat = lat
    }
    if (lng < lowLng) {
        lowLng = lng
    }
    if (lng > highLng) {
        highLng = lng
    }
    arr.push({
        lat: lat,
        lng: lng
    })
}
const rowLen = 67
const latDiv = (highLat-lowLat)/rowLen
const lngDiv = (highLng-lowLng)/rowLen
let curLat = lowLat
let curLng = lowLng
let lastLat = curLat
let lastLng = curLng
let regions = []
for (let i = 0; i < rowLen; i++) {
    curLng = lowLng
    curLat += latDiv
    for (let j = 0; j < rowLen; j++) {
        curLng += lngDiv
        if (j != 0) {
            regions.push([
                {"lat": lastLat, "lng": lastLng},
                {"lat": lastLat, "lng": curLng},
                {"lat": curLat, "lng": curLng},
                {"lat": curLat, "lng": lastLng},
            ])
        }
        else {
            regions.push([
                {"lat": lastLat, "lng": curLng-lngDiv},
                {"lat": lastLat, "lng": curLng},
                {"lat": curLat, "lng": curLng},
                {"lat": curLat, "lng": curLng-lngDiv},
            ])
        }
        lastLng = curLng
    }
    lastLat = curLat
}
fs.writeFileSync(folder + '-Output/fullRegions.json', JSON.stringify({"regions": regions}))