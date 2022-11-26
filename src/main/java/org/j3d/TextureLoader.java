package org.j3d;

import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;

public class TextureLoader implements AssetLoader {

    @Override
    public Object load(File file, AssetManager assets) throws Exception {
        int width = 0;
        int height = 0;
        byte[] pixels = null;
        ByteBuffer bufPixels = null;
        BufferedImage image = null;
        int[] iPixels = null;

        image = ImageIO.read(file);
        width = image.getWidth();
        height = image.getHeight();
        pixels = new byte[width * height * 4];
        iPixels = new int[width * height];
        image.getRGB(0, 0, width, height, iPixels, 0, width);
        bufPixels = BufferUtils.createByteBuffer(pixels.length);
        for (int x = 0; x != width; x++) {
            for (int y = 0; y != height; y++) {
                int p = iPixels[y * width + x];
                int r = (p >> 16) & 0xFF;
                int g = (p >> 8) & 0xFF;
                int b = p & 0xFF;
                int a = (p >> 24) & 0xFF;
                int i = y * width * 4 + x * 4;

                pixels[i++] = (byte) r;
                pixels[i++] = (byte) g;
                pixels[i++] = (byte) b;
                pixels[i] = (byte) a;
            }
        }

        bufPixels.put(pixels);
        bufPixels.flip();

        return new Texture(file, width, height, PixelFormat.COLOR, bufPixels);
    }
}
