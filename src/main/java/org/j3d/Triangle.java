package org.j3d;

import org.joml.GeometryUtils;
import org.joml.Intersectionf;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Triangle {

    public final Vector3f p1 = new Vector3f();
    public final Vector3f p2 = new Vector3f();
    public final Vector3f p3 = new Vector3f();
    public final Vector3f n = new Vector3f();
    public float d = 0;
    public int tag = 1;
    public Object data = null;

    private final Vector3f v1 = new Vector3f();
    private final Vector3f n1 = new Vector3f();
    private final Vector3f pt = new Vector3f();
    private final Vector3f v2 = new Vector3f();

    public void set(Triangle triangle) {
        p1.set(triangle.p1);
        p2.set(triangle.p2);
        p3.set(triangle.p3);
        n.set(triangle.n);
        d = triangle.d;
        tag = triangle.tag;
        data = triangle.data;
    }

    public Vector3f pointAt(int i) {
        if (i == 1) {
            return p2;
        } else if (i == 2) {
            return p3;
        } else {
            return p1;
        }
    }

    public void calcPlane() {
        GeometryUtils.normal(p1, p2, p3, n);
        d = -p1.dot(n);
    }

    public void transform(Matrix4f m) {
        p1.mulPosition(m);
        p2.mulPosition(m);
        p3.mulPosition(m);
        calcPlane();
    }

    public boolean contains(Vector3f point, float buffer) {
        for (int i = 0; i != 3; i++) {
            Vector3f a = pointAt(i);
            Vector3f b = pointAt(i + 1);
            b.sub(a, v1);
            v1.cross(n, n1).normalize();
            pt.set(n1).mul(buffer);
            pt.add(a);
            float d1 = -pt.dot(n1);
            if (point.dot(n1) + d1 > 0) {
                return false;
            }
        }
        return true;
    }

    public boolean intersectsPlane(Vector3f origin, Vector3f direction, float[] time) {
        float t = direction.dot(n);
        if (Math.abs(t) > 0.0000001) {
            t = (-d - origin.dot(n)) / t;
            if (t >= 0 && t < time[0]) {
                time[0] = t;
                return true;
            }
        }
        return false;
    }

    public boolean intersects(Vector3f origin, Vector3f direction, float buffer, float[] time) {
        float t = time[0];
        if (intersectsPlane(origin, direction, time)) {
            v2.set(direction).mul(time[0]).add(origin);
            if (contains(v2, buffer)) {
                return true;
            }
            time[0] = t;
        }
        return false;
    }

    public int closestEdgePoint(Vector3f point, Vector3f closest) {
        return Intersectionf.findClosestPointOnTriangle(p1, p2, p3, point, closest);
    }

    private static final Vector3f origin = new Vector3f();
    private static final Vector3f direction = new Vector3f();

    public boolean resolve(Vector3f resolvedPosition, float radius, Vector3f position, Vector3f normal, float[] time) {
        if (Float.isNaN(d)) {
            return false;
        }
        float t = time[0];
        n.negate(direction);
        origin.set(position);
        if (intersectsPlane(origin, direction, time)) {
            direction.mul(time[0]).add(origin, origin);
            if (contains(origin, 0)) {
                direction.set(n).mul(radius).add(origin, resolvedPosition);
                normal.set(n);
                return true;
            } else {
                closestEdgePoint(position, origin);
                position.sub(origin, direction);
                time[0] = t;
                if (direction.length() > 0.0000001 && direction.length() < time[0]) {
                    time[0] = direction.length();
                    direction.normalize().mul(radius).add(origin, resolvedPosition);
                    direction.normalize(normal);
                    return true;
                }
            }
        }
        return false;
    }
}
