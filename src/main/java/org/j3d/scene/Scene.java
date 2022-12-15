package org.j3d.scene;

import org.j3d.Collider;
import org.j3d.Game;
import org.j3d.Utils;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Scene {
    
    public final Vector4f backgroundColor = new Vector4f(0, 0, 0, 1);
    public final Vector3f eye = new Vector3f(100, 100, 100);
    public final Vector3f target = new Vector3f();
    public final Vector3f up = new Vector3f(0, 1, 0);
    public float fieldOfView = 60;
    public float zNear = 1;
    public float zFar = 50000;
    public boolean polygonOffsetEnabled = false;
    public float polygonOffsetFactor = 1;
    public float polygonOffsetUnits = 1;
    public final Node root = new Node();
    public final Matrix4f projection = new Matrix4f();
    public final Matrix4f view = new Matrix4f();
    public boolean sortLightsAroundTarget = true;
    public final Lines lines;
    public final boolean inDesign;
    public Node selection = null;

    private final Vector3f offset = new Vector3f();
    private final Vector3f direction = new Vector3f();
    private final Vector3f r = new Vector3f();
    private final Vector3f u = new Vector3f();
    private final Vector3f f = new Vector3f();

    public Scene(Game game, boolean inDesign) throws Exception {
        this.inDesign = inDesign;
        if(inDesign) {
            lines = game.getAssets().getResources().manage(new Lines());
        } else {
            lines = null;
        }
    }

    public void rotateAroundTarget(Game game) {
        eye.sub(target, offset);
        Utils.rotateOffsetAndUp(offset, up, game);
        target.add(offset, eye);
    }

    public void rotateAroundEye(Game game) {
        target.sub(eye, direction).normalize();
        Utils.rotateDirectionAndUp(direction, up, game);
        eye.add(direction, target);
    }

    public void setVelocity(Collider collider, float velocity) {
        target.sub(eye, direction).normalize();
        direction.mul(1, 0, 1);
        if(direction.length() > 0.0000001) {
            direction.normalize().mul(velocity);
            collider.velocity.add(direction);
        }
    }

    public void move(Vector3f point, float dx, float dy, Matrix4f transform) {
        float dl = Vector2f.length(dx, dy);

        eye.sub(target, offset);
        f.set(offset).mul(1, 0, 1);
        if(f.length() > 0.0000001 && dl > 0.1) {
            f.normalize().cross(u.set(0, 1, 0), r).normalize();
            f.mul(dy);
            r.mul(dx);
            f.add(r);
            if(transform != null) {
                f.mulDirection(transform);
            }
            point.add(f);
        }
        target.add(offset, eye);
    }

    public void move(Vector3f point, float dy, Matrix4f transform) {
        eye.sub(target, offset);
        u.set(0, dy, 0);
        if(transform != null) {
            u.mulDirection(transform);
        }
        point.add(u);
        target.add(offset, eye);
    }

    public void move(float amount, Matrix4f transform) {
        target.sub(eye, direction).normalize();
        f.set(direction).mul(1, 0, 1);
        if(f.length() > 0.0000001) {
            f.normalize().mul(amount);
            eye.add(f);
        }
        eye.add(direction, target);
    }

    public void zoom(float amount) {
        eye.sub(target, offset);
        f.set(offset).normalize().mul(offset.length() + amount);
        target.add(f, eye);
    }

    public void calcTransform(Game game) {
        projection.identity().perspective(fieldOfView * (float)Math.PI / 180, game.getAspectRatio(), zNear, zFar);
        view.identity().lookAt(eye, target, up);
    }

    public void unProject(float x, float y, float z, Game game, Vector3f point) {
        calcTransform(game);

        Utils.unProject(x, y, z, 0, 0, game.getRenderTargetWidth(), game.getRenderTargetHeight(), projection, view, point);
    }
}
