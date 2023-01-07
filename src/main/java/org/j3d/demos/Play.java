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

    public Play(String name) {
        this.name = name;
    }

    @Override
    public void init(App app) throws Exception {
        Game game = app.getGame();

        scene = new Scene(game, name);
        collider = new Collider();
        collider.radius = scene.playerRadius;
        collider.addTriangleSelector(scene);
    }

    @Override
    public boolean run(App app) throws Exception {
        Game game = app.getGame();

        projection.identity().perspective(Utils.toRadians(60), game.getAspectRatio(), 1, 50000);

        scene.render(projection, view);
        game.getSpritePipeline().begin(game.getRenderTargetWidth(), game.getRenderTargetHeight());
        pushInfo(app, collider);
        game.getSpritePipeline().end();
        game.nextFrame();

        if(game.isButtonDown(1)) {
            Utils.rotateOffsetAndUp(scene.playerOffset, scene.up, game.getDeltaX() * 0.025f, 0);
        }

        float dx = game.getMouseX() - game.getRenderTargetWidth() / 2;
        float dy = game.getMouseY() - game.getRenderTargetHeight() / 2;
        float dl = Vector2f.length(dx, dy);

        f.set(scene.playerOffset).mul(-1, 0, -1);
        collider.velocity.mul(0, 1, 0);
        if(game.isButtonDown(0) && collider.getOnGround() && f.length() > 0.0000001 && dl > 0.1) {
            f.normalize().cross(u.set(0, 1, 0), r).normalize().mul(dx / dl * scene.playerSpeed);
            f.mul(-dy / dl * scene.playerSpeed);
            collider.velocity.add(f.add(r));
            f.normalize();
            scene.playerDegrees1 = Utils.toDegrees((float)Math.acos(Math.max(-0.99, Math.min(0.99, f.x))));
            if(f.z > 0) {
                scene.playerDegrees1 = 360 - scene.playerDegrees1;
            }
            scene.playerDegrees2 -= 180 * game.getElapsedTime();
        }
        collider.collide(game, scene.playerPosition);

        return !game.isKeyDown(GLFW.GLFW_KEY_ESCAPE);
    }
    
    @Override
    public String toString() {
        return name;
    }
}
