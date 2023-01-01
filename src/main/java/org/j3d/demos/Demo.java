package org.j3d.demos;

import org.j3d.Collider;
import org.j3d.Font;
import org.j3d.Game;
import org.j3d.Resource;
import org.j3d.Utils;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public abstract class Demo {

    public final Matrix4f projection = new Matrix4f();
    public final Matrix4f view = new Matrix4f();
    public float degrees1 = 0;
    public float degrees2 = 0;

    private Vector3f f = new Vector3f();
    private Vector3f u = new Vector3f();
    private Vector3f r = new Vector3f();
    
    public abstract void init(App app) throws Exception;

    public abstract boolean run(App app) throws Exception;

    public void pushInfo(App app, Collider collider) {
        Game game = app.getGame();
        Font font = app.getFont();

        String info = "FPS = " + game.getFPS() + "\n";

        info += "RES = " + Resource.getInstances() + "\n";
        if(collider != null) {
            info += "TST = " + collider.getTested() + "\n";
        }
        info += "ESC = Quit";

        game.getSpritePipeline().beginSprite(font);
        game.getSpritePipeline().push(font, info, 5, 10, 10, 1, 1, 1, 1);
        game.getSpritePipeline().endSprite();
    }

    public void collide(Game game, Collider collider, Vector3f offset, Vector3f position, float speed) throws Exception {
        float dx = game.getMouseX() - game.getRenderTargetWidth() / 2;
        float dy = game.getMouseY() - game.getRenderTargetHeight() / 2;
        float dl = Vector2f.length(dx, dy);
        
        collider.velocity.mul(0, 1, 0);
        f.set(offset).mul(-1, 0, -1);
        if(game.isButtonDown(0) && f.length() > 0.0000001 && dl > 0.1) {
            f.normalize().cross(u.set(0, 1, 0), r).normalize().mul(dx / dl * speed);
            collider.velocity.add(f.mul(-dy / dl * speed).add(r));
            f.normalize();
            degrees1 = Utils.toDegrees((float)Math.acos(Math.max(-0.99, Math.min(0.99, f.x))));
            if(f.z > 0) {
                degrees1 = 360 - degrees1;
            }
            degrees2 -= 180 * game.getElapsedTime();
        }
        collider.collide(game, position);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
