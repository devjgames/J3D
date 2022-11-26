package org.j3d;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class Texture extends Resource implements Asset {

    private final File file;

    public final int width;
    public final int height;

    private int id;

    public Texture(File file, int width, int height, PixelFormat format, ByteBuffer pixels) {
        this.file = file;
        this.width = width;
        this.height = height;
        id = GL11.glGenTextures();
        bind();
        if (pixels != null) {
            toNearest(false);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA,
                    GL11.GL_UNSIGNED_BYTE, pixels);
        } else {
            if (format == PixelFormat.COLOR) {
                toNearest(true);
                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA,
                        GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
            } else {
                toLinear(true);
                if (format == PixelFormat.VECTOR4) {
                    GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RGBA32F, width, height, 0, GL11.GL_RGBA,
                            GL11.GL_FLOAT, (FloatBuffer) null);
                } else {
                    GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_R32F, width, height, 0, GL11.GL_RED, GL11.GL_FLOAT,
                            (FloatBuffer) null);
                }
            }
        }
        unBind();
    }

    @Override
    public File getFile() {
        return file;
    }

    public int getTexture() {
        return id;
    }

    public void bind() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
    }

    public void unBind() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    public void toNearest(boolean clampToEdge) {
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S,
                (clampToEdge) ? GL15.GL_CLAMP_TO_EDGE : GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T,
                (clampToEdge) ? GL15.GL_CLAMP_TO_EDGE : GL11.GL_REPEAT);
    }

    public void toLinear(boolean clampToEdge) {
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S,
                (clampToEdge) ? GL15.GL_CLAMP_TO_EDGE : GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T,
                (clampToEdge) ? GL15.GL_CLAMP_TO_EDGE : GL11.GL_REPEAT);
    }

    @Override
    public void destroy() throws Exception {
        GL11.glDeleteTextures(id);
        super.destroy();
    }
}
