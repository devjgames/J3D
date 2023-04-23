package org.j3d;

import java.util.Iterator;
import java.util.Vector;

public final class Node implements Iterable<Node> {

    private static final Vec3 f = new Vec3();
    private static final Vec3 u = new Vec3();
    private static final Vec3 r = new Vec3();

    public String name = "Node";
    public boolean visible = true;
    public Renderable renderable = null;
    public final Vec3 position = new Vec3();
    public final Mat4 rotation = new Mat4();
    public final Vec3 scale = new Vec3(1, 1, 1);
    public final Mat4 localModel = new Mat4();
    public final Mat4 model = new Mat4();
    public final Mat4 modelIT = new Mat4();
    public final AABB bounds = new AABB();
    public CullState cullState = CullState.BACK;
    public boolean blendEnabled = false;
    public boolean additiveBlend = false;
    public boolean depthWriteEnabled = true;
    public boolean depthTestEnabled = true;
    public boolean maskEnabled = false;
    public Texture texture = null;
    public Texture texture2 = null;
    public int zOrder = 0;
    public boolean isLight = false;
    public final Vec4 ambientColor = new Vec4(0.2f, 0.2f, 0.2f, 1);
    public final Vec4 diffuseColor = new Vec4(0.8f, 0.8f, 0.8f, 1);
    public final Vec4 lightColor = new Vec4(1, 1, 1, 1);
    public float lightRadius = 300;
    public final Vec3 absolutePosition = new Vec3();
    public int lightSampleCount = 32;
    public float lightSampleRadius = 32;
    public boolean lightingEnabled = false;
    public boolean lightMapEnabled = false;
    public boolean castsShadow = true;
    public boolean receivesShadow = true;
    public FollowCamera follow = FollowCamera.NONE;
    public Object tag = null;

    private final Vector<Node> children = new Vector<>();
    private Node parent = null;

    public Node() {
    }

    public Node(Node node) {
        name = node.name;
        visible = node.visible;
        renderable = node.renderable;
        position.set(node.position);
        rotation.set(node.rotation);
        scale.set(node.scale);
        cullState = node.cullState;
        blendEnabled = node.blendEnabled;
        additiveBlend = node.additiveBlend;
        depthWriteEnabled = node.depthWriteEnabled;
        depthTestEnabled = node.depthTestEnabled;
        maskEnabled = node.maskEnabled;
        texture = node.texture;
        texture2 = node.texture2;
        zOrder = node.zOrder;
        isLight = node.isLight;
        ambientColor.set(node.ambientColor);
        diffuseColor.set(node.diffuseColor);
        lightColor.set(node.lightColor);
        lightRadius = node.lightRadius;
        lightSampleCount = node.lightSampleCount;
        lightSampleRadius = node.lightSampleRadius;
        lightMapEnabled = node.lightMapEnabled;
        castsShadow = node.castsShadow;
        receivesShadow = node.receivesShadow;
        lightingEnabled = node.lightingEnabled;
        follow = node.follow;
        for(Node child : node) {
            add(new Node(child));
        }
    }

    public Mesh getMesh() {
        if(renderable instanceof Mesh) {
            return (Mesh)renderable;
        }
        return null;
    }

    public MD2Mesh getAnimatedMesh() {
        if(renderable instanceof MD2Mesh) {
            return (MD2Mesh)renderable;
        }
        return null;
    }

    public Sprite3D getSprite3D() {
        if(renderable instanceof Sprite3D) {
            return (Sprite3D)renderable;
        }
        return null;
    }

    public Node getParent() {
        return parent;
    }

    public Node getRoot() {
        Node root = this;
        while (root.parent != null) {
            root = root.parent;
        }
        return root;
    }

    public int count() {
        return children.size();
    }

    public Node childAt(int i) {
        return children.get(i);
    }

    public void detachFromParent() {
        if (parent != null) {
            parent.children.remove(this);
            parent = null;
        }
    }

    public void add(Node node) {
        node.detachFromParent();
        children.add(node);
        node.parent = this;
    }

    public void removeAllChildren() {
        while(count() != 0) {
            children.get(0).detachFromParent();
        }
    }

    @Override
    public Iterator<Node> iterator() {
        return children.iterator();
    }

    public Node find(String name, boolean recursive) {
        for(Node node : this) {
            if(node.name.startsWith(name)) {
                return node;
            }
        }
        if(recursive) {
            for(Node node : this) {
                Node r = node.find(name, true);
                if(r != null) {
                    return r;
                }
            }
        }
        return null;
    }

    public void calcBoundsAndTransform(Camera camera) {
        if(follow == FollowCamera.EYE) {
            position.set(camera.eye);
        } else if(follow == FollowCamera.TARGET) {
            position.set(camera.target);
        }
        localModel.toIdentity().translate(position).mul(rotation).scale(scale);
        model.set(localModel);
        if(parent != null) {
            model.set(parent.model).mul(localModel);
        }
        modelIT.set(model).invert().transpose();
        if(renderable != null) {
            renderable.getBounds(this, camera, bounds).transform(model);
        }
        for(Node node : this) {
            node.calcBoundsAndTransform(camera);
            bounds.add(node.bounds);
        }
        absolutePosition.set(0, 0, 0).transform(model, absolutePosition);
    }

    public void rotate(int axis, float degrees) {
        r.set(rotation.m00, rotation.m10, rotation.m20).normalize();
        u.set(rotation.m01, rotation.m11, rotation.m21).normalize();
        f.set(rotation.m02, rotation.m12, rotation.m22).normalize();
        if(axis == 0) {
            rotation.toIdentity().rotate(r, degrees);
            u.transformNormal(rotation, u).normalize();
            f.transformNormal(rotation, f).normalize();
        } else if(axis == 1) {
            rotation.toIdentity().rotate(u, degrees);
            r.transformNormal(rotation, r).normalize();
            f.transformNormal(rotation, f).normalize();
        } else {
            rotation.toIdentity().rotate(f, degrees);
            r.transformNormal(rotation, r).normalize();
            u.transformNormal(rotation, u).normalize();
        }
        rotation.set(
                r.x, u.x, f.x, 0,
                r.y, u.y, f.y, 0,
                r.z, u.z, f.z, 0,
                0, 0, 0, 1
        );
    }

    public int triangleCount() {
        if(renderable != null) {
            return renderable.triangleCount();
        }
        return 0;
    }

    public Triangle getTriangle(Camera camera, int i, Triangle triangle) {
        return renderable.getTriangle(this, camera, i, triangle).transform(model);
    }
}