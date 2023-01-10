package org.j3d;

import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Vector;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class TexturePipeline extends Resource implements TrianglePipeline {

    public static final int COMPONENTS = 6;
    public static final int MAX_TEXTURES = 6;

    public final Vector4f color = new Vector4f(1, 1, 1, 1);
    public final Texture[] textures = new Texture[MAX_TEXTURES];

    private final File file;
    private final Pipeline pipeline;
    private final int uProjection, uView, uModel;
    private final int[] uTextures = new int[MAX_TEXTURES];
    private final int uColor;
    private final int vao, vbo, veo;
    private FloatBuffer vBuf = BufferUtils.createFloatBuffer(COMPONENTS * 4 * 6);
    private IntBuffer iBuf = BufferUtils.createIntBuffer(6 * 6);
    private final BoundingBox bounds = new BoundingBox();
    private final Triangle triangle = new Triangle();
    private int vertexCount = 0;
    private final Matrix4f model = new Matrix4f();
    private int triangleTag = 1;
    private final Vector<int[]> faces = new Vector<>();

    public TexturePipeline(File file) throws Exception {
        this.file = file;

        pipeline = new Pipeline(
            new String(IO.readAllBytes(TexturePipeline.class, "/TextureVertexShader.glsl")), 
            new String(IO.readAllBytes(TexturePipeline.class, "/TextureFragmentShader.glsl")),
            "vsInPosition", "vsInTextureCoordinate", "vsInTextureIndex"
        );
        uProjection = pipeline.getUniformLocation("uProjection");
        uView = pipeline.getUniformLocation("uView");
        uModel = pipeline.getUniformLocation("uModel");
        for(int i = 0; i != MAX_TEXTURES; i++) {
            uTextures[i] = pipeline.getUniformLocation("uTexture" + i);
        }
        uColor = pipeline.getUniformLocation("uColor");
        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();
        veo = GL15.glGenBuffers();

        for(int i = 0; i != MAX_TEXTURES; i++) {
            textures[i] = null;
        }
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public int getTriangleTag() {
        return triangleTag;
    }

    @Override
    public void setTriangleTag(int tag) {
        triangleTag = tag;
    }

    @Override
    public int getFaceCount() {
        return faces.size();
    }

    @Override
    public int getFaceVertexCount(int i) {
        return faces.get(i).length;
    }

    @Override
    public int faceVertexAt(int i, int j) {
        return faces.get(i)[j];
    }

    @Override
    public Matrix4f getModel() {
        return model;
    }

    @Override
    public void setTransform(float x, float y, float z, float rx, float ry, float rz, float scale) {
        model.identity()
            .translate(x, y, z)
            .rotate(Utils.toRadians(rx), 1, 0, 0)
            .rotate(Utils.toRadians(ry), 0, 1, 0)
            .rotate(Utils.toRadians(rz), 0, 0, 1)
            .scale(scale);
    }

    @Override
    public int getTriangleCount() {
        return iBuf.limit() / 3;
    }

    @Override
    public void triangleAt(int i, Triangle triangle) {
        calcTriangle(i * 3);
        triangle.set(this.triangle);
    }

    @Override
    public int getVertexCount() {
        return vertexCount;
    }

    @Override
    public float vertexAt(int i, int j) {
        return vBuf.get(i * COMPONENTS + j);
    }
    
    @Override
    public void setVertexAt(int i, int j, float x) {
        vBuf.put(i * COMPONENTS + j, x);
    }

    @Override
    public BoundingBox getBounds() {
        return bounds;
    }

    public void pushVertex(float x, float y, float z, float u, float v, int index) {
        vBuf = Utils.ensureCapacity(vBuf, 1000 * COMPONENTS * 4);
        vBuf.put(x);
        vBuf.put(y);
        vBuf.put(z);
        vBuf.put(u);
        vBuf.put(v);
        vBuf.put(index + 0.1f);

        bounds.add(x, y, z);

        vertexCount++;
    }

    public void pushFace(int ... indices) {
        int tris = indices.length - 2;

        for(int i = 0; i != tris; i++) {
            iBuf = Utils.ensureCapacity(iBuf, 1000 * 6);
            iBuf.put(indices[0]);
            iBuf.put(indices[i + 1]);
            iBuf.put(indices[i + 2]);
        }
        faces.add(indices.clone());
    }

    public void buffer() {
        vBuf = Utils.trimCapacity(vBuf);
        bufferVertices();

        iBuf = Utils.trimCapacity(iBuf);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, veo);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, iBuf, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    @Override
    public void bufferVertices() {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vBuf, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    @Override
    public void begin(Matrix4f projection, Matrix4f view) {
        pipeline.begin();
        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, veo);
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, COMPONENTS * 4, 0);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, COMPONENTS * 4, 3 * 4);
        pipeline.set(uProjection, projection);
        pipeline.set(uView, view);
        for(int i = 0; i != MAX_TEXTURES; i++) {
            Texture texture = textures[i];

            if(texture != null) {
                pipeline.set(uTextures[i], i, texture);
            }
        }
        pipeline.set(uColor, color);
    }

    @Override
    public void render() {
        pipeline.set(uModel, model);
        GL11.glDrawElements(GL11.GL_TRIANGLES, iBuf.limit(), GL11.GL_UNSIGNED_INT, 0);
    }

    @Override
    public void end() {
        pipeline.end();
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    @Override
    public void render(Matrix4f projection, Matrix4f view) {
        begin(projection, view);
        render();
        end();
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
        
        triangle.p1.set(vBuf.get(i1 * COMPONENTS + 0), vBuf.get(i1 * COMPONENTS + 1), vBuf.get(i1 * COMPONENTS + 2));
        triangle.p2.set(vBuf.get(i2 * COMPONENTS + 0), vBuf.get(i2 * COMPONENTS + 1), vBuf.get(i2 * COMPONENTS + 2));
        triangle.p3.set(vBuf.get(i3 * COMPONENTS + 0), vBuf.get(i3 * COMPONENTS + 1), vBuf.get(i3 * COMPONENTS + 2));
        triangle.calcPlane();
        triangle.tag = triangleTag;
        triangle.transform(model);

        return i;
    }
}
