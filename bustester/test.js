const testRegions = require('./testRegions.json');
const testRoutes = require('./testRoutes.json');
const fs = require('fs');
for (let i of Object.keys(testRegions)) {
    testRegions[i]["toRegions"] = [];
    for (let j of testRegions[i].routes) {
        for (let k of Object.keys(testRoutes[j].regions)) {
            if (! testRegions[i]["toRegions"].includes(k)) {
                testRegions[i]["toRegions"].push(k);
            }
        }
    }
}

fs.writeFileSync('testRegions.json', JSON.stringify(testRegions));