package org.j3d;

public final class Triangle {

    private static final Vec3 e1 = new Vec3();
    private static final Vec3 e2 = new Vec3();
    private static final Vec3 n2 = new Vec3();
    private static final Vec3 ip = new Vec3();
    private static final Vec3 c = new Vec3();
    private static final Vec3 ab = new Vec3();
    private static final Vec3 ap = new Vec3();

    public final Vec3 p1 = new Vec3();
    public final Vec3 p2 = new Vec3();
    public final Vec3 p3 = new Vec3();
    public final Vec3 n = new Vec3();
    public float d;
    public int tag = 0;
    public Object data = null;

    public Triangle() {
        p1.set(0, 0, 0);
        p2.set(1, 0, 0);
        p3.set(0, 1, 0);
        calcPlane();
    }

    public Triangle(Vec3 p1, Vec3 p2, Vec3 p3) {
        set(p1, p2, p3);
    }

    public Node getNode() {
        if(data instanceof Node) {
            return (Node)data;
        }
        return null;
    }

    public Triangle(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3) {
        set(x1, y1, z1, x2, y2, z2, x3, y3, z3);
    }

    public Triangle set(Vec3 p1, Vec3 p2, Vec3 p3) {
        this.p1.set(p1);
        this.p2.set(p2);
        this.p3.set(p3);
        calcPlane();
        return this;
    }

    public Triangle set(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3) {
        p1.set(x1, y1, z1);
        p2.set(x2, y2, z2);
        p3.set(x3, y3, z3);
        calcPlane();
        return this;
    }

    public Triangle set(Triangle triangle) {
        p1.set(triangle.p1);
        p2.set(triangle.p2);
        p3.set(triangle.p3);
        n.set(triangle.n);
        d = triangle.d;
        tag = triangle.tag;
        data = triangle.data;
        return this;
    }

    public Triangle setTag(int tag) {
        this.tag = tag;
        return this;
    }

    public Triangle setData(Object data) {
        this.data = data;
        return this;
    }

    public Triangle calcPlane() {
        p2.sub(p1, e1).normalize();
        p3.sub(p2, e2).normalize();
        e1.cross(e2, n).normalize();
        d = -n.dot(p1);
        return this;
    }

    public Triangle transform(Mat4 matrix) {
        p1.transform(matrix, p1);
        p2.transform(matrix, p2);
        p3.transform(matrix, p3);
        calcPlane();
        return this;
    }

    public Vec3 pointAt(int i) {
        if(i == 1) {
            return p2;
        } else if(i == 2) {
            return p3;
        } else {
            return p1;
        }
    }

    public boolean contains(Vec3 point, float buffer) {
        for(int i = 0; i != 3; i++) {
            Vec3 p1 = pointAt(i);
            Vec3 p2 = pointAt(i + 1);

            p2.sub(p1, e1).normalize();
            e1.cross(n, n2).normalize();
            e2.set(p1);
            if(Math.abs(buffer) > 0.0000001) {
                n2.scale(buffer);
                e2.add(n2);
                n2.normalize();
            }

            float d2 = -n2.dot(e2);
            float s = n2.dot(point) + d2;

            if(s > 0) {
                return false;
            }
        }
        return true;
    }

    public boolean intersectsPlane(Vec3 origin, Vec3 direction, float[] time) {
        float t = direction.dot(n);
        if(Math.abs(t) > 0.0000001) {
            t = (-d - origin.dot(n)) / t;
            if(t >= 0 && t < time[0]) {
                time[0] = t;
                return true;
            }
        }
        return false;
    }

    public boolean intersects(Vec3 origin, Vec3 direction, float buffer, float[] time) {
        float t = time[0];

        if(intersectsPlane(origin, direction, time)) {
            ip.set(direction);
            ip.scale(time[0]);
            origin.add(ip, ip);
            if(contains(ip, buffer)) {
                return true;
            }
            time[0] = t;
        }
        return false;
    }

    public Vec3 closestEdgePoint(Vec3 point, Vec3 closestPoint) {
        float min = Float.MAX_VALUE;

        closestPoint.set(pointAt(0));
        for(int i = 0; i != 3; i++) {
            Vec3 a = pointAt(i);
            Vec3 b = pointAt(i + 1);

            b.sub(a, ab);
            point.sub(a, ap);

            float s = ab.dot(ap);

            c.set(a);
            if(s > 0) {
                s /= ab.lengthSquared();
                if(s < 1) {
                    c.set(ab).scale(s).add(a);
                } else {
                    c.set(b);
                }
            }
            float dist = point.distance(c);
            if(dist < min) {
                min = dist;
                closestPoint.set(c);
            }
        }
        return closestPoint;
    }
}
