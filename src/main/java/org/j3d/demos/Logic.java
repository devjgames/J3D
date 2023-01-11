package org.j3d.demos;

import org.j3d.BoundingBox;
import org.j3d.Collider;
import org.j3d.Game;
import org.j3d.IO;
import org.j3d.TexturePipeline;
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
                mesh.animator = new Animator(mesh, -32);
            } else if(name.equals("door-bottom")) {
                mesh.animator = new Animator(mesh, -64);
                for(int j = 0; j != scene.getMeshCount(); j++) {
                    Mesh mesh2 = scene.meshAt(j);

                    name = IO.fileNameWithOutExtension(mesh2.selector.pipeline.getFile());
                    if(name.equals("door-top")) {
                        if(mesh.position.distance(mesh2.position) < 44) {
                            mesh2.animator = new Animator(mesh2, 64);
                            mesh2.animator.join = mesh.animator;
                            mesh.animator.join = mesh2.animator;
                        }
                    }
                }
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
            int w = game.getFramebufferWidth();
            int h = game.getFramebufferHeight();
            int x = game.getMouseX();
            int y = h - game.getMouseY() - 1;

            Utils.unProject(x, y, 0, 0, 0, w, h, demo.projection, demo.view, collider.origin);
            Utils.unProject(x, y, 1, 0, 0, w, h, demo.projection, demo.view, collider.direction);

            collider.direction.sub(collider.origin).normalize();
        }
        collider.time[0] = Float.MAX_VALUE;

        Mesh mesh = select(scene, collider);
        boolean wasDown = spaceDown;
    
        spaceDown = game.isKeyDown(GLFW.GLFW_KEY_SPACE);
        
        if(mesh != null && wasDown) {
            ((TexturePipeline)mesh.selector.pipeline).color.set(4, 3, 4, 1);
            for(int i = 0; i != scene.getMeshCount(); i++) {
                Mesh mesh2 = scene.meshAt(i);

                if(mesh2.animator == mesh.animator.join && mesh2.animator != null) {
                    ((TexturePipeline)mesh2.selector.pipeline).color.set(4, 3, 4, 1);
                }
            }
            if(!spaceDown) {
                mesh.animator.start();
                if(mesh.animator.join != null) {
                    mesh.animator.join.start();
                }
            }
        }
        for(int i = 0; i != scene.getMeshCount(); i++) {
            mesh = scene.meshAt(i);
            if(mesh.animator != null) {
                mesh.animator.animate(game);
            }
        }
    }

    private Mesh select(Scene scene, Collider collider) {
        Mesh hit = null;

        for(int i = 0; i != scene.getMeshCount(); i++) {
            Mesh mesh = scene.meshAt(i);
            float t = collider.time[0];

            mesh.setTransform();
            mesh.selector.pipeline.getModel().set(mesh.selector.model);
            bounds.min.set(mesh.selector.pipeline.getBounds().min);
            bounds.max.set(mesh.selector.pipeline.getBounds().max);
            bounds.transform(mesh.selector.model);

            scene.resetColor(mesh);

            collider.time[0] = Float.MAX_VALUE;
            if(bounds.intersects(collider.origin, collider.direction, collider.time)) {
                collider.time[0] = t;

                for(int j = 0; j != mesh.selector.pipeline.getTriangleCount(); j++) {
                    mesh.selector.pipeline.triangleAt(j, triangle);
                    if(triangle.intersects(collider.origin, collider.direction, 0, collider.time)) {
                        if(mesh.animator != null) {
                            if(mesh.animator.getDone()) {
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
