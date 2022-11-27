package org.j3d.gpu.fractal;

import org.j3d.Font;
import org.j3d.Game;
import org.j3d.IO;
import org.j3d.Parser;
import org.j3d.PixelFormat;
import org.j3d.RenderTarget;
import org.j3d.Resource;
import org.j3d.UIManager;
import org.j3d.Utils;
import org.j3d.gpu.ParameterPipeline;
import org.lwjgl.glfw.GLFW;

public class App {

    private static Game game = null;
    private static Fractal fractal = null;
    private static RenderTarget renderTarget = null;
    private static UIManager manager = null;
    private static boolean resetParameters = false;
    private static boolean render = true;
    private static boolean down = false;
    private static float zoom = 0.8f;
    private static boolean resetZoom = true;

    public static void main(String[] args) throws Exception {
        try {
            game = new Game(1200, 800, true);
            manager = new UIManager(game, game.getResources().manage(new Font(IO.file("assets/pics/font.fnt"))));
            fractal = game.getResources().manage(new Fractal());

            GLFW.glfwSwapInterval(1);

            String error = fractal.getError();

            if(error != null) {
                System.out.println(error);
            }
            
            GLFW.glfwSetWindowTitle(game.getWindow(), "JFractal");

            while(game.run()) {
                int w = game.getRenderTargetWidth() - 350;
                int h = game.getRenderTargetHeight() - 85;

                if(renderTarget == null) {
                    renderTarget = game.getResources().manage(new RenderTarget(w, h, PixelFormat.COLOR));
                } else if(w > 1 && h > 1 && (w != renderTarget.getTexture(0).width || h != renderTarget.getTexture(0).height)) {
                    game.getResources().unManage(renderTarget);
                    renderTarget = game.getResources().manage(new RenderTarget(w, h, PixelFormat.COLOR));
                    render = true;
                }
                if(render) {
                    render = false;
                    renderTarget.begin();
                    Utils.clear(0, 0, 0, 1);
                    fractal.render(renderTarget.getTexture(0));
                    renderTarget.end();
                }
                game.beginRenderTarget();
                Utils.clear(0, 0, 0, 1);
                game.getSpritePipeline().begin(game.getRenderTargetWidth(), game.getRenderTargetHeight());
                game.getSpritePipeline().beginSprite(renderTarget.getTexture(0));
                game.getSpritePipeline().push(0, 0, w, h, 0, 50, w, h, 1, 1, 1, 1, true);
                game.getSpritePipeline().endSprite();
                game.getSpritePipeline().beginSprite(manager.getFont());
                String info = "FPS=" + game.getFPS() + ", RES=" + Resource.getInstances();
                int ch = manager.getFont().getCharHeight();
                game.getSpritePipeline().push(manager.getFont(), info, 5, 10, game.getRenderTargetHeight() - ch - 10, 1, 1, 1, 1);
                game.getSpritePipeline().endSprite();
                game.getSpritePipeline().end();
                manager.begin();
                handleUI();
                boolean handled = manager.end();
                game.nextFrame();

                if(game.isButtonDown(0)) {
                    if(!down) {
                        down = true;
                        if(!handled) {
                            fractal.zoom(renderTarget.getTexture(0), game.getMouseX(), game.getMouseY() - 50, zoom);
                            render = true;
                        }
                    }
                } else {
                    down = false;
                }
            }
        } finally {
            if(game != null) {
                game.destroy();
            }
        }
    }

    private static void handleUI() throws Exception {
        Object changed;

        manager.moveTo(10, 10);
        if(manager.label("fs-label", 0, "fs", 0, game.isFullscreen())) {
            game.toggleFullscreen();
        }
        if(manager.label("reset-window-label", 5, "win", 0, false)) {
            fractal.resetWindow();
            render = true;
        }
        if(manager.label("reset-parameters-label", 5, "params", 0, false)) {
            fractal.resetParameters();
            resetParameters = true;
            render = true;
        }
        if(manager.label("render-label", 5, "render", 0, false)) {
            render = true;
        }
        manager.moveTo(game.getRenderTargetWidth() - 345, 10);
        if((changed = manager.textField("zoom-field", 0, "Zoom Amount", "" + zoom, resetZoom, 20)) != null) {
            zoom = Parser.parse(((String)changed).split("\\s+"), 0, zoom);
        }
        resetZoom = false;
        for(int i = 0; i != fractal.getUniformCount(); i++) {
            String name = fractal.getUniformName(i);
            String value = fractal.getUniformValue(name);
            int type = fractal.getUniformType(name);

            manager.addRow(5);
            if(type == ParameterPipeline.BOOL) {
                if(manager.label("UNIFORM-" + name, 0, name, 0, value.equals("true"))) {
                    if(value.equals("true")) {
                        fractal.setUniformValue(name, "false");
                    } else {
                        fractal.setUniformValue(name, "true");
                    }
                    render = true;
                }
            } else {
                if((changed = manager.textField("UNIFORM-" + name, 0, name, value, resetParameters, 20)) != null) {
                    fractal.setUniformValue(name, (String)changed);
                }
            }
        }
        resetParameters = false;
    }
}
