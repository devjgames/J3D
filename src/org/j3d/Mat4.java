package org.j3d;

import java.nio.FloatBuffer;

public final class Mat4 {

    public float m00, m01, m02, m03;
    public float m10, m11, m12, m13;
    public float m20, m21, m22, m23;
    public float m30, m31, m32, m33;

    public Mat4() {
        toIdentity();
    }

    public Mat4(
            float m00, float m01, float m02, float m03,
            float m10, float m11, float m12, float m13,
            float m20, float m21, float m22, float m23,
            float m30, float m31, float m32, float m33
    ) {
        set(
                m00, m01, m02, m03,
                m10, m11, m12, m13,
                m20, m21, m22, m23,
                m30, m31, m32, m33
        );
    }

    public Mat4(Mat4 m) {
        set(m);
    }

    public Mat4 toIdentity() {
        set(
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
        );
        return this;
    }

    public Mat4 set(
            float m00, float m01, float m02, float m03,
            float m10, float m11, float m12, float m13,
            float m20, float m21, float m22, float m23,
            float m30, float m31, float m32, float m33
    ) {
        this.m00 = m00;
        this.m01 = m01;
        this.m02 = m02;
        this.m03 = m03;
        this.m10 = m10;
        this.m11 = m11;
        this.m12 = m12;
        this.m13 = m13;
        this.m20 = m20;
        this.m21 = m21;
        this.m22 = m22;
        this.m23 = m23;
        this.m30 = m30;
        this.m31 = m31;
        this.m32 = m32;
        this.m33 = m33;
        return this;
    }

    public Mat4 set(Mat4 m) {
        set(
                m.m00, m.m01, m.m02, m.m03,
                m.m10, m.m11, m.m12, m.m13,
                m.m20, m.m21, m.m22, m.m23,
                m.m30, m.m31, m.m32, m.m33
        );
        return this;
    }

    public Mat4 mul(Mat4 m) {
        mul(
                m.m00, m.m01, m.m02, m.m03,
                m.m10, m.m11, m.m12, m.m13,
                m.m20, m.m21, m.m22, m.m23,
                m.m30, m.m31, m.m32, m.m33
        );
        return this;
    }

    public Mat4 mul(
            float m00, float m01, float m02, float m03,
            float m10, float m11, float m12, float m13,
            float m20, float m21, float m22, float m23,
            float m30, float m31, float m32, float m33
    ) {
        set(
                this.m00 * m00 + this.m01 * m10 + this.m02 * m20 + this.m03 * m30,
                this.m00 * m01 + this.m01 * m11 + this.m02 * m21 + this.m03 * m31,
                this.m00 * m02 + this.m01 * m12 + this.m02 * m22 + this.m03 * m32,
                this.m00 * m03 + this.m01 * m13 + this.m02 * m23 + this.m03 * m33,

                this.m10 * m00 + this.m11 * m10 + this.m12 * m20 + this.m13 * m30,
                this.m10 * m01 + this.m11 * m11 + this.m12 * m21 + this.m13 * m31,
                this.m10 * m02 + this.m11 * m12 + this.m12 * m22 + this.m13 * m32,
                this.m10 * m03 + this.m11 * m13 + this.m12 * m23 + this.m13 * m33,

                this.m20 * m00 + this.m21 * m10 + this.m22 * m20 + this.m23 * m30,
                this.m20 * m01 + this.m21 * m11 + this.m22 * m21 + this.m23 * m31,
                this.m20 * m02 + this.m21 * m12 + this.m22 * m22 + this.m23 * m32,
                this.m20 * m03 + this.m21 * m13 + this.m22 * m23 + this.m23 * m33,

                this.m30 * m00 + this.m31 * m10 + this.m32 * m20 + this.m33 * m30,
                this.m30 * m01 + this.m31 * m11 + this.m32 * m21 + this.m33 * m31,
                this.m30 * m02 + this.m31 * m12 + this.m32 * m22 + this.m33 * m32,
                this.m30 * m03 + this.m31 * m13 + this.m32 * m23 + this.m33 * m33
        );
        return this;
    }

    public Mat4 transpose() {
        set(
                m00, m10, m20, m30,
                m01, m11, m21, m31,
                m02, m12, m22, m32,
                m03, m13, m23, m33
        );
        return this;
    }

    public Mat4 invert() {
        float a, b, c, d, e, f, g, h, i, j, k, l, det;

        a = m00 * m11 - m01 * m10;
        b = m00 * m12 - m02 * m10;
        c = m00 * m13 - m03 * m10;
        d = m01 * m12 - m02 * m11;
        e = m01 * m13 - m03 * m11;
        f = m02 * m13 - m03 * m12;
        g = m20 * m31 - m21 * m30;
        h = m20 * m32 - m22 * m30;
        i = m20 * m33 - m23 * m30;
        j = m21 * m32 - m22 * m31;
        k = m21 * m33 - m23 * m31;
        l = m22 * m33 - m23 * m32;
        det = a * l - b * k + c * j + d * i - e * h + f * g;
        det = 1.0f / det;
        set(
                (+m11 * l - m12 * k + m13 * j) * det,
                (-m01 * l + m02 * k - m03 * j) * det,
                (+m31 * f - m32 * e + m33 * d) * det,
                (-m21 * f + m22 * e - m23 * d) * det,
                (-m10 * l + m12 * i - m13 * h) * det,
                (+m00 * l - m02 * i + m03 * h) * det,
                (-m30 * f + m32 * c - m33 * b) * det,
                (+m20 * f - m22 * c + m23 * b) * det,
                (+m10 * k - m11 * i + m13 * g) * det,
                (-m00 * k + m01 * i - m03 * g) * det,
                (+m30 * e - m31 * c + m33 * a) * det,
                (-m20 * e + m21 * c - m23 * a) * det,
                (-m10 * j + m11 * h - m12 * g) * det,
                (+m00 * j - m01 * h + m02 * g) * det,
                (-m30 * d + m31 * b - m32 * a) * det,
                (+m20 * d - m21 * b + m22 * a) * det
        );
        return this;
    }

    public Mat4 translate(float x, float y, float z) {
        mul(
                1, 0, 0, x,
                0, 1, 0, y,
                0, 0, 1, z,
                0, 0, 0, 1
        );
        return this;
    }

    public Mat4 translate(Vec3 t) {
        return translate(t.x, t.y, t.z);
    }

    public Mat4 rotate(float x, float y, float z, float degrees) {
        float radians = degrees * (float) Math.PI / 180;
        float c = (float) Math.cos(radians);
        float s = (float) Math.sin(radians);
        float l = Vec3.length(x, y, z);
        x /= l;
        y /= l;
        z /= l;
        mul(
                x * x * (1 - c) + c, x * y * (1 - c) - z * s, x * z * (1 - c) + y * s, 0,
                y * x * (1 - c) + z * s, y * y * (1 - c) + c, y * z * (1 - c) - x * s, 0,
                x * z * (1 - c) - y * s, y * z * (1 - c) + x * s, z * z * (1 - c) + c, 0,
                0, 0, 0, 1
        );
        return this;
    }

    public Mat4 rotate(Vec3 axis, float degrees) {
        return rotate(axis.x, axis.y, axis.z, degrees);
    }

    public Mat4 scale(float x, float y, float z) {
        mul(
                x, 0, 0, 0,
                0, y, 0, 0,
                0, 0, z, 0,
                0, 0, 0, 1
        );
        return this;
    }

    public Mat4 scale(Vec3 s) {
        return scale(s.x, s.y, s.z);
    }

    public Mat4 ortho(float l, float r, float b, float t, float n, float f) {
        float tX = -(r + l) / (r - l);
        float tY = -(t + b) / (t - b);
        float tZ = -(f + n) / (f - n);
        float sX = 2 / (r - l);
        float sY = 2 / (t - b);
        float sZ = -2 / (f - n);
        set(
                sX, 0, 0, tX,
                0, sY, 0, tY,
                0, 0, sZ, tZ,
                0, 0, 0, 1
        );
        return this;
    }

    public Mat4 perspective(float fovDegrees, float aspect, float n, float f) {
        float radians = fovDegrees * (float) Math.PI / 180.0f;
        float x = 1 / (float) Math.tan(radians / 2.0f);
        set(
                x / aspect, 0, 0, 0,
                0, x, 0, 0,
                0, 0, (f + n) / (n - f), 2 * f * n / (n - f),
                0, 0, -1, 0
        );
        return this;
    }

    public Mat4 lookAt(float eyeX, float eyeY, float eyeZ, float targetX, float targetY, float targetZ, float upX, float upY, float upZ) {
        float fX = targetX - eyeX;
        float fY = targetY - eyeY;
        float fZ = targetZ - eyeZ;
        float uX = upX;
        float uY = upY;
        float uZ = upZ;
        float rX = fY * uZ - fZ * uY;
        float rY = fZ * uX - fX * uZ;
        float rZ = fX * uY - fY * uX;
        float l;

        uX = rY * fZ - rZ * fY;
        uY = rZ * fX - rX * fZ;
        uZ = rX * fY - rY * fX;

        l = (float) Math.sqrt(fX * fX + fY * fY + fZ * fZ);
        fX /= l;
        fY /= l;
        fZ /= l;

        l = (float) Math.sqrt(uX * uX + uY * uY + uZ * uZ);
        uX /= l;
        uY /= l;
        uZ /= l;

        l = (float) Math.sqrt(rX * rX + rY * rY + rZ * rZ);
        rX /= l;
        rY /= l;
        rZ /= l;

        fX = -fX;
        fY = -fY;
        fZ = -fZ;

        mul(
                rX, rY, rZ, -Vec3.dot(rX, rY, rZ, eyeX, eyeY, eyeZ),
                uX, uY, uZ, -Vec3.dot(uX, uY, uZ, eyeX, eyeY, eyeZ),
                fX, fY, fZ, -Vec3.dot(fX, fY, fZ, eyeX, eyeY, eyeZ),
                0, 0, 0, 1
        );
        return this;
    }

    public Mat4 lookAt(Vec3 eye, Vec3 target, Vec3 up) {
        return lookAt(eye.x, eye.y, eye.z, target.x, target.y, target.z, up.x, up.y, up.z);
    }


    public void parse(String text) throws Exception {
        String[] tokens = text.split("\\s+");
        int i = 0;

        m00 = Float.parseFloat(tokens[i++]);
        m01 = Float.parseFloat(tokens[i++]);
        m02 = Float.parseFloat(tokens[i++]);
        m03 = Float.parseFloat(tokens[i++]);

        m10 = Float.parseFloat(tokens[i++]);
        m11 = Float.parseFloat(tokens[i++]);
        m12 = Float.parseFloat(tokens[i++]);
        m13 = Float.parseFloat(tokens[i++]);

        m20 = Float.parseFloat(tokens[i++]);
        m21 = Float.parseFloat(tokens[i++]);
        m22 = Float.parseFloat(tokens[i++]);
        m23 = Float.parseFloat(tokens[i++]);

        m30 = Float.parseFloat(tokens[i++]);
        m31 = Float.parseFloat(tokens[i++]);
        m32 = Float.parseFloat(tokens[i++]);
        m33 = Float.parseFloat(tokens[i]);
    }
    
    public void put(FloatBuffer fBuf) {
        fBuf.put(m00);
        fBuf.put(m10);
        fBuf.put(m20);
        fBuf.put(m30);
        fBuf.put(m01);
        fBuf.put(m11);
        fBuf.put(m21);
        fBuf.put(m31);
        fBuf.put(m02);
        fBuf.put(m12);
        fBuf.put(m22);
        fBuf.put(m32);
        fBuf.put(m03);
        fBuf.put(m13);
        fBuf.put(m23);
        fBuf.put(m33);
        fBuf.flip();
    }

    @Override
    public String toString() {
        return
                m00 + " " + m01 + " " + m02 + " " + m03 + " " +
                        m10 + " " + m11 + " " + m12 + " " + m13 + " " +
                        m20 + " " + m21 + " " + m22 + " " + m23 + " " +
                        m30 + " " + m31 + " " + m32 + " " + m33;
    }
}
