package org.j3d.demos;

import org.j3d.Game;
import org.j3d.IO;
import org.j3d.LightPipeline;
import org.j3d.Utils;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class Mesh1Demo extends Demo {
    
    private Vector3f offset = new Vector3f(100, 100, 100);
    private Vector3f up = new Vector3f(0, 1, 0);
    private Vector3f zero = new Vector3f();

    protected LightPipeline block;

    @Override
    public void init(App app) throws Exception {
        Game game = app.getGame();

        block = game.getAssets().load(IO.file("assets/meshes/block.obj"));
        block.texture = game.getAssets().load(IO.file("assets/meshes/stone1.png"));
        block.ambientColor.set(1, 1, 1, 1);
        block.setTransform(0, -32, 0, 0, 0, 0, 1);
    }

    @Override
    public boolean run(App app) throws Exception {
        Game game = app.getGame();

        projection.identity().perspective(Utils.toRadians(60), game.getAspectRatio(), 1, 1000);
        view.identity().lookAt(offset, zero, up);

        game.beginRenderTarget();
        Utils.clear(0, 0, 0, 1);
        block.render(projection, view);
        game.getSpritePipeline().begin(game.getRenderTargetWidth(), game.getRenderTargetHeight());
        pushInfo(app, null);
        game.getSpritePipeline().end();
        game.nextFrame();

        if(game.isButtonDown(0)) {
            Utils.rotateOffsetAndUp(offset, up, game);
        }
        return !game.isKeyDown(GLFW.GLFW_KEY_ESCAPE);
    }

    
}
