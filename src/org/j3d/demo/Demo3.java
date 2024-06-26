package org.j3d.demo;

import org.j3d.Game;
import org.j3d.IO;
import org.j3d.MeshBuilder;
import org.j3d.Node;
import org.j3d.Scene;
import org.j3d.demo.App.Demo;
import org.lwjgl.input.Mouse;

public class Demo3 extends Demo {

    private Scene scene;

    @Override
    public void init(Game game) throws Exception {

        scene = new Scene(null, false, game);

        Node node = new Node();
        MeshBuilder builder = new MeshBuilder();

        builder.addBox(0, 0, 0, 100, 50, 100, 10, 5, 10, 50, false);
        builder.smooth();
        builder.smooth();
        builder.calcNormals(true);

        node.renderable = builder.build();
        node.lightingEnabled = true;
        node.texture = game.assets().load(IO.file("assets\\checker.png"));
        node.ambientColor.set(0.2f, 0.2f, 0.5f, 1);
        node.diffuseColor.set(1, 1, 1, 1);
        scene.root.add(node);

        node = new Node();
        node.isLight = true;
        node.position.set(200, 200, 200);
        node.lightColor.set(1.25f, 0.5f, 0, 1);
        node.lightRadius = 1000;
        scene.root.add(node);

        node = new Node();
        node.addComponent(game, scene, new Info());
        scene.root.add(node);
    }

    @Override
    public void nextFrame(Game game) throws Exception {
        scene.render(game);

        if(Mouse.isButtonDown(0)) {
            scene.camera.rotate(-Mouse.getDX(), -Mouse.getDY());
        }
    }
    
}
