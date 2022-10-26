const stops = require('./newerStops.json');
const fs = require('fs');
let nono = [
    '100001', '100009', '100011', '100012', '100013',
    '100016', '100017', '100020', '100044', '100059',
    '100062', '100068', '100071', '100082', '100106',
    '100108', '100109', '100124', '100129', '100132',
    '100148', '100159', '100169', '100177', '100178',
    '100186', '100201', '100223', '100229', '100238',
    '100242', '100246', '100253', '100289', '100337',
    '100341', '100344', '100345', '100346', '100347',
    '100350', '100451', '100459', '100487', '100495',
    '102555', '102572', '102628', '102634', '102653',
    '102698', '102699', '102715', '102727', '102728',
    '102729', '102731'
  ];
let stopsList = Object.keys(stops);
let none = 0;
let has = 0;
let noArr = [];
finalObj = {};
for (let i in stopsList) {
    let routesList = stops[stopsList[i]]["route_ids"];
    if (routesList.length == 0) {
        none++;
        noArr.push(stopsList[i]);
    }
    else {
        finalObj[stopsList[i]] = stops[stopsList[i]];
        has++;
    }
}
console.log(none + " VS " + has);
console.log(noArr);
fs.writeFileSync('evenNewerStops.json',JSON.stringify(finalObj));