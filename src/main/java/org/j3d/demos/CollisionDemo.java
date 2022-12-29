package org.j3d.demos;

import org.j3d.Collider;
import org.j3d.DualTexturePipeline;
import org.j3d.Game;
import org.j3d.IO;
import org.j3d.LightMapPipeline;
import org.j3d.Utils;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class CollisionDemo extends Demo {
    
    private Vector3f target = new Vector3f();
    private Vector3f offset = new Vector3f(200, 200, 200);
    private Vector3f up = new Vector3f(0, 1, 0);
    private Vector3f r = new Vector3f();
    private Vector3f u = new Vector3f(0, 1, 0);
    private Vector3f f = new Vector3f();
    private Vector3f eye = new Vector3f();
    private DualTexturePipeline cube;
    private Collider collider;
    private float radians1 = 0;
    private float radians2 = 0;

    @Override
    public void init(App app) throws Exception {
        Game game = app.getGame();
        LightMapPipeline lightMapper = app.getLightMapper();
        DualTexturePipeline mesh = game.getAssets().getResources().manage(new DualTexturePipeline());

        mesh.texture = game.getAssets().load(IO.file("assets/textures/stone.png"));
        mesh.pushBox(0, 0, 0, 256, 32, 256);
        mesh.pushBox(0, 0, 0, 064, 64, 32);
        mesh.pushBox(0, 0, 0, 032, 64, 64);
        mesh.calcTextureCoordinates(0, mesh.getFaceCount(), 128);
        mesh.transform(00, 24, new Matrix4f().translate(+00, -16, +00));
        mesh.transform(24, 48, new Matrix4f().translate(-64, +32, -64).rotate((float)Math.PI / 4, 0, 1, 0));
        mesh.transform(48, 72, new Matrix4f().translate(+64, +00, +00).rotate((float)Math.PI / 4, 1, 0, 0));
        mesh.calcBounds();
        mesh.buffer();

        lightMapper.meshes.add(mesh);

        lightMapper.addLight(+64, 64, +000, 4, 2, 1, 100);
        lightMapper.addLight(-64, 32, +000, 1, 2, 4, 75);
        lightMapper.addLight(+00, 50, +150, 1, 2, 4, 100);
        lightMapper.addLight(+00, 50, -150, 1, 2, 4, 100);

        lightMapper.map();

        target.set(0, 64, 0);
        cube = game.getAssets().getResources().manage(new DualTexturePipeline());
        cube.pushBox(0, 0, 0, 16, 16, 16);
        cube.buffer();

        collider = new Collider();
        collider.addTriangleSelector(mesh);
    }

    @Override
    public boolean update(App app) throws Exception {
        Game game = app.getGame();
        LightMapPipeline lightMapper = app.getLightMapper();
        float y = target.y;

        projection.identity().perspective((float)Math.PI / 3, game.getAspectRatio(), 1, 25000);
        target.y = 0;
        target.add(offset, eye);
        view.identity().lookAt(eye, target, up);
        target.y = y;

        game.beginRenderTarget();
        Utils.clear(0, 0, 0, 1);
        for(DualTexturePipeline mesh : lightMapper.meshes) {
            mesh.render(projection, view);
        }
        cube.model.identity().translate(target).rotate(radians1, 0, 1, 0).rotate(radians2, 0, 0, 1);
        cube.render(projection, view);
        game.getSpritePipeline().begin(game.getRenderTargetWidth(), game.getRenderTargetHeight());
        pushInfo(app, collider);
        game.getSpritePipeline().end();
        game.nextFrame();

        if(game.isButtonDown(1)) {
            target.y = 0;
            Utils.rotateOffsetAndUp(offset, up, game);
            target.y = y;
        }

        float dx = game.getMouseX() - game.getRenderTargetWidth() / 2;
        float dy = game.getMouseY() - game.getRenderTargetHeight() / 2;
        float dl = Vector2f.length(dx, dy);

        collider.velocity.mul(0, 1, 0);
        offset.negate(f).mul(1, 0, 1);
        if(game.isButtonDown(0) && f.length() > 0.0000001 && dl > 0.1) {
            f.normalize().cross(u, r).normalize();
            f.mul(-dy / dl * 150);
            r.mul(+dx / dl * 150);
            collider.velocity.add(f.add(r));
            f.normalize();
            radians1 = (float)Math.acos(Math.max(-0.99f, Math.min(0.99f, f.x)));
            if(f.z > 0) {
                radians1 = (float)Math.PI * 2 - radians1;
            }
            radians2 -= (float)Math.PI * game.getElapsedTime();
        }
        collider.collide(game, target);

        if(target.y < -100) {
            target.set(0, 64, 0);
        }
        return !game.isKeyDown(GLFW.GLFW_KEY_ESCAPE);
    }
}
