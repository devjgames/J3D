package org.j3d;

public abstract class NodeComponent {
    
    private Game game = null;
    private Scene scene = null;
    private Node node = null;
    private boolean setup = false;

    public final Game game() {
        return game;
    }

    public final Scene scene() {
        return scene;
    }

    public final Node node() {
        return node;
    }

    final boolean setup() {
        return setup;
    }

    final void complete() {
        setup = true;
    }

    final void init(Game game, Scene scene, Node node) {
        this.game = game;
        this.scene = scene;
        this.node = node;
    }

    public void init() throws Exception {
    }

    public void start() throws Exception {
    }

    public void update() throws Exception {
    }

    public void renderSprites() throws Exception {
    }

    public final void remove() {
        node.components.remove(this);
    }

    final void newInstance(Game game, Scene scene, Node node) throws Exception {
        NodeComponent component = (NodeComponent)getClass().getConstructors()[0].newInstance();

        Utils.copy(this, component);

        component.init(game, scene, node);

        node.components.add(component);
    }
}
