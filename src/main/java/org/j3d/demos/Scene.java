package org.j3d.demos;

import java.io.File;

import org.j3d.BoundingBox;
import org.j3d.Collider;
import org.j3d.Game;
import org.j3d.IO;
import org.j3d.LightPipeline;
import org.j3d.Utils;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class Scene extends Demo {

    private Vector3f offset = new Vector3f(0, 200, 100);
    private Vector3f target = new Vector3f();
    private Vector3f up = new Vector3f(0, 1, 0);
    private Vector3f eye = new Vector3f();
    private Vector3f position = new Vector3f();
    private Collider collider;
    private LightPipeline cube;
    private Tiles tiles;
    private File file;
    private float degrees1, degrees2;
    private BoundingBox bounds = new BoundingBox();
    private BoundingBox tBounds = new BoundingBox();

    public Scene(String name) {
        file = IO.file(IO.file("assets/tiles"), name + ".txt");
    }

    @Override
    public void init(App app) throws Exception {
        Game game = app.getGame();

        tiles = new Tiles(game, file);

        collider = new Collider();
        collider.addTriangleSelector(tiles);
        collider.radius = 8;

        cube = game.getAssets().load(IO.file("assets/meshes/cube.obj"));
        cube.ambientColor.set(1, 1, 1, 1);
        cube.texture = game.getAssets().load(Tiles.getTextureFile());
        cube.zeroCenter();

        degrees1 = degrees2 = 0;

        for(Tile tile : tiles.getTiles()) {
            tBounds.min.set(tile.getSelector(game).mesh.getBounds().min);
            tBounds.max.set(tile.getSelector(game).mesh.getBounds().max);
            tBounds.transform(tile.getSelector(game).model);
            bounds.add(tBounds);
        }
    }

    @Override
    public boolean run(App app) throws Exception {
        Game game = app.getGame();
        float side = 190;
        float minz = 190;
        float maxz = 105;
        float x = Math.min(Math.max(tiles.position.x, bounds.min.x + side), bounds.max.x - side);
        float z = Math.min(Math.max(tiles.position.z, bounds.min.z + minz), bounds.max.z - maxz);

        projection.identity().perspective(Utils.toRadians(60), game.getAspectRatio(), 1, 10000);
        target.set(x, 0, z);
        target.add(offset, eye);
        view.identity().lookAt(eye, target, up);

        Utils.clear(0, 0, 0, 1);
        tiles.render(projection, view, null);
        cube.setTransform(tiles.position.x, tiles.position.y, tiles.position.z, 0, degrees1, degrees2, 16);
        cube.render(projection, view);
        game.getSpritePipeline().begin(game.getRenderTargetWidth(), game.getRenderTargetHeight());
        pushInfo(app, collider);
        game.getSpritePipeline().end();
        game.nextFrame();

        int speed = 75;
        
        collider.velocity.mul(0, 1, 0);
        if(collider.getOnGround()) {
            if(game.isKeyDown(GLFW.GLFW_KEY_LEFT)) {
                degrees1 = 0;
                collider.velocity.x = -speed;
                degrees2 += 180 * game.getElapsedTime();
            } else if(game.isKeyDown(GLFW.GLFW_KEY_RIGHT)) {
                degrees1 = 180;
                collider.velocity.x = +speed;
                degrees2 += 180 * game.getElapsedTime();
            }
            if(game.isKeyDown(GLFW.GLFW_KEY_UP)) {
                degrees1 = -90;
                collider.velocity.z = -speed;
                degrees2 += 180 * game.getElapsedTime();
            } else if(game.isKeyDown(GLFW.GLFW_KEY_DOWN)) {
                degrees1 = 90;
                collider.velocity.z = +speed;
                degrees2 += 180 * game.getElapsedTime();
            }
        }
        position.set(tiles.position);
        collider.collide(game, position);
        tiles.position.set(position);

        return !game.isKeyDown(GLFW.GLFW_KEY_ESCAPE);
    } 
    
}
