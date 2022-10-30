import json


print("writing stops.json")
lines = open("./busdata/stops.txt", "r").read()
lines = lines.split("\n")
lines = lines[1:]
stopIDs = {}
# start writing stops.json and get stop name, latitude, and longitude
for line in lines:
    values = line.split(",")
    stopIDs[values[0]] = {
        "stop_name":values[2][1:-1],
        "latitude":values[4],
        "longitude":values[5],
        "trip_ids":[]
    }
# access all trips
lines = open("./busdata/stop_times.txt", "r").read()
lines = lines.split("\n")
lines = lines[1:]
for line in lines:
    values = line.split(",")
    stopIDs[values[3]]["trip_ids"].append(values[0])
# Serializing json
json_object = json.dumps(stopIDs, indent=4)
# Writing to json
with open("stops.json", "w") as outfile:
    outfile.write(json_object)






# time to start writing trips.json
print("writing trips.json")
tripIDs = {}
lines = open("./busdata/stop_times.txt", "r").read()
lines = lines.split("\n")
lines = lines[1:]
for line in lines:
    values = line.split(",")
    try:
        tripIDs[values[0]].append({"time":values[1], "stop_id":values[3]})
    except KeyError:
        tripIDs[values[0]] = []
# Serializing json
json_object = json.dumps(tripIDs, indent=4)
# Writing to json
with open("trips.json", "w") as outfile:
    outfile.write(json_object)





# ooga booga routes.json
print("writing routes.json")
routeToShape = {}
routeToTrips = {}

lines = open("./busdata/trips.txt", "r").read()
lines = lines.split("\n")[1:]
for line in lines:
    values = line.split(",")
    try:
        routeToShape[values[0]].append(values[7])
        routeToTrips[values[0]].append(values[2])
    except KeyError:
        routeToShape[values[0]] = []
        routeToTrips[values[0]] = []

for line in lines:
    values = line.split(",")
    routeToShape[values[0]] = [*set(routeToShape[values[0]])]
    routeToTrips[values[0]] = [*set(routeToTrips[values[0]])]

routeIDs = {}

lines = open("./busdata/routes.txt", "r").read()
lines = lines.split("\n")[1:]
for line in lines:
    values = line.split(",")

    routeIDs[values[0]] = {
        "short_name":values[2][1:-1],
        #"shape_ids":tripToRoute[values[0]],
        "description":values[4][1:-1],
        "url":values[6],
        "shape_ids":routeToShape[values[0]],
        "trip_ids":routeToTrips[values[0]]
    }

# Serializing json
json_object = json.dumps(routeIDs, indent=4)
# Writing to json
with open("routes.json", "w") as outfile:
    outfile.write(json_object)


# writing shapes.json
print("writing shapes.json")
lines = open("./busdata/shapes.txt", "r").read()
lines = lines.split("\n")[1:]
shapeIDs = {}
for line in lines:
    values = line.split(",")
    try:
        shapeIDs[values[0]].append({"latitude": values[1], "longitude": values[2]})
    except KeyError:
        shapeIDs[values[0]] = []
# Serializing json
json_object = json.dumps(shapeIDs, indent=4)
# Writing to json
with open("shapes.json", "w") as outfile:
    outfile.write(json_object)