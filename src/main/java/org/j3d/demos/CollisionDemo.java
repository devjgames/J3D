package org.j3d.demos;

import java.util.Vector;

import org.j3d.Collider;
import org.j3d.Game;
import org.j3d.IO;
import org.j3d.LightPipeline;
import org.j3d.MeshTriangleSelector;
import org.j3d.Utils;
import org.j3d.Collider.TriangleSelector;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class CollisionDemo extends Demo {
        
    private Vector3f offset = new Vector3f(0, 200, 400);
    private Vector3f up = new Vector3f(0, 1, 0);
    private Vector3f target = new Vector3f();
    private Vector3f eye = new Vector3f();
    private Vector<MeshTriangleSelector> selectors;
    private LightPipeline cube;
    private Collider collider;

    @Override
    public void init(App app) throws Exception {
        Game game = app.getGame();

        selectors = new Vector<>();

        selectors.add(new MeshTriangleSelector(game.getAssets().load(IO.file("assets/meshes/ledge1.obj"))));
        selectors.lastElement().mesh.ambientColor.set(1, 1, 1, 1);
        selectors.lastElement().mesh.diffuseColor.set(0, 0, 0, 1);
        selectors.lastElement().mesh.texture = game.getAssets().load(IO.file("assets/meshes/ledge1.png"));

        selectors.add(new MeshTriangleSelector(selectors.firstElement().mesh));
        selectors.lastElement().model.identity().translate(-300, 100, -300);

        selectors.add(new MeshTriangleSelector(selectors.firstElement().mesh));
        selectors.lastElement().model.identity().translate(+200, 150, -200).rotate((float)Math.PI / 4, 0, 1, 0).scale(0.75f);

        selectors.add(new MeshTriangleSelector(selectors.firstElement().mesh));
        selectors.lastElement().model.identity().translate(0, -100, 300).rotate((float)Math.PI / 4, 0, 1, 0).scale(0.5f);

        target.set(0, 64, 0);
        cube = game.getAssets().load(IO.file("assets/meshes/cube.obj"));
        cube.ambientColor.set(1, 1, 1, 1);
        cube.diffuseColor.set(0, 0, 0, 1);
        cube.texture = game.getAssets().load(IO.file("assets/meshes/cube.png"));

        collider = new Collider();
        for(TriangleSelector selector : selectors) {
            collider.addTriangleSelector(selector);
        }
    }

    @Override
    public boolean update(App app) throws Exception {
        Game game = app.getGame();

        projection.identity().perspective((float)Math.PI / 3, game.getAspectRatio(), 1, 25000);
        target.add(offset, eye);
        view.identity().lookAt(eye, target, up);

        game.beginRenderTarget();
        Utils.clear(0.2f, 0.2f, 0.2f, 1);
        for(MeshTriangleSelector selector : selectors) {
            selector.render(projection, view);
        }
        cube.model.identity().translate(target).rotate(radians1, 0, 1, 0).rotate(radians2, 0, 0, 1);
        cube.render(projection, view);
        game.getSpritePipeline().begin(game.getRenderTargetWidth(), game.getRenderTargetHeight());
        pushInfo(app, collider);
        game.getSpritePipeline().end();
        game.nextFrame();

        if(game.isButtonDown(1)) {
            Utils.rotateOffsetAndUp(offset, up, game);
        }
        if(!collide(game, collider, target, offset, 250, 1200, -500)) {
            target.set(0, 64, 0);
        }
        return !game.isKeyDown(GLFW.GLFW_KEY_ESCAPE);
    }
}
