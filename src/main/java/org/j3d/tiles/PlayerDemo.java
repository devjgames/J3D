package org.j3d.tiles;

import java.io.File;

import org.j3d.Game;
import org.j3d.Utils;
import org.lwjgl.glfw.GLFW;

public class PlayerDemo implements Player {

    @Override
    public void init(App app, File tileFile) throws Exception {
    }

    @Override
    public boolean run(App app) throws Exception {
        Game game = app.getGame();

        game.beginRenderTarget();
        Utils.clear(0.15f, 0.15f, 0.15f, 1);
        game.nextFrame();
        
        return !game.isKeyDown(GLFW.GLFW_KEY_ESCAPE);
    }
    
}
