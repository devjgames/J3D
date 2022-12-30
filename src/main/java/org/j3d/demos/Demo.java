package org.j3d.demos;

import org.j3d.Collider;
import org.j3d.Font;
import org.j3d.Game;
import org.j3d.Resource;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public abstract class Demo {

    public final Matrix4f projection = new Matrix4f();
    public final Matrix4f view = new Matrix4f();
    public float radians1 = 0;
    public float radians2 = 0;

    private Vector3f r = new Vector3f();
    private Vector3f u = new Vector3f();
    private Vector3f f = new Vector3f();
    
    public abstract void init(App app) throws Exception;

    public abstract boolean update(App app) throws Exception;

    public void pushInfo(App app, Collider collider) {
        Game game = app.getGame();
        Font font = app.getFont();
        String info = "";

        info += "FPS = " + game.getFPS() + "\n";
        info += "RES = " + Resource.getInstances() + "\n";

        if(collider != null) {
            info += "TST = " + collider.getTested() + "\n";
        }
        info += "ESC = BACK";

        game.getSpritePipeline().beginSprite(font);
        game.getSpritePipeline().push(font, info, 5, 10, 10, 1, 1, 1, 1);
        game.getSpritePipeline().endSprite();
    }

    public boolean collide(Game game, Collider collider, Vector3f target, Vector3f offset, int speed, int jump, int fall) throws Exception {
        float dx = game.getMouseX() - game.getRenderTargetWidth() / 2;
        float dy = game.getMouseY() - game.getRenderTargetHeight() / 2;
        float dl = Vector2f.length(dx, dy);

        collider.velocity.mul(0, 1, 0);
        offset.negate(f).mul(1, 0, 1);
        if(game.isButtonDown(0) && f.length() > 0.0000001f && dl > 0.1f) {
            f.normalize().cross(u.set(0, 1, 0), r).normalize();
            f.mul(-dy / dl * 250).add(r.mul(dx / dl * 250));
            collider.velocity.add(f);
            f.normalize();
            radians1 = (float)Math.acos(Math.max(-0.99f, Math.min(0.99f, f.x)));
            if(f.z > 0) {
                radians1 = (float)Math.PI * 2 - radians1;
            }
            radians2 -= (float)Math.PI * game.getElapsedTime();
        }
        if(game.isKeyDown(GLFW.GLFW_KEY_SPACE) && collider.getOnGround() && jump > 0) {
            collider.velocity.y = 1200;
        }
        collider.collide(game, target);
        if(target.y < fall) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
