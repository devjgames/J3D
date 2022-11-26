package org.j3d.gpu.fractal;

import org.j3d.IO;
import org.j3d.Texture;
import org.j3d.gpu.ParameterPipeline;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class Fractal extends ParameterPipeline {

    private int vao;
    private int vbo;
    private int uMin, uMax, uPixelSize;
    private final Vector2f min = new Vector2f(-1.5f, -1.5f);
    private final Vector2f max = new Vector2f(+1.5f, +1.5f);
    
    public Fractal() throws Exception {
        super(IO.readAllBytes(Fractal.class, "/org/j3d/gpu/fractal/Vertex.glsl"), IO.readAllBytes(Fractal.class, "/org/j3d/gpu/fractal/Fragment.glsl"), "vsInPosition");

        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, 
        new float[] {
            -1, -1,
            +1, -1,
            +1, +1,
            +1, +1,
            -1, +1,
            -1, -1,
        }, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        if(getPipeline() != null) {
            uMin = getPipeline().getUniformLocation("uMin");
            uMax = getPipeline().getUniformLocation("uMax");
            uPixelSize = getPipeline().getUniformLocation("uPixelSize");
        }
    }

    public void resetWindow() {
        min.set(-1.5f, -1.5f);
        max.set(+1.5f, +1.5f);
    }

    public void zoom(Texture texture, int x, int y, float amount) {
        y = texture.height - y - 1;

        float aspect = texture.width / (float)texture.height;
        float dx = max.x - min.x;
        float dy = max.y - min.y;
        float px = (max.x + min.x) / 2;
        float cx = px - dx * aspect / 2 + dx * aspect * x / (float)texture.width;
        float cy = min.y + dy * y / (float)texture.height;

        min.set(cx - dx / 2 * amount, cy - dy / 2 * amount);
        max.set(cx + dx / 2 * amount, cy + dy / 2 * amount);
    }

    public void render(Texture texture) {
        float aspect = texture.width / (float)texture.height;
        float dx = (max.x - min.x) * aspect;
        float dy = max.y - min.y;
        float cx = (max.x + min.x) / 2;
        float cy = (max.y + min.y) / 2;

        getPipeline().begin();
        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 0, 0);
        getPipeline().set(uMin, cx - dx / 2, cy - dy / 2);
        getPipeline().set(uMax, cx + dx / 2, cy + dy / 2);
        getPipeline().set(uPixelSize, 1.0f / texture.width, 1.0f / texture.height);
        bindParameters();
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
        getPipeline().end();
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    @Override
    public void destroy() throws Exception {
        GL30.glDeleteVertexArrays(vao);
        GL15.glDeleteBuffers(vbo);
        super.destroy();
    }
}
