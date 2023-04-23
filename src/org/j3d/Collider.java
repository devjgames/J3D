package org.j3d;

import java.util.Vector;

public class Collider implements TriangleVisitor {

    public static interface ContactListener {
        void contactMade(Collider collider, Collidable collidable, Triangle triangle);
    }

    public final Vec3 velocity = new Vec3();
    public float speed = 100;
    public float gravity = -2000;
    public float radius = 16;
    public int loopCount = 3;
    public float groundSlope = 45;
    public float roofSlope = 45;

    private boolean onGround = false;
    private boolean hitRoof = false;
    private Mat4 groundMatrix = new Mat4();
    private Vec3 groundNormal = new Vec3();
    private Vec3 delta = new Vec3();
    private Vec3 hNormal = new Vec3();
    private Vec3 rPosition = new Vec3();
    private Triangle hTriangle = new Triangle();
    private Vec3 cPoint = new Vec3();
    private Vec3 iPoint = new Vec3();
    private Vec3 origin = new Vec3();
    private Vec3 direction = new Vec3();
    private Vec3 r = new Vec3();
    private Vec3 u = new Vec3();
    private Vec3 f = new Vec3();
    private AABB bounds = new AABB();
    private float[] time = new float[] { 0 };
    private Collidable hit = null;
    private Vector<ContactListener> listeners = new Vector<>();
    private int tested = 0;

    public void addContactListener(ContactListener l) {
        listeners.add(l);
    }

    public void removeContactListener(ContactListener l) {
        listeners.remove(l);
    }

    public void removeAllContactListeners() {
        listeners.removeAllElements();
    }

    public boolean getOnGround() {
        return onGround;
    }

    public boolean getHitRoof() {
        return hitRoof;
    }

    public int getTested() {
        return tested;
    }

    public Collidable intersect(Vector<Collidable> collidables, Camera camera, Vec3 origin, Vec3 direction, float buffer, float[] time) {
        Collidable iCollidable = null;
        for(Collidable collidable : collidables) {
            if(collidable.enabled) {
                if(collidable.intersects(camera, origin, direction, buffer, time)) {
                    iCollidable = collidable;
                }
            }
        }
        return iCollidable;
    }

    public boolean move(Vector<Collidable> collidables, Camera camera, Node node, Game game) {
        boolean moving = false;
        velocity.x = velocity.z = 0;
        if(game.buttonDown(0)) {
            float dX = game.w() / 2 - game.mouseX();
            float dY = game.mouseY() - game.h() / 2;
            float d = (float)Math.sqrt(dX * dX + dY * dY);
            float y = velocity.y;
            float degrees;
            velocity.y = 0;
            camera.move(velocity, dX / d * speed, dY / d * speed, null);
            if(velocity.length() > 1) {
                moving = true;
                f.set(velocity);
                f.normalize();
                degrees = (float)Math.acos(Math.max(-0.99, Math.min(0.99, f.x))) * 180 / (float)Math.PI;
                if(f.z > 0) {
                    degrees = 360 - degrees;
                }
                node.rotation.toIdentity();
                node.rotate(1, degrees);
            }
            velocity.y = y;
        }
        velocity.y += gravity * game.elapsedTime();

        velocity.scale(game.elapsedTime(), delta);
        if(delta.length() > 0.0000001) {
            float len = delta.length();
            if(len > radius * 0.5f) {
                delta.normalize().scale(radius * 0.5f);
            }
            delta.transformNormal(groundMatrix, delta);
            node.position.add(delta);
            resolve(collidables, node.position, camera, game);
        }
        return moving;
    }

    public void resolve(Vector<Collidable> collidables, Vec3 position, Camera camera, Game game) {
        groundMatrix.toIdentity();
        onGround = false;
        hitRoof = false;
        groundNormal.set(0, 0, 0);

        tested = 0;

        for(int i = 0; i < loopCount; i++) {
            origin.set(position);
            bounds.set(
                position.x - radius - 1, position.y - radius - 1, position.z - radius - 1,
                position.x + radius + 1, position.y + radius + 1, position.z + radius + 1
            );
            hit = null;
            time[0] = radius;
            for(Collidable collidable : collidables) {
                if(collidable.enabled) {
                    collidable.traverse(camera, bounds, this);
                }
            }
            if(hit != null) {
                u.set(0, 1, 0);
                if(Math.acos(hNormal.dot(u)) * 180 / Math.PI < groundSlope) {
                    groundNormal.add(hNormal);
                    onGround = true;
                }
                u.set(0, -1, 0);
                if(Math.acos(hNormal.dot(u)) * 180 / Math.PI < roofSlope) {
                    hitRoof = true;
                    velocity.y = 0;
                }
                position.set(rPosition);
                for(ContactListener l : listeners) {
                    l.contactMade(this, hit, hTriangle);
                }
            } else {
                break;
            }
        }

        if(onGround) {
            u.normalize(groundNormal);
            r.set(1, 0, 0);
            r.cross(u, f).normalize();
            u.cross(f, r).normalize();
            groundMatrix.set(
                r.x, u.x, f.x, 0,
                r.y, u.y, f.y, 0,
                r.z, u.z, f.z, 0,
                0, 0, 0, 1
            );
            velocity.y = 0;
        }
    }

    @Override
    public void visit(Triangle triangle, Collidable collidable) {
        float t = time[0];
        direction.neg(triangle.n);
        if(triangle.intersectsPlane(origin, direction, time)) {
            iPoint.set(direction).scale(time[0]);
            origin.add(iPoint, iPoint);
            if(triangle.contains(iPoint, 0)) {
                hNormal.set(triangle.n);
                iPoint.add(rPosition.set(hNormal).scale(radius), rPosition);
                hit = collidable;
                hTriangle.set(triangle.p1, triangle.p2, triangle.p3);
                hTriangle.tag = triangle.tag;
            } else {
                time[0] = t;
                cPoint = triangle.closestEdgePoint(origin, cPoint);
                origin.sub(cPoint, direction);
                if(direction.length() > 0.0000001 && direction.length() < time[0]) {
                    time[0] = direction.length();
                    hNormal.normalize(direction);
                    cPoint.add(rPosition.set(hNormal).scale(radius), rPosition);
                    hit = collidable;
                    hTriangle.set(triangle.p1, triangle.p2, triangle.p3);
                    hTriangle.tag = triangle.tag;
                }
            }
        }
        tested++;
    }
}
