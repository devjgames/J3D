package org.j3d;

public abstract class Collidable {

    public boolean enabled = true;

    public abstract Object getObject();

    public abstract boolean intersects(Camera camera, Vec3 origin, Vec3 direction, float buffer, float[] time);

    public abstract void traverse(Camera camera, AABB bounds, TriangleVisitor visitor);
}
