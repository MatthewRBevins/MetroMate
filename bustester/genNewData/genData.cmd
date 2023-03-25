@echo off
echo CREATE SHAPES
node createShapes.js
echo CREATE STOPS
node createStops.js
echo CREATE GEOGUESSR
node createGeoguessrMap.js
echo CREATE FULL REGIONS
node createFullRegions.js
echo CREATE TRIPS
node createTrips.js
echo CREATE NEW TRIPS
node createNewTrips.js
echo CREATE ROUTES
node createRoutes.js
echo CREATE NEW ROUTES
node createNewRoutes.js
echo CREATE REGIONS
node createNewRegions.js
echo DONE