package org.j3d;

import java.util.Vector;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Collider {

    public static interface TriangleSelector {
        boolean getEnabled();
        void setEnabled(boolean enabled);
        boolean intersect(Collider collider);
        boolean resolve(Collider collider);
    }
    
    public static interface ContactListener {
        void contactMade(TriangleSelector selector) throws Exception;
    }

    public final Vector3f velocity = new Vector3f();
    public final Vector3f origin = new Vector3f();
    public final Vector3f direction = new Vector3f();
    public final float[] time = new float[] { 0 };
    public float gravity = 2000;
    public float groundSlope = 60;
    public float roofSlope = 45;
    public float speed = 125;
    public float radius = 16;
    public float intersectionBuffer = 0;
    public int intersectionBits = 0xFF;
    public int loopCount = 3;
    public final BoundingBox resolveBounds = new BoundingBox();

    private final Vector3f groundNormal = new Vector3f();
    private final Vector3f rPos = new Vector3f();
    private final Vector3f rNormal = new Vector3f();
    private final Vector3f delta = new Vector3f();
    private final Vector3f r = new Vector3f();
    private final Vector3f u = new Vector3f();
    private final Vector3f f = new Vector3f();
    private final Matrix4f groundMatrix = new Matrix4f();
    private boolean onGround = false;
    private boolean hitRoof = false;
    private final Vector<ContactListener> listeners = new Vector<>();
    private final Vector<TriangleSelector> selectors = new Vector<>();
    private final Triangle triangle = new Triangle();
    private int tested = 0;
    private Vector3f position = null;

    public boolean getOnGround() {
        return onGround;
    }

    public boolean getHitRoot() {
        return hitRoof;
    }

    public int getTested() {
        return tested;
    }

    public Triangle getTriangle() {
        return triangle;
    }

    public void addTriangleSelector(TriangleSelector selector) {
        selectors.add(selector);
    }

    public void addContactListener(ContactListener listener) {
        listeners.add(listener);
    }

    public boolean selectorIntersect(Triangle triangle) {
        if((triangle.tag & intersectionBits) != 0) {
            if(triangle.intersects(origin, direction, intersectionBuffer, time)) {
                this.triangle.set(triangle);
                return true;
            }
        }
        return false;
    }

    public boolean selectorResolve(Triangle triangle) {
        if(triangle.resolve(rPos, radius, position, rNormal, time)) {
            this.triangle.set(triangle);
            return true;
        }
        tested++;

        return false;
    }

    public TriangleSelector intersect() {
        TriangleSelector hit = null;

        for(TriangleSelector selector : selectors) {
            if(selector.getEnabled()) {
                if(selector.intersect(this)) {
                    hit = selector;
                }
            }
        }
        return hit;
    }

    public void collide(Game game, Vector3f position) throws Exception {
        tested = 0;
        velocity.y -= gravity * game.getElapsedTime();
        velocity.mul(game.getElapsedTime(), delta).mulDirection(groundMatrix);
        if(delta.length() > radius * 0.5f) {
            delta.normalize().mul(radius * 0.5f);
        }
        position.add(delta);
        this.position = position;
        groundMatrix.identity();
        onGround = false;
        hitRoof = false;
        groundNormal.zero();
        for(int i = 0; i < loopCount; i++) {
            resolveBounds.min.set(position).sub(radius, radius, radius);
            resolveBounds.max.set(position).add(radius, radius, radius);
            time[0] = radius;
            rPos.zero();
            rNormal.zero();

            TriangleSelector hit = null;

            for(TriangleSelector selector : selectors) {
                if(selector.getEnabled()) {
                    if(selector.resolve(this)) {
                        hit = selector;
                    }
                }
            }
            if(hit != null) {
                if(Math.acos(Math.max(-0.99f, Math.min(0.99f, rNormal.dot(u.set(0, 1, 0))))) * 180 / Math.PI < groundSlope) {
                    onGround = true;
                    groundNormal.add(rNormal);
                    velocity.y = 0;
                }
                if(Math.acos(Math.max(-0.99f, Math.min(0.99f, rNormal.dot(u.set(0, -1, 0))))) * 180 / Math.PI < roofSlope) {
                    hitRoof = true;
                    velocity.y = 0;
                }
                position.set(rPos);

                for(ContactListener listener : listeners) {
                    listener.contactMade(hit);
                }
            } else {
                break;
            }
        }
        if(onGround) {
            groundNormal.normalize(u);
            r.set(1, 0, 0);
            r.cross(u, f).normalize();
            u.cross(f, r).normalize();
            groundMatrix.set(
                r.x, r.y, r.z, 0,
                u.x, u.y, u.z, 0,
                f.x, f.y, f.z, 0,
                0, 0, 0, 1
            );
        }
    }
}
