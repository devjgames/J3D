package org.j3d.scene;

import java.util.Iterator;
import java.util.Vector;

import org.j3d.BlendState;
import org.j3d.BoundingBox;
import org.j3d.Collider;
import org.j3d.CullState;
import org.j3d.DepthState;
import org.j3d.Game;
import org.j3d.Triangle;
import org.j3d.Collider.TriangleSelector;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Node implements Iterable<Node>, TriangleSelector {
    
    public boolean visible = true;
    public final Vector3f position = new Vector3f();
    public final Vector3f absolutePosition = new Vector3f();
    public final Vector3f scale = new Vector3f(1, 1, 1);
    public final Matrix4f rotation = new Matrix4f();
    public final Matrix4f model = new Matrix4f();
    public final BoundingBox bounds = new BoundingBox();
    public final Vector3f lightColor = new Vector3f(1, 1, 1);
    public float lightRadius = 600;
    public boolean isLight = false;
    public DepthState depthState = DepthState.READ_WRITE;
    public BlendState blendState = BlendState.OPAQUE;
    public CullState cullState = CullState.BACK;
    public Renderable renderable = null;
    public int zOrder = 0;
    public Object data = null;
    public boolean collidable = false;
    public int triangleTag = 1;

    private final Vector<Node> children = new Vector<>();
    private final Vector<NodeComponent> components = new Vector<>();
    private Node parent = null;
    private boolean enabled = true;

    public int getCount() {
        return children.size();
    }

    public Node childAt(int i) {
        return children.get(i);
    }

    public Node getParent() {
        return parent;
    }

    public Node getRoot() {
        Node root = parent;

        while(root.parent != null) {
            root = root.parent;
        }
        return root;
    }

    public void detachFromParent() {
        if(parent != null) {
            parent.children.remove(this);
            parent = null;
        }
    }

    public void addChild(Node child) {
        child.detachFromParent();
        child.parent = this;
        children.add(child);
    }

    public void detachAllChildren() {
        while(getCount() != 0) {
            children.firstElement().detachFromParent();
        }
    }

    public int getComponentCount() {
        return components.size();
    }

    @SuppressWarnings("unchecked")
    public <T extends NodeComponent> T componentAt(int i) {
        return (T)components.get(i);
    }

    @SuppressWarnings("unchecked")
    public <T extends NodeComponent> T findComponent(Class<? extends NodeComponent> cls, boolean recursive) {
        for(NodeComponent component : components) {
            if(cls.isAssignableFrom(component.getClass())) {
                return (T)component;
            }
        }
        if(recursive) {
            for(Node node : this) {
                NodeComponent r = node.findComponent(cls, true);

                if(r != null) {
                    return (T)r;
                }
            }
        }
        return null;
    }

    public void addComponent(Game game, Scene scene, NodeComponent component) {
        component.init(game, scene, this);
        components.add(component);
    }

    public void removeComponent(int i) {
        components.removeElementAt(i);
    }

    public void clearComponents() {
        components.clear();
    }

    public void init() throws Exception {
        for(NodeComponent component : components) {
            component.init();
        }
        for(Node node : this) {
            node.init();
        }
    }

    public void start() throws Exception {
        for(NodeComponent component : components) {
            component.start();
        }
        for(Node node : this) {
            node.start();
        }
    }

    public void update(Scene scene) throws Exception {
        for(NodeComponent component : components) {
            component.update();
        }
        if(renderable != null) {
            renderable.update(scene, this);
        }
        for(Node node : this) {
            node.update(scene);
        }
    }

    public void pushSprites(Renderer renderer) throws Exception {
        for(NodeComponent component : components) {
            component.pushSprites(renderer);
        }
        for(Node node : this) {
            node.pushSprites(renderer);
        }
    }

    public void calcBoundsAndTransform() {
        model.identity().translate(position).mul(rotation).scale(scale);
        if(parent != null) {
            parent.model.mul(model, model);
        }
        absolutePosition.zero().mulPosition(model);
        bounds.clear();
        if(renderable != null) {
            bounds.min.set(renderable.getBounds().min);
            bounds.max.set(renderable.getBounds().max);
            bounds.transform(model);
        }
        for(Node node : this) {
            node.calcBoundsAndTransform();
            bounds.add(node.bounds);
        }
    }

    public int getTriangleCount() {
        if(renderable != null) {
            return renderable.getTriangleCount();
        }
        return 0;
    }

    public void triangleAt(int i, Triangle triangle) {
        renderable.triangleAt(i, triangle);
        triangle.transform(model);
        triangle.tag = triangleTag;
    }

    public int countTriangles() {
        int count = getTriangleCount();

        for(Node node : this) {
            count += node.getTriangleCount();
        }
        return count;
    }

    @Override
    public Iterator<Node> iterator() {
        return children.iterator();
    }

    @Override
    public boolean getEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private static Triangle triangle = new Triangle();

    @Override
    public boolean intersect(Collider collider) {
        boolean hit = false;

        for(int i = 0; i != getTriangleCount(); i++) {
            triangleAt(i, triangle);
            if(collider.selectorIntersect(triangle)) {
                hit = true;
            }
        }
        return hit;
    }

    @Override
    public boolean resolve(Collider collider) {
        boolean hit = false;

        if(collider.resolveBounds.touches(bounds)) {
            for(int i = 0; i != getTriangleCount(); i++) {
                triangleAt(i, triangle);
                if(collider.selectorResolve(triangle)) {
                    hit = true;
                }
            }
        }
        return hit;
    }

    public void addToCollider(Collider collider) {
        if(collidable) {
            collider.addTriangleSelector(this);
        }
        for(Node node : this) {
            node.addToCollider(collider);
        }
    }
}
