# mesh-set name
mesh-set cave

# player x y z off-x off-y off-z scale speed radius
player 0 32 0 20 200 100 20 100 16

# scale s
scale 16

# lighting-enabled bool
lighting-enabled false

# light directional x y z r g b a radius
# ...

# background r g b a
background 0 0 0 1

# mesh-cfg name collidable
# ...

# mesh name x y z rotation-degrees
# ...
mesh flat1 0 0 0 0
mesh side -32 0 0 0
mesh side 32 0 0 180
mesh side-edge-l -32 0 32 -90
mesh side-edge-r 32 0 32 -90
mesh side-edge-r -32 0 -32 90
mesh side-edge-l 32 0 -32 90
mesh flat-edge 0 0 32 -90
mesh flat-edge 0 0 -32 90

