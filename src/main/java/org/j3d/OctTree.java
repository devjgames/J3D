package org.j3d;

import java.util.Vector;

public class OctTree {

    public static class Selector implements Collider.TriangleSelector {

        private boolean enabled = true;
        private final OctTree octTree;

        public Selector(OctTree octTree) {
            this.octTree = octTree;
        }

        @Override
        public boolean getEnabled() {
            return enabled;
        }

        @Override
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public boolean intersect(Collider collider) {
            return intersect(octTree, collider);
        }

        private boolean intersect(OctTree tree, Collider collider) {
            boolean hit = false;
            float t = collider.time[0];

            collider.time[0] = Float.MAX_VALUE;
            if(tree.bounds.intersects(collider.origin, collider.direction, collider.time)) {
                collider.time[0] = t;
                for(Triangle triangle : tree.getTriangles()) {
                    if(collider.selectorIntersect(triangle)) {
                        hit = true;
                    }
                }
                for(OctTree child : tree.getChildren()) {
                    if(intersect(child, collider)) {
                        hit = true;
                    }
                }
            } else {
                collider.time[0] = t;
            }
            return hit;
        }

        @Override
        public boolean resolve(Collider collider) {
            return resolve(octTree, collider);
        }

        private boolean resolve(OctTree tree, Collider collider) {
            boolean hit = false;

            if(tree.bounds.touches(collider.resolveBounds)) {
                for(Triangle triangle : tree.getTriangles()) {
                    if(collider.selectorResolve(triangle)) {
                        hit = true;
                    }
                }
                for(OctTree child : tree.getChildren()) {
                    if(resolve(child, collider)) {
                        hit = true;
                    }
                }
            }
            return hit;
        }
    }

    public static OctTree create(Vector<Triangle> triangles, int minTrisPerTree) {
        Triangle[] tris = triangles.toArray(new Triangle[triangles.size()]);
        BoundingBox bounds = new BoundingBox();

        for (Triangle tri : tris) {
            bounds.add(tri.p1);
            bounds.add(tri.p2);
            bounds.add(tri.p3);
        }
        bounds.min.sub(1, 1, 1);
        bounds.max.add(1, 1, 1);

        return new OctTree(tris, bounds, minTrisPerTree);
    }

    private Vector<OctTree> children = new Vector<>();
    private Triangle[] triangles;
    private BoundingBox bounds;

    public OctTree(Triangle[] triangles, BoundingBox bounds, int minTrisPerTree) {
        this.bounds = bounds;
        if (triangles.length > minTrisPerTree) {
            float lx = bounds.min.x;
            float ly = bounds.min.y;
            float lz = bounds.min.z;
            float hx = bounds.max.x;
            float hy = bounds.max.y;
            float hz = bounds.max.z;
            float cx = (hx + lx) / 2;
            float cy = (hy + ly) / 2;
            float cz = (hz + lz) / 2;
            BoundingBox[] bList = new BoundingBox[] { 
                new BoundingBox(lx, ly, lz, cx, cy, cz),
                new BoundingBox(cx, ly, lz, hx, cy, cz), 
                new BoundingBox(lx, cy, lz, cx, hy, cz),
                new BoundingBox(lx, ly, cz, cx, cy, hz), 
                new BoundingBox(cx, cy, cz, hx, hy, hz),
                new BoundingBox(cx, cy, lz, hx, hy, cz), 
                new BoundingBox(cx, ly, cz, hx, cy, hz),
                new BoundingBox(lx, cy, cz, cx, hy, hz) 
            };
            Vector<Vector<Triangle>> triLists = new Vector<>();
            Vector<Triangle> keep = new Vector<>();
            triLists.add(new Vector<>());
            triLists.add(new Vector<>());
            triLists.add(new Vector<>());
            triLists.add(new Vector<>());
            triLists.add(new Vector<>());
            triLists.add(new Vector<>());
            triLists.add(new Vector<>());
            triLists.add(new Vector<>());
            for (Triangle triangle : triangles) {
                boolean added = false;

                for (int i = 0; i != bList.length; i++) {
                    BoundingBox b = bList[i];

                    if (b.contains(triangle.p1) && b.contains(triangle.p2) && b.contains(triangle.p3)) {
                        added = true;
                        triLists.get(i).add(triangle);
                        break;
                    }
                }
                if (!added) {
                    keep.add(triangle);
                }
            }
            for (int i = 0; i != triLists.size(); i++) {
                if (triLists.get(i).size() != 0) {
                    children.add(new OctTree(triLists.get(i).toArray(new Triangle[triLists.get(i).size()]), bList[i],
                            minTrisPerTree));
                }
            }
            this.triangles = keep.toArray(new Triangle[keep.size()]);
        } else {
            this.triangles = triangles;
        }
    }

    public Vector<OctTree> getChildren() {
        return children;
    }

    public Triangle[] getTriangles() {
        return triangles;
    }

    public BoundingBox getBounds() {
        return bounds;
    }
}