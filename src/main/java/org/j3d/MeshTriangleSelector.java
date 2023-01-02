package org.j3d;

import org.j3d.Collider.TriangleSelector;
import org.joml.Matrix4f;

public class MeshTriangleSelector implements TriangleSelector {

    public final Matrix4f model = new Matrix4f();
    public final LightPipeline mesh;

    private boolean enabled = true;
    private final Triangle triangle = new Triangle();
    private final BoundingBox bounds = new BoundingBox();

    public MeshTriangleSelector(LightPipeline mesh) {
        this.mesh = mesh;
    }

    public void render(Matrix4f projection, Matrix4f view) {
        mesh.model.set(model);
        mesh.render(projection, view);
    }

    public void setTransform(float x, float y, float z, float rx, float ry, float rz, float scale) {
        model.identity()
            .translate(x, y, z)
            .rotate(Utils.toRadians(rx), 1, 0, 0)
            .rotate(Utils.toRadians(ry), 0, 1, 0)
            .rotate(Utils.toRadians(rz), 0, 0, 1)
            .scale(scale);
    }

    @Override
    public boolean getEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean intersect(Collider collider) {
        float t = collider.time[0];
        boolean hit = false;

        mesh.model.set(model);
        bounds.min.set(mesh.getBounds().min);
        bounds.max.set(mesh.getBounds().max);
        bounds.transform(model);

        collider.time[0] = Float.MAX_VALUE;
        if(bounds.intersects(collider.origin, collider.direction, collider.time)) {
            collider.time[0] = t;
            for(int i = 0; i != mesh.getTriangleCount(); i++) {
                mesh.triangleAt(i, triangle);
                if(collider.selectorIntersect(triangle)) {
                    hit = true;
                }
            }
        } else {
            collider.time[0] = t;
        }
        return hit;
    }

    @Override
    public boolean resolve(Collider collider) {
        boolean hit = false;

        mesh.model.set(model);
        bounds.min.set(mesh.getBounds().min);
        bounds.max.set(mesh.getBounds().max);
        bounds.transform(model);
        bounds.min.sub(1, 1, 1);
        bounds.max.add(1, 1, 1);

        if(bounds.touches(collider.resolveBounds)) {
            for(int i = 0; i != mesh.getTriangleCount(); i++) {
                mesh.triangleAt(i, triangle);
                if(collider.selectorResolve(triangle)) {
                    hit = true;
                }
            }
        }
        return hit;
    }
}
