package org.j3d;

import java.util.Vector;

public class OctTreeCollidable extends Collidable {

    private OctTree tree;
    private Triangle triangle = new Triangle();
    private AABB rayBounds = new AABB();
    private Vec3 point = new Vec3();

    public OctTreeCollidable(Vector<Triangle> triangles, int minTrisPerTree) {
        tree = OctTree.create(triangles, minTrisPerTree);
    }

    @Override
    public Object getObject() {
        return null;
    }

    @Override
    public boolean intersects(Camera camera, Vec3 origin, Vec3 direction, float buffer, float[] time) {
        return intersects(tree, camera, origin, direction, buffer, time);
    }

    @Override
    public void traverse(Camera camera, AABB bounds, TriangleVisitor visitor) {
        traverse(tree, camera, bounds, visitor);
    }
    
    private boolean intersects(OctTree tree, Camera camera, Vec3 origin, Vec3 direction, float buffer, float[] time) {
        boolean hit = false;
        rayBounds.clear();
        rayBounds.add(origin);
        rayBounds.add(point.set(direction).scale(time[0]).add(origin));
        if(tree.bounds.touches(rayBounds)) {
            for(int i = 0; i != tree.triangleCount(); i++) {
                if(tree.getTriangle(i, triangle).intersects(origin, direction, buffer, time)) {
                    hit = true;
                }
            }
            for(OctTree child : tree) {
                boolean r = intersects(child, camera, origin, direction, buffer, time);
                if(r) {
                    hit = true;
                }
            }
        }
        return hit;
    }

    private void traverse(OctTree tree, Camera camera, AABB bounds, TriangleVisitor visitor) {
        if(tree.bounds.touches(bounds)) {
            for(int i = 0; i != tree.triangleCount(); i++) {
                visitor.visit(tree.getTriangle(i, triangle), this);
            }
            for(OctTree child : tree) {
                traverse(child, camera, bounds, visitor);
            }
        }
    }
}
