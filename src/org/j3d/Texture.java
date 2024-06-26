package org.j3d;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class Texture implements Resource {

    public final byte[] pixels;
    public final int w;
    public final int h;
    public final File file;

    private int id = 0;

    public Texture(int w, int h, File file) {
        pixels = new byte[w * h * 4];
        this.w = w;
        this.h = h;
        this.file = file;
        for (int i = 0; i != pixels.length; i++) {
            pixels[i] = (byte)255;
        }
    }

    public static Texture load(File file) throws IOException {
        BufferedImage image = ImageIO.read(file.getAbsoluteFile());
        Texture texture = new Texture(image.getWidth(), image.getHeight(), file);
        int[] pixels = new int[image.getWidth() * image.getHeight()];

        image.getRGB(0, 0, texture.w, texture.h, pixels, 0, texture.w);

        texture.setPixels(pixels);

        return texture;
    }

    public static Texture load(InputStream input) throws IOException {
        BufferedImage image = ImageIO.read(input);
        Texture texture = new Texture(image.getWidth(), image.getHeight(), null);
        int[] pixels = new int[image.getWidth() * image.getHeight()];

        image.getRGB(0, 0, texture.w, texture.h, pixels, 0, texture.w);

        texture.setPixels(pixels);

        return texture;
    }

    private void setPixels(int[] iPixels) {
        for(int x = 0; x != w; x++) {
            for(int y = 0; y != h; y++) {
                int iPixel = iPixels[y * w + x];
                int r = (int)(iPixel >> 16) & 0xFF;
                int g = (int)(iPixel >> 8) & 0xFF;
                int b = (int)iPixel & 0xFF;
                int a = (int)(iPixel >> 24) & 0xFF;
                int j = y * w * 4 + x * 4;

                pixels[j++] = (byte)r;
                pixels[j++] = (byte)g;
                pixels[j++] = (byte)b;
                pixels[j] = (byte)a;
            }
        }
    }

    public int getID() {
        if(id == 0) {
            ByteBuffer buf = BufferUtils.createByteBuffer(pixels.length);

            buf.put(pixels);
            buf.flip();
            id = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, w, h, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        }
        return id;
    }

    @Override
    public String toString() {
        if(file != null) {
            return file.getPath();
        }
        return "Texture";
    }

    @Override
    public void destroy() throws Exception {
        if(id != 0) {
            GL11.glDeleteTextures(id);
            id = 0;
        }
    }
}
