package org.j3d.lm.demo1;

import java.io.File;
import java.util.Vector;

import org.j3d.Collider;
import org.j3d.Font;
import org.j3d.Game;
import org.j3d.IO;
import org.j3d.Log;
import org.j3d.Mesh;
import org.j3d.MeshPart;
import org.j3d.OctTree;
import org.j3d.Resource;
import org.j3d.Sound;
import org.j3d.Texture;
import org.j3d.Triangle;
import org.j3d.Utils;
import org.j3d.lm.DualTextureMaterial;
import org.j3d.lm.LightMapper;
import org.j3d.lm.Surface;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class App {

    public static void main(String[] args) throws Exception {
        Game game = null;

        Log.level = 2;

        try {
            game = new Game(1000, 700, false);

            Mesh mesh = DualTextureMaterial.load(IO.file("assets/meshes/lm1.obj"), game.getAssets());
            Collider collider = new Collider();
            Vector<Triangle> triangles = new Vector<>();
            Vector3f eye = new Vector3f(0, 100, 0);
            Vector3f direction = new Vector3f(1, 0, 0);
            Vector3f up = new Vector3f(0, 1, 0);
            Vector3f f = new Vector3f();
            boolean spaceDown = false;
            boolean sKeyDown = false;
            boolean lKeyDown = false;
            boolean mKeyDown = false;
            boolean tKeyDown = false;
            boolean aKeyDown = false;
            boolean sync = true;
            boolean linear = true;
            float ao = 0.5f;
            Font font = game.getAssets().load(IO.file("assets/pics/font.fnt"));
            Matrix4f projection = new Matrix4f();
            Matrix4f view = new Matrix4f();
            Sound sound = game.getAssets().load(IO.file("assets/sounds/ambient.wav"));

            for(MeshPart part : mesh) {
                DualTextureMaterial material = (DualTextureMaterial)part.material;
  
                material.color.set(2, 2, 2, 1);
                for(int i = 0; i != part.getFaceCount(); i++) {
                    Surface surface = new Surface();
                    
                    if(material.texture == null) {
                        surface.color.set(10, 12, 14);
                        surface.emitsLight = true;
                        material.emitsLight = true;
                    }
                    part.setFaceData(i, surface);
                }
                for(int i = 0; i != part.getTriangleCount(); i++) {
                    Triangle triangle = new Triangle();

                    part.triangleAt(i, triangle);
                    triangles.add(triangle);
                }
            }
            collider.addTriangleSelector(new OctTree.Selector(OctTree.create(triangles,16)));

            File file = IO.file("assets/lightmaps/demo1.png");

            map(file, game, mesh, ao, linear);

            game.enableFPSMouse();

            sound.setVolume(0);
            sound.play(true);

            GLFW.glfwSwapInterval(1);

            game.resetTimer();

            while(game.run()) {
                String info = "FPS = " + game.getFPS();
                
                info += "\nRES = " + Resource.getInstances();
                info += "\nCOL = " + collider.getTested();
                info += "\nSPC = FS\nM   = Map\nT   = Texture\nA   = AO\nL   = Linear\nS   = Sync\nESC = Quit";

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
                game.getSpritePipeline().push(font, info, 5, 10, 10, 1, 0.5f, 0, 1);
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
                if(game.isKeyDown(GLFW.GLFW_KEY_L)) {
                    if(!lKeyDown) {
                        lKeyDown = true;
                        linear = !linear;
                        for(MeshPart part : mesh) {
                            DualTextureMaterial material = (DualTextureMaterial)part.material;
                            material.texture2.bind();
                            if(linear) {
                                material.texture2.toLinear(true);
                            } else {
                                material.texture2.toNearest(true);
                            }
                            material.texture2.unBind();
                        }
                    }
                } else {
                    lKeyDown = false;
                }
                if(game.isKeyDown(GLFW.GLFW_KEY_M)) {
                    if(!mKeyDown) {
                        file.delete();
                        mKeyDown = true;
                        game.getAssets().unload(file);
                        map(file, game, mesh, ao, linear);
                    }
                } else {
                    mKeyDown = false;
                }
                if(game.isKeyDown(GLFW.GLFW_KEY_T)) {
                    if(!tKeyDown) {
                        tKeyDown = true;
                        for(MeshPart part : mesh) {
                            DualTextureMaterial material = (DualTextureMaterial)part.material;

                            if(part.data != null) {
                                material.texture = (Texture)part.data;
                                part.data = null;
                            } else {
                                part.data = material.texture;
                                material.texture = null;
                            }
                        }
                    }
                } else {
                    tKeyDown = false;
                }
                if(game.isKeyDown(GLFW.GLFW_KEY_A)) {
                    if(!aKeyDown) {
                        aKeyDown = true;
                        if(ao < 0.6f) {
                            ao = 1;
                        } else {
                            ao = 0.5f;
                        }
                    }
                } else {
                    aKeyDown = false;
                }
            }
        } finally {
            if(game != null) {
                game.destroy();
            }
        }
    }

    private static void map(File file, Game game, Mesh mesh, float ao, boolean linear) throws Exception {
        DualTextureMaterial material;

        for(MeshPart part : mesh) {
            material = (DualTextureMaterial)part.material;

            if(part.data != null) {
                material.texture = (Texture)part.data;
            }
        }

        if(new LightMapper().map(file, IO.file("assets/meshes/samples.obj"), 128, 128, 2, ao, mesh)) {
            Texture texture = game.getAssets().load(file);

            if(linear) {
                texture.bind();
                texture.toLinear(true);
                texture.unBind();
            }

            for(MeshPart part : mesh) {
                material = (DualTextureMaterial)part.material;
                material.texture2 = texture;
            }
        } else {
            Log.log(0, "failed to allocate light map");
            for(MeshPart part : mesh) {
                material = (DualTextureMaterial)part.material;
                material.texture2 = null;
            }
        }

        for(MeshPart part : mesh) {
            material = (DualTextureMaterial)part.material;

            if(part.data != null) {
                material.texture = null;
            }
        }
    }
}
