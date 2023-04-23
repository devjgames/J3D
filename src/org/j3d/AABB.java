package org.j3d;

public class AABB {
    
    private static final AABB temp = new AABB();

    public final Vec3 min = new Vec3(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
    public final Vec3 max = new Vec3(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE).neg();

    public AABB() {
    }

    public AABB(float x1, float y1, float z1, float x2, float y2, float z2) {
        set(x1, y1, z1, x2, y2, z2);
    }

    public AABB(Vec3 min, Vec3 max) {
        set(min, max);
    }

    public AABB(AABB b) {
        set(b);
    }

    public boolean isEmpty() {
        return min.x > max.x || min.y > max.y || min.z > max.z;
    }

    public void center(Vec3 center) {
        max.add(min, center).scale(0.5f);
    }

    public void size(Vec3 size) {
        max.sub(min, size);
    }

    public AABB clear() {
        min.set(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        max.neg(min);
        return this;
    }

    public AABB set(float x1, float y1, float z1, float x2, float y2, float z2) {
        min.set(x1, y1, z1);
        max.set(x2, y2, z2);
        return this;
    }

    public AABB set(Vec3 min, Vec3 max) {
        this.min.set(min);
        this.max.set(max);
        return this;
    }

    public AABB set(AABB b) {
        min.set(b.min);
        max.set(b.max);
        return this;
    }

    public AABB add(float x, float y, float z) {
        min.x = Math.min(x, min.x);
        min.y = Math.min(y, min.y);
        min.z = Math.min(z, min.z);
        max.x = Math.max(x, max.x);
        max.y = Math.max(y, max.y);
        max.z = Math.max(z, max.z);
        return this;
    }

    public AABB add(Vec3 p) {
        return add(p.x, p.y, p.z);
    }

    public AABB add(float x1, float y1, float z1, float x2, float y2, float z2) {
        temp.set(x1, y1, z1, x2, y2, z2);
        return add(temp);
    }

    public AABB add(AABB b) {
        if(!b.isEmpty()) {
            add(b.min);
            add(b.max);
        }
        return this;
    }

    public boolean contains(Vec3 p) {
        return p.x >= min.x && p.x <= max.x && p.y >= min.y && p.y <= max.y && p.z >= min.z && p.z <= max.z;
    }

    public boolean touches(AABB b) {
        if(!isEmpty() && !b.isEmpty()) {
            return !(
                b.min.x > max.x || b.max.x < min.x ||
                b.min.y > max.y || b.max.y < min.y ||
                b.min.z > max.z || b.max.z < min.z
            );
        }
        return false;
    }

    public AABB transform(Mat4 m) {
        if(!isEmpty()) {
            float x1 = m.m03;
            float y1 = m.m13;
            float z1 = m.m23;
            float x2 = x1;
            float y2 = y1;
            float z2 = z1;

            x1 += (m.m00 < 0) ? m.m00 * max.x : m.m00 * min.x;
            x1 += (m.m01 < 0) ? m.m01 * max.y : m.m01 * min.y;
            x1 += (m.m02 < 0) ? m.m02 * max.z : m.m02 * min.z;
            x2 += (m.m00 > 0) ? m.m00 * max.x : m.m00 * min.x;
            x2 += (m.m01 > 0) ? m.m01 * max.y : m.m01 * min.y;
            x2 += (m.m02 > 0) ? m.m02 * max.z : m.m02 * min.z;

            y1 += (m.m10 < 0) ? m.m10 * max.x : m.m10 * min.x;
            y1 += (m.m11 < 0) ? m.m11 * max.y : m.m11 * min.y;
            y1 += (m.m12 < 0) ? m.m12 * max.z : m.m12 * min.z;
            y2 += (m.m10 > 0) ? m.m10 * max.x : m.m10 * min.x;
            y2 += (m.m11 > 0) ? m.m11 * max.y : m.m11 * min.y;
            y2 += (m.m12 > 0) ? m.m12 * max.z : m.m12 * min.z;

            z1 += (m.m20 < 0) ? m.m20 * max.x : m.m20 * min.x;
            z1 += (m.m21 < 0) ? m.m21 * max.y : m.m21 * min.y;
            z1 += (m.m22 < 0) ? m.m22 * max.z : m.m22 * min.z;
            z2 += (m.m20 > 0) ? m.m20 * max.x : m.m20 * min.x;
            z2 += (m.m21 > 0) ? m.m21 * max.y : m.m21 * min.y;
            z2 += (m.m22 > 0) ? m.m22 * max.z : m.m22 * min.z;

            set(x1, y1, z1, x2, y2, z2);
        }
        return this;
    }

    public boolean intersects(Vec3 origin, Vec3 direction, float[] time) {
        float tnear, tfar, t1, t2, temp, tolerance;
    
        if(isEmpty()) {
            return false;
        }
        
        tnear = -Float.MAX_VALUE;
        tfar = Float.MAX_VALUE;
        tolerance = 0.0000001f;
    
        if(Math.abs(direction.x) < tolerance) {
            if (origin.x <= min.x || origin.x >= max.x) {
                return false;
            }
        } else {
            t1 = (min.x - origin.x) / direction.x;
            t2 = (max.x - origin.x) / direction.x;
            if(t1 > t2) {
                temp = t1;
                t1 = t2;
                t2 = temp;
            }
            if(t1 > tnear) {
                tnear = t1;
            }
            if(t2 < tfar) {
                tfar = t2;
            }
            if(tnear > tfar || tfar < 0) {
                return false;
            }
        }
        
        if(Math.abs(direction.y) < tolerance) {
            if (origin.y <= min.y || origin.y >= max.y) {
                return false;
            }
        } else {
            t1 = (min.y - origin.y) / direction.y;
            t2 = (max.y - origin.y) / direction.y;
            if(t1 > t2) {
                temp = t1;
                t1 = t2;
                t2 = temp;
            }
            if(t1 > tnear) {
                tnear = t1;
            }
            if(t2 < tfar) {
                tfar = t2;
            }
            if(tnear > tfar || tfar < 0) {
                return false;
            }
        }
        
        if(Math.abs(direction.z) < tolerance) {
            if (origin.z <= min.z || origin.z >= max.z) {
                return false;
            }
        } else {
            t1 = (min.z - origin.z) / direction.z;
            t2 = (max.z - origin.z) / direction.z;
            if(t1 > t2) {
                temp = t1;
                t1 = t2;
                t2 = temp;
            }
            if(t1 > tnear) {
                tnear = t1;
            }
            if(t2 < tfar) {
                tfar = t2;
            }
            if(tnear > tfar || tfar < 0) {
                return false;
            }
        }
        
        if(tnear >= 0 && tnear < time[0]) {
            time[0] = tnear;
        }
        return true;
    }
}
