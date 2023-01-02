package org.j3d.demos;

import org.j3d.Collider;
import org.j3d.Font;
import org.j3d.Game;
import org.j3d.Resource;
import org.joml.Matrix4f;

public abstract class Demo {

    public final Matrix4f projection = new Matrix4f();
    public final Matrix4f view = new Matrix4f();
    
    public abstract void init(App app) throws Exception;

    public abstract boolean run(App app) throws Exception;

    public void pushInfo(App app, Collider collider) {
        Game game = app.getGame();
        Font font = app.getFont();

        String info = "FPS=" + game.getFPS() + ", ";

        info += "RES=" + Resource.getInstances() + ", ";
        if(collider != null) {
            info += "TST=" + collider.getTested() + ", ";
        }
        info += "ESC=Quit";

        game.getSpritePipeline().beginSprite(font);
        game.getSpritePipeline().push(
            font, 
            info, 
            5, 10, game.getRenderTargetHeight() - 10 - font.getCharHeight(), 
            1, 1, 1, 1
            );
        game.getSpritePipeline().endSprite();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
