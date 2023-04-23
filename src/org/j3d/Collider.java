package org.j3d;

import java.util.Vector;

public class Collider {

    public static interface ContactListener {
        void contactMade(Collider collider, Node node, Triangle triangle);
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
    private Triangle triangle = new Triangle();
    private Vec3 cPoint = new Vec3();
    private Vec3 iPoint = new Vec3();
    private Vec3 origin = new Vec3();
    private Vec3 direction = new Vec3();
    private Vec3 r = new Vec3();
    private Vec3 u = new Vec3();
    private Vec3 f = new Vec3();
    private AABB bounds = new AABB();
    private float[] time = new float[] { 0 };
    private Node hit = null;
    private Triangle hitTri = null;
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

    public Triangle intersect(Camera camera, Node root, Vec3 origin, Vec3 direction, float buffer, float[] time) throws Exception {
        hitTri = null;
        root.traverse((n) -> {
            bounds.clear();
            bounds.add(origin);
            bounds.add(iPoint.set(direction).scale(time[0]).add(origin));

            if(n.bounds.touches(bounds)) {
                OctTree tree = n.getOctTree(camera);

                if(tree != null) {
                    tree.traverse(bounds, (t) -> {
                        if(t.intersects(origin, direction, buffer, time)) {
                            hitTri = hTriangle.set(t);
                        }
                    });
                } else {
                    for(int i = 0; i != n.triangleCount(); i++) {
                        if(n.triangleAt(camera, i, triangle).intersects(origin, direction, buffer, time)) {
                            hitTri = hTriangle.set(triangle);
                        }
                    }
                }
                return true;
            }
            return false;
        });
        return hitTri;
    }

    public boolean move(Camera camera, Node root, Node node, Game game) throws Exception {
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
            resolve(root, node.position, camera, game);
        }
        return moving;
    }

    public void resolve(Node root, Vec3 position, Camera camera, Game game) throws Exception {
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
            root.traverse((n) -> {
                if(n.bounds.touches(bounds)) {
                    OctTree tree = n.getOctTree(camera);
    
                    if(tree != null) {
                        tree.traverse(bounds, (t) -> {
                            resolve(t, n);                            
                        });
                    } else {
                        for(int j = 0; j != n.triangleCount(); j++) {
                            resolve(n.triangleAt(camera, j, triangle), n);
                        }
                    }
                    return true;
                }
                return false;
            });
            if(hit != null) {
                u.set(0, 1, 0);
                if(Math.acos(Math.max(-0.99, Math.min(0.99, hNormal.dot(u)))) * 180 / Math.PI < groundSlope) {
                    groundNormal.add(hNormal);
                    onGround = true;
                    velocity.y = 0;
                }
                u.set(0, -1, 0);
                if(Math.acos(Math.max(-0.99, Math.min(0.99, hNormal.dot(u))))  * 180 / Math.PI < roofSlope) {
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
        }
    }

    public void resolve(Triangle triangle, Node node) {
        float t = time[0];
        direction.neg(triangle.n);
        if(triangle.intersectsPlane(origin, direction, time)) {
            iPoint.set(direction).scale(time[0]);
            origin.add(iPoint, iPoint);
            if(triangle.contains(iPoint, 0)) {
                hNormal.set(triangle.n);
                iPoint.add(rPosition.set(hNormal).scale(radius), rPosition);
                hit = node;
                hTriangle.set(triangle);
            } else {
                time[0] = t;
                cPoint = triangle.closestEdgePoint(origin, cPoint);
                origin.sub(cPoint, direction);
                if(direction.length() > 0.0000001 && direction.length() < time[0]) {
                    time[0] = direction.length();
                    hNormal.normalize(direction);
                    cPoint.add(rPosition.set(hNormal).scale(radius), rPosition);
                    hit = node;
                    hTriangle.set(triangle);
                }
            }
        }
        tested++;
    }
}
