package org.j3d;

public final class Frustum {

    private static final Mat4 m = new Mat4();
    private static final Vec4 r1 = new Vec4();
    private static final Vec4 r2 = new Vec4();
    private static final Vec4 r3 = new Vec4();
    private static final Vec4 r4 = new Vec4();


    public final Vec4 l = new Vec4();
    public final Vec4 r = new Vec4();
    public final Vec4 b = new Vec4();
    public final Vec4 t = new Vec4();
    public final Vec4 n = new Vec4();
    public final Vec4 f = new Vec4();

    public void set(Mat4 projection, Mat4 view) {
        m.set(projection).mul(view);

        r1.set(m.m00, m.m01, m.m02, m.m03);
        r2.set(m.m10, m.m11, m.m12, m.m13);
        r3.set(m.m20, m.m21, m.m22, m.m23);
        r4.set(m.m30, m.m31, m.m32, m.m33);

        r4.add(r1, l);
        r4.sub(r1, r);
        r4.add(r2, b);
        r4.sub(r2, t);
        r4.add(r3, n);
        r4.sub(r3, f);

        l.div(Vec3.length(l.x, l.y, l.z));
        r.div(Vec3.length(r.x, r.y, r.z));
        b.div(Vec3.length(b.x, b.y, b.z));
        t.div(Vec3.length(t.x, t.y, t.z));
        n.div(Vec3.length(n.x, n.y, n.z));
        f.div(Vec3.length(f.x, f.y, f.z));
    }
}
