package org.j3d.scene.demo1.factories;

import org.j3d.Game;
import org.j3d.IO;
import org.j3d.scene.Node;
import org.j3d.scene.Scene;
import org.j3d.scene.demo1.NodeFactory;

public class Bridge1 extends NodeFactory {
    
    @Override
    protected Node create(Game game, Scene scene) throws Exception {
        Node node = new Node();

        node.renderable = game.getAssets().load(IO.file("assets/meshes2/bridge1.obj"));
        node.renderable = node.renderable.newInstance();
        node.collidable = true;

        return node;
    }
}
