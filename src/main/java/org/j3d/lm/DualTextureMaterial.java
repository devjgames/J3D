package org.j3d.lm;

import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.j3d.AssetManager;
import org.j3d.IO;
import org.j3d.Material;
import org.j3d.Mesh;
import org.j3d.MeshData;
import org.j3d.MeshDataPart;
import org.j3d.MeshDataVertex;
import org.j3d.MeshPart;
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

    private Pipeline pipeline;
    private int vao, vbo, veo;
    private int uProjection, uView, uModel;
    private int uTexture, uTextureEnabled;
    private int uTexture2, uTexture2Enabled;
    private int uColor;
    private MeshPart source = null;

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
    }

    @Override
    public void setSource(Object source) {
        this.source = (MeshPart)source;
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
    public void begin(Object data) {
        pipeline.begin();
        pipeline.set(uTextureEnabled, texture != null);
        if(texture != null) {
            pipeline.set(uTexture, 0, texture);
        }
        pipeline.set(uTexture2Enabled, texture2 != null);
        if(texture2 != null) {
            pipeline.set(uTexture2, 1, texture2);
        }
        pipeline.set(uColor, color);
    }

    @Override
    public void render(Matrix4f projection, Matrix4f view, Matrix4f model) {
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
        GL15.glDrawElements(GL11.GL_TRIANGLES, source.getIndexCount(), GL11.GL_UNSIGNED_INT, 0);

    }

    @Override
    public void end() {
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

    @Override
    public boolean isEqualTo(Material material) {
        if(material instanceof DualTextureMaterial) {
            if(material == this) {
                return true;
            } 
        } 
        return false;
    }

    public static Mesh load(File file, AssetManager assets) throws Exception {
        Mesh mesh = new Mesh();
        MeshData data = new MeshData(file);

        for(MeshDataPart dataPart : data.parts) {
            MeshPart part = new MeshPart(mesh, 7);
            DualTextureMaterial material = assets.getResources().manage(new DualTextureMaterial());

            if(dataPart.texture != null) {
                material.texture = assets.load(dataPart.texture);
            }
            for(MeshDataVertex vertex : dataPart.vertices) {
                part.push(vertex.position.x, vertex.position.y, vertex.position.z);
                part.push(vertex.textureCoordinate.x, vertex.textureCoordinate.y);
                part.push(0, 0);
            }
            for(int[] face : dataPart.faces) {
                part.pushFace(face);
            }
            part.material = material;
            part.trim();
            part.bufferVertices(false);
            part.bufferIndices();
            part.calcBounds();
            mesh.addMeshPart(part);
        }
        mesh.calcBounds();

        return mesh;
    }
}
