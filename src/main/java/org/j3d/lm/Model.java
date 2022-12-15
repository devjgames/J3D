package org.j3d.lm;

import org.j3d.Mesh;
import org.joml.Matrix4f;

public class Model {
    
    public final Matrix4f model = new Matrix4f();
    public final Mesh mesh;

    public Model(Matrix4f model, Mesh mesh) {
        this.model.set(model);
        this.mesh = mesh;
    }
}
