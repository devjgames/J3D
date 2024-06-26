package org.j3d;

import java.util.Iterator;
import java.util.Vector;

public final class Node implements Iterable<Node> {

    public static interface Visitor {
        boolean visit(Node node) throws Exception;
    }

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
    public boolean collidable = false;
    public boolean dynamic = false;
    public Texture texture = null;
    public Texture texture2 = null;
    public int zOrder = 0;
    public boolean isLight = false;
    public final Vec4 color = new Vec4(1, 1, 1, 1);
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
    public int triangleTag = 1;
    public int minTrisPerTree = 16;

    private final Vector<Node> children = new Vector<>();
    private Node parent = null;
    private OctTree octTree = null;

    final Vector<NodeComponent> components = new Vector<>();

    public Node() {
    }

    public Node(Game game, Scene scene, Node node) throws Exception {
        if(node.renderable != null) {
            renderable = node.renderable.newInstance();
        }
        if(node.texture != null) {
            texture = game.assets().load(node.texture.file);
        }
        if(node.texture2 != null) {
            texture2 = game.assets().load(node.texture2.file);
        }
        Utils.copy(node, this);
        for(Node child : node) {
            add(new Node(game, scene, child));
        }
        for(NodeComponent component : node.components) {
            component.newInstance(game, scene, this);
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

    public ParticleSystem getParticleSystem() {
        if(renderable instanceof ParticleSystem) {
            return (ParticleSystem)renderable;
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

    public Triangle triangleAt(Camera camera, int i, Triangle triangle) {
        return renderable.triangleAt(this, camera, i, triangle).setTag(triangleTag).setData(this).transform(model);
    }

    public OctTree getOctTree(Camera camera) {
        if(renderable != null && octTree == null && collidable && !dynamic) {
            if(renderable.triangleCount() != 0) {
                Vector<Triangle> triangles = new Vector<>();

                for(int i = 0; i != triangleCount(); i++) {
                    triangles.add(triangleAt(camera, i, new Triangle()));
                }
                octTree = OctTree.create(triangles, minTrisPerTree);
            }
        }
        return octTree;
    }

    public void clearOctTree() {
        octTree = null;
    }

    public void traverse(Visitor visitor) throws Exception {
        if(visitor.visit(this)) {
            for(Node node : this) {
                node.traverse(visitor);
            }
        }
    }

    public Node find(Visitor visitor, boolean recursive) throws Exception {
        if(visitor.visit(this)) {
            return this;
        }
        if(recursive) {
            for(Node node : this) {
                Node r = node.find(visitor, true);

                if(r != null) {
                    return r;
                }
            }
        }
        return null;
    }

    public int componentCount() {
        return components.size();
    }

    public NodeComponent componentAt(int i) {
        return components.get(i);
    }

    public NodeComponent find(Class<? extends NodeComponent> cls, boolean recursive) {
        for(NodeComponent component : components) {
            if(cls.isAssignableFrom(component.getClass())) {
                return component;
            }
        }
        if(recursive) {
            for(Node node : this) {
                NodeComponent r = node.find(cls, true);

                if(r != null) {
                    return r;
                }
            }
        }
        return null;
    }

    public void addComponent(Game game, Scene scene, NodeComponent component) {
        component.init(game, scene, this);
        components.add(component);
    }

    @Override
    public String toString() {
        return name;
    }
}