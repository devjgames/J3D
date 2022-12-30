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

public class CameraDemo extends Demo {

    private Vector3f offset = new Vector3f();
    private Vector3f up = new Vector3f();
    private Vector3f target = new Vector3f();
    private Vector3f eye = new Vector3f();
    private LightPipeline flats;
    private LightPipeline walls;
    private LightPipeline cube;
    private Collider collider;
    private Vector<MeshTriangleSelector> ledges;
    
    @Override
    public void init(App app) throws Exception {
        Game game = app.getGame();

        offset.set(1, 1, 1).normalize().mul(100);
        up.set(0, 1, 0);
        target.set(0, 64, 0);

        flats = game.getAssets().load(IO.file("assets/meshes/dungeon-flat.obj"));
        flats.texture = game.getAssets().load(IO.file("assets/meshes/brick-flat.png"));
        flats.addLight(000, 125, 000, 3, 2, 1, 1, 200, false);
        flats.addLight(000, 150, 375, 1, 2, 3, 1, 200, false);
        flats.addLight(375, 125, 375, 3, 2, 1, 1, 200, false);

        walls = game.getAssets().load(IO.file("assets/meshes/dungeon-walls.obj"));
        walls.texture = game.getAssets().load(IO.file("assets/meshes/brick-wall.png"));
        walls.lights.addAll(flats.lights);

        ledges = new Vector<>();
        ledges.add(new MeshTriangleSelector(game.getAssets().load(IO.file("assets/meshes/ledge1.obj"))));
        ledges.lastElement().mesh.texture = game.getAssets().load(IO.file("assets/meshes/ledge1.png"));
        ledges.lastElement().model.identity().translate(400, 40, 375).rotate((float)Math.PI / 8, 1, 0, 0).rotate((float)Math.PI / 5, 0, 0, 1).scale(0.35f);
        ledges.lastElement().mesh.lights.addAll(flats.lights);
        
        ledges.add(new MeshTriangleSelector(ledges.firstElement().mesh));
        ledges.lastElement().model.identity().translate(0, 125, 375).rotate(-(float)Math.PI / 4, 1, 0, 0).rotate(-(float)Math.PI / 6, 0, 0, 1).scale(0.25f);

        ledges.add(new MeshTriangleSelector(ledges.firstElement().mesh));
        ledges.lastElement().model.identity().translate(-50, 100, -50).rotate((float)Math.PI / 4, 1, 0, 0).rotate(-(float)Math.PI / 6, 0, 0, 1).scale(0.25f);

        collider = new Collider();
        collider.radius = 20;
        collider.addTriangleSelector(new MeshTriangleSelector(flats));
        collider.addTriangleSelector(new MeshTriangleSelector(walls));

        for(TriangleSelector ledge : ledges) {
            collider.addTriangleSelector(ledge);
        }

        cube = game.getAssets().load(IO.file("assets/meshes/cube.obj"));
        cube.ambientColor.set(1, 1, 1, 1);
        cube.diffuseColor.set(0, 0, 0, 1);
        cube.texture = game.getAssets().load(IO.file("assets/meshes/cube.png"));
    }

    @Override
    public boolean update(App app) throws Exception {
        Game game = app.getGame();

        projection.identity().perspective((float)Math.PI / 3, game.getAspectRatio(), 1, 25000);
        target.add(offset, eye);
        view.identity().lookAt(eye, target, up);

        game.beginRenderTarget();
        Utils.clear(0, 0, 1, 1);
        flats.render(projection, view);
        walls.render(projection, view);
        for(MeshTriangleSelector ledge : ledges) {
            ledge.render(projection, view);
        }
        cube.model.identity().translate(target).rotate(radians1, 0, 1, 0).rotate(radians2, 0, 0, 1).scale(0.5f);
        cube.render(projection, view);
        game.getSpritePipeline().begin(game.getRenderTargetWidth(), game.getRenderTargetHeight());
        pushInfo(app, collider);
        game.getSpritePipeline().end();
        game.nextFrame();

        if(game.isButtonDown(1)) {
            Utils.rotateOffsetAndUp(offset, up, game);
        }
        collide(game, collider, target, offset, 100, 0, -1000);

        float length = 100;

        collider.origin.set(target);
        collider.direction.set(offset).normalize();
        collider.time[0] = length + collider.radius - 1;
        if(collider.intersect() != null) {
            length = Math.min(length, collider.time[0]) - collider.radius - 1;
        }
        offset.set(collider.direction).mul(length);

        return !game.isKeyDown(GLFW.GLFW_KEY_ESCAPE);
    }
}
