package org.j3d.scene.demo1;

import org.j3d.Game;
import org.j3d.Log;
import org.j3d.scene.Node;
import org.j3d.scene.Scene;

public abstract class NodeFactory {
    
    private Node node = null;

    public Node newInstance(Game game, Scene scene) throws Exception {
        if(node == null) {
            Log.log(2, "creating node ...");
            node = create(game, scene);
        }
        return node.newInstance(game, scene);
    }

    protected abstract Node create(Game game, Scene scene) throws Exception;

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
