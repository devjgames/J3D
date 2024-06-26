package org.j3d;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

public final class Game implements Resource {

    private Renderer renderer = null;
    private AssetManager assets = new AssetManager();
    private double lastTime = 0;
    private double totalTime = 0;
    private double elapsedTime = 0;
    private double seconds = 0;
    private int frames = 0;
    private int fps = 0;
    private long window = 0;
    private int deltaX = 0;
    private int deltaY = 0;
    private int lastX = 0;
    private int lastY = 0;
    private int[] w = new int[1];
    private int[] h = new int[1];
    private int scale = 1;
    private double[] mx = new double[1];
    private double[] my = new double[1];

    public Game(int w, int h) throws Exception {
        if(!GLFW.glfwInit()) {
            throw new Exception("failed to initialize GLFW");
        }

        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_DOUBLEBUFFER, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_RED_BITS, 8);
        GLFW.glfwWindowHint(GLFW.GLFW_GREEN_BITS, 8);
        GLFW.glfwWindowHint(GLFW.GLFW_BLUE_BITS, 8);
        GLFW.glfwWindowHint(GLFW.GLFW_ALPHA_BITS, 8);
        GLFW.glfwWindowHint(GLFW.GLFW_DEPTH_BITS, 24);
        GLFW.glfwWindowHint(GLFW.GLFW_STENCIL_BITS, 8);
        GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_OPENGL_API);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);

        window = GLFW.glfwCreateWindow(w, h, "J3D", 0, 0);
        if(window == 0) {
            GLFW.glfwTerminate();
            throw new Exception("failed to create GLFW window");
        }

        GLFW.glfwMakeContextCurrent(window);
        GL.createCapabilities();

        GLFW.glfwSwapInterval(1);

        renderer = new Renderer(this);

        GLFW.glfwGetWindowSize(window, this.w, this.h);

        scale = this.w[0];
        scale = w() / scale;

        resetTimer();
    }

    public long window() {
        return window;
    }

    public Renderer renderer() {
        return renderer;
    }

    public AssetManager assets() { return assets; }

    public int getScale() {
        return scale;
    }

    public int w() {
        GLFW.glfwGetFramebufferSize(window, w, h);
        return w[0];
    }

    public int h() {
        GLFW.glfwGetFramebufferSize(window, w, h);
        return h[0];
    }

    public int getMouseX() {
        GLFW.glfwGetCursorPos(window, mx, my);
        return (int)mx[0] * scale;
    }

    public int getMouseY() {
        GLFW.glfwGetCursorPos(window, mx, my);
        return (int)my[0] * scale;
    }

    public int getDeltaX() {
        return deltaX;
    }

    public int getDeltaY() {
        return deltaY;
    }

    public boolean isButtonDown(int button) {
        return GLFW.glfwGetMouseButton(window, button) == GLFW.GLFW_PRESS;
    }

    public boolean isKeyDown(int key) {
        return GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
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

    public boolean run() {
        GLFW.glfwPollEvents();

        if(GLFW.glfwWindowShouldClose(window)) {
            return false;
        }

        GLFW.glfwMakeContextCurrent(window);
        GL11.glViewport(0, 0, w(), h());

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

        int x = getMouseX();
        int y = getMouseY();

        deltaX = x - lastX;
        deltaY = lastY - y;

        lastX = x;
        lastY = y;

        return true;
    }

    public void swapBuffers() {
        GLFW.glfwSwapBuffers(window);
    }

    @Override
    public void destroy() throws Exception {
        if(renderer != null) {
            renderer.destroy();
        }
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }
}
