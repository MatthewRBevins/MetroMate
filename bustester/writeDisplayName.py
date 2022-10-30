import json

print("writing displayNameToRouteID.json")
tripIDs = {}
lines = open("./busdata/routes.txt", "r").read()
lines = lines.split("\n")
lines = lines[1:]
for line in lines:
    values = line.split(",")
    tripIDs[values[2][1:-1]] = values[0]

json_object = json.dumps(tripIDs, indent=4)
# Writing to json
with open("displayNameToRouteID.json", "w") as outfile:
    outfile.write(json_object)