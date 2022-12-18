package org.j3d.scene.demo1.factories;

import org.j3d.Game;
import org.j3d.scene.Node;
import org.j3d.scene.Scene;
import org.j3d.scene.demo1.NodeFactory;
import org.j3d.scene.demo1.components.LightComponent;

public class Light1 extends NodeFactory {
    
    @Override
    protected Node create(Game game, Scene scene) throws Exception {
        Node node = new Node();

        node.isLight = true;
        node.lightColor.set(2, 1, 0.5f);
        node.addComponent(game, scene, new LightComponent());

        return node;
    }
}
