package org.j3d;

import java.awt.Canvas;
import java.awt.Dimension;

import org.lwjgl.opengl.Display;

public final class Game implements Resource {

    private Canvas canvas;
    private Renderer renderer = null;
    private AssetManager assets = new AssetManager();
    private double lastTime = 0;
    private double totalTime = 0;
    private double elapsedTime = 0;
    private double seconds = 0;
    private int frames = 0;
    private int fps = 0;

    public Game(int w, int h) {
        canvas = new Canvas();
        canvas.setPreferredSize(new Dimension(w, h));
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public void createRenderer() throws Exception {
        renderer = new Renderer(this);
    }

    public Renderer renderer() {
        return renderer;
    }

    public AssetManager assets() { return assets; }

    public int w() {
        return Display.getWidth();
    }

    public int h() {
        return Display.getHeight();
    }

    public float aspectRatio() {
        return w() / (float) h();
    }

    public float totalTime() {
        return (float) totalTime;
    }

    public float elapsedTime() {
        return (float) elapsedTime;
    }

    public int frameRate() {
        return fps;
    }

    public void resetTimer() {
        lastTime = System.nanoTime() / 1000000000.0;
        totalTime = 0;
        elapsedTime = 0;
        seconds = 0;
        frames = 0;
        fps = 0;
    }

    public void tick() {
        double nowTime = System.nanoTime() / 1000000000.0;
        elapsedTime = nowTime - lastTime;
        lastTime = nowTime;
        seconds += elapsedTime;
        totalTime += elapsedTime;
        frames++;
        if (seconds >= 1) {
            fps = frames;
            frames = 0;
            seconds = 0;
        }
    }

    @Override
    public void destroy() throws Exception {
        if(renderer != null) {
            renderer.destroy();
        }
    }
}
