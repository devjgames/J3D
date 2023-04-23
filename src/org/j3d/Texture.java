package org.j3d;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Texture {

    public final int[] pixels;
    public final int w;
    public final int h;
    public final File file;

    public Texture(int w, int h, File file) {
        pixels = new int[w * h];
        this.w = w;
        this.h = h;
        this.file = file;
        for (int i = 0; i != pixels.length; i++) {
            pixels[i] = 0xFFFFFFFF;
        }
    }

    protected Texture(int w, int h, Object file) {
        pixels = null;
        this.w = w;
        this.h = h;
        this.file = (File)file;
    }

    public static Texture load(File file) throws IOException {
        BufferedImage image = ImageIO.read(file.getAbsoluteFile());
        Texture texture = new Texture(image.getWidth(), image.getHeight(), file);

        image.getRGB(0, 0, texture.w, texture.h, texture.pixels, 0, texture.w);

        return texture;
    }

    @Override
    public String toString() {
        return file.getPath();
    }
}
