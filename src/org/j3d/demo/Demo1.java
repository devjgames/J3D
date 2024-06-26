package org.j3d.demo;

import org.j3d.Game;
import org.j3d.IO;
import org.j3d.Mesh;
import org.j3d.Node;
import org.j3d.Scene;
import org.j3d.SceneVertex;
import org.j3d.demo.App.Demo;

public class Demo1 extends Demo {

    private Scene scene;

    @Override
    public void init(Game game) throws Exception {
        scene = new Scene(null, false, game);

        Mesh mesh = new Mesh(
            new SceneVertex[] {
                new SceneVertex(-50, 0, -50, 1, 0, 0, 0, 0, 1, 1.0f, 0, 1, 0, 1, 0),
                new SceneVertex(-50, 0, +50, 1, 0, 2, 0, 0, 1, 0.5f, 0, 1, 0, 1, 0),
                new SceneVertex(+50, 0, +50, 1, 2, 2, 0, 0, 1, 0.5f, 0, 1, 0, 1, 0),
                new SceneVertex(+50, 0, -50, 1, 2, 0, 0, 0, 1, 1.0f, 0, 1, 0, 1, 0),
                new SceneVertex(-50, 0, -50, 1, 0, 0, 0, 0, 1, 1.0f, 0, 1, 0, 1, 0),
                new SceneVertex(-50, 0, +50, 1, 0, 2, 0, 0, 1, 0.5f, 0, 1, 0, 1, 0),
                new SceneVertex(+50, 0, +50, 1, 2, 2, 0, 0, 1, 0.5f, 0, 1, 0, 1, 0),
                new SceneVertex(+50, 0, -50, 1, 2, 0, 0, 0, 1, 1.0f, 0, 1, 0, 1, 0)
            },
            new int[] {
                0, 1, 2, 2, 3, 0,
                7, 6, 5, 5, 4, 7
            },
            new int[][] {
                { 0, 1, 2, 3 },
                { 7, 6, 5, 4 }
            }
        );
        Node node = new Node();

        node.renderable = mesh;
        node.texture = game.assets().load(IO.file("assets/checker.png"));
        scene.root.add(node);

        node = new Node();
        node.addComponent(game, scene, new Info());
        node.addComponent(game, scene, new FireLight());
        node.position.y = 32;
        scene.root.add(node);
    }

    @Override
    public void nextFrame(Game game) throws Exception {
        scene.render(game);

        if(game.isButtonDown(0)) {
            scene.camera.rotate(-game.getDeltaX(), -game.getDeltaY());
        }
    }
    
}
