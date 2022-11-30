package org.j3d.lm;

import org.j3d.Mesh;
import org.j3d.Texture;
import org.joml.Matrix4f;

public interface Renderer {

    Mesh getMesh();

    void render(Matrix4f projection, Matrix4f view, Texture texture2);
}
