package org.j3d;

import org.j3d.Collider.TriangleSelector;
import org.joml.Matrix4f;

public class PipelineTriangleSelector implements TriangleSelector {

    public final Matrix4f model = new Matrix4f();
    public final TrianglePipeline pipeline;

    private boolean enabled = true;
    private final Triangle triangle = new Triangle();
    private final BoundingBox bounds = new BoundingBox();

    public PipelineTriangleSelector(TrianglePipeline pipeline) {
        this.pipeline = pipeline;
    }

    public void begin(Matrix4f projection, Matrix4f view) {
        pipeline.begin(projection, view);
    }

    public void render() {
        pipeline.getModel().set(model);
        pipeline.render();
    }

    public void end() {
        pipeline.end();
    }

    public void render(Matrix4f projection, Matrix4f view) {
        begin(projection, view);
        render();
        end();
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

        pipeline.getModel().set(model);
        bounds.min.set(pipeline.getBounds().min);
        bounds.max.set(pipeline.getBounds().max);
        bounds.transform(model);

        collider.time[0] = Float.MAX_VALUE;
        if(bounds.intersects(collider.origin, collider.direction, collider.time)) {
            collider.time[0] = t;
            for(int i = 0; i != pipeline.getTriangleCount(); i++) {
                pipeline.triangleAt(i, triangle);
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

        pipeline.getModel().set(model);
        bounds.min.set(pipeline.getBounds().min);
        bounds.max.set(pipeline.getBounds().max);
        bounds.transform(model);
        bounds.min.sub(1, 1, 1);
        bounds.max.add(1, 1, 1);

        if(bounds.touches(collider.resolveBounds)) {
            for(int i = 0; i != pipeline.getTriangleCount(); i++) {
                pipeline.triangleAt(i, triangle);
                if(collider.selectorResolve(triangle)) {
                    hit = true;
                }
            }
        }
        return hit;
    }
}
