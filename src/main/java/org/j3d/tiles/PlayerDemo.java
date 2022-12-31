package org.j3d.tiles;

import java.io.File;

import org.j3d.Collider;
import org.j3d.Font;
import org.j3d.Game;
import org.j3d.IO;
import org.j3d.LightPipeline;
import org.j3d.Resource;
import org.j3d.Utils;
import org.j3d.LightPipeline.Light;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class PlayerDemo implements Player {

    private Vector3f offset = new Vector3f();
    private Vector3f target = new Vector3f();
    private Vector3f up = new Vector3f();
    private Vector3f eye = new Vector3f();
    private Vector3f f = new Vector3f();
    private Vector3f u = new Vector3f();
    private Vector3f r = new Vector3f();
    private Matrix4f projection = new Matrix4f();
    private Matrix4f view = new Matrix4f();
    private Tiles tiles;
    private Collider collider;
    private LightPipeline ball;
    private float degrees1;
    private float degrees2;

    @Override
    public void init(App app, File tileFile, Light ... lights) throws Exception {
        Game game = app.getGame();

        tiles = new Tiles(app, tileFile, offset, target, up, lights);

        collider = new Collider();
        collider.addTriangleSelector(tiles);

        ball = game.getAssets().load(IO.file("assets/meshes/ball.obj"));
        ball.ambientColor.set(1, 0, 1, 1);

        offset.normalize().mul(500, 500,500);
        target.set(tiles.playerCol * 64, tiles.playerLayer * 64 + 64 + 64, tiles.playerRow * 64);
        tiles.position.set(target);
        degrees1 = 0;
        degrees2 = 0;
    }

    @Override
    public boolean run(App app) throws Exception {
        Game game = app.getGame();
        Font font = app.getFont();

        projection.identity().perspective(Utils.toRadians(60), game.getAspectRatio(), 1, 25000);
        target.add(offset, eye);
        view.identity().lookAt(eye, target, up);

        game.beginRenderTarget();
        Utils.clear(0.15f, 0.15f, 0.15f, 1);
        tiles.render(projection, view);
        ball.setTransform(tiles.position.x, tiles.position.y, tiles.position.z, 0, degrees1, degrees2, 1.5f);
        ball.render(projection, view);
        game.getSpritePipeline().begin(game.getRenderTargetWidth(), game.getRenderTargetHeight());
        game.getSpritePipeline().beginSprite(font);
        game.getSpritePipeline().push(
            font, 
            "FPS = " + game.getFPS() + 
            "\nRES = " + Resource.getInstances() + 
            "\nTST = " + collider.getTested() + 
            "\nESC = Quit",
            5, 10, 10, 1, 1, 1, 1
        );
        game.getSpritePipeline().endSprite();
        game.getSpritePipeline().end();
        game.nextFrame();

        if(game.isButtonDown(1)) {
            Utils.rotateOffsetAndUp(offset, up, game.getDeltaX() * 0.025f, 0);
        }

        float dx = game.getMouseX() - game.getRenderTargetWidth() / 2;
        float dy = game.getMouseY() - game.getRenderTargetHeight() / 2;
        float dl = Vector2f.length(dx, dy);

        collider.velocity.mul(0, 1, 0);
        f.set(offset).mul(-1, 0, -1);
        if(f.length() > 0.0000001 && dl > 0.1 && game.isButtonDown(0) && collider.getOnGround()) {
            f.normalize().cross(u.set(0, 1, 0), r).normalize().mul(dx / dl * 150);
            collider.velocity.add(f.mul(-dy / dl * 150).add(r));
            f.normalize();
            degrees1 = Utils.toDegrees((float)Math.acos(Math.max(-0.99, Math.min(0.99, f.x))));
            if(f.z > 0) {
                degrees1 = 360 - degrees1;
            }
            degrees2 -= 180 * game.getElapsedTime();
        }
        collider.collide(game, tiles.position);
        if(tiles.position.y < -500) {
            target.set(tiles.playerCol * 64, tiles.playerLayer * 64 + 64 + 64, tiles.playerRow * 64);
            tiles.position.set(target);
        }
        target.set(tiles.position);
        if(target.y < 64) {
            target.y =  64;
        }
        return !game.isKeyDown(GLFW.GLFW_KEY_ESCAPE);
    }
    
}
