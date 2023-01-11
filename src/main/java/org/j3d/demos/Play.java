package org.j3d.demos;

import org.j3d.Collider;
import org.j3d.Game;
import org.j3d.Utils;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class Play extends Demo {

    private final String name;
    private Scene scene;
    private Collider collider;
    private final Vector3f f = new Vector3f();
    private final Vector3f u = new Vector3f();
    private final Vector3f r = new Vector3f();
    private final Vector3f start = new Vector3f();
    private final Vector3f startVector = new Vector3f();
    private final Logic logic;
    private final boolean fpsCamera;
    
    public Play(String name, Logic logic, boolean fpsCamera) {
        this.name = name;
        this.logic = logic;
        this.fpsCamera = fpsCamera;
    }

    @Override
    public void init(App app) throws Exception {
        Game game = app.getGame();

        scene = new Scene(game, name);
        collider = new Collider();
        collider.radius = scene.playerRadius;
        collider.addTriangleSelector(scene);

        logic.init(this, app, scene, collider, fpsCamera);

        start.set(scene.playerPosition);
        if(fpsCamera) {
            game.enableFPSMouse();
            startVector.set(scene.playerDirection);
        } else {
            startVector.set(scene.playerOffset);
        }
    }

    @Override
    public boolean run(App app) throws Exception {
        Game game = app.getGame();

        projection.identity().perspective(Utils.toRadians(60), game.getAspectRatio(), 0.25f, 10000);

        scene.render(projection, view, fpsCamera);
        game.getSpritePipeline().begin(game.getFramebufferWidth(), game.getFramebufferHeight());
        pushInfo(app, collider, scene, fpsCamera);
        game.getSpritePipeline().end();
        game.nextFrame();

        collider.velocity.mul(0, 1, 0);
        if(fpsCamera) {
            Utils.rotateDirectionAndUp(scene.playerDirection, scene.up, game);

            f.set(scene.playerDirection).mul(1, 0, 1);
            if((game.isButtonDown(0) || game.isButtonDown(1)) && f.length() > 0.0000001) {
                f.normalize().mul(scene.playerSpeed);
                if(game.isButtonDown(1)) {
                    f.negate();
                }
                collider.velocity.add(f);
            }
        } else {
            if(game.isButtonDown(1)) {
                Utils.rotateOffsetAndUp(scene.playerOffset, scene.up, game);
            }

            float dx = game.getMouseX() - game.getFramebufferWidth() / 2;
            float dy = game.getMouseY() - game.getFramebufferHeight() / 2;
            float dl = Vector2f.length(dx, dy);

            f.set(scene.playerOffset).mul(1, 0, 1);
            if(game.isButtonDown(0) && f.length() > 0.0000001 && dl > 0.1) {
                f.normalize().cross(u.set(0, 1, 0), r).normalize().mul(-dx / dl * scene.playerSpeed);
                f.mul(dy / dl * scene.playerSpeed).add(r);
                collider.velocity.add(f);
                f.normalize();
                scene.playerDegrees1 = Utils.toDegrees((float)Math.acos(Math.max(-0.99f, Math.min(0.99f, f.x))));
                if(f.z > 0) {
                    scene.playerDegrees1 = 360 - scene.playerDegrees1;
                }
                scene.playerDegrees2 -= 180 * game.getElapsedTime();
            }
        }
        collider.collide(game, scene.playerPosition);
        if(scene.playerPosition.y < -8) {
            scene.playerPosition.set(start);
            if(fpsCamera) {
                scene.playerDirection.set(startVector);
            } else {
                scene.playerOffset.set(startVector);
            }
            scene.up.set(0, 1, 0);
        }
        if(!fpsCamera) {
            float fullLength = 40;
            float length = fullLength;
            float height = 2;

            collider.origin.set(scene.playerPosition);
            collider.direction.set(scene.playerOffset).mul(1, 0, 1).normalize();
            collider.time[0] = length + (collider.radius - 1);
            collider.intersectionBits = 1;
            collider.intersectionBuffer = 1;
            if(collider.intersect() != null) {
                length = Math.min(length, collider.time[0]) - (collider.radius - 1);
            }
            scene.playerOffset.mul(1, 0, 1).normalize().mul(length);
            scene.playerOffset.y = height + (fullLength - length);
            scene.up.set(0, 1, 0);
        }
        logic.update(this, app, scene, collider, fpsCamera);

        return !game.isKeyDown(GLFW.GLFW_KEY_ESCAPE);
    }
    
    @Override
    public String toString() {
        return name + ((fpsCamera) ? "-FPS" : "");
    }
}
