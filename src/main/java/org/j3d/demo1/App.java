package org.j3d.demo1;

import java.util.Vector;

import org.j3d.Collider;
import org.j3d.Font;
import org.j3d.Game;
import org.j3d.IO;
import org.j3d.Mesh;
import org.j3d.MeshPart;
import org.j3d.OctTree;
import org.j3d.PixelLightMaterial;
import org.j3d.Resource;
import org.j3d.Sound;
import org.j3d.Triangle;
import org.j3d.Utils;
import org.j3d.PixelLightMaterial.Light;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class App {

    public static void main(String[] args) throws Exception {
        Game game = null;

        try {
            game = new Game(1000, 700, false);

            Mesh mesh = game.getAssets().load(IO.file("assets/meshes/temple.obj"));
            Collider collider = new Collider();
            Vector<Triangle> triangles = new Vector<>();
            Vector3f eye = new Vector3f(0, 100, 0);
            Vector3f direction = new Vector3f(1, 0, 0);
            Vector3f up = new Vector3f(0, 1, 0);
            Vector3f f = new Vector3f();
            boolean spaceDown = false;
            boolean sKeyDown = false;
            boolean sync = true;
            Font font = game.getAssets().load(IO.file("assets/pics/font.fnt"));
            Matrix4f projection = new Matrix4f();
            Matrix4f view = new Matrix4f();
            Sound sound = game.getAssets().load(IO.file("assets/sounds/ambient.wav"));

            for(MeshPart part : mesh) {
                PixelLightMaterial material = (PixelLightMaterial)part.material;

                material.lights.add(new Light());
                material.lights.lastElement().color.set(2, 1.5f, 1);
                material.lights.lastElement().radius = 300;
                material.lights.lastElement().position.set(0, 75, 0);
                material.lights.add(new Light());
                material.lights.lastElement().color.set(1, 1.5f, 2);
                material.lights.lastElement().radius = 300;
                material.lights.lastElement().position.set(400, 150, 0);

                for(int i = 0; i != part.getTriangleCount(); i++) {
                    Triangle triangle = new Triangle();

                    part.triangleAt(i, triangle);
                    triangles.add(triangle);
                }
            }
            collider.addTriangleSelector(new OctTree.Selector(OctTree.create(triangles,16)));

            game.enableFPSMouse();

            sound.setVolume(0);
            sound.play(true);

            GLFW.glfwSwapInterval(1);

            while(game.run()) {
                String info = "FPS = " + game.getFPS();
                
                info += "\nRES = " + Resource.getInstances();
                info += "\nCOL = " + collider.getTested();
                info += "\nSPC = FS\nS   = Sync\nESC = Quit";

                sound.setVolume(1 - Math.min(eye.distance(400, 150, 0) / 300, 1));

                projection.identity().perspective((float)Math.PI / 3, game.getAspectRatio(), 1, 25000);
                eye.add(direction, direction);
                view.identity().lookAt(eye, direction, up);
                direction.sub(eye, direction);
                
                game.beginRenderTarget();
                Utils.clear(0, 0, 0, 1);
                mesh.render(projection, view);
                game.getSpritePipeline().begin(game.getRenderTargetWidth(), game.getRenderTargetHeight());
                game.getSpritePipeline().beginSprite(font);
                game.getSpritePipeline().push(font, info, 5, 10, 10, 1, 1, 1, 0.5f);
                game.getSpritePipeline().endSprite();
                game.getSpritePipeline().end();
                game.nextFrame();

                Utils.rotateDirectionAndUp(direction, up, game);

                collider.velocity.mul(0, 1, 0);
                if(game.isButtonDown(0) || game.isButtonDown(1)) {
                    f.set(direction);
                    f.mul(1, 0, 1);
                    if(f.length() > 0.0000001) {
                        f.normalize().mul(100);
                        if(game.isButtonDown(1)) {
                            f.negate();
                        }
                        collider.velocity.add(f);
                    }
                }
                collider.velocity.y -= 2000 * game.getElapsedTime();
                collider.collide(game, eye);

                if(game.isKeyDown(GLFW.GLFW_KEY_ESCAPE)) {
                    game.disableFPSMouse();
                    break;
                }
                if(game.isKeyDown(GLFW.GLFW_KEY_SPACE)) {
                    if(!spaceDown) {
                        spaceDown = true;
                        game.toggleFullscreen();
                    }
                } else { 
                    spaceDown = false;
                }
                if(game.isKeyDown(GLFW.GLFW_KEY_S)) {
                    if(!sKeyDown) {
                        sKeyDown = true;
                        if(sync) {
                            GLFW.glfwSwapInterval(0);
                        } else {
                            GLFW.glfwSwapInterval(1);
                        }
                        sync = !sync;
                    }
                } else {
                    sKeyDown = false;
                }
            }
        } finally {
            if(game != null) {
                game.destroy();
            }
        }
    }
}
