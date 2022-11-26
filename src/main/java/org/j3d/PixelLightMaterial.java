package org.j3d;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Vector;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class PixelLightMaterial extends Resource implements Material {

    public static final int MAX_LIGHTS = 3;

    public static class Light {
        public final Vector3f position = new Vector3f();
        public final Vector3f color = new Vector3f(1, 1, 1);
        public float radius = 400;
    }

    public final Vector<Light> lights = new Vector<>();
    public final Vector4f ambientColor = new Vector4f(0.2f, 0.2f, 0.2f, 1);
    public final Vector4f diffuseColor = new Vector4f(0.9f, 0.9f, 0.9f, 1);
    public Texture texture = null;

    private final Pipeline pipeline;
    private final int uProjection, uView, uModel, uModelIT;
    private final int[] uLightPosition = new int[MAX_LIGHTS];
    private final int[] uLightColor = new int[MAX_LIGHTS];
    private final int[] uLightRadius = new int[MAX_LIGHTS];
    private final int uLightCount, uAmbientColor, uDiffuseColor, uTexture, uTextureEnabled;
    private final int vao, vbo, veo;
    private final Matrix4f modelIT = new Matrix4f();

    public PixelLightMaterial() throws Exception {
        pipeline = new Pipeline(
            new String(IO.readAllBytes(PixelLightMaterial.class, "/PixelLightVertexShader.glsl")), 
            new String(IO.readAllBytes(PixelLightMaterial.class, "/PixelLightFragmentShader.glsl")), 
            "vsInPosition", "vsInTextureCoordinate", "vsInNormal");
        uProjection = pipeline.getUniformLocation("uProjection");
        uView = pipeline.getUniformLocation("uView");
        uModel = pipeline.getUniformLocation("uModel");
        uModelIT = pipeline.getUniformLocation("uModelIT");
        for(int i = 0; i != MAX_LIGHTS; i++) {
            uLightPosition[i] = pipeline.getUniformLocation("uLightPosition[" + i + "]");
            uLightColor[i] = pipeline.getUniformLocation("uLightColor[" + i + "]");
            uLightRadius[i] = pipeline.getUniformLocation("uLightRadius[" + i + "]");
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
    public void buffer(FloatBuffer vBuf, boolean dynamic) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vBuf, (dynamic) ? GL15.GL_DYNAMIC_DRAW : GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    @Override
    public void buffer(IntBuffer iBuf) {
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, veo);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, iBuf, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    @Override
    public void render(Matrix4f projection, Matrix4f view, Matrix4f model, int indexCount) {
        int count = Math.min(MAX_LIGHTS, lights.size());

        pipeline.begin();
        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, veo);
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glEnableVertexAttribArray(2);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 32, 0);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 32, 12);
        GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, 32, 20);
        pipeline.set(uProjection, projection);
        pipeline.set(uView, view);
        pipeline.set(uModel, model);
        pipeline.set(uModelIT, model.invert(modelIT).transpose());
        pipeline.set(uLightCount, count);
        for(int i = 0; i != count; i++) {
            Light light = lights.get(i);

            pipeline.set(uLightPosition[i], light.position);
            pipeline.set(uLightColor[i], light.color);
            pipeline.set(uLightRadius[i], light.radius);
        }
        pipeline.set(uAmbientColor, ambientColor);
        pipeline.set(uDiffuseColor, diffuseColor);
        pipeline.set(uTextureEnabled, texture != null);
        if(texture != null) {
            pipeline.set(uTexture, 0, texture);
        }
        GL15.glDrawElements(GL11.GL_TRIANGLES, indexCount, GL11.GL_UNSIGNED_INT, 0);
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
}
