package org.j3d;

public class NodeCollidable extends Collidable {

    public final Node node;

    private Triangle[] triangles = null;
    private Triangle triangle = new Triangle();
    private AABB rayBounds = new AABB();
    private Vec3 point = new Vec3();

    public NodeCollidable(Node node) {
        this.node = node;
    }

    public void cacheTriangles(Camera camera) {
        triangles = new Triangle[node.triangleCount()];
        for(int i = 0; i != node.triangleCount(); i++) {
            Triangle triangle = new Triangle();
            node.getTriangle(camera, i, triangle);
            triangles[i] = triangle;
        }
    }

    @Override
    public Object getObject() {
        return node;
    }

    @Override
    public boolean intersects(Camera camera, Vec3 origin, Vec3 direction, float buffer, float[] time) {
        boolean hit = false;
        rayBounds.clear();
        rayBounds.add(origin);
        rayBounds.add(point.set(direction).scale(time[0]).add(origin));
        if(node.bounds.touches(rayBounds)) {
            if(triangles != null) {
                for(Triangle triangle : triangles) {
                    if(triangle.intersects(origin, direction, buffer, time)) {
                        hit = true;
                    }
                }
            } else {
                for(int i = 0; i != node.triangleCount(); i++) {
                    if(node.getTriangle(camera, i, triangle).intersects(origin, direction, buffer, time)) {
                        hit = true;
                    }
                }
            }
        }
        return hit;
    }

    @Override
    public void traverse(Camera camera, AABB bounds, TriangleVisitor visitor) {     
        if(node.bounds.touches(bounds)) {
            if(triangles != null) {
                for(Triangle triangle : triangles) {
                    visitor.visit(triangle, this);
                }
            } else {
                for(int i = 0; i != node.triangleCount(); i++) {
                    visitor.visit(node.getTriangle(camera, i, triangle), this);
                }
            }
        }   
    }
}
