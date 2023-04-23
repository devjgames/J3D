package org.j3d.demo;

import org.j3d.Game;
import org.j3d.IO;
import org.j3d.Node;
import org.j3d.NodeComponent;
import org.j3d.Scene;
import org.j3d.Serializer;
import org.j3d.Vec4;

public class App extends NodeComponent {

    public final Vec4 components = new Vec4(1, 2, 3, 4);

    public static void main(String[] args) throws Exception {
        Game game = new Game(200, 100, 4, null);

        Scene scene = new Scene(false, game);
        Node node = new Node();

        node.name = "<>==='\"\n\t888&";
        node.addComponent(game, scene, new App());
        node.renderable = game.assets().load(IO.file("assets/md2/hero.md2"));
        node.renderable = node.renderable.newInstance();
        scene.root.add(node);

        node.add(game.assets().load(IO.file("assets/maps/map1.obj")));

        Serializer.serialize(scene, IO.file("scenes/test.xml"));

        game.assets().clear();

        scene = Serializer.deserialize(game, false, IO.file("scenes/test.xml"));

        System.out.println(scene.root.childAt(0).name);
        System.out.println(scene.root.find(App.class, true));

        Serializer.serialize(scene, IO.file("scenes/test2.xml"));
    }
}