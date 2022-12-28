package org.j3d.scene.demo1.factories;

import org.j3d.Game;
import org.j3d.IO;
import org.j3d.scene.Node;
import org.j3d.scene.Scene;
import org.j3d.scene.demo1.NodeFactory;
import org.j3d.scene.demo1.components.PlayerComponent;

public class Player extends NodeFactory {
    
    @Override
    protected Node create(Game game, Scene scene) throws Exception {
        Node node = new Node();
        
        node.addChild(new Node());
        node.childAt(0).renderable = game.getAssets().load(IO.file("assets/meshes/cube.obj"));
        node.childAt(0).renderable = node.childAt(0).renderable.newInstance();
        node.addComponent(game, scene, new PlayerComponent());

        return node;

    }
}
