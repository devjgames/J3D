package org.j3d.demos;

import org.j3d.Collider;
import org.j3d.Font;
import org.j3d.Game;
import org.j3d.Resource;
import org.j3d.Utils;
import org.joml.Matrix4f;

public abstract class Demo {

    public final Matrix4f projection = new Matrix4f();
    public final Matrix4f view = new Matrix4f();
    
    public abstract void init(App app) throws Exception;

    public abstract boolean run(App app) throws Exception;

    public void pushInfo(App app, Collider collider, Scene scene, boolean drawCrossHair) {
        Game game = app.getGame();
        Font font = app.getFont();
        int w = game.getFramebufferWidth();
        int h = game.getFramebufferHeight();

        String info = "FPS=" + game.getFPS() + ", ";

        info += "RES=" + Resource.getInstances() + ", ";
        info += "ALC=" + Utils.getAllocated() + ", ";
        if(collider != null) {
            info += "TST=" + collider.getTested() + ", ";
        }
        if(scene != null) {
            info += "BND=" + scene.getBinds() + ", ";
        }
        info += "ESC=Quit";

        game.getSpritePipeline().beginSprite(font);
        game.getSpritePipeline().push(
            font, 
            info, 
            5, 10, h - 10 - font.getCharHeight(), 
            1, 1, 1, 1
            );
        if(drawCrossHair) {
            int s = game.getScale();

            game.getSpritePipeline().push(
                font.getWhiteX(), font.getWhiteY(), 1, 1, 
                w / 2 - 8 * s, h / 2, 17 * s, 1, 
                1, 1, 1, 1, false
                );
            game.getSpritePipeline().push(
                font.getWhiteX(), font.getWhiteY(), 1, 1, 
                w / 2, h / 2 - 8 * s, 1, 18 * s, 
                1, 1, 1, 1, false)
                ;
        }
        game.getSpritePipeline().endSprite();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
