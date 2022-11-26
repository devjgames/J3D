package org.j3d;

import org.joml.*;

import java.lang.Math;

public class BoundingBox {

    public final Vector3f min = new Vector3f();
    public final Vector3f max = new Vector3f();

    private final Vector2f result = new Vector2f();

    public BoundingBox() {
        clear();
    }

    public BoundingBox(float x1, float y1, float z1, float x2, float y2, float z2) {
        min.set(x1, y1, z1);
        max.set(x2, y2, z2);
    }

    public void clear() {
        min.set(1, 1, 1).mul(Float.MAX_VALUE);
        max.set(1, 1, 1).mul(-Float.MAX_VALUE);
    }

    public boolean isEmpty() {
        return min.x > max.x || min.y > max.y || min.z > max.z;
    }

    public void add(float x, float y, float z) {
        min.x = Math.min(x, min.x);
        min.y = Math.min(y, min.y);
        min.z = Math.min(z, min.z);
        max.x = Math.max(x, max.x);
        max.y = Math.max(y, max.y);
        max.z = Math.max(z, max.z);
    }

    public void add(Vector3f point) {
        add(point.x, point.y, point.z);
    }

    public void add(BoundingBox bounds) {
        if (!bounds.isEmpty()) {
            add(bounds.min);
            add(bounds.max);
        }
    }

    public void transform(Matrix4f m) {
        m.transformAab(min, max, min, max);
    }

    public boolean contains(float x, float y, float z) {
        if (!isEmpty()) {
            return x >= min.x && x <= max.x && y >= min.y && y <= max.y && z >= min.z && z <= max.z;
        }
        return false;
    }

    public boolean contains(Vector3f point) {
        return contains(point.x, point.y, point.z);
    }

    public boolean touches(BoundingBox bounds) {
        if (!isEmpty() && !bounds.isEmpty()) {
            return !(bounds.min.x > max.x || bounds.max.x < min.x || bounds.min.y > max.y || bounds.max.y < min.y
                    || bounds.min.z > max.z || bounds.max.z < min.z);
        }
        return false;
    }

    public boolean intersects(float ox, float oy, float oz, float dx, float dy, float dz, float[] time) {
        boolean hit = false;
        if (Intersectionf.intersectRayAab(ox, oy, oz, dx, dy, dz, min.x, min.y, min.z, max.x, max.y, max.z, result)) {
            if (result.x < 0) {
                if (result.y < time[0]) {
                    time[0] = result.y;
                    hit = true;
                }
            } else if (result.x < time[0]) {
                time[0] = result.x;
                hit = true;
            }
        }
        return hit;
    }

    public boolean intersects(Vector3f origin, Vector3f direction, float[] time) {
        return intersects(origin.x, origin.y, origin.z, direction.x, direction.y, direction.z, time);
    }
}
