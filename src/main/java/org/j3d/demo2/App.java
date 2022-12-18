package org.j3d.demo2;

import java.util.Vector;

import org.j3d.Collider;
import org.j3d.Font;
import org.j3d.Game;
import org.j3d.IO;
import org.j3d.Mesh;
import org.j3d.MeshPart;
import org.j3d.PixelLightMaterial;
import org.j3d.Resource;
import org.j3d.Triangle;
import org.j3d.Utils;
import org.j3d.Light;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class App {
    
    public static void main(String[] args) throws Exception {
        Game game = null;

        try {
            game = new Game(1000, 700, true);

            Mesh mesh = game.getAssets().load(IO.file("assets/meshes/temple.obj"));
            Mesh cube = game.getAssets().load(IO.file("assets/meshes/cube.obj"));
            Collider collider = new Collider();
            Vector<Triangle> triangles = new Vector<>();
            Vector3f target = new Vector3f(0, 100, 0);
            Vector3f offset = new Vector3f(100, 100, 100);
            Vector3f up = new Vector3f(0, 1, 0);
            Vector3f f = new Vector3f();
            Vector3f r = new Vector3f();
            boolean spaceDown = false;
            boolean sKeyDown = false;
            boolean sync = true;
            Font font = game.getAssets().load(IO.file("assets/pics/font.fnt"));
            Matrix4f projection = new Matrix4f();
            Matrix4f view = new Matrix4f();
            float radians = 0;
            float radians2 = 0;
            PixelLightMaterial material = (PixelLightMaterial)cube.meshPartAt(0).material;

            material.ambientColor.set(1, 1, 1, 1);
            material.diffuseColor.set(0, 0, 0, 1);

            for(MeshPart part : mesh) {
                material = (PixelLightMaterial)part.material;
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
                collider.addTriangleSelector(part);
            }
            collider.radius = 20;

            GLFW.glfwSwapInterval(1);

            while(game.run()) {
                String info = "FPS = " + game.getFPS();
                
                info += "\nRES = " + Resource.getInstances();
                info += "\nCOL = " + collider.getTested();
                info += "\nSPC = FS\nS   = Sync\nESC = Quit";

                projection.identity().perspective((float)Math.PI / 3, game.getAspectRatio(), 1, 25000);
                target.add(offset, offset);
                view.identity().lookAt(offset, target, up);
                offset.sub(target, offset);

                cube.model.identity().translate(target).rotate(radians, 0, 1, 0).rotate(radians2, 0, 0, 1);
                
                game.beginRenderTarget();
                Utils.clear(0, 0, 1, 1);
                mesh.render(projection, view);
                cube.render(projection, view);
                game.getSpritePipeline().begin(game.getRenderTargetWidth(), game.getRenderTargetHeight());
                game.getSpritePipeline().beginSprite(font);
                game.getSpritePipeline().push(font, info, 5, 10, 10, 1, 1, 1, 0.5f);
                game.getSpritePipeline().endSprite();
                game.getSpritePipeline().end();
                game.nextFrame();

                if(game.isButtonDown(1)) {
                    Utils.rotateOffsetAndUp(offset, up, game);
                }

                float dx = game.getMouseX() - game.getRenderTargetWidth() / 2;
                float dy = game.getMouseY() - game.getRenderTargetHeight() / 2;
                float dl = Vector2f.length(dx, dy);

                offset.negate(f).mul(1, 0, 1);

                collider.velocity.mul(0, 1, 0);
                if(game.isButtonDown(0) && dl > 0.1 && f.length() > 0.0000001) {
                    f.normalize().cross(0, 1, 0, r).normalize().mul(dx / dl * 150);
                    f.mul(-dy / dl * 150).add(r);
                    collider.velocity.add(f);
                    f.normalize();

                    radians = (float)Math.acos(Math.max(-0.99f, Math.min(0.99f, f.x)));

                    if(f.z > 0) {
                        radians = (float)Math.PI * 2 - radians;
                    }
                    radians2 -= (float)Math.PI * game.getElapsedTime();
                }
                collider.velocity.y -= 2000 * game.getElapsedTime();
                collider.collide(game, target);

                float length = 150;

                collider.origin.set(target);
                offset.normalize(collider.direction);
                collider.time[0] = length + 19;
                collider.intersectionBuffer = 1;
                if(collider.intersect() != null) {
                    length = Math.min(length, collider.time[0]) - 19;
                }
                offset.normalize().mul(length);

                if(game.isKeyDown(GLFW.GLFW_KEY_ESCAPE)) {
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
