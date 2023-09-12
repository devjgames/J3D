package org.j3d.demo;

import org.j3d.Collider;
import org.j3d.IO;
import org.j3d.Node;
import org.j3d.NodeComponent;
import org.j3d.Sound;
import org.j3d.Texture;
import org.j3d.Triangle;
import org.j3d.Vec3;
import org.j3d.Collider.ContactListener;

import java.awt.event.KeyEvent;

public class Player extends NodeComponent implements ContactListener {

    public int jumpAmount = 800;
    public float offset = 75;

    private Collider collider = new Collider();
    private Sound jump, pain;
    private float[] time = new float[1];
    private Vec3 origin = new Vec3();
    private Vec3 direction = new Vec3();
    private Vec3 startPosition = new Vec3();
    private Vec3 startOffset = new Vec3();
    private Texture font;
    private boolean dead = false;
    private float seconds = 2;

    @Override
    public void init() throws Exception {
        if(scene().inDesign()) {
            return;
        }

        jump = game().assets().load(IO.file("assets/jump.wav"));
        jump.setVolume(0.75f);

        pain = game().assets().load(IO.file("assets/pain.wav"));
        pain.setVolume(0.75f);

        font = game().assets().load(IO.file("assets/font.png"));

        scene().camera.calcOffset();
        scene().camera.offset.normalize().scale(offset);
        startOffset.set(scene().camera.offset);
        scene().camera.target.set(node().position);
        scene().camera.target.add(scene().camera.offset, scene().camera.eye);

        collider.addContactListener(this);
        startPosition.set(node().position);
    }

    @Override
    public void update() throws Exception {
        if(scene().inDesign()) {
            return;
        }

        if(dead) {
            seconds -= game().elapsedTime();
            if(seconds <= 0) {
                dead = false;
                node().position.set(startPosition);
                scene().camera.offset.set(startOffset);
                scene().camera.target.set(node().position);
                scene().camera.target.add(scene().camera.offset, scene().camera.eye);
                scene().camera.up.set(0, 1, 0);
                node().childAt(0).getAnimatedMesh().setSequence(0, 39, 9, true);
            }
            if(dead) {
                return;
            }
        }
        if(game().buttonDown(2)) {
            scene().camera.rotate(game().dX(), game().dY());
        }
        if(jumpAmount > 0) {
            boolean down = game().keyDown(KeyEvent.VK_SPACE);
            if(down && collider.getOnGround()) {
                collider.velocity.y = jumpAmount;
                jump.play(false);
            }
        }
        boolean moving = collider.move(scene().camera, scene().root, node(), game());
        if((!collider.getOnGround() && jumpAmount > 0) || dead) {
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
    public void renderSprites() throws Exception {
        if(scene().inDesign()) {
            return;
        }

        game().renderer().render(font, "C=" + collider.getTested(), 10, 12, 16, 0, 5, game().h() - 17, 1, 1, 1, 1);
    }

    @Override
    public void contactMade(Collider collider, Node node, Triangle triangle) {
        if(triangle.tag == 2 && !dead) {
            dead = true;
            seconds = 2;
            pain.play(false);
        }
    }
}