package org.j3d.demos;

import org.j3d.Collider;
import org.j3d.Game;
import org.j3d.demos.Scene.Mesh;

public class Animator {
    
    public final Mesh mesh;
    public Animator join;
    
    private int amount;
    private boolean done = true;
    private float y, direction;

    public Animator(Mesh mesh, int amount) {
        this.mesh = mesh;
        this.amount = amount;
    }

    public boolean getDone() {
        return done;
    }

    public void start() {
        if(done) {
            y = mesh.position.y;
            direction = y + amount - y;
            if(direction < 0) {
                direction = -1;
            } else {
                direction = 1;
            }
        }
        done = false;
    }

    public void animate(Game game, Collider collider) throws Exception {
        if(!done) {
            float x = 50 * game.getElapsedTime() * direction;

            if(Math.abs(x) > collider.radius * 0.5f) {
                x = collider.radius * 0.5f * ((x < 0) ? -1 : 1);
            }
            mesh.position.y += x;
            if(Math.abs(mesh.position.y - y) > Math.abs(amount)) {
                mesh.position.y = y + amount;
                amount = -amount;
                done = true;
            }
            mesh.setTransform();
        }
    }
}
