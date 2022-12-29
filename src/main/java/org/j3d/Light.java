package org.j3d;

import org.joml.Vector3f;

public class Light {
    
    public final Vector3f position = new Vector3f();
    public final Vector3f color = new Vector3f(1, 1, 1);
    public float radius = 100;

    private BoundingBox bounds = new BoundingBox();

    public BoundingBox calcBounds() {
        bounds.min.set(position).sub(radius, radius, radius);
        bounds.max.set(position).add(radius, radius, radius);

        return bounds;
    }
}
