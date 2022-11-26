package org.j3d.gpu.imaging;

import java.io.File;

import org.j3d.IO;
import org.j3d.Texture;
import org.j3d.gpu.ParameterPipeline;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class Image extends ParameterPipeline {

    public final File file;

    private int uMin = -1;
    private int uMax = -1;
    private int vao = 0;
    private int vbo = 0;

    public Image(File file) throws Exception {
        super(IO.readAllBytes(Image.class, "/org/j3d/gpu/imaging/Vertex.glsl"), IO.readAllBytes(file), "vsInPosition");

        this.file = file;

        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, new float[] {
            -1, -1,
            +1, -1,
            +1, +1,
            +1, +1,
            -1, +1,
            -1, -1
        }, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        if(getError() == null) {
            uMin = getPipeline().getUniformLocation("uMin");
            uMax = getPipeline().getUniformLocation("uMax");
        }
    }

    public void render(Texture texture) {
        float aspect = texture.width / (float)texture.height;

        getPipeline().begin();
        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 8, (long)0);
        getPipeline().set(uMin, -aspect, -1);
        getPipeline().set(uMax, +aspect, +1);
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