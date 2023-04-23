package org.j3d.sample;

import org.j3d.*;
import org.j3d.Collider.ContactListener;

import java.awt.event.KeyEvent;
import java.util.*;

public class Player extends Entity implements ContactListener {

    public int jumpAmount = 0;
    public boolean sky = false;
    public final Vec3 offset = new Vec3(50, 50, 50);

    private Node node;
    private Collider collider = new Collider();
    private Vector<Collidable> collidables = new Vector<>();
    private Sound jump, pain;
    private float distance;
    private float[] time = new float[] { 0 };
    private Vec3 origin = new Vec3();
    private Vec3 direction = new Vec3();
    private Texture font;
    private boolean dead = false;
    private float seconds = 2;

    @Override
    public void init() throws Exception {
        jump = game().assets().load(IO.file("assets/sound/jump.wav"));
        jump.setVolume(0.75f);

        pain = assets().load(IO.file("assets/sound/pain.wav"));
        pain.setVolume(0.75f);

        font = assets().load(IO.file("assets/font.png"));

        Node node = new Node();

        node.renderable = new MD2Mesh((MD2Mesh)assets().load(IO.file("assets/md2/hero.md2")));
        node.getAnimatedMesh().setSequence(0, 39, 9, true);
        node.texture = game().assets().load(IO.file("assets/md2/hero.png"));
        node.lightingEnabled = true;
        node.position.y = 2;
        node.scale.set(0.75f, 0.75f, 0.75f);
        node.rotate(0, -90);
        node.ambientColor.set(0.2f, 0.2f, 0.6f, 1);
        this.node = new Node();
        this.node.position.set(position);
        this.node.add(node);
        scene().root.add(this.node);

        if(sky) {
            node = assets().load(IO.file("assets/sky.obj"));
            node.follow = FollowCamera.EYE;
            scene().root.add(node);
            node = node.childAt(0);
            node.texture = assets().load(IO.file("assets/sky.png"));
            node.zOrder = -1000;
            node.depthTestEnabled = false;
            node.depthWriteEnabled = false;
        }
    }
    
    @Override
    public void start() throws Exception {
        Node node = scene().root.childAt(0);
        Vector<Triangle> triangles = new Vector<Triangle>(10000);

        for(int i = 0; i != node.count(); i++) {
            Node child = node.childAt(i);
            for(int j = 0; j != child.triangleCount(); j++) {
                Triangle triangle = child.getTriangle(camera(), j, new Triangle());
                if(child.name.equals("lava")) {
                    triangle.tag = 1;
                }
                triangles.add(triangle);
            }
        }
        collidables.add(new OctTreeCollidable(triangles, 16));
        collider.addContactListener(this);

        camera().target.set(0, 0, 0);
        camera().eye.set(offset);
        camera().up.set(0, 1, 0);
        camera().calcOffset();
        distance = camera().offset.length();
        camera().setTarget(this.node.position);

        scene().backgroundColor.set(0, 0, 1);
    }

    @Override
    public void renderSprites() {
        renderer().render(font, "F=" + game().frameRate() + "\nT=" + scene().getTrianglesRendered() , 10, 12, 16, 0, 5, 5, 1, 1, 1, 1);
        renderer().render(font, "C=" + collider.getTested(), 10, 12, 16, 0, 5, game().h() - 17, 1, 1, 1, 1);
    }

    @Override
    public void update() {
        if(dead) {
            seconds -= game().elapsedTime();
            if(seconds <= 0) {
                dead = false;
                this.node.position.set(position);
                camera().target.set(0, 0, 0);
                camera().eye.set(offset);
                camera().up.set(0, 1, 0);
                camera().calcOffset();
                distance = camera().offset.length();
                camera().setTarget(this.node.position);
                node.childAt(0).getAnimatedMesh().setSequence(0, 39, 9, true);
            }
            if(dead) {
                return;
            }
        }
        if(game().buttonDown(2)) {
            camera().rotate(game().dX(), game().dY());
        }
        if(jumpAmount > 0) {
            boolean down = game().keyDown(KeyEvent.VK_SPACE);
            if(down && collider.getOnGround()) {
                collider.velocity.y = jumpAmount;
                jump.play(false);
            }
        }
        boolean moving = collider.move(collidables, camera(), node, game());
        if((!collider.getOnGround() && jumpAmount > 0) || dead) {
            node.childAt(0).getAnimatedMesh().setSequence(66, 68, 7, false);
        } else if(moving) {
            node.childAt(0).getAnimatedMesh().setSequence(40, 45, 10, true);
        } else {
            node.childAt(0).getAnimatedMesh().setSequence(0, 39, 9, true);
        }
        camera().setTarget(node.position);

        origin.set(camera().target);
        direction.normalize(camera().offset);
        time[0] =  distance + 12;

        float d = distance;
        if(collider.intersect(collidables, camera(), origin, direction, 1, time) != null) {
            time[0] = Math.min(distance, time[0]);
            d = time[0] - 12;
        }
        direction.scale(d);
        camera().target.add(direction, camera().eye);
    }

    @Override
    public void contactMade(Collider collider, Collidable collidable, Triangle triangle) {
        if(triangle.tag == 1 && !dead) {
            dead = true;
            seconds = 2;
            pain.play(false);
        }
    }
}
