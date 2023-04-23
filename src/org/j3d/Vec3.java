package org.j3d;

public final class Vec3 {

    public float x = 0;
    public float y = 0;
    public float z = 0;

    public Vec3() {
    }

    public Vec3(float x, float y, float z) {
        set(x, y, z);
    }

    public Vec3(Vec2 v, float z) {
        set(v, z);
    }

    public Vec3(Vec3 v) {
        set(v);
    }

    public Vec3(Vec4 v) {
        set(v);
    }

    public Vec3 set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vec3 set(Vec2 v, float z) {
        x = v.x;
        y = v.y;
        this.z = z;
        return this;
    }

    public Vec3 set(Vec3 v) {
        x = v.x;
        y = v.y;
        z = v.z;
        return this;
    }

    public Vec3 set(Vec4 v) {
        x = v.x;
        y = v.y;
        z = v.z;
        return this;
    }

    public Vec3 add(float x, float y, float z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public Vec3 add(Vec3 v) {
        this.x += v.x;
        this.y += v.y;
        this.z += v.z;
        return this;
    }

    public Vec3 add(Vec3 v, Vec3 result) {
        result.x = x + v.x;
        result.y = y + v.y;
        result.z = z + v.z;
        return result;
    }

    public Vec3 sub(float x, float y, float z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    public Vec3 sub(Vec3 v) {
        this.x -= v.x;
        this.y -= v.y;
        this.z -= v.z;
        return this;
    }

    public Vec3 sub(Vec3 v, Vec3 result) {
        result.x = x - v.x;
        result.y = y - v.y;
        result.z = z - v.z;
        return result;
    }

    public Vec3 mul(float x, float y, float z) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        return this;
    }

    public Vec3 mul(Vec3 v) {
        this.x *= v.x;
        this.y *= v.y;
        this.z *= v.z;
        return this;
    }

    public Vec3 mul(Vec3 v, Vec3 result) {
        result.x = x * v.x;
        result.y = y * v.y;
        result.z = z * v.z;
        return result;
    }

    public Vec3 div(float x, float y, float z) {
        this.x /= x;
        this.y /= y;
        this.z /= z;
        return this;
    }

    public Vec3 div(Vec3 v) {
        this.x /= v.x;
        this.y /= v.y;
        this.z /= v.z;
        return this;
    }

    public Vec3 div(Vec3 v, Vec3 result) {
        result.x = x / v.x;
        result.y = y / v.y;
        result.z = z / v.z;
        return result;
    }

    public Vec3 div(float s) {
        x /= s;
        y /= s;
        z /= s;
        return this;
    }

    public Vec3 div(float s, Vec3 result) {
        result.x = x / s;
        result.y = y / s;
        result.z = z / s;
        return result;
    }

    public Vec3 scale(float s) {
        x *= s;
        y *= s;
        z *= s;
        return this;
    }

    public Vec3 scale(float s, Vec3 result) {
        result.x = x * s;
        result.y = y * s;
        result.z = z * s;
        return result;
    }

    public Vec3 neg() {
        x = -x;
        y = -y;
        z = -z;
        return this;
    }

    public Vec3 neg(Vec3 v) {
        x = -v.x;
        y = -v.y;
        z = -v.z;
        return this;
    }

    public float dot(Vec3 v) {
        return x * v.x + y * v.y + z * v.z;
    }

    public float dot(float x, float y, float z) {
        return this.x * x + this.y * y + this.z * z;
    }

    public float length() {
        return (float)Math.sqrt(x * x + y * y + z * z);
    }

    public float lengthSquared() {
        return x * x + y * y + z * z;
    }

    public Vec3 normalize() {
        float l = (float)Math.sqrt(x * x + y * y + z * z);

        x /= l;
        y /= l;
        z /= l;
        return this;
    }

    public Vec3 normalize(Vec3 v) {
        float l = (float)Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z);

        x = v.x / l;
        y = v.y / l;
        z = v.z / l;
        return this;
    }

    public float distance(Vec3 v) {
        float dX = v.x - x;
        float dY = v.y - y;
        float dZ = v.z - z;

        return (float)Math.sqrt(dX * dX + dY * dY + dZ * dZ);
    }

    public float distance(float x, float y, float z) {
        float dX = x - this.x;
        float dY = y - this.y;
        float dZ = z - this.z;

        return (float)Math.sqrt(dX * dX + dY * dY + dZ * dZ);
    }

    public Vec3 lerp(Vec3 v, float amount) {
        x = x + amount * (v.x - x);
        y = y + amount * (v.y - y);
        z = z + amount * (v.z - z);
        return this;
    }

    public Vec3 lerp(Vec3 v, float amount, Vec3 result) {
        result.x = x + amount * (v.x - x);
        result.y = y + amount * (v.y - y);
        result.z = z + amount * (v.z - z);
        return result;
    }

    public Vec3 cross(float x, float y, float z) {
        float cX = this.y * z - this.z * y;
        float cY = this.z * x - this.x * z;
        float cZ = this.x * y - this.y * x;
        this.x = cX;
        this.y = cY;
        this.z = cZ;
        return this;
    }

    public Vec3 cross(Vec3 v) {
        float cX = y * v.z - z * v.y;
        float cY = z * v.x - x * v.z;
        float cZ = x * v.y - y * v.x;
        x = cX;
        y = cY;
        z = cZ;
        return this;
    }

    public Vec3 cross(Vec3 v, Vec3 result) {
        float cX = y * v.z - z * v.y;
        float cY = z * v.x - x * v.z;
        float cZ = x * v.y - y * v.x;
        result.x = cX;
        result.y = cY;
        result.z = cZ;
        return result;
    }

    public Vec3 transform(Mat4 m, Vec3 v) {
        float tX = m.m00 * v.x + m.m01 * v.y + m.m02 * v.z + m.m03;
        float tY = m.m10 * v.x + m.m11 * v.y + m.m12 * v.z + m.m13;
        float tZ = m.m20 * v.x + m.m21 * v.y + m.m22 * v.z + m.m23;
        x = tX;
        y = tY;
        z = tZ;
        return this;
    }

    public Vec3 transformNormal(Mat4 m, Vec3 v) {
        float tX = m.m00 * v.x + m.m01 * v.y + m.m02 * v.z;
        float tY = m.m10 * v.x + m.m11 * v.y + m.m12 * v.z;
        float tZ = m.m20 * v.x + m.m21 * v.y + m.m22 * v.z;
        x = tX;
        y = tY;
        z = tZ;
        return this;
    }

    public Vec3 parse(String[] tokens, int i) throws NumberFormatException {
        x = Float.parseFloat(tokens[i++]);
        y = Float.parseFloat(tokens[i++]);
        z = Float.parseFloat(tokens[i++]);
        return this;
    }

    @Override
    public String toString() {
        return x + " " + y + " " + z;
    }

    public static float length(float x, float y, float z) {
        return (float)Math.sqrt(x * x + y * y + z * z);
    }

    public static float lengthSquared(float x, float y, float z) {
        return x * x + y * y + z * z;
    }

    public static float dot(float x1, float y1, float z1, float x2, float y2, float z2) {
        return x1 * x2 + y1 * y2 + z1 * z2;
    }

    public static float distance(float x1, float y1, float z1, float x2, float y2, float z2) {
        float dX = x2 - x1;
        float dY = y2 - y1;
        float dZ = z2 - z1;

        return (float)Math.sqrt(dX * dX + dY * dY + dZ * dZ);
    }
}
