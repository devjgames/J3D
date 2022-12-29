package org.j3d;

import org.j3d.Collider.TriangleSelector;
import org.joml.Matrix4f;

public class MeshTriangleSelector implements TriangleSelector {

    public final Matrix4f model = new Matrix4f();
    public final LightPipeline mesh;

    private boolean enabled = true;
    private final Triangle triangle = new Triangle();

    public MeshTriangleSelector(LightPipeline mesh) {
        this.mesh = mesh;
    }

    public void render(Matrix4f projection, Matrix4f view) {
        mesh.model.set(model);
        mesh.render(projection, view);
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

        collider.time[0] = Float.MAX_VALUE;
        if(mesh.getBounds().intersects(collider.origin, collider.direction, collider.time)) {
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

        if(mesh.getBounds().touches(collider.resolveBounds)) {
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
