package org.j3d.scene.demo1.factories;

import org.j3d.Game;
import org.j3d.scene.Node;
import org.j3d.scene.Scene;
import org.j3d.scene.demo1.NodeFactory;
import org.j3d.scene.demo1.components.InfoComponent;

public class Info1 extends NodeFactory {
    
    @Override
    protected Node create(Game game, Scene scene) throws Exception {
        Node node = new Node();

        node.addComponent(game, scene, new InfoComponent());

        return node;
    }
}
