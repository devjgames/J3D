package org.j3d.demo;

import org.j3d.Game;
import org.j3d.IO;
import org.j3d.LightMapper;
import org.j3d.Node;
import org.j3d.Scene;
import org.j3d.Serializer;
import org.j3d.demo.App.Demo;

public class Demo2 extends Demo {

    private Scene scene;

    @Override
    public void init(Game game) throws Exception {
        /*
        createScene(game);

        game.assets().clear();
        */

        scene = Serializer.deserialize(game, false, IO.file("scenes/scene1.xml"));
    }

    @Override
    public void nextFrame(Game game) throws Exception {
        scene.render(game);
    }
    

    public void createScene(Game game) throws Exception {

        scene = new Scene(null, false, game);

        Node node = game.assets().load(IO.file("assets/map3.obj"));

        for(Node child : node) {
            child.lightMapEnabled = true;
            child.collidable = true;
        }

        scene.root.add(node);

        node = new Node();
        node.isLight = true;
        node.position.set(-171.0f, 81.0f, 52.0f);
        node.lightColor.set(1.0f, 0.75f, 0.5f, 1.0f);
        node.lightRadius = 800;
        scene.root.add(node);

        node = new Node();
        node.isLight = true;
        node.position.set(-121.0f, 194.0f, -331.0f);
        node.lightColor.set(1.0f, 0.75f, 0.5f, 1.0f);
        node.lightRadius = 800;
        scene.root.add(node);

        node = new Node();
        node.isLight = true;
        node.position.set(37.0f, 53.0f, -106.0f);
        node.lightColor.set(0.5f, 0.75f, 1.0f, 1.0f);
        node.lightRadius = 300;
        scene.root.add(node);

        Node child = new Node();

        child.renderable = game.assets().load(IO.file("assets/hero.md2"));
        child.lightingEnabled = true;
        child.texture = game.assets().load(IO.file("assets/hero.png"));
        child.ambientColor.set(0.2f, 0.2f, 0.4f, 1);
        child.diffuseColor.set(1, 1, 1, 1);
        child.position.set(0, 2, 0);
        child.rotate(0, -90);
        child.scale.set(0.75f, 0.75f, 0.75f);
        
        node = new Node();
        node.add(child);
        node.addComponent(game, scene, new Player());
        node.addComponent(game, scene, new Info());
        node.position.set(0, 60, 0);
        scene.root.add(node);

        LightMapper mapper = new LightMapper();

        mapper.light(IO.file("scenes/scene1.png"), game, scene, true);

        Serializer.serialize(scene, IO.file("scenes/scene1.xml"));
    }
}
