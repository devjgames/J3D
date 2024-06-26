package org.j3d.demo;

import org.j3d.Game;
import org.j3d.IO;
import org.j3d.LightMapper;
import org.j3d.MeshBuilder;
import org.j3d.Node;
import org.j3d.Scene;
import org.j3d.demo.App.Demo;

public class Demo4 extends Demo {

    private Scene scene;

    @Override
    public void init(Game game) throws Exception {
        scene = new Scene(null, false, game);

        MeshBuilder builder = new MeshBuilder();

        builder.addBox(0, 128, 0, 256, 256, 256, 1, 1, 1, 100, true);
        builder.addBox(-64, 128, 0, 32, 256, 32, 1, 1, 1, 100, false);
        builder.addBox(+64, 128, 0, 32, 256, 32, 1, 1, 1, 100, false);
        builder.addBox(0, 128, -64, 32, 256, 32, 1, 1, 1, 100, false);
        builder.addBox(0, 128, +64, 32, 256, 32, 1, 1, 1, 100, false);
        builder.calcNormals(false);
        builder.calcTextureCoordinates(0, 0, 0, 128);

        Node node = new Node();

        node.renderable = builder.build();
        node.lightMapEnabled = true;
        node.collidable = true;
        node.texture = game.assets().load(IO.file("assets/wall.png"));
        scene.root.add(node);

        Node child = new Node();

        child.renderable = game.assets().load(IO.file("assets/faerie.md2"));
        child.lightingEnabled = true;
        child.texture = game.assets().load(IO.file("assets/faerie.png"));
        child.ambientColor.set(0.2f, 0.2f, 0.6f, 1);
        child.diffuseColor.set(1, 1, 1, 1);
        child.position.set(0, 2, 0);
        child.rotate(0, -90);
        child.scale.set(0.75f, 0.75f, 0.75f);

        Player player = new Player();

        player.jumpAmount = 0;
        
        node = new Node();
        node.add(child);
        node.addComponent(game, scene, player);
        node.addComponent(game, scene, new Info());
        node.position.set(0, 60, 0);
        scene.root.add(node);

        node = new Node();
        node.isLight = true;
        node.position.set(0, 64, 0);
        node.lightRadius = 400;
        node.lightColor.set(1.5f, 1.5f, 1.5f, 1);
        scene.root.add(node);

        node = new Node();
        node.addComponent(game, scene, new FireLight());
        node.position.y = 32;
        scene.root.add(node);

        new LightMapper().light(IO.file("assets/demo4.png"), game, scene, false);
    }

    @Override
    public void nextFrame(Game game) throws Exception {
        scene.render(game);
    }
    
}
