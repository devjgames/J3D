package org.j3d;

public final class Vec2 {


    public float x = 0;
    public float y = 0;

    public Vec2() {
    }

    public Vec2(float x, float y) {
        set(x, y);
    }

    public Vec2(Vec2 v) {
        set(v);
    }

    public Vec2(Vec3 v) {
        set(v);
    }

    public Vec2(Vec4 v) {
        set(v);
    }

    public Vec2 set(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Vec2 set(Vec2 v) {
        x = v.x;
        y = v.y;
        return this;
    }

    public Vec2 set(Vec3 v) {
        x = v.x;
        y = v.y;
        return this;
    }

    public Vec2 set(Vec4 v) {
        x = v.x;
        y = v.y;
        return this;
    }

    public Vec2 add(float x, float y, float z) {
        this.x += x;
        this.y += y;
        return this;
    }

    public Vec2 add(Vec2 v) {
        this.x += v.x;
        this.y += v.y;
        return this;
    }

    public Vec2 add(Vec2 v, Vec2 result) {
        result.x = x + v.x;
        result.y = y + v.y;
        return result;
    }

    public Vec2 sub(float x, float y, float z) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    public Vec2 sub(Vec2 v) {
        this.x -= v.x;
        this.y -= v.y;
        return this;
    }

    public Vec2 sub(Vec2 v, Vec2 result) {
        result.x = x - v.x;
        result.y = y - v.y;
        return result;
    }

    public Vec2 mul(float x, float y) {
        this.x *= x;
        this.y *= y;
        return this;
    }

    public Vec2 mul(Vec2 v) {
        this.x *= v.x;
        this.y *= v.y;
        return this;
    }

    public Vec2 mul(Vec2 v, Vec2 result) {
        result.x = x * v.x;
        result.y = y * v.y;
        return result;
    }

    public Vec2 div(float x, float y) {
        this.x /= x;
        this.y /= y;
        return this;
    }

    public Vec2 div(Vec2 v) {
        this.x /= v.x;
        this.y /= v.y;
        return this;
    }

    public Vec2 div(Vec2 v, Vec2 result) {
        result.x = x / v.x;
        result.y = y / v.y;
        return result;
    }

    public Vec2 div(float s) {
        x /= s;
        y /= s;
        return this;
    }

    public Vec2 div(float s, Vec2 result) {
        result.x = x / s;
        result.y = y / s;
        return result;
    }

    public Vec2 scale(float s) {
        x *= s;
        y *= s;
        return this;
    }

    public Vec2 scale(float s, Vec2 result) {
        result.x = x * s;
        result.y = y * s;
        return result;
    }

    public Vec2 neg() {
        x = -x;
        y = -y;
        return this;
    }

    public Vec2 neg(Vec2 v) {
        x = -v.x;
        y = -v.y;
        return this;
    }

    public float dot(Vec2 v) {
        return x * v.x + y * v.y;
    }

    public float dot(float x, float y) {
        return this.x * x + this.y * y;
    }

    public float length() {
        return (float)Math.sqrt(x * x + y * y);
    }

    public float lengthSquared() {
        return x * x + y * y;
    }

    public Vec2 normalize() {
        float l = (float)Math.sqrt(x * x + y * y);

        x /= l;
        y /= l;
        return this;
    }

    public Vec2 normalize(Vec2 v) {
        float l = (float)Math.sqrt(v.x * v.x + v.y * v.y);

        x = v.x / l;
        y = v.y / l;
        return this;
    }

    public float distance(Vec2 v) {
        float dX = v.x - x;
        float dY = v.y - y;

        return (float)Math.sqrt(dX * dX + dY * dY);
    }

    public float distance(float x, float y) {
        float dX = x - this.x;
        float dY = y - this.y;

        return (float)Math.sqrt(dX * dX + dY * dY);
    }

    public Vec2 lerp(Vec2 v, float amount) {
        x = x + amount * (v.x - x);
        y = y + amount * (v.y - y);
        return this;
    }

    public Vec2 lerp(Vec2 v, float amount, Vec2 result) {
        result.x = x + amount * (v.x - x);
        result.y = y + amount * (v.y - y);
        return result;
    }
    public Vec2 transform(Mat4 m, Vec2 v) {
        float tX = m.m00 * v.x + m.m01 * v.y + m.m03;
        float tY = m.m10 * v.x + m.m11 * v.y + m.m13;
        x = tX;
        y = tY;
        return this;
    }

    public Vec2 transformNormal(Mat4 m, Vec2 v) {
        float tX = m.m00 * v.x + m.m01 * v.y;
        float tY = m.m10 * v.x + m.m11 * v.y;
        x = tX;
        y = tY;
        return this;
    }

    public Vec2 parse(String[] tokens, int i) throws NumberFormatException {
        x = Float.parseFloat(tokens[i++]);
        y = Float.parseFloat(tokens[i++]);
        return this;
    }

    @Override
    public String toString() {
        return x + " " + y;
    }

    public static float length(float x, float y) {
        return (float)Math.sqrt(x * x + y * y);
    }

    public static float lengthSquared(float x, float y) {
        return x * x + y * y;
    }

    public static float dot(float x1, float y1, float x2, float y2) {
        return x1 * x2 + y1 * y2;
    }

    public static float distance(float x1, float y1, float x2, float y2) {
        float dX = x2 - x1;
        float dY = y2 - y1;

        return (float)Math.sqrt(dX * dX + dY * dY);
    }
}
