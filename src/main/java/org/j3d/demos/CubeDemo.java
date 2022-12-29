package org.j3d.demos;

import org.j3d.DualTexturePipeline;
import org.j3d.Font;
import org.j3d.Game;
import org.j3d.IO;
import org.j3d.LightMapPipeline;
import org.j3d.Resource;
import org.j3d.Utils;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class CubeDemo extends Demo {

    private Vector3f offset = new Vector3f(200, 200, 200);
    private Vector3f up = new Vector3f(0, 1, 0);
    private Vector3f zero = new Vector3f();

    @Override
    public void init(App app) throws Exception {
        DualTexturePipeline mesh = app.getGame().getAssets().getResources().manage(new DualTexturePipeline());
        LightMapPipeline lightMapper = app.getLightMapper();
        int n = 0;

        mesh.texture = app.getGame().getAssets().load(IO.file("assets/textures/checker.png"));

        mesh.pushVertex(-50, -50, -50, 0, 0, 0, 0);
        mesh.pushVertex(-50, +50, -50, 2, 0, 0, 0);
        mesh.pushVertex(+50, +50, -50, 2, 2, 0, 0);
        mesh.pushVertex(+50, -50, -50, 0, 2, 0, 0);
        mesh.pushFace(n, n + 1, n + 2, n + 3);
        n += 4;

        mesh.pushVertex(-50, -50, +50, 0, 0, 0, 0);
        mesh.pushVertex(+50, -50, +50, 2, 0, 0, 0);
        mesh.pushVertex(+50, +50, +50, 2, 2, 0, 0);
        mesh.pushVertex(-50, +50, +50, 0, 2, 0, 0);
        mesh.pushFace(n, n + 1, n + 2, n + 3);
        n += 4;

        mesh.pushVertex(-50, -50, -50, 0, 0, 0, 0);
        mesh.pushVertex(-50, -50, +50, 2, 0, 0, 0);
        mesh.pushVertex(-50, +50, +50, 2, 2, 0, 0);
        mesh.pushVertex(-50, +50, -50, 0, 2, 0, 0);
        mesh.pushFace(n, n + 1, n + 2, n + 3);
        n += 4;

        mesh.pushVertex(+50, -50, -50, 0, 0, 0, 0);
        mesh.pushVertex(+50, +50, -50, 2, 0, 0, 0);
        mesh.pushVertex(+50, +50, +50, 2, 2, 0, 0);
        mesh.pushVertex(+50, -50, +50, 0, 2, 0, 0);
        mesh.pushFace(n, n + 1, n + 2, n + 3);
        n += 4;

        mesh.pushVertex(-50, -50, -50, 0, 0, 0, 0);
        mesh.pushVertex(+50, -50, -50, 2, 0, 0, 0);
        mesh.pushVertex(+50, -50, +50, 2, 2, 0, 0);
        mesh.pushVertex(-50, -50, +50, 0, 2, 0, 0);
        mesh.pushFace(n, n + 1, n + 2, n + 3);
        n += 4;

        mesh.pushVertex(-50, +50, -50, 0, 0, 0, 0);
        mesh.pushVertex(-50, +50, +50, 2, 0, 0, 0);
        mesh.pushVertex(+50, +50, +50, 2, 2, 0, 0);
        mesh.pushVertex(+50, +50, -50, 0, 2, 0, 0);
        mesh.pushFace(n, n + 1, n + 2, n + 3);

        mesh.buffer();

        lightMapper.meshes.add(mesh);

        lightMapper.addLight(+100, +100, +100, 4, 2, 0, 200);
        lightMapper.addLight(-100, -100, -100, 0, 2, 4, 200);
        lightMapper.addLight(+000, +100, +000, 2, 1, 2, 75);
        lightMapper.addLight(+000, -100, +000, 2, 1, 2, 75);

        lightMapper.map();
    }

    @Override
    public boolean update(App app) throws Exception {
        Game game = app.getGame();
        Font font = app.getFont();
        LightMapPipeline lightMapper = app.getLightMapper();

        if(game.isButtonDown(0)) {
            Utils.rotateOffsetAndUp(offset, up, game);
        }
        projection.identity().perspective((float)Math.PI / 3, game.getAspectRatio(), 1, 10000);
        view.identity().lookAt(offset, zero, up);

        game.beginRenderTarget();
        Utils.clear(0, 0, 0, 1);
        for(DualTexturePipeline mesh : lightMapper.meshes) {
            mesh.render(projection, view);
        }
        game.getSpritePipeline().begin(game.getRenderTargetWidth(), game.getRenderTargetHeight());
        game.getSpritePipeline().beginSprite(font);
        game.getSpritePipeline().push(
            font, 
            "FPS = " + game.getFPS() + 
            "\nRES = " + Resource.getInstances() + 
            "\nLIT = " + lightMapper.lights.size() + 
            "\nESC = BACK", 
            5, 10, 10, 1, 1, 1, 1
            );
        game.getSpritePipeline().endSprite();
        game.getSpritePipeline().end();
        game.nextFrame();

        return !game.isKeyDown(GLFW.GLFW_KEY_ESCAPE);
    }
    
}
