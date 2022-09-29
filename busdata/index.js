const stops = require('./stops.json');
const fs = require('fs');
fs.writeFileSync('arr.txt',"\n\n\n" + Object.keys(stops).length);