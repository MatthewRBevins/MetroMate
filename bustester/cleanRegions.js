const testRegions = require('./testRegions.json');
const fs = require('fs');
var obj = {};
for (let i of Object.keys(testRegions)) {
    if (! testRegions[i].routes.includes("100016") || ! testRegions[i].routes.includes("100017")) {
        obj[i] = testRegions[i];
    }
}
fs.writeFileSync('cleanRegions.json', JSON.stringify(obj));