package org.j3d;

public final class Camera {

    public final Vec3 eye = new Vec3(100, 100, 100);
    public final Vec3 target = new Vec3();
    public final Vec3 up = new Vec3(0, 1, 0);
    public final Vec3 offset = new Vec3();
    public float fieldOfView = 60;
    public float zNear = 0.1f;
    public float zFar = 10000;
    public final Mat4 projection = new Mat4();
    public final Mat4 view = new Mat4();

    private final Vec3 r = new Vec3();
    private final Vec3 u = new Vec3();
    private final Vec3 f = new Vec3();
    private final Vec3 f2 = new Vec3();
    private final Vec4 v = new Vec4();
    private final Mat4 m = new Mat4();

    public Camera() {
    }

    public Camera(Camera camera) {
        eye.set(camera.eye);
        target.set(camera.target);
        up.set(camera.up);
        fieldOfView = camera.fieldOfView;
        zNear = camera.zNear;
        zFar = camera.zFar;
    }

    public void calcOffset() {
        eye.sub(target, offset);
    }

    public void calcTransforms(float aspectRatio) {
        projection.toIdentity().perspective(fieldOfView, aspectRatio, zNear, zFar);
        view.toIdentity().lookAt(eye, target, up);
    }

    public void setTarget(Vec3 target) {
        calcOffset();
        this.target.set(target);
        target.add(offset, eye);
    }

    public void rotate(float dX, float dY) {
        eye.sub(target, f);
        m.toIdentity().rotate(0, 1, 0, dX);
        f.cross(up, r).transformNormal(m, r).normalize();
        f.transformNormal(m, f);
        m.toIdentity().rotate(r, dY);
        r.cross(f, up).transformNormal(m, up).normalize();
        f.transformNormal(m, f);
        target.add(f, eye);
    }

    public void move(Vec3 point, float dX, float dY, Mat4 transform) {
        eye.sub(target, f);
        f2.set(f);
        f.y = 0;
        if(f.length() > 0.0000001) {
            f.normalize();
            u.set(0, 1, 0);
            f.cross(u, r).normalize().scale(dX);
            f.scale(dY);
            f.add(r);
            float len = f.length();
            if(transform != null) {
                f.transformNormal(transform, f).normalize().scale(len);
            }
            point.add(f);
        }
        target.add(f2, eye);
    }

    public void move(Vec3 point, float dY, Mat4 transform) {
        eye.sub(target, f);
        u.set(0, dY, 0);
        if(transform != null) {
            u.transformNormal(transform, u).normalize().scale(dY);
        }
        point.add(u);
        target.add(f, eye);
    }

    public void zoom(float amount) {
        float len;
        eye.sub(target, f);
        len = f.length();
        f.normalize().scale(len + amount);
        target.add(f, eye);
    }

    public void unproject(float x, float y, float z, int w, int h, Vec3 p) {
        m.set(projection).mul(view).invert();
        v.x = (x - 0) / (float)w * 2 - 1;
        v.y = (y - 0) / (float)h * 2 - 1;
        v.z = 2 * z - 1;
        v.w = 1;
        v.transform(m, v);
        v.scale(1 / v.w);
        p.set(v);
    }
}
