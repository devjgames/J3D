package org.j3d;

import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Vector;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class LightPipeline extends Resource implements Asset {

    public static final int MAX_LIGHTS = 6;
    
    public static class Light {
        public final Vector3f vector = new Vector3f(0, -1, 0);
        public final Vector4f color = new Vector4f(1, 1, 1, 1);
        public float radius = 200;
        public boolean directional = true;
    }

    public final Vector<Light> lights = new Vector<>();
    public final Vector4f ambientColor = new Vector4f(0.2f, 0.2f, 0.2f, 1);
    public final Vector4f diffuseColor = new Vector4f(0.8f, 0.8f, 0.8f, 1);
    public Texture texture = null;
    public int triangleTag = 1;
    public final Matrix4f model = new Matrix4f();

    private final File file;
    private final Pipeline pipeline;
    private final int uProjection, uView, uModel, uModelIT;
    private final int[] uLightVector = new int[MAX_LIGHTS];
    private final int[] uLightColor = new int[MAX_LIGHTS];
    private final int[] uLightRadius = new int[MAX_LIGHTS];
    private final int[] uLightDirectional = new int[MAX_LIGHTS];
    private final int uLightCount;
    private final int uAmbientColor, uDiffuseColor;
    private final int uTexture, uTextureEnabled;
    private final int vao, vbo, veo;
    private final Matrix4f matrix = new Matrix4f();
    private FloatBuffer vBuf = BufferUtils.createFloatBuffer(8 * 4 * 6);
    private IntBuffer iBuf = BufferUtils.createIntBuffer(6 * 6);
    private final BoundingBox bounds = new BoundingBox();
    private final Triangle triangle = new Triangle();

    public LightPipeline(File file) throws Exception {
        this.file = file;

        pipeline = new Pipeline(
            new String(IO.readAllBytes(LightPipeline.class, "/LightVertexShader.glsl")), 
            new String(IO.readAllBytes(LightPipeline.class, "/LightFragmentShader.glsl")),
            "vsInPosition", "vsInTextureCoordinate", "vsInNormal"
        );
        uProjection = pipeline.getUniformLocation("uProjection");
        uView = pipeline.getUniformLocation("uView");
        uModel = pipeline.getUniformLocation("uModel");
        uModelIT = pipeline.getUniformLocation("uModelIT");
        for(int i = 0; i != MAX_LIGHTS; i++) {
            uLightVector[i] = pipeline.getUniformLocation("uLightVector[" + i + "]");
            uLightColor[i] = pipeline.getUniformLocation("uLightColor[" + i + "]");
            uLightRadius[i] = pipeline.getUniformLocation("uLightRadius[" + i + "]");
            uLightDirectional[i] = pipeline.getUniformLocation("uLightDirectional[" + i + "]");
        }
        uLightCount = pipeline.getUniformLocation("uLightCount");
        uAmbientColor = pipeline.getUniformLocation("uAmbientColor");
        uDiffuseColor = pipeline.getUniformLocation("uDiffuseColor");
        uTexture = pipeline.getUniformLocation("uTexture");
        uTextureEnabled = pipeline.getUniformLocation("uTextureEnabled");
        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();
        veo = GL15.glGenBuffers();
    }

    @Override
    public File getFile() {
        return file;
    }

    public void setTransform(float x, float y, float z, float rx, float ry, float rz, float scale) {
        model.identity()
            .translate(x, y, z)
            .rotate(Utils.toRadians(rx), 1, 0, 0)
            .rotate(Utils.toRadians(ry), 0, 1, 0)
            .rotate(Utils.toRadians(rz), 0, 0, 1)
            .scale(scale);
    }

    public void addLight(float x, float y, float z, float r, float g, float b, float a, float radius, boolean directional) {
        Light light = new Light();

        light.vector.set(x, y, z);
        light.color.set(r, g, b, a);
        light.radius = radius;
        light.directional = directional;

        lights.add(light);
    }

    public int getTriangleCount() {
        return iBuf.limit() / 3;
    }

    public void triangleAt(int i, Triangle triangle) {
        calcTriangle(i * 3);
        triangle.set(this.triangle);
    }

    public BoundingBox getBounds() {
        return bounds;
    }

    public void pushVertex(float x, float y, float z, float u, float v, float nx, float ny, float nz) {
        vBuf = Utils.ensureCapacity(vBuf, 1000 * 8 * 4);
        vBuf.put(x);
        vBuf.put(y);
        vBuf.put(z);
        vBuf.put(u);
        vBuf.put(v);
        vBuf.put(nx);
        vBuf.put(ny);
        vBuf.put(nz);

        bounds.add(x, y, z);
    }

    public void pushFace(int ... indices) {
        int tris = indices.length - 2;

        for(int i = 0; i != tris; i++) {
            iBuf = Utils.ensureCapacity(iBuf, 1000 * 6);
            iBuf.put(indices[0]);
            iBuf.put(indices[i + 1]);
            iBuf.put(indices[i + 2]);
        }
    }

    public void buffer() {
        vBuf = Utils.trimCapacity(vBuf);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vBuf, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        iBuf = Utils.trimCapacity(iBuf);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, veo);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, iBuf, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public void render(Matrix4f projection, Matrix4f view) {
        int count = Math.min(MAX_LIGHTS, lights.size());

        pipeline.begin();
        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, veo);
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glEnableVertexAttribArray(2);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 8 * 4, 0);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 8 * 4, 3 * 4);
        GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, 8 * 4, 5 * 4);
        pipeline.set(uProjection, projection);
        pipeline.set(uView, view);
        pipeline.set(uModel, model);
        pipeline.set(uModelIT, matrix.set(model).invert().transpose());
        pipeline.set(uLightCount, count);
        for(int i = 0; i != count; i++) {
            Light light = lights.get(i);

            pipeline.set(uLightVector[i], light.vector);
            pipeline.set(uLightColor[i], light.color);
            pipeline.set(uLightRadius[i], light.radius);
            pipeline.set(uLightDirectional[i], light.directional);
        }
        pipeline.set(uAmbientColor, ambientColor);
        pipeline.set(uDiffuseColor, diffuseColor);
        pipeline.set(uTextureEnabled, texture != null);
        if(texture != null) {
            pipeline.set(uTexture, 0, texture);
        }
        GL11.glDrawElements(GL11.GL_TRIANGLES, iBuf.limit(), GL11.GL_UNSIGNED_INT, 0);
        pipeline.end();
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
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
        
        triangle.p1.set(vBuf.get(i1 * 8 + 0), vBuf.get(i1 * 8 + 1), vBuf.get(i1 * 8 + 2));
        triangle.p2.set(vBuf.get(i2 * 8 + 0), vBuf.get(i2 * 8 + 1), vBuf.get(i2 * 8 + 2));
        triangle.p3.set(vBuf.get(i3 * 8 + 0), vBuf.get(i3 * 8 + 1), vBuf.get(i3 * 8 + 2));
        triangle.calcPlane();
        triangle.tag = triangleTag;
        triangle.transform(model);

        return i;
    }
}
