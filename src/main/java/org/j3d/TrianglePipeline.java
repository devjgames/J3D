package org.j3d;

import org.joml.Matrix4f;

public interface TrianglePipeline extends Asset {

    BoundingBox getBounds();

    Matrix4f getModel();

    int getFaceCount();

    int getFaceVertexCount(int i);

    int faceVertexAt(int i, int j);

    int getTriangleTag();

    void setTriangleTag(int tag);

    void setTransform(float x, float y, float z, float rx, float ry, float rz, float scale);

    int getTriangleCount();

    void triangleAt(int i, Triangle triangle);

    int getVertexCount();

    float vertexAt(int i, int j);

    void setVertexAt(int i, int j, float x);

    void bufferVertices();

    void begin(Matrix4f projection, Matrix4f view);

    void render();

    void end();

    void render(Matrix4f projection, Matrix4f view);
}
