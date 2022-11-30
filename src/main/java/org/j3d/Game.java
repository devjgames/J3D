package org.j3d;

import java.nio.ByteBuffer;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

public class Game {

    private long device, context;
    private final long window;
    private final ResourceManager resources;
    private final AssetManager assets;
    private final SpritePipeline spritePipeline;
    private RenderTarget renderTarget = null;
    private float lastTime;
    private float elapsedTime;
    private float totalTime;
    private float seconds;
    private int frames;
    private int fps;
    private int[] ww = new int[1];
    private int[] wh = new int[1];
    private int[] fw = new int[1];
    private int[] fh = new int[1];
    private double[] x = new double[1];
    private double[] y = new double[1];
    private int lx = 0;
    private int ly = 0;
    private int dx = 0;
    private int dy = 0;
    private long monitor;
    private GLFWVidMode mode;
    private boolean fullscreen = false;
    private int[] sx = new int[1];
    private int[] sy = new int[1];
    private int[] sw = new int[1];
    private int[] sh = new int[1];
    private boolean fpsMouseEnabled = false;

    public Game(int width, int height, boolean resizable) throws Exception {

        device = ALC10.alcOpenDevice((ByteBuffer) null);
        ALCCapabilities caps = ALC.createCapabilities(device);
        context = ALC10.alcCreateContext(device, new int[]{0});
        ALC10.alcMakeContextCurrent(context);
        AL.createCapabilities(caps);
        checkOpenALErrors(device);

        if (!GLFW.glfwInit()) {
            throw new Exception("failed to initialize GLFW");
        }

        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, (resizable) ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_OPENGL_API);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);

        window = GLFW.glfwCreateWindow(width, height, "jgame", 0, 0);
        if (window == 0) {
            GLFW.glfwTerminate();
            throw new Exception("failed to create GLFW window");
        }

        GLFW.glfwMakeContextCurrent(window);
        GL.createCapabilities();
        GLFW.glfwSwapInterval(0);

        resources = new ResourceManager();

        assets = resources.manage(new AssetManager());
        spritePipeline = resources.manage(new SpritePipeline());

        lx = getMouseX();
        ly = getMouseY();

        monitor = GLFW.glfwGetPrimaryMonitor();
        mode = GLFW.glfwGetVideoMode(monitor);
        
        resetTimer();
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    public void toggleFullscreen() {
        fullscreen = !fullscreen;
        if(fullscreen) {
            GLFW.glfwGetWindowPos(window, sx, sy);
            GLFW.glfwGetWindowSize(window, sw, sh);
            GLFW.glfwSetWindowMonitor(window, monitor, 0, 0, mode.width(), mode.height(), mode.refreshRate());
        } else {
            GLFW.glfwSetWindowMonitor(window, 0, sx[0], sy[0], sw[0], sh[0], 0);
        }
    }

    public void enableFPSMouse() {
        if(!fpsMouseEnabled) {
            GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
            fpsMouseEnabled = true;
        }
    }

    public void disableFPSMouse() {
        if(fpsMouseEnabled) {
            GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
            fpsMouseEnabled = false;
        }
    }

    public long getALDevice() {
        return device;
    }

    public long getALContext() {
        return context;
    }

    public long getWindow() {
        return window;
    }

    public ResourceManager getResources() {
        return resources;
    }

    public AssetManager getAssets() {
        return assets;
    }

    public SpritePipeline getSpritePipeline() {
        return spritePipeline;
    }

    public float getElapsedTime() {
        return elapsedTime;
    }

    public float getTotalTime() {
        return totalTime;
    }

    public int getFPS() {
        return fps;
    }

    public int getRenderTargetWidth() {
        if(renderTarget != null) {
            return renderTarget.getTexture(0).width;
        }
        return getWidth();
    }

    public int getRenderTargetHeight() {
        if(renderTarget != null) {
            return renderTarget.getTexture(0).height;
        }
        return getHeight();
    }

    private int getWidth() {
        GLFW.glfwGetWindowSize(window, ww, wh);
        return ww[0];
    }

    private int getHeight() {
        GLFW.glfwGetWindowSize(window, ww, wh);
        return wh[0];
    }

    private int getFramebufferWidth() {
        GLFW.glfwGetFramebufferSize(window, fw, fh);
        return fw[0];
    }

    private int getFramebufferHeight() {
        GLFW.glfwGetFramebufferSize(window, fw, fh);
        return fh[0];
    }

    private int getFramebufferScale() {
        return getFramebufferWidth() / getWidth();
    }

    public float getAspectRatio() {
        return getWidth() / (float) getHeight();
    }

    public int getMouseX() {
        GLFW.glfwGetCursorPos(window, x, y);
        return (int) x[0];
    }

    public int getMouseY() {
        GLFW.glfwGetCursorPos(window, x, y);
        return (int) y[0];
    }

    public int getDeltaX() {
        return dx;
    }

    public int getDeltaY() {
        return dy;
    }

    public boolean isButtonDown(int button) {
        return GLFW.glfwGetMouseButton(window, button) == GLFW.GLFW_PRESS;
    }

    public boolean isKeyDown(int key) {
        return GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
    }

    public void resetTimer() {
        lastTime = (float) GLFW.glfwGetTime();
        elapsedTime = 0;
        totalTime = 0;
        seconds = 0;
        frames = 0;
        fps = 0;
    }

    public boolean run() throws Exception {
        if (!GLFW.glfwWindowShouldClose(window)) {
            GLFW.glfwPollEvents();
            GL11.glViewport(0, 0, getFramebufferWidth(), getFramebufferHeight());
            
            return true;
        }
        return false;
    }

    public void beginRenderTarget() throws Exception {
        if(getFramebufferScale() > 1) {
            int w = getFramebufferWidth() / getFramebufferScale();
            int h = getFramebufferHeight() / getFramebufferScale();
    
            if(renderTarget != null) {
                if(w > 1 && h > 1 && (w != renderTarget.getTexture(0).width || h != renderTarget.getTexture(0).height)) {
                    Log.log(1, "creating render target -> " + w + " x " + h + ", scale -> " + getFramebufferScale() + " ...");
                    resources.unManage(renderTarget);
                    renderTarget = resources.manage(new RenderTarget(w, h, PixelFormat.COLOR));
                }
            } else {
                Log.log(1, "creating render target -> " + w + " x " + h + ", scale -> " + getFramebufferScale() + " ...");
                renderTarget = resources.manage(new RenderTarget(w, h, PixelFormat.COLOR));
            }
            renderTarget.begin();
        }
    }

    public void nextFrame() {
        if(getFramebufferScale() > 1) {
            int w = getFramebufferWidth();
            int h = getFramebufferHeight();
            Texture texture = renderTarget.getTexture(0);
        
            renderTarget.end();
            Utils.clear(0, 0, 0, 1);
            getSpritePipeline().begin(w, h);
            spritePipeline.beginSprite(texture);
            spritePipeline.push(0, 0, texture.width, texture.height, 0, 0, getFramebufferWidth(), getFramebufferHeight(), 1, 1, 1, 1, true);
            spritePipeline.endSprite();
            getSpritePipeline().end();
          }

        Utils.checkError("nextFrame()");
        GLFW.glfwSwapBuffers(window);

        float now = (float) GLFW.glfwGetTime();

        elapsedTime = now - lastTime;
        lastTime = now;
        totalTime += elapsedTime;
        seconds += elapsedTime;
        frames++;
        if (seconds >= 1) {
            fps = frames;
            frames = 0;
            seconds = 0;
        }

        int x = getMouseX();
        int y = getMouseY();

        dx = lx - x;
        dy = y - ly;

        lx = x;
        ly = y;
    }

    public void destroy() throws Exception {
        Log.log(1, Resource.getInstances() + " allocated resources");
        checkOpenALErrors(device);
        ALC10.alcDestroyContext(context);
        ALC10.alcCloseDevice(device);
        getResources().destroy();
        Log.log(1, Resource.getInstances() + " allocated resources");
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }

    private void checkOpenALErrors(long device) {
        int error = ALC10.alcGetError(device);
        if (error != ALC10.ALC_NO_ERROR) {
            Log.log(0, "OpenAL Context Error - " + error);
        }
        error = AL10.alGetError();
        if (error != AL10.AL_NO_ERROR) {
            Log.log(0, "OpenAL Error - " + error);
        }
    }
}
