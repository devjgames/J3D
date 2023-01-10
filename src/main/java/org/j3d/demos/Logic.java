package org.j3d.demos;

import org.j3d.BoundingBox;
import org.j3d.Collider;
import org.j3d.Game;
import org.j3d.IO;
import org.j3d.Triangle;
import org.j3d.Utils;
import org.j3d.demos.Scene.Mesh;
import org.lwjgl.glfw.GLFW;

public class Logic {

    private final Triangle triangle = new Triangle();
    private final BoundingBox bounds = new BoundingBox();
    private boolean spaceDown;
    
    public void init(Demo demo, App app, Scene scene, Collider collider, boolean fpsCamera) throws Exception {
        for(int i = 0; i != scene.getMeshCount(); i++) {
            Mesh mesh = scene.meshAt(i);
            String name = IO.fileNameWithOutExtension(mesh.selector.pipeline.getFile());

            if(name.equals("wood-bridge")) {
                mesh.position.y = 32;
                mesh.setTransform();
                mesh.data = new Animator(mesh, -32);
            } else if(name.equals("door-bottom")) {
                mesh.data = new Animator(mesh, -64);
            } else if(name.equalsIgnoreCase("door-top")) {
                mesh.data = new Animator(mesh, 64);
            }
        }
        spaceDown = false;
    }

    public void update(Demo demo, App app, Scene scene, Collider collider, boolean fpsCamera) throws Exception {
        Game game = app.getGame();

        if(fpsCamera) {
            collider.origin.set(scene.playerPosition);
            collider.direction.set(scene.playerDirection);
        } else {
            int w = game.getRenderTargetWidth();
            int h = game.getRenderTargetHeight();
            int x = game.getMouseX();
            int y = h - game.getMouseY() - 1;

            Utils.unProject(x, y, 0, 0, 0, w, h, demo.projection, demo.view, collider.origin);
            Utils.unProject(x, y, 1, 0, 0, w, h, demo.projection, demo.view, collider.direction);

            collider.direction.sub(collider.origin).normalize();
        }
        collider.time[0] = Float.MAX_VALUE;

        Mesh mesh = select(scene, collider);
        boolean down = false;

        if(game.isKeyDown(GLFW.GLFW_KEY_SPACE)) {
            if(!spaceDown) {
                down = true;
            }
            spaceDown = true;
        } else {
            spaceDown = false;
        }

        if(mesh != null) {
            mesh.selector.pipeline.ambientColor.set(4, 3, 4, 1);
            if(down) {
                ((Animator)mesh.data).start();
            }
        }
        for(int i = 0; i != scene.getMeshCount(); i++) {
            mesh = scene.meshAt(i);
            if(mesh.data instanceof Animator) {
                ((Animator)mesh.data).animate(game);
            }
        }
    }

    private Mesh select(Scene scene, Collider collider) {
        Mesh hit = null;

        for(int i = 0; i != scene.getMeshCount(); i++) {
            Mesh mesh = scene.meshAt(i);
            float t = collider.time[0];

            mesh.setTransform();
            mesh.selector.pipeline.model.set(mesh.selector.model);
            bounds.min.set(mesh.selector.pipeline.getBounds().min);
            bounds.max.set(mesh.selector.pipeline.getBounds().max);
            bounds.transform(mesh.selector.model);

            if(scene.lightingEnabled) {
                mesh.selector.pipeline.ambientColor.set(0.2f, 0.2f, 0.2f, 1);
            } else {
                mesh.selector.pipeline.ambientColor.set(1, 1, 1, 1);
            }

            collider.time[0] = Float.MAX_VALUE;
            if(bounds.intersects(collider.origin, collider.direction, collider.time)) {
                collider.time[0] = t;

                for(int j = 0; j != mesh.selector.pipeline.getTriangleCount(); j++) {
                    mesh.selector.pipeline.triangleAt(j, triangle);
                    if(triangle.intersects(collider.origin, collider.direction, 0, collider.time)) {
                        if(mesh.data instanceof Animator) {
                            if(((Animator)mesh.data).getDone()) {
                                hit = mesh;
                            }
                        }
                    }
                }
            } else {
                collider.time[0] = t;
            }
        }
        return hit;
    }
}
