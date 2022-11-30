package org.j3d.lm;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.j3d.IO;
import org.j3d.Material;
import org.j3d.Pipeline;
import org.j3d.Resource;
import org.j3d.Texture;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class DualTextureMaterial extends Resource implements Material {

    public Texture texture = null;
    public Texture texture2 = null;
    public final Vector4f color = new Vector4f(1, 1, 1, 1);
    public final Vector4f emissiveColor = new Vector4f(1, 1, 1, 1);
    public boolean emissiveColorEnabled = false;

    private Pipeline pipeline;
    private int vao, vbo, veo;
    private int uProjection, uView, uModel;
    private int uTexture, uTextureEnabled;
    private int uTexture2, uTexture2Enabled;
    private int uColor;
    private int uEmissiveColor;
    private int uEmissiveColorEnabled;

    public DualTextureMaterial() throws Exception {
        pipeline = new Pipeline(
            new String(IO.readAllBytes(DualTextureMaterial.class, "/DualTextureVertexShader.glsl")),
            new String(IO.readAllBytes(DualTextureMaterial.class, "/DualTextureFragmentShader.glsl")),
            "vsInPosition", "vsInTextureCoordinate", "vsInTextureCoordinate2"
        );
        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();
        veo = GL15.glGenBuffers();

        uProjection = pipeline.getUniformLocation("uProjection");
        uView = pipeline.getUniformLocation("uView");
        uModel = pipeline.getUniformLocation("uModel");
        uTexture = pipeline.getUniformLocation("uTexture");
        uTextureEnabled = pipeline.getUniformLocation("uTextureEnabled");
        uTexture2 = pipeline.getUniformLocation("uTexture2");
        uTexture2Enabled = pipeline.getUniformLocation("uTexture2Enabled");
        uColor = pipeline.getUniformLocation("uColor");
        uEmissiveColor = pipeline.getUniformLocation("uEmissiveColor");
        uEmissiveColorEnabled = pipeline.getUniformLocation("uEmissiveColorEnabled");
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
        pipeline.begin();
        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, veo);
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glEnableVertexAttribArray(2);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 28, 0);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 28, 12);
        GL20.glVertexAttribPointer(2, 2, GL11.GL_FLOAT, false, 28, 20);
        pipeline.set(uProjection, projection);
        pipeline.set(uView, view);
        pipeline.set(uModel, model);
        pipeline.set(uTextureEnabled, texture != null);
        if(texture != null) {
            pipeline.set(uTexture, 0, texture);
        }
        pipeline.set(uTexture2Enabled, texture2 != null && !emissiveColorEnabled);
        if(texture2 != null && !emissiveColorEnabled) {
            pipeline.set(uTexture2, 1, texture2);
        }
        pipeline.set(uColor, color);
        pipeline.set(uEmissiveColor, emissiveColor);
        pipeline.set(uEmissiveColorEnabled, emissiveColorEnabled);
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
