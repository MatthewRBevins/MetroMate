lines = open("./busdata/shapes.txt", "r").read()
lines = lines.split("\n")[1:]
shapeIDs = []
for line in lines:
    values = line.split(",")
    shapeIDs.append(values[0])

print(len(shapeIDs))

res = [*set(shapeIDs)]
print(len(res))