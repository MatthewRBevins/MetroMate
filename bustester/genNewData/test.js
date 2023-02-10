const stops = require('./stops.json');
const fs = require('fs');
fs.writeFileSync('dataDump.json', JSON.stringify(Object.keys(stops)));