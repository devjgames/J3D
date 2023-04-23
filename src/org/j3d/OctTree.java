package org.j3d;

import java.util.Iterator;
import java.util.Vector;

public class OctTree implements Iterable<OctTree> {

    public final AABB bounds = new AABB();
    
    private Vector<OctTree> children = new Vector<>();
    private Vector<Triangle> triangles = new Vector<>();

    private OctTree(Vector<Triangle> triangles, AABB bounds, int minTrisPerTree) {
        this.bounds.set(bounds);
        if(triangles.size() >= minTrisPerTree) {
            Vec3 l = bounds.min;
            Vec3 h = bounds.max;
            Vec3 c = new Vec3();
            bounds.center(c);
            AABB[] ba = new AABB[] {
                new AABB(l.x, l.y, l.z, c.x, c.y, c.z),
                new AABB(c.x, l.y, l.z, h.x, c.y, c.z),
                new AABB(l.x, c.y, l.z, c.x, h.y, c.z), 
                new AABB(l.x, l.y, c.z, c.x, c.y, h.z),
                new AABB(c.x, c.y, c.z, h.x, h.y, h.z),
                new AABB(c.x, c.y, l.z, h.x, h.y, c.z), 
                new AABB(l.x, c.y, c.z, c.x, h.y, h.z),
                new AABB(c.x, l.y, c.z, h.x, c.y, h.z)
            };
            Vector<Vector<Triangle>> subTris = new Vector<>();
            Vector<Triangle> left = new Vector<>(triangles.size());

            for(int i = 0; i != ba.length; i++) {
                Vector<Triangle> tris = new Vector<>(triangles.size());
                AABB b = ba[i];

                for(Triangle triangle : triangles) {
                    if(b.contains(triangle.p1) && b.contains(triangle.p2) && b.contains(triangle.p3)) {
                        tris.add(triangle);
                    } else {
                        left.add(triangle);
                    }
                }
                Vector<Triangle> temp = triangles;
                triangles = left;
                left = temp;
                left.removeAllElements();
                subTris.add(tris);
            }
            for(int i = 0; i != ba.length; i++) {
                Vector<Triangle> tris = subTris.get(i);
                if(tris.size() != 0) {
                    children.add(new OctTree(tris, ba[i], minTrisPerTree));
                }
            }
        }
        this.triangles.addAll(triangles);
    }

    public int count() {
        return children.size();
    }

    public OctTree childAt(int i) {
        return children.get(i);
    }

    public int triangleCount() {
        return triangles.size();
    }

    public Triangle getTriangle(int i, Triangle triangle) {
        Triangle tri = triangles.get(i);

        triangle.set(tri.p1, tri.p2, tri.p3);
        triangle.tag = tri.tag;

        return triangle;
    }

    @Override
    public Iterator<OctTree> iterator() {
        return children.iterator();
    }

    public static OctTree create(Vector<Triangle> triangles, int minTrisPerTree) {
        AABB bounds = new AABB();

        for(Triangle triangle : triangles) {
            bounds.add(triangle.p1);
            bounds.add(triangle.p2);
            bounds.add(triangle.p3);
        }
        bounds.min.sub(1, 1, 1);
        bounds.max.add(1, 1, 1);

        return new OctTree(triangles, bounds, minTrisPerTree);
    }
}
