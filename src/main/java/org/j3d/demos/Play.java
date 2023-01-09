package org.j3d.demos;

import org.j3d.Collider;
import org.j3d.Game;
import org.j3d.Utils;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class Play extends Demo {

    private final String name;
    private Scene scene;
    private Collider collider;
    private final Vector3f f = new Vector3f();
    private final Vector3f start = new Vector3f();
    private final Vector3f startDirection = new Vector3f();
    
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

        start.set(scene.playerPosition);
        startDirection.set(scene.playerDirection);

        game.enableFPSMouse();
    }

    @Override
    public boolean run(App app) throws Exception {
        Game game = app.getGame();

        projection.identity().perspective(Utils.toRadians(60), game.getAspectRatio(), 1, 50000);

        scene.render(projection, view, true);
        game.getSpritePipeline().begin(game.getRenderTargetWidth(), game.getRenderTargetHeight());
        pushInfo(app, collider);
        game.getSpritePipeline().end();
        game.nextFrame();

        Utils.rotateDirectionAndUp(scene.playerDirection, scene.up, game);

        f.set(scene.playerDirection).mul(1, 0, 1);
        collider.velocity.mul(0, 1, 0);
        if((game.isButtonDown(0) || game.isButtonDown(1)) && f.length() > 0.0000001) {
            f.normalize().mul(scene.playerSpeed);
            if(game.isButtonDown(1)) {
                f.negate();
            }
            collider.velocity.add(f);
        }
        collider.collide(game, scene.playerPosition);
        if(scene.playerPosition.y < -8) {
            scene.playerPosition.set(start);
            scene.playerDirection.set(startDirection);
            scene.up.set(0, 1, 0);
        }
        return !game.isKeyDown(GLFW.GLFW_KEY_ESCAPE);
    }
    
    @Override
    public String toString() {
        return name;
    }
}
