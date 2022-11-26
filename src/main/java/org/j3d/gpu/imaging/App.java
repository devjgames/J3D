package org.j3d.gpu.imaging;

import java.io.File;
import java.util.Vector;

import org.j3d.BlendState;
import org.j3d.Font;
import org.j3d.Game;
import org.j3d.IO;
import org.j3d.PixelFormat;
import org.j3d.RenderTarget;
import org.j3d.Resource;
import org.j3d.UIManager;
import org.j3d.Utils;
import org.j3d.gpu.ParameterPipeline;
import org.lwjgl.glfw.GLFW;

public class App {

    private static Game game = null;
    private static Image image = null;
    private static RenderTarget renderTarget = null;
    private static UIManager manager = null;
    private static Vector<String> images = new Vector<>();
    private static boolean resetParameters = false;
    private static boolean showImages = false;
    private static boolean render = false;

    public static void main(String[] args) throws Exception {
        try {
            game = new Game(1200, 800, true);
            manager = new UIManager(game, game.getResources().manage(new Font(IO.file("assets/pics/font.fnt"))));
            
            GLFW.glfwSetWindowTitle(game.getWindow(), "JImage");
            GLFW.glfwSwapInterval(1);

            listImages();

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
                renderTarget.begin();
                if(image != null) {
                    if(render) {
                        render = false;
                        image.render(renderTarget.getTexture(0));
                    }
                }
                renderTarget.end();
                game.beginRenderTarget();
                Utils.clear(0, 0, 0, 1);
                game.getSpritePipeline().begin(game.getRenderTargetWidth(), game.getRenderTargetHeight());
                Utils.setBlendState(BlendState.OPAQUE);
                game.getSpritePipeline().beginSprite(renderTarget.getTexture(0));
                game.getSpritePipeline().push(0, 0, w, h, 0, 50, w, h, 1, 1, 1, 1, true);
                game.getSpritePipeline().endSprite();
                Utils.setBlendState(BlendState.ALPHA);
                game.getSpritePipeline().beginSprite(manager.getFont());
                String info = "FPS=" + game.getFPS() + ", RES=" + Resource.getInstances();
                int ch = manager.getFont().getCharHeight();
                game.getSpritePipeline().push(manager.getFont(), info, 5, 10, game.getRenderTargetHeight() - ch - 10, 1, 1, 1, 1);
                game.getSpritePipeline().endSprite();
                game.getSpritePipeline().end();
                manager.begin();
                handleUI();
                manager.end();
                game.nextFrame();
            }
        } finally {
            if(game != null) {
                game.destroy();
            }
        }
    }

    private static void listImages() {
        File[] files = IO.file("assets/images").listFiles();

        if(files != null) {
            for(File file : files) {
                if(IO.extension(file).equals(".glsl")) {
                    images.add(IO.fileNameWithOutExtension(file));
                }
            }
        }
        images.sort((a, b) -> a.compareTo(b));
    }

    private static void handleUI() throws Exception {
        Object changed;

        manager.moveTo(10, 10);
        if(manager.label("images-label", 0, "image", 0, showImages)) {
            showImages = !showImages;
        }
        if(manager.label("fs-label", 5, "fs", 0, game.isFullscreen())) {
            game.toggleFullscreen();
        }
        if(image != null) {
            if(manager.label("reset-parameters-label", 5, "reset", 0, false)) {
                image.resetParameters();
                resetParameters = true;
                render = true;
            }
            if(manager.label("render-label", 5, "render", 0, false)) {
                render = true;
            }
            showImages();
            if(image != null) {
                manager.moveTo(game.getRenderTargetWidth() - 345, 10);
                for(int i = 0; i != image.getUniformCount(); i++) {
                    String name = image.getUniformName(i);
                    String value = image.getUniformValue(name);
                    int type = image.getUniformType(name);

                    manager.addRow(5);
                    if(type == ParameterPipeline.BOOL) {
                        if(manager.label("UNIFORM-" + name + " " + image.file, 0, name, 0, value.equals("true"))) {
                            if(value.equals("true")) {
                                image.setUniformValue(name, "false");
                            } else {
                                image.setUniformValue(name, "true");
                            }
                        }
                    } else {
                        if((changed = manager.textField("UNIFORM-" + name + image.file, 0, name, value, resetParameters, 20)) != null) {
                            image.setUniformValue(name, (String)changed);
                        }
                    }
                }
                resetParameters = false;
            }
        } else {
            showImages();
        }
    }

    private static void showImages() throws Exception {
        if(showImages) {
            Object changed;
            manager.addRow(5);
            if((changed = manager.list("image-list", 0, images, 20, 6, -2)) != null) {
                if(image != null) {
                    game.getResources().unManage(image);
                }
                image = game.getResources().manage(new Image(IO.file(IO.file("assets/images"), images.get((Integer)changed) + ".glsl")));

                String error = image.getError();

                if(error != null) {
                    System.out.println(error);
                    game.getResources().unManage(image);
                    image = null;
                } else {
                    resetParameters = true;
                    render = true;
                }
            }
        }
    }
}
