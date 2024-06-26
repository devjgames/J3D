package org.j3d.demo;

import org.j3d.Collider;
import org.j3d.IO;
import org.j3d.Node;
import org.j3d.NodeComponent;
import org.j3d.Sound;
import org.j3d.Triangle;
import org.j3d.Vec3;
import org.j3d.Collider.ContactListener;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class Player extends NodeComponent implements ContactListener {

    public int jumpAmount = 900;
    public float offset = 75;

    private Collider collider = new Collider();
    private Sound jump;
    private float[] time = new float[1];
    private Vec3 origin = new Vec3();
    private Vec3 direction = new Vec3();

    @Override
    public void init() throws Exception {
        if(scene().inDesign()) {
            return;
        }

        jump = game().assets().load(IO.file("assets/jump.wav"));
        jump.setVolume(0.75f);

        scene().camera.calcOffset();
        scene().camera.offset.normalize().scale(offset);
        scene().camera.target.set(node().position);
        scene().camera.target.add(scene().camera.offset, scene().camera.eye);

        collider.addContactListener(this);
    }

    @Override
    public void update() throws Exception {
        if(scene().inDesign()) {
            return;
        }

        if(Mouse.isButtonDown(1)) {
            scene().camera.rotate(-Mouse.getDX(), -Mouse.getDY());
        }
        if(jumpAmount > 0) {
            boolean down = Keyboard.isKeyDown(Keyboard.KEY_SPACE);
            if(down && collider.getOnGround()) {
                collider.velocity.y = jumpAmount;
                jump.play(false);
            }
        }
        boolean moving = collider.move(scene().camera, scene().root, node(), game());
        if(!collider.getOnGround() && jumpAmount > 0) {
            node().childAt(0).getAnimatedMesh().setSequence(66, 68, 7, false);
        } else if(moving) {
            node().childAt(0).getAnimatedMesh().setSequence(40, 45, 10, true);
        } else {
            node().childAt(0).getAnimatedMesh().setSequence(0, 39, 9, true);
        }
        scene().camera.setTarget(node().position);

        origin.set(scene().camera.target);
        direction.normalize(scene().camera.offset);
        time[0] =  offset + 12;

        float d = offset;
        if(collider.intersect(scene().camera, scene().root, origin, direction, 1, time) != null) {
            time[0] = Math.min(offset, time[0]);
            d = time[0] - 12;
        }
        direction.scale(d);
        scene().camera.target.add(direction, scene().camera.eye);
    }

    @Override
    public void contactMade(Collider collider, Node node, Triangle triangle) {

    }
}