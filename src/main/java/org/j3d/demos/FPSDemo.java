package org.j3d.demos;

import java.util.Vector;

import org.j3d.Collider;
import org.j3d.Game;
import org.j3d.IO;
import org.j3d.LightPipeline;
import org.j3d.MeshTriangleSelector;
import org.j3d.Sound;
import org.j3d.Utils;
import org.j3d.Collider.TriangleSelector;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class FPSDemo extends Demo {

    private Vector3f eye = new Vector3f();
    private Vector3f direction = new Vector3f();
    private Vector3f up = new Vector3f();
    private Vector3f target = new Vector3f();
    private Collider collider;
    private LightPipeline flats;
    private LightPipeline walls;
    private Vector<MeshTriangleSelector> ledges;
    private Sound sound;

    @Override
    public void init(App app) throws Exception {
        Game game = app.getGame();

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
        ledges.lastElement().mesh.lights.addAll(flats.lights);
        ledges.lastElement().setTransform(400, 40, 375, 22.5f, 0, 36, 0.35f); 
        
        ledges.add(new MeshTriangleSelector(ledges.firstElement().mesh));
        ledges.lastElement().setTransform(0, 125, 375, -45, 0, -30, 0.25f);

        ledges.add(new MeshTriangleSelector(ledges.firstElement().mesh));
        ledges.lastElement().setTransform(-50, 100, -50, 45, 0, -30, 0.25f);

        collider = new Collider();
        collider.addTriangleSelector(new MeshTriangleSelector(flats));
        collider.addTriangleSelector(new MeshTriangleSelector(walls));

        for(TriangleSelector ledge : ledges) {
            collider.addTriangleSelector(ledge);
        }

        eye.set(0, 64, 0);
        direction.set(0, 0, 1);
        up.set(0, 1, 0);

        sound = game.getAssets().load(IO.file("assets/sounds/ambient.wav"));
        sound.setVolume(0);
        sound.play(true);

        game.enableFPSMouse();
    }

    @Override
    public boolean update(App app) throws Exception {
        Game game = app.getGame();

        sound.setVolume(1 - Math.max(0, Math.min(1, eye.distance(0, 125, 375) / 300)));

        projection.identity().perspective((float)Math.PI / 3, game.getAspectRatio(), 1, 25000);
        eye.add(direction, target);
        view.identity().lookAt(eye, target, up);

        game.beginRenderTarget();
        Utils.clear(0, 0, 0, 1);
        flats.render(projection, view);
        walls.render(projection, view);
        for(MeshTriangleSelector ledge : ledges) {
            ledge.render(projection, view);
        }
        game.getSpritePipeline().begin(game.getRenderTargetWidth(), game.getRenderTargetHeight());
        pushInfo(app, collider);
        game.getSpritePipeline().end();
        game.nextFrame();

        Utils.rotateDirectionAndUp(direction, up, game);

        collide(game, collider, eye, direction, 75);

        return !game.isKeyDown(GLFW.GLFW_KEY_ESCAPE);
    }
    
}
