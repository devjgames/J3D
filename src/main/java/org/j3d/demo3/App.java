package org.j3d.demo3;

import java.util.Vector;

import org.j3d.BlendState;
import org.j3d.DepthState;
import org.j3d.Game;
import org.j3d.IO;
import org.j3d.Mesh;
import org.j3d.PixelLightMaterial;
import org.j3d.UIManager;
import org.j3d.Utils;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class App {
    
    public static void main(String[] args) throws Exception {
        Game game = null;

        try {
            game = new Game(1000, 700, true);

            Mesh floor = game.getAssets().load(IO.file("assets/meshes/stone-floor.obj"));
            Mesh cube = game.getAssets().load(IO.file("assets/meshes/cube.obj"));
            Vector3f offset = new Vector3f(150, 150, 150);
            Vector3f up = new Vector3f(0, 1, 0);
            Vector3f zero = new Vector3f();
            Matrix4f projection = new Matrix4f();
            Matrix4f view = new Matrix4f();
            UIManager manager = new UIManager(game, game.getAssets().load(IO.file("assets/pics/font.fnt")));
            Vector3f origin = new Vector3f();
            Vector3f direction = new Vector3f();
            float[] time = new float[1];
            float[] values = new float[] { 1, 1, 1 };
            boolean reset = true;
            PixelLightMaterial material = (PixelLightMaterial)floor.meshPartAt(0).material;
            String[] modes = new String[] { "Rotate", "Zoom", "Select" };
            int selection = -1;
            int mode = 0;
            boolean down = false;
            Matrix4f[] models = new Matrix4f[3];
            Vector<Integer> sorted = new Vector<>();
            Vector3f p = new Vector3f();

            material.ambientColor.set(1, 1, 1, 1);
            material.diffuseColor.set(0, 0, 0, 1);

            material = (PixelLightMaterial)cube.meshPartAt(0).material;
            material.ambientColor.set(1, 1, 1, 1);
            material.diffuseColor.set(0, 0, 0, 1);

            models[0] = new Matrix4f().translate(0, 16, 0).rotate((float)Math.PI / 4, 1, 0, 0).rotate((float)Math.PI / 4, 0, 1, 0);
            models[1] = new Matrix4f().translate(-48, 16, -48).rotate((float)Math.PI / 4, 1, 0, 0).rotate((float)Math.PI / 4, 0, 1, 0);
            models[2] = new Matrix4f().translate(48, 16, -48).rotate((float)Math.PI / 4, 1, 0, 0).rotate((float)Math.PI / 4, 0, 1, 0);

            sorted.add(0);
            sorted.add(1);
            sorted.add(2);

            GLFW.glfwSwapInterval(1);

            while(game.run()) {
                projection.identity().perspective((float)Math.PI / 3, game.getAspectRatio(), 1, 25000);
                view.identity().lookAt(offset, zero, up);

                game.beginRenderTarget();

                Utils.clear(0, 0, 0, 1);
                floor.render(projection, view);
                Utils.setBlendState(BlendState.ALPHA);
                Utils.setDepthState(DepthState.READONLY);
                sorted.sort((a, b) -> {
                    float da = p.zero().mulPosition(models[a]).distance(offset);
                    float db = p.zero().mulPosition(models[b]).distance(offset);

                    return Float.compare(db, da);
                });
                for(int i = 0; i != sorted.size(); i++) {
                    cube.model.set(models[sorted.get(i)]);
                    material.ambientColor.w = values[sorted.get(i)];
                    cube.render(projection, view);
                }

                manager.begin();
                manager.moveTo(10, 10);
                if(manager.label("fs-label", 0, "FS", 0, game.isFullscreen())) {
                    game.toggleFullscreen();
                }
                for(int i = 0; i != modes.length; i++) {
                    if(manager.label("mode-" + modes[i], 5, modes[i], 0, i == mode)) {
                        mode = i;
                    }
                }
                if(selection != -1) {
                    Float changed;
                    if((changed = manager.slider("slider", 5, "Alpha", values[selection], 12, reset)) != null) {
                        values[selection] = changed;
                    }
                    reset = false;
                }
                boolean handled = manager.end();
                
                game.nextFrame();

                if(!handled) {
                    if(game.isButtonDown(0)) {
                        if(mode == 0) {
                            Utils.rotateOffsetAndUp(offset, up, game);
                        } else if(mode == 1) {
                            float length = offset.length() + game.getDeltaY();

                            offset.normalize().mul(length);
                        } else {
                            if(!down) {
                                int w = game.getRenderTargetWidth();
                                int h = game.getRenderTargetHeight();
                                int x = game.getMouseX();
                                int y = h - game.getMouseY() - 1;

                                Utils.unProject(x, y, 0, 0, 0, w, h, projection, view, origin);
                                Utils.unProject(x, y, 1, 0, 0, w, h, projection, view, direction);

                                direction.sub(origin).normalize();
                                time[0] = Float.MAX_VALUE;

                                selection = -1;

                                for(int i = 0; i != models.length; i++) {
                                    cube.model.set(models[i]);
                                    cube.calcBounds();
                                    if(cube.bounds.intersects(origin, direction, time)) {
                                        selection = i;
                                        reset = true;    
                                    }
                                }
                            }
                        }
                        down = true;
                    } else {
                        down = false;
                    }
                }
            }
        } finally {
            if(game != null) {
                game.destroy();
            }
        }
    }
}
