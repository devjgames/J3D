package org.j3d;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Vector;

import org.j3d.Collider.TriangleSelector;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class DualTexturePipeline extends Resource implements TriangleSelector {
    
    public final Matrix4f model = new Matrix4f();
    public final Vector4f color = new Vector4f(1, 1, 1, 1);
    public final Vector4f ambientColor = new Vector4f(0.1f, 0.1f, 0.1f, 1);
    public final Vector4f diffuseColor = new Vector4f(0.9f, 0.9f, 0.9f, 1);
    public Texture texture = null;
    public Texture texture2 = null;
    public int tag = 1;

    private final Pipeline pipeline;
    private final int vao, vbo, veo;
    private final int uProjection, uView, uModel;
    private final int uColor;
    private final int uTexture, uTextureEnabled;
    private final int uTexture2, uTexture2Enabled;
    private FloatBuffer vBuf = BufferUtils.createFloatBuffer(10 * 7);
    private IntBuffer iBuf = BufferUtils.createIntBuffer(9);
    private boolean enabled = true;
    private final Triangle triangle = new Triangle();
    private final Vector<int[]> faces = new Vector<>();
    private final BoundingBox bounds = new BoundingBox();
    private final Vector3f v1 = new Vector3f();
    private final Vector3f v2 = new Vector3f();
    private final Vector3f p1 = new Vector3f();
    private final Vector3f p2 = new Vector3f();
    private final Vector3f p3 = new Vector3f();
    private int vertexCount = 0;
    private int indexCount = 0;

    public DualTexturePipeline() throws Exception {
        pipeline = new Pipeline(
            new String(IO.readAllBytes(DualTexturePipeline.class, "/DualTextureVertexShader.glsl")),
            new String(IO.readAllBytes(DualTexturePipeline.class, "/DualTextureFragmentShader.glsl")), 
            "vsInPosition", "vsInTextureCoordinate", "vsInTextureCoordinate2"
            );
        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();
        veo = GL15.glGenBuffers();
        uProjection = pipeline.getUniformLocation("uProjection");
        uView = pipeline.getUniformLocation("uView");
        uModel = pipeline.getUniformLocation("uModel");
        uColor = pipeline.getUniformLocation("uColor");
        uTexture = pipeline.getUniformLocation("uTexture");
        uTextureEnabled = pipeline.getUniformLocation("uTextureEnabled");
        uTexture2 = pipeline.getUniformLocation("uTexture2");
        uTexture2Enabled = pipeline.getUniformLocation("uTexture2Enabled");
    }

    public int getFaceCount() {
        return faces.size();
    }

    public int faceVertexCountAt(int i) {
        return faces.get(i).length;
    }

    public int faceVertexAt(int i, int j) {
        return faces.get(i)[j];
    }

    public void faceNormalAt(int i, Vector3f normal) {
        float x1 = vBuf.get(faceVertexAt(i, 0) * 7 + 0);
        float y1 = vBuf.get(faceVertexAt(i, 0) * 7 + 1);
        float z1 = vBuf.get(faceVertexAt(i, 0) * 7 + 2);
        float x2 = vBuf.get(faceVertexAt(i, 1) * 7 + 0);
        float y2 = vBuf.get(faceVertexAt(i, 1) * 7 + 1);
        float z2 = vBuf.get(faceVertexAt(i, 1) * 7 + 2);
        float x3 = vBuf.get(faceVertexAt(i, 2) * 7 + 0);
        float y3 = vBuf.get(faceVertexAt(i, 2) * 7 + 1);
        float z3 = vBuf.get(faceVertexAt(i, 2) * 7 + 2);

        p1.set(x1, y1, z1).mulPosition(model);
        p2.set(x2, y2, z2).mulPosition(model);
        p3.set(x3, y3, z3).mulPosition(model);

        p2.sub(p1, v1).normalize();
        p3.sub(p2, v2).normalize();

        v1.cross(v2, normal).normalize();
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public void vertexAt(int i, Vector3f position) {
        position.set(vBuf.get(i * 7 + 0), vBuf.get(i * 7 + 1), vBuf.get(i * 7 + 2));
    }

    public void setVertexPositionAt(int i, float x, float y, float z) {
        vBuf.put(i * 7 + 0, x);
        vBuf.put(i * 7 + 1, y);
        vBuf.put(i * 7 + 2, z);
    }

    public void setVertexPositionAt(int i, Vector3f position) {
        setVertexPositionAt(i, position.x, position.y, position.z);
    }

    public void setTextureCoordinate2At(int i, float u, float v) {
        vBuf.put(i * 7 + 5, u);
        vBuf.put(i * 7 + 6, v);
    }

    public int getIndexCount() {
        return indexCount;
    }

    public int indexAt(int i) {
        return iBuf.get(i);
    }

    public int getTriangleCount() {
        return getIndexCount() / 3;
    }

    public void triangleAt(int i, Triangle triangle) {
        calcTriangle(i * 3);
        triangle.set(this.triangle);
    }

    public void pushVertex(float x, float y, float z, float s, float t, float u, float v) {
        vBuf = Utils.ensureCapacity(vBuf, 1000 * 7);
        vBuf.put(x);
        vBuf.put(y);
        vBuf.put(z);
        vBuf.put(s);
        vBuf.put(t);
        vBuf.put(u);
        vBuf.put(v);

        bounds.add(x, y, z);

        vertexCount++;
    }

    public void pushFace(int ... indices) {
        int tris = indices.length - 2;

        faces.add(indices.clone());
        for(int i = 0; i != tris; i++, indexCount += 3) {
            iBuf = Utils.ensureCapacity(iBuf, 1000 * 3);
            iBuf.put(indices[0]);
            iBuf.put(indices[i + 1]);
            iBuf.put(indices[i + 2]);
        }
    }

    public void buffer() {
        vBuf = Utils.trimCapacity(vBuf);
        bufferVertices();

        iBuf = Utils.trimCapacity(iBuf);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, veo);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, iBuf, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public void bufferVertices() {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vBuf, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public void render(Matrix4f projection, Matrix4f view) {
        pipeline.begin();
        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, veo);
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glEnableVertexAttribArray(2);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 7 * 4, 0);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 7 * 4, 3 * 4);
        GL20.glVertexAttribPointer(2, 2, GL11.GL_FLOAT, false, 7 * 4, 5 * 4);
        pipeline.set(uProjection, projection);
        pipeline.set(uView, view);
        pipeline.set(uModel, model);
        pipeline.set(uColor, color);
        pipeline.set(uTextureEnabled, texture != null);
        if(texture != null) {
            pipeline.set(uTexture, 0, texture);
        }
        pipeline.set(uTexture2Enabled, texture2 != null);
        if(texture2 != null) {
            pipeline.set(uTexture2, 1, texture2);
        }
        GL11.glDrawElements(GL11.GL_TRIANGLES, getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
        pipeline.end();
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
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

        if(bounds.intersects(collider.origin, collider.direction, collider.time)) {
            int n = iBuf.limit();

            collider.time[0] = t;
            for(int i = 0; i != n; ) {
                i = calcTriangle(i);
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

        if(collider.resolveBounds.touches(bounds)) {
            int n = iBuf.limit();

            for(int i = 0; i != n; ) {
                i = calcTriangle(i);
                if(collider.selectorResolve(triangle)) {
                    hit = true;
                }
            }
        }
        return hit;
    }

    @Override
    public void destroy() throws Exception {
        pipeline.destroy();
        GL30.glDeleteVertexArrays(vao);
        GL15.glDeleteBuffers(vbo);
        GL15.glDeleteBuffers(veo);
        super.destroy();
    }

    private int calcTriangle(int i) {
        int i1 = iBuf.get(i++);
        int i2 = iBuf.get(i++);
        int i3 = iBuf.get(i++);
        float x1 = vBuf.get(i1 * 7 + 0);
        float y1 = vBuf.get(i1 * 7 + 1);
        float z1 = vBuf.get(i1 * 7 + 2);
        float x2 = vBuf.get(i2 * 7 + 0);
        float y2 = vBuf.get(i2 * 7 + 1);
        float z2 = vBuf.get(i2 * 7 + 2);
        float x3 = vBuf.get(i3 * 7 + 0);
        float y3 = vBuf.get(i3 * 7 + 1);
        float z3 = vBuf.get(i3 * 7 + 2);

        triangle.p1.set(x1, y1, z1);
        triangle.p2.set(x2, y2, z2);
        triangle.p3.set(x3, y3, z3);
        triangle.tag = tag;
        triangle.transform(model);

        return i;
    }
}
