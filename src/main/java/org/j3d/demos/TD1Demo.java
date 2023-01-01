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

public class TD1Demo extends Demo {

    private Vector3f offset = new Vector3f(200, 200, 200);
    private Vector3f target = new Vector3f(0, 64, 0);
    private Vector3f up = new Vector3f(0, 1, 0);
    private Vector3f eye = new Vector3f();
    private Vector<MeshTriangleSelector> selectors = new Vector<>();
    private LightPipeline ball;
    private Collider collider;

    @Override
    public void init(App app) throws Exception {
        Game game = app.getGame();

        selectors.clear();
        addSelectors(app, selectors);

        collider = new Collider();
        for(TriangleSelector selector : selectors) {
            collider.addTriangleSelector(selector);
        }

        ball = game.getAssets().load(IO.file("assets/meshes/ball.obj"));
        ball.ambientColor.set(1, 1, 1, 1);
    }

    @Override
    public boolean run(App app) throws Exception {
        Game game = app.getGame();

        projection.identity().perspective(Utils.toRadians(60), game.getAspectRatio(), 1, 25000);
        target.add(offset, eye);
        view.identity().lookAt(eye, target, up);

        game.beginRenderTarget();
        Utils.clear(0, 0, 0, 1);
        for(MeshTriangleSelector mesh : selectors) {
            mesh.render(projection, view);
        }
        ball.setTransform(target.x, target.y, target.z, 0, degrees1, degrees2, 1);
        ball.render(projection, view);
        game.getSpritePipeline().begin(game.getRenderTargetWidth(), game.getRenderTargetHeight());
        pushInfo(app, collider);
        game.getSpritePipeline().end();
        game.nextFrame();

        if(game.isButtonDown(1)) {
            Utils.rotateOffsetAndUp(offset, up, game.getDeltaX() * 0.025f, 0);
        }
        collide(game, collider, offset, target, 150);

        return !game.isKeyDown(GLFW.GLFW_KEY_ESCAPE);
    }

    protected void addSelectors(App app, Vector<MeshTriangleSelector> selectors) throws Exception {
        Game game = app.getGame();
        LightPipeline block = game.getAssets().load(IO.file("assets/meshes/block.obj"));
        LightPipeline ramp = game.getAssets().load(IO.file("assets/meshes/ramp.obj"));

        block.texture = game.getAssets().load(IO.file("assets/meshes/stone1.png"));
        block.addLight(-100, 75, -100, 4, 2, 1, 1, 150, false);
        block.addLight(+100, 75, +100, 1, 2, 4, 1, 150, false);
        block.addLight(-100, 75, +100, 1, 2, 4, 1, 150, false);

        ramp.texture = block.texture;
        ramp.lights.addAll(block.lights);

        for(int r = 0; r != 4; r++) {
            for(int c = 0; c != 4; c++) {
                MeshTriangleSelector mesh = new MeshTriangleSelector(block);

                mesh.setTransform((c - 2) * 64, -64, (r - 2) * 64, 0, 0, 0, 1);
                selectors.add(mesh);

                if(c == 2 && r == 1) {
                    mesh = new MeshTriangleSelector(ramp);
                    mesh.setTransform((c - 2) * 64, 0, (r - 2) * 64, 0, -90, 0, 1);
                    selectors.add(mesh);
                } else if(c == 3 && r == 2) {
                    mesh = new MeshTriangleSelector(ramp);
                    mesh.setTransform((c - 2) * 64, 0, (r - 2) * 64, 0, 0, 0, 1);
                    selectors.add(mesh);
                } else if(c == 3 && r == 1) {
                    mesh = new MeshTriangleSelector(block);
                    mesh.setTransform((c - 2) * 64, 0, (r - 2) * 64, 0, 0, 0, 1);
                    selectors.add(mesh);
                }
            }
        }
    }
}
