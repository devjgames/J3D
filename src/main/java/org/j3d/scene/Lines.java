package org.j3d.scene;

import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.j3d.BoundingBox;
import org.j3d.IO;
import org.j3d.Material;
import org.j3d.Pipeline;
import org.j3d.Resource;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.j3d.Triangle;
import org.j3d.Utils;

public class Lines extends Resource implements Renderable, Material {

    public Node selection = null;
    public Scene scene = null;
    public final Vector3f selectionColor = new Vector3f(1, 0, 1);

    private Pipeline pipeline;
    private int vao, vbo;
    private int uProjection, uView, uModel;
    private BoundingBox bounds = new BoundingBox();
    private FloatBuffer vBuf = BufferUtils.createFloatBuffer(10 * 6);
    private Vector3f r = new Vector3f();
    private Vector3f u = new Vector3f();
    private Vector3f f = new Vector3f();
    private Triangle triangle = new Triangle();
    private int vertexCount = 0;

    public Lines() throws Exception {
        pipeline = new Pipeline(
            new String(IO.readAllBytes(Pipeline.class, "/LineVertexShader.glsl")),
            new String(IO.readAllBytes(Pipeline.class, "/LineFragmentShader.glsl")),
            "vsInPosition", "vsInColor"
            );
        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();
        uProjection = pipeline.getUniformLocation("uProjection");
        uView = pipeline.getUniformLocation("uView");
        uModel = pipeline.getUniformLocation("uModel");
    }

    @Override
    public void setSource(Object source) {
    }

    @Override
    public File getFile() {
        return null;
    }

    @Override
    public int getMaterialCount() {
         return 1;
    }

    @Override
    public Material materialAt(int i) {
        return this;
    }

    @Override
    public BoundingBox getBounds() {
        return bounds;
    }

    @Override
    public int getTriangleCount() {
        return 0;
    }

    @Override
    public void triangleAt(int i, Triangle triangle) {  
    }

    @Override
    public void begin(Object data) {
        pipeline.begin();
    }

    @Override
    public void render(Matrix4f projection, Matrix4f view, Matrix4f model) {
        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 6 * 4, 0);
        GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 6 * 4, 3 * 4);
        pipeline.set(uProjection, scene.projection);
        pipeline.set(uView, scene.view);
        pipeline.set(uModel, model);
        GL11.glDrawArrays(GL11.GL_LINES, 0, vertexCount);
    }

    @Override
    public void end() {
        pipeline.end();
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    @Override
    public void buffer(FloatBuffer vBuf, boolean dynamic) {
    }

    @Override
    public void buffer(IntBuffer iBuf) {
    }

    @Override
    public boolean isEqualTo(Material material) {
        if(material instanceof Lines) {
            if(material == this) {
                return true;
            } 
        }
        return false;
    }

    @Override
    public void update(Scene scene, Node node) throws Exception {
        Vector3f t = scene.target;
        float d;

        vertexCount = 0;

        scene.eye.sub(scene.target, f);
        d = f.length() / 6;
        pushLine(t.x, t.y, t.z, t.x + d, t.y, t.z, 1, 0, 0);
        pushLine(t.x, t.y, t.z, t.x, t.y + d, t.z, 0, 1, 0);
        pushLine(t.x, t.y, t.z, t.x, t.y, t.z + d, 0, 0, 1);

        pushLights(scene.root);
        pushTransforms(scene.root);
        if(selection != null) {
            pushTriangles(selection);
        }
        vBuf.flip();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vBuf, GL15.GL_DYNAMIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        vBuf.limit(vBuf.capacity());
        vBuf.position(0);
    }

    @Override
    public Renderable newInstance() throws Exception {
        return this;
    }
    
    @Override
    public void destroy() throws Exception {
        pipeline.destroy();
        GL30.glDeleteVertexArrays(vao);
        GL15.glDeleteBuffers(vbo);
        super.destroy();
    }

    private void pushLine(float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b) {
        vBuf = Utils.ensureCapacity(vBuf, 1000 * 6);
        vBuf.put(x1);
        vBuf.put(y1);
        vBuf.put(z1);
        vBuf.put(r);
        vBuf.put(g);
        vBuf.put(b);
        vBuf.put(x2);
        vBuf.put(y2);
        vBuf.put(z2);
        vBuf.put(r);
        vBuf.put(g);
        vBuf.put(b);
        vertexCount += 2;
    }

    private void pushLights(Node node) {
        if(node.isLight) {
            Vector3f p = node.absolutePosition;
            float d = 8;

            pushLine(p.x - d, p.y, p.z, p.x + d, p.y, p.z, 1, 0, 0);
            pushLine(p.x, p.y - d, p.z, p.x, p.y + d, p.z, 0, 1, 0);
            pushLine(p.x, p.y, p.z - d, p.x, p.y, p.z + d, 0, 0, 1);
        }
        for(Node child : node) {
            pushLights(child);
        }
    }

    private void pushTransforms(Node node) {
        if(node.getParent() != null && node.renderable == null && !node.isLight) {
            Vector3f p = node.absolutePosition;
            float d = 8;

            node.model.getColumn(0, r).normalize().mul(d);
            node.model.getColumn(1, u).normalize().mul(d);
            node.model.getColumn(2, f).normalize().mul(d);

            pushLine(p.x - r.x, p.y - r.y, p.z - r.z, p.x + r.x, p.y + r.y, p.z + r.z, 1, 0, 0);
            pushLine(p.x - u.x, p.y - u.y, p.z - u.z, p.x + u.x, p.y + u.y, p.z + u.z, 0, 1, 0);
            pushLine(p.x - f.x, p.y - f.y, p.z - f.z, p.x + f.x, p.y + f.y, p.z + f.z, 0, 0, 1);
        }
        for(Node child : node) {
            pushTransforms(child);
        }
    }

    private void pushTriangles(Node node) {
        float r = selectionColor.x;
        float g = selectionColor.y;
        float b = selectionColor.z;

        if(node.visible) {
            Vector3f p1 = triangle.p1;
            Vector3f p2 = triangle.p2;
            Vector3f p3 = triangle.p3;

            for(int i = 0; i != node.getTriangleCount(); i++) {
                node.triangleAt(i, triangle);
                pushLine(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z, r, g, b);
                pushLine(p2.x, p2.y, p2.z, p3.x, p3.y, p3.z, r, g, b);
                pushLine(p3.x, p3.y, p3.z, p1.x, p1.y, p1.z, r, g, b);
            }
            for(Node child : node) {
                pushTriangles(child);
            }
        } 
    }
}
