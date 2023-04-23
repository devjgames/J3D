package org.j3d;

public final class Vec4 {

    public float x = 0;
    public float y = 0;
    public float z = 0;
    public float w = 0;

    public Vec4() {
    }

    public Vec4(float x, float y, float z, float w) {
        set(x, y, z, w);
    }

    public Vec4(Vec2 v, float z, float w) {
        set(v, z, w);
    }

    public Vec4(Vec3 v, float w) {
        set(v, w);
    }

    public Vec4(Vec4 v) {
        set(v);
    }

    public Vec4 set(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        return this;
    }

    public Vec4 set(Vec2 v, float z, float w) {
        x = v.x;
        y = v.y;
        this.z = z;
        this.w = w;
        return this;
    }

    public Vec4 set(Vec3 v, float w) {
        x = v.x;
        y = v.y;
        z = v.z;
        this.w = w;
        return this;
    }

    public Vec4 set(Vec4 v) {
        x = v.x;
        y = v.y;
        z = v.z;
        w = v.w;
        return this;
    }

    public Vec4 add(float x, float y, float z, float w) {
        this.x += x;
        this.y += y;
        this.z += z;
        this.w += w;
        return this;
    }

    public Vec4 add(Vec4 v) {
        this.x += v.x;
        this.y += v.y;
        this.z += v.z;
        this.w += v.w;
        return this;
    }

    public Vec4 add(Vec4 v, Vec4 result) {
        result.x = x + v.x;
        result.y = y + v.y;
        result.z = z + v.z;
        result.w = w + v.w;
        return result;
    }

    public Vec4 sub(float x, float y, float z, float w) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        this.w -= w;
        return this;
    }

    public Vec4 sub(Vec4 v) {
        this.x -= v.x;
        this.y -= v.y;
        this.z -= v.z;
        this.w -= v.w;
        return this;
    }

    public Vec4 sub(Vec4 v, Vec4 result) {
        result.x = x - v.x;
        result.y = y - v.y;
        result.z = z - v.z;
        result.w = w - v.w;
        return result;
    }

    public Vec4 mul(float x, float y, float z, float w) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        this.w *= w;
        return this;
    }

    public Vec4 mul(Vec4 v) {
        this.x *= v.x;
        this.y *= v.y;
        this.z *= v.z;
        this.w *= v.w;
        return this;
    }

    public Vec4 mul(Vec4 v, Vec4 result) {
        result.x = x * v.x;
        result.y = y * v.y;
        result.z = z * v.z;
        result.w = w * v.w;
        return result;
    }

    public Vec4 div(float x, float y, float z, float w) {
        this.x /= x;
        this.y /= y;
        this.z /= z;
        this.w /= w;
        return this;
    }

    public Vec4 div(Vec4 v) {
        this.x /= v.x;
        this.y /= v.y;
        this.z /= v.z;
        this.w /= v.w;
        return this;
    }

    public Vec4 div(Vec4 v, Vec4 result) {
        result.x = x / v.x;
        result.y = y / v.y;
        result.z = z / v.z;
        result.w = w / v.w;
        return result;
    }

    public Vec4 div(float s) {
        x /= s;
        y /= s;
        z /= s;
        w /= s;
        return this;
    }

    public Vec4 div(float s, Vec4 result) {
        result.x = x / s;
        result.y = y / s;
        result.z = z / s;
        result.w = w / s;
        return result;
    }

    public Vec4 scale(float s) {
        x *= s;
        y *= s;
        z *= s;
        w *= s;
        return this;
    }

    public Vec4 scale(float s, Vec4 result) {
        result.x = x * s;
        result.y = y * s;
        result.z = z * s;
        result.w = w * s;
        return result;
    }

    public Vec4 neg() {
        x = -x;
        y = -y;
        z = -z;
        w = -w;
        return this;
    }

    public Vec4 neg(Vec4 v) {
        x = -v.x;
        y = -v.y;
        z = -v.z;
        w = -v.w;
        return this;
    }

    public float dot(Vec4 v) {
        return x * v.x + y * v.y + z * v.z + w * v.w;
    }

    public float dot(float x, float y, float z, float w) {
        return this.x * x + this.y * y + this.z * z + this.w * w;
    }

    public float length() {
        return (float)Math.sqrt(x * x + y * y + z * z + w * w);
    }

    public float lengthSquared() {
        return x * x + y * y + z * z + w * w;
    }

    public Vec4 normalize() {
        float l = (float)Math.sqrt(x * x + y * y + z * z + w * w);

        x /= l;
        y /= l;
        z /= l;
        w /= l;
        return this;
    }

    public Vec4 normalize(Vec4 v) {
        float l = (float)Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z + v.w * v.w);

        x = v.x / l;
        y = v.y / l;
        z = v.z / l;
        w = v.w / l;
        return this;
    }

    public float distance(Vec4 v) {
        float dX = v.x - x;
        float dY = v.y - y;
        float dZ = v.z - z;
        float dW = v.w - w;

        return (float)Math.sqrt(dX * dX + dY * dY + dZ * dZ + dW * dW);
    }

    public float distance(float x, float y, float z, float w) {
        float dX = x - this.x;
        float dY = y - this.y;
        float dZ = z - this.z;
        float dW = w - this.w;

        return (float)Math.sqrt(dX * dX + dY * dY + dZ * dZ + dW * dW);
    }

    public Vec4 lerp(Vec4 v, float amount) {
        x = x + amount * (v.x - x);
        y = y + amount * (v.y - y);
        z = z + amount * (v.z - z);
        w = w + amount * (v.w - w);
        return this;
    }

    public Vec4 lerp(Vec4 v, float amount, Vec4 result) {
        result.x = x + amount * (v.x - x);
        result.y = y + amount * (v.y - y);
        result.z = z + amount * (v.z - z);
        result.w = w + amount * (v.w - w);
        return result;
    }

    public Vec4 transform(Mat4 m, Vec4 v) {
        float tX = m.m00 * v.x + m.m01 * v.y + m.m02 * v.z + m.m03 * v.w;
        float tY = m.m10 * v.x + m.m11 * v.y + m.m12 * v.z + m.m13 * v.w;
        float tZ = m.m20 * v.x + m.m21 * v.y + m.m22 * v.z + m.m23 * v.w;
        float tW = m.m30 * v.x + m.m31 * v.y + m.m32 * v.z + m.m33 * v.w;
        x = tX;
        y = tY;
        z = tZ;
        w = tW;
        return this;
    }

    public Vec4 parse(String[] tokens, int i) throws NumberFormatException {
        x = Float.parseFloat(tokens[i++]);
        y = Float.parseFloat(tokens[i++]);
        z = Float.parseFloat(tokens[i++]);
        w = Float.parseFloat(tokens[i++]);
        return this;
    }

    @Override
    public String toString() {
        return x + " " + y + " " + z + " " + w;
    }

    public static float length(float x, float y, float z, float w) {
        return (float)Math.sqrt(x * x + y * y + z * z + w * w);
    }

    public static float lengthSquared(float x, float y, float z, float w) {
        return x * x + y * y + z * z + w * w;
    }

    public static float dot(float x1, float y1, float z1, float w1, float x2, float y2, float z2, float w2) {
        return x1 * x2 + y1 * y2 + z1 * z2 + w1 * w2;
    }

    public static float distance(float x1, float y1, float z1, float w1, float x2, float y2, float z2, float w2) {
        float dX = x2 - x1;
        float dY = y2 - y1;
        float dZ = z2 - z1;
        float dW = w2 - w1;

        return (float)Math.sqrt(dX * dX + dY * dY + dZ * dZ + dW * dW);
    }
}
