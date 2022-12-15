package org.j3d.scene;

import javax.swing.UIManager;

import org.j3d.Game;

public class NodeComponent {
    
    private Game game = null;
    private Scene scene = null;
    private Node node = null;

    public Game getGame() {
        return game;
    }

    public Scene getScene() {
        return scene;
    }

    public Node getNode() {
        return node;
    }

    void init(Game game, Scene scene, Node node) {
        this.game = game;
        this.scene = scene;
        this.node = node;
    }

    public void init() throws Exception {
    }

    public void start() throws Exception {
    }

    public void pushSprites(Renderer renderer) throws Exception {
    }

    public void handleUI(UIManager manager) throws Exception {
    }

    public void update() throws Exception {
    }
}
