package org.j3d;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.joml.Matrix4f;

public interface Material {
    
    void setSource(Object source);
    void buffer(FloatBuffer vBuf, boolean dynamic);
    void buffer(IntBuffer iBuf);
    void begin(Object data);
    void render(Matrix4f projection, Matrix4f view, Matrix4f model);
    void end();
    boolean isEqualTo(Material material);
}
