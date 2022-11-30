package org.j3d.lm;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.j3d.PixelFormat;
import org.j3d.RenderTarget;
import org.j3d.Resource;
import org.j3d.Texture;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public class Texel extends Resource {
    
    public final Matrix4f projection = new Matrix4f();
    public final Matrix4f view = new Matrix4f();
    public final Vector3f eye = new Vector3f();
    public final Vector3f direction = new Vector3f();
    public final FloatBuffer pixels;
    
    private final Texture texture;
    private final ByteBuffer buf;
    private Renderer renderer;
    private RenderTarget target;
    private int x, y;

    Texel(Renderer renderer, int width, int height, int viewSize) throws Exception {
        this.renderer = renderer;

        buf = BufferUtils.createByteBuffer(width * height * 4);
        for(int i = 0; i != buf.capacity(); i += 4) {
            buf.put((byte)255);
            buf.put((byte)255);
            buf.put((byte)255);
            buf.put((byte)255);
        }
        buf.flip();
        texture = new Texture(null, width, height, PixelFormat.COLOR, buf);

        target = new RenderTarget(viewSize, viewSize, PixelFormat.VECTOR4);
        pixels = BufferUtils.createFloatBuffer(viewSize * viewSize * 4);
    }

    public void setPixel(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void updateTexels() {
        texture.bind();
        GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, texture.width, texture.height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
        texture.unBind();
    }

    public void render() {
        target.begin();
        renderer.render(projection, view, texture);
        target.end();

        Texture targetTexture = target.getTexture(0);

        targetTexture.bind();
        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_FLOAT, pixels);
        targetTexture.unBind();
    }

    public int getViewSize() {
        return target.getTexture(0).width;
    }

    public void setColor(float r, float g, float b) {
        float max = Math.max(r, Math.max(g, b));

        if(max > 1) {
            r /= max;
            g /= max;
            b /= max;
        }

        byte br = (byte)(r * 255);
        byte bg = (byte)(g * 255);
        byte bb = (byte)(b * 255);
        int i = y * texture.width * 4 + x * 4;

        buf.put(i++, br);
        buf.put(i++, bg);
        buf.put(i++, bb);
        buf.put(i, (byte)255);
    }

    public int getColor() {
        int i = y * texture.width * 4 + x * 4;
        int r = ((int)buf.get(i + 0)) & 0xFF;
        int g = ((int)buf.get(i + 1)) & 0xFF;
        int b = ((int)buf.get(i + 2)) & 0xFF;
        
        return 0xFF000000 | ((r << 16) & 0xFF0000) | ((g << 8) & 0xFF00) | (b & 0xFF);
    }

    @Override
    public void destroy() throws Exception {
        texture.destroy();
        target.destroy();

        super.destroy();
    }
}
