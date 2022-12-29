package org.j3d.demos;

import org.j3d.Game;
import org.j3d.IO;
import org.j3d.LightPipeline;
import org.j3d.Utils;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class InstanceDemo extends Demo {
    
    private Vector3f offset = new Vector3f(0, 400, 800);
    private Vector3f up = new Vector3f(0, 1, 0);
    private Vector3f zero = new Vector3f();
    private LightPipeline mesh;

    @Override
    public void init(App app) throws Exception {
        Game game = app.getGame();

        mesh = game.getAssets().load(IO.file("assets/meshes/ledge1.obj"));
        mesh.ambientColor.set(1, 1, 1, 1);
        mesh.diffuseColor.set(0, 0, 0, 1);
        mesh.texture = game.getAssets().load(IO.file("assets/meshes/ledge1.png"));
    }

    @Override
    public boolean update(App app) throws Exception {
        Game game = app.getGame();

        projection.identity().perspective((float)Math.PI / 3, game.getAspectRatio(), 1, 25000);
        view.identity().lookAt(offset, zero, up);

        game.beginRenderTarget();
        Utils.clear(0.2f, 0.2f, 0.2f, 1);
        mesh.model.identity();
        mesh.render(projection, view);
        mesh.model.identity().translate(-300, 100, -300);
        mesh.render(projection, view);
        mesh.model.identity().translate(+200, 150, -200).rotate((float)Math.PI / 4, 0, 1, 0).scale(0.75f);
        mesh.render(projection, view);
        mesh.model.identity().translate(0, -100, 300).rotate((float)Math.PI / 4, 0, 1, 0).scale(0.5f);
        mesh.render(projection, view);
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
