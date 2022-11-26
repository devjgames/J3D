package org.j3d;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Vector;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

public class MeshPart {
    
    public final int stride;
    public Material material = null;
    public final Mesh mesh;
    public final BoundingBox bounds = new BoundingBox();
    public final Matrix4f model = new Matrix4f();
    public Object data = null;

    private FloatBuffer vBuf;
    private IntBuffer iBuf = BufferUtils.createIntBuffer(900);
    private final Vector<int[]> faces = new Vector<>();
    private int vertexCount = 0;
    private final Matrix4f matrix = new Matrix4f();

    public MeshPart(Mesh mesh, int stride) {
        this.stride = stride;
        this.mesh = mesh;

        vBuf = BufferUtils.createFloatBuffer(300 * stride);
    }

    public int getTriangleCount() {
        return iBuf.limit() / 3;
    }

    public void triangleAt(int i, Triangle triangle) {
        int i1 = iBuf.get(i * 3 + 0);
        int i2 = iBuf.get(i * 3 + 1);
        int i3 = iBuf.get(i * 3 + 2);

        triangle.p1.set(vBuf.get(i1 * stride + 0), vBuf.get(i1 * stride + 1), vBuf.get(i1 * stride + 2));
        triangle.p2.set(vBuf.get(i2 * stride + 0), vBuf.get(i2 * stride + 1), vBuf.get(i2 * stride + 2));
        triangle.p3.set(vBuf.get(i3 * stride + 0), vBuf.get(i3 * stride + 1), vBuf.get(i3 * stride + 2));
        triangle.calcPlane();
        triangle.data = this;
    }

    public int getFaceCount() {
        return faces.size();
    }

    public int getFaceVertexCount(int i) {
        return faces.get(i).length;
    }

    public int faceVertexAt(int i, int j) {
        return faces.get(i)[j];
    }

    public void push(int ...indices) {
        int tris = indices.length - 2;

        iBuf = Utils.ensureCapacity(iBuf, tris * 3 + 3000);
        faces.add(indices.clone());

        for(int i = 0; i != tris; i++) {
            iBuf.put(indices[0]);
            iBuf.put(indices[i + 1]);
            iBuf.put(indices[i + 2]);
        }
    }

    public int getIndexCount() {
        return iBuf.limit();
    }

    public int indexAt(int i) {
        return iBuf.get(i);
    }

    public int getVertexCount() {
        return vertexCount / stride;
    }

    public float vertexAt(int i, int j) {
        return vBuf.get(i * stride + j);
    }

    public void setVertexAt(int i, int j, float x) {
        vBuf.put(i * stride + j, x);
    }

    public void calcBounds() {
        bounds.clear();
        for(int i = 0; i != getVertexCount(); i++) {
            bounds.add(vertexAt(i, 0), vertexAt(i, 1), vertexAt(i, 2));
        }
        bounds.transform(model);
    }

    public void push(float x) {
        vBuf = Utils.ensureCapacity(vBuf, stride * 1000);
        vBuf.put(x);
        vertexCount++;
    }

    public void push(float x, float y) {
        vBuf = Utils.ensureCapacity(vBuf, stride * 1000);
        vBuf.put(x);
        vBuf.put(y);
        vertexCount += 2;
    }

    public void push(float x, float y, float z) {
        vBuf = Utils.ensureCapacity(vBuf, stride * 1000);
        vBuf.put(x);
        vBuf.put(y);
        vBuf.put(z);
        vertexCount += 3;
    }

    public void push(float x, float y, float z, float w) {
        vBuf = Utils.ensureCapacity(vBuf, stride * 1000);
        vBuf.put(x);
        vBuf.put(y);
        vBuf.put(z);
        vBuf.put(w);
        vertexCount += 4;
    }

    public void bufferVertices(boolean dynamic) {
        material.buffer(vBuf, dynamic);
    }

    public void bufferIndices() {
        material.buffer(iBuf);
    }

    public void trim() {
        iBuf = Utils.trimCapacity(iBuf);
        vBuf = Utils.trimCapacity(vBuf);
    }

    public void render(Matrix4f projection, Matrix4f view) {
        if(material != null) {
            material.render(projection, view, matrix.set(mesh.model).mul(model), getIndexCount());
        }
    }
}
