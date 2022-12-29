package org.j3d;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Vector;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class LightMapPipeline extends Resource {
    
    public static final int MAX_LIGHTS = 8;

    public final Vector<Light> lights = new Vector<>();
    public final Vector<DualTexturePipeline> meshes = new Vector<>();

    private final Pipeline pipeline;
    private final int vao, vbo, veo;
    private final int uProjection;
    private final int[] uLightPosition = new int[MAX_LIGHTS];
    private final int[] uLightColor = new int[MAX_LIGHTS];
    private final int[] uLightRadius = new int[MAX_LIGHTS];
    private final int uLightCount;
    private final int uAmbientColor, uDiffuseColor;
    private final RenderTarget renderTarget;
    private final Vector3f p1 = new Vector3f();
    private final Vector3f p2 = new Vector3f();
    private final Vector3f p3 = new Vector3f();
    private final Vector3f p4 = new Vector3f();
    private final Vector3f u = new Vector3f();
    private final Vector3f v = new Vector3f();
    private final Vector3f normal = new Vector3f();
    private final FloatBuffer vBuf = BufferUtils.createFloatBuffer(8 * 4);
    private final Matrix4f matrix = new Matrix4f();
    private final BoundingBox bounds = new BoundingBox();

    public LightMapPipeline(int width, int height) throws Exception {
        pipeline = new Pipeline(
            new String(IO.readAllBytes(LightMapPipeline.class, "/LightMapVertexShader.glsl")),
            new String(IO.readAllBytes(LightMapPipeline.class, "/LightMapFragmentShader.glsl")),
            "vsInCoord", "vsInPosition", "vsInNormal"
        );
        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();
        veo = GL15.glGenBuffers();
        uProjection = pipeline.getUniformLocation("uProjection");
        for(int i = 0; i != MAX_LIGHTS; i++) {
            uLightPosition[i] = pipeline.getUniformLocation("uLightPosition[" + i + "]");
            uLightColor[i] = pipeline.getUniformLocation("uLightColor[" + i + "]");
            uLightRadius[i] = pipeline.getUniformLocation("uLightRadius[" + i + "]");
        }
        uLightCount = pipeline.getUniformLocation("uLightCount");
        uAmbientColor = pipeline.getUniformLocation("uAmbientColor");
        uDiffuseColor = pipeline.getUniformLocation("uDiffuseColor");
        renderTarget = new RenderTarget(width, height, PixelFormat.VECTOR4);

        ByteBuffer iBuf = BufferUtils.createByteBuffer(6);

        iBuf.put((byte)0);
        iBuf.put((byte)1);
        iBuf.put((byte)2);
        iBuf.put((byte)2);
        iBuf.put((byte)3);
        iBuf.put((byte)0);
        iBuf.flip();

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, veo);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, iBuf, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public Texture getTexture() {
        return renderTarget.getTexture(0);
    }

    public void addLight(float x, float y, float z, float r, float g, float b, float radius) {
        Light light = new Light();

        light.position.set(x, y, z);
        light.color.set(r, g, b);
        light.radius = radius;

        lights.add(light);
    }

    public boolean map() {
        boolean ok = true;
        int width = renderTarget.getTexture(0).width;
        int height = renderTarget.getTexture(0).height;

        renderTarget.begin();
        Utils.clear(1, 0, 1, 1);
        Utils.setCullState(CullState.NONE);
        Utils.setDepthState(DepthState.NONE);
        pipeline.begin();
        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, veo);
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glEnableVertexAttribArray(2);
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 8 * 4, 0);
        GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 8 * 4, 2 * 4);
        GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, 8 * 4, 5 * 4);
        matrix.identity().ortho(0, width, height, 0, -1, 1);
        pipeline.set(uProjection, matrix);   
        ok = doMap();
        pipeline.end();
        renderTarget.end();
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);     

        for(DualTexturePipeline mesh : meshes) {
            if(ok) {
                mesh.texture2 = renderTarget.getTexture(0);
            } else {
                mesh.texture2 = null;
            }
        }
        return ok;
    }

    @Override
    public void destroy() throws Exception {
        pipeline.destroy();
        renderTarget.destroy();
        GL30.glDeleteVertexArrays(vao);
        GL15.glDeleteBuffers(vbo);
        GL15.glDeleteBuffers(veo);
        super.destroy();
    }

    private boolean doMap() {
        int width = renderTarget.getTexture(0).width;
        int height = renderTarget.getTexture(0).height;
        int x = 0;
        int y = 0;
        int mh = 0;

        for(DualTexturePipeline mesh : meshes) {
            for(int i = 0; i != mesh.getFaceCount(); i++) {
                if(mesh.faceVertexCountAt(i) != 4) {
                    Log.log(0, "mesh face " + i + " not a quad");
                    return false;
                }
                mesh.faceNormalAt(i, normal);
                mesh.vertexAt(mesh.faceVertexAt(i, 0), p1);
                mesh.vertexAt(mesh.faceVertexAt(i, 1), p2);
                mesh.vertexAt(mesh.faceVertexAt(i, 2), p3);
                mesh.vertexAt(mesh.faceVertexAt(i, 3), p4);

                p2.sub(p1, u);
                u.cross(normal, v);

                int w = (int)(u.length() / 16) + 1;
                int h = (int)(v.length() / 16) + 1;

                if(x + w >= width) {
                    if(y + mh >= height) {
                        Log.log(0, "failed to allocate light map");
                        return false;
                    }
                    x = 0;
                    y += mh;
                    mh = 0;
                } 
                if(y + h >= height) {
                    Log.log(0, "failed to allocate light map");
                    return false;
                }
                mh = Math.max(h, mh);

                mesh.setTextureCoordinate2At(mesh.faceVertexAt(i, 0), (x + 0.5f + 0) / (float)width, 1 - (y + 0.5f + 0) / (float)height);
                mesh.setTextureCoordinate2At(mesh.faceVertexAt(i, 1), (x + w - 0.5f) / (float)width, 1 - (y + 0.5f + 0) / (float)height);
                mesh.setTextureCoordinate2At(mesh.faceVertexAt(i, 2), (x + w - 0.5f) / (float)width, 1 - (y + h - 0.5f) / (float)height);
                mesh.setTextureCoordinate2At(mesh.faceVertexAt(i, 3), (x + 0.5f + 0) / (float)width, 1 - (y + h - 0.5f) / (float)height);

                vBuf.put(x);
                vBuf.put(y);
                vBuf.put(p1.x);
                vBuf.put(p1.y);
                vBuf.put(p1.z);
                vBuf.put(normal.x);
                vBuf.put(normal.y);
                vBuf.put(normal.z);

                vBuf.put(x + w);
                vBuf.put(y);
                vBuf.put(p2.x);
                vBuf.put(p2.y);
                vBuf.put(p2.z);
                vBuf.put(normal.x);
                vBuf.put(normal.y);
                vBuf.put(normal.z);

                vBuf.put(x + w);
                vBuf.put(y + h);
                vBuf.put(p3.x);
                vBuf.put(p3.y);
                vBuf.put(p3.z);
                vBuf.put(normal.x);
                vBuf.put(normal.y);
                vBuf.put(normal.z);

                vBuf.put(x);
                vBuf.put(y + h);
                vBuf.put(p4.x);
                vBuf.put(p4.y);
                vBuf.put(p4.z);
                vBuf.put(normal.x);
                vBuf.put(normal.y);
                vBuf.put(normal.z);

                vBuf.flip();

                GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vBuf, GL15.GL_DYNAMIC_DRAW);

                vBuf.position(0);
                vBuf.limit(vBuf.capacity());

                pipeline.set(uAmbientColor, mesh.ambientColor);
                pipeline.set(uDiffuseColor, mesh.diffuseColor);

                int lightCount = 0;

                bounds.clear();
                bounds.add(p1);
                bounds.add(p2);
                bounds.add(p3);
                bounds.add(p4);
                bounds.min.sub(1, 1, 1);
                bounds.max.add(1, 1, 1);
                for(Light light : lights) {
                    BoundingBox lightBounds = light.calcBounds();

                    if(lightBounds.touches(bounds)) {
                        int l = lightCount++;
                        
                        pipeline.set(uLightPosition[l], light.position);
                        pipeline.set(uLightColor[l], light.color);
                        pipeline.set(uLightRadius[l], light.radius);

                        if(lightCount == MAX_LIGHTS) {
                            break;
                        }
                    }
                }
                pipeline.set(uLightCount, lightCount);

                GL15.glDrawElements(GL11.GL_TRIANGLES, 6, GL11.GL_UNSIGNED_BYTE, 0);

                x += w;
            }
            mesh.bufferVertices();
        }
        return true;
    }
}
