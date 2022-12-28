package org.j3d.scene.demo1.components;

import org.j3d.Collider;
import org.j3d.Game;
import org.j3d.PixelLightMaterial;
import org.j3d.Utils;
import org.j3d.scene.MeshRenderable;
import org.j3d.scene.NodeComponent;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class PlayerComponent extends NodeComponent {
    
    private Collider collider = new Collider();
    private Vector3f offset = new Vector3f();
    private Vector3f up = new Vector3f(0, 1, 0);
    private Vector3f f = new Vector3f();
    private Vector3f r = new Vector3f();

    @Override
    public void start() throws Exception {
        MeshRenderable mesh = ((MeshRenderable)getNode().childAt(0).renderable);
        PixelLightMaterial material = (PixelLightMaterial)mesh.mesh.meshPartAt(0).material;

        material.ambientColor.set(1, 1, 1, 1);
        material.diffuseColor.set(0, 0, 0, 1);

        if(getScene().inDesign) {
            return;
        }

        getScene().root.addToCollider(collider);

        getScene().eye.sub(getScene().target, offset).normalize().mul(200);
        setTarget();
    }

    @Override
    public void update() throws Exception {
        if(getScene().inDesign) {
            return;
        }

        Game game = getGame();

        if(game.isButtonDown(1)) {
            Utils.rotateOffsetAndUp(offset, up, getGame().getDeltaX() * 0.025f, 0);
        }


        float dx = game.getMouseX() - game.getRenderTargetWidth() / 2;
        float dy = game.getMouseY() - game.getRenderTargetHeight() / 2;
        float dl = Vector2f.length(dx, dy);

        offset.negate(f).mul(1, 0, 1);

        collider.velocity.mul(0, 1, 0);
        if(game.isButtonDown(0) && dl > 0.1 && f.length() > 0.0000001) {
            f.normalize().cross(0, 1, 0, r).normalize().mul(dx / dl * 150);
            f.mul(-dy / dl * 150).add(r);
            collider.velocity.add(f);
            f.normalize();

            float radians = (float)Math.acos(Math.max(-0.99f, Math.min(0.99f, f.x)));

            if(f.z > 0) {
                radians = (float)Math.PI * 2 - radians;
            }
            getNode().rotation.identity().rotate(radians, 0, 1, 0);
            getNode().childAt(0).rotation.rotate(-(float)Math.PI * game.getElapsedTime(), 0, 0, 1);
        }
        collider.velocity.y -= 2000 * game.getElapsedTime();
        collider.collide(game, getNode().position);

        setTarget();
    }

    private void setTarget() {
        getScene().target.set(getNode().position);
        getScene().target.y = 0;
        getScene().target.add(offset, getScene().eye);
        getScene().up.set(0, 1, 0);
    }
}
