package org.j3d.scene.demo1;

import java.io.File;
import java.util.Vector;

import org.j3d.BoundingBox;
import org.j3d.Font;
import org.j3d.Game;
import org.j3d.IO;
import org.j3d.Parser;
import org.j3d.Resource;
import org.j3d.Triangle;
import org.j3d.UIManager;
import org.j3d.scene.Lines;
import org.j3d.scene.Node;
import org.j3d.scene.Renderer;
import org.j3d.scene.Scene;
import org.j3d.scene.Serializer;
import org.j3d.scene.demo1.factories.Bridge1;
import org.j3d.scene.demo1.factories.Info1;
import org.j3d.scene.demo1.factories.Light1;
import org.j3d.scene.demo1.factories.Player;
import org.j3d.scene.demo1.factories.Stone1;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class App {

    private static Game game = null;
    private static Scene scene = null;
    private static UIManager manager;
    private static Vector<NodeFactory> factories = new Vector<>();
    private static Vector<String> factoryNames = new Vector<>();
    private static Vector<String> sceneList = new Vector<>();
    private static String[] modes = new String[] { "ZM", "PXZ", "PY", "RT", "SEL", "MXZ", "MY", "RX", "RY", "RZ", "S" };
    private static int mode = 2;
    private static boolean showScenes = false;
    private static File sceneFile = null;
    private static NodeFactory factory = null;
    private static Matrix4f matrix = new Matrix4f();
    private static Triangle triangle = new Triangle();
    private static BoundingBox bounds = new BoundingBox();
    private static int snap = 1;
    private static boolean resetSnap = true;
    private static boolean resetComponents = false;
    private static boolean sync = true;
    private static boolean resetFactories = false;

    public static void main(String[] args) throws Exception {

        try {
            game = new Game(1000, 700, true);
            game.getAssets().registerAssetLoader(".obj", new Serializer());

            load(IO.file("assets/scenes/scene1.xml"), game, true);

            File[] files = IO.file("assets/scenes").listFiles();

            if(files != null) {
                for(File sceneFile : files) {
                    if(sceneFile.isFile() && IO.extension(sceneFile).equals(".xml")) {
                        sceneList.add(IO.fileNameWithOutExtension(sceneFile));
                    }
                }
            }
            sceneList.sort((a, b) -> a.compareTo(b));

            manager = new UIManager(game, game.getResources().manage(new Font(IO.file("assets/pics/font.fnt"))));

            Renderer renderer = new Renderer();
            boolean down = false;

            GLFW.glfwSwapInterval(1);

            game.resetTimer();

            while(game.run()) {
                boolean handled = true;

                game.beginRenderTarget();
                renderer.render(scene, game);
                if(scene.inDesign) {
                    pushInfo(game, scene, renderer, "");
                    handled = handleUI();
                }
                game.nextFrame();

                if(!handled && scene.inDesign) {
                    if(game.isButtonDown(0)) {
                        if(mode == 0) {
                            scene.zoom(game.getDeltaY());
                        } else if(mode == 1) {
                            scene.move(scene.target, game.getDeltaX(), game.getDeltaY(), null);
                        } else if(mode == 2) {
                            scene.move(scene.target, game.getDeltaY(), null);
                        } else if(mode == 3) {
                            scene.rotateAroundTarget(game);
                        } else if(mode == 4) {
                            if(!down) {
                                int h = game.getRenderTargetHeight();
                                int x = game.getMouseX();
                                int y = h - game.getMouseY() - 1;
                                Vector3f origin = new Vector3f();
                                Vector3f direction = new Vector3f();
                                float[] time = new float[] { Float.MAX_VALUE };

                                scene.unProject(x, y, 0, game, origin);
                                scene.unProject(x, y, 1, game, direction);
                                direction.sub(origin).normalize();

                                scene.selection = null;
                                select(scene.root, origin, direction, time);
                                if(scene.selection != null) {
                                    resetComponents = true;
                                }
                            }
                        } else if(scene.selection != null) {
                            matrix.identity().set(scene.selection.getParent().model).invert();

                            if(mode == 5) {
                                scene.move(scene.selection.position, game.getDeltaX(), game.getDeltaY(), matrix);
                            } else if(mode == 6) {
                                scene.move(scene.selection.position, -game.getDeltaY(), matrix);
                            } else if(mode == 7) {
                                scene.selection.rotation.rotate(game.getDeltaX() * 0.025f, 1, 0, 0);
                            } else if(mode == 8) {
                                scene.selection.rotation.rotate(game.getDeltaX() * 0.025f, 0, 1, 0);
                            } else if(mode == 9) {
                                scene.selection.rotation.rotate(game.getDeltaX() * 0.025f, 0, 0, 1);
                            } else if(mode == 10) {
                                float d = game.getDeltaY();

                                if(d < 0) {
                                    scene.selection.scale.mul(0.99f);
                                } else if(d > 0) {
                                    scene.selection.scale.mul(1.01f);
                                }
                            }
                        } 
                        down = true;
                    } else {
                        if(down && scene.selection != null && (mode == 5 || mode == 6) && snap > 0) {
                            Vector3f p = scene.selection.position;

                            p.x = Math.round(p.x / snap) * snap;
                            p.y = Math.round(p.y / snap) * snap;
                            p.z = Math.round(p.z / snap) * snap;
                        }
                        down = false;
                    }
                }

                if(!scene.inDesign) {
                    if(game.isKeyDown(GLFW.GLFW_KEY_ESCAPE)) {
                        load(sceneFile, game, true);
                    }
                }
            }
        } finally {
            if(game != null) {
                game.destroy();
            }
        }
    }

    public static Scene getScene() {
        return scene;
    }

    public static void pushInfo(Game game, Scene scene, Renderer renderer, String extra) {
        String info = "";

        info += "F=" + game.getFPS() + ", ";
        info += "R=" + Resource.getInstances() + ", ";
        info += "T=" + scene.root.countTriangles() + ", ";
        info += "B=" + renderer.getBinds() + ", ";
        info += "N=" + renderer.getNodes() + ", ";
        info += "L=" + renderer.getLights() + ", ";
        info += "O=" + renderer.getObjects();
        info += " " + extra;

        game.getSpritePipeline().begin(game.getRenderTargetWidth(), game.getRenderTargetHeight());
        game.getSpritePipeline().beginSprite(manager.getFont());
        game.getSpritePipeline().push(manager.getFont(), info, 5, 10, game.getRenderTargetHeight() - 10 - manager.getFont().getCharHeight(), 1, 1, 1, 1);
        game.getSpritePipeline().endSprite();
        game.getSpritePipeline().end();
    }

    private static void load(File file, Game game, boolean inDesign) throws Exception {
        game.getAssets().clear();

        factory = null;
        factories.clear();
        factories.add(new Player());
        factories.add(new Stone1());
        factories.add(new Bridge1());
        factories.add(new Light1());
        factories.add(new Info1());

        factoryNames.clear();
        for(NodeFactory iFactory : factories) {
            factoryNames.add(iFactory.toString());
        }

        scene = Serializer.deserialize(file, game, inDesign);
        sceneFile = file;

        resetFactories = true;
    }

    private static boolean handleUI() throws Exception {
        Object r = null;

        manager.begin();
        manager.moveTo(10, 10);
        if(manager.label("fs-label", 0, "fs", 0, game.isFullscreen())) {
            game.toggleFullscreen();
        }
        if(manager.label("sync-label", 5, "I", 0, sync)) {
            sync = !sync;
            if(sync) {
                GLFW.glfwSwapInterval(1);
            } else {
                GLFW.glfwSwapInterval(0);
            }
        }
        for(int i = 0; i != modes.length; i++) {
            if(manager.label("mode-" + modes[i] + "-label", 5, modes[i], 0, mode == i)) {
                mode = i;
            }
        }
        manager.addRow(5);
        if(manager.label("load-label", 0, "L", 0, showScenes)) {
            showScenes = !showScenes;
        }
        if(manager.label("save-label", 5, "S", 0, false)) {
            try {
                Serializer.serialize(scene, sceneFile);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
        if(manager.label("play-label", 5, "P", 0, false)) {
            load(sceneFile, game, false);
            return manager.end();
        }
        if(scene.selection != null) {
            if(manager.label("position-to-target-label", 5, "P2T", 0, false)) {
                scene.selection.position.set(scene.target);
            }
            if(manager.label("target-to-position-label", 5, "T2P", 0, false)) {
                scene.target.set(scene.selection.position);
            }
            if(manager.label("target-to-zero-label", 5, "T2Z", 0, false)) {
                Vector3f offset = new Vector3f();

                scene.eye.sub(scene.target, offset);
                scene.target.zero();
                scene.target.add(offset, scene.eye);
            }
            if(manager.label("zero-rot-label", 5, "ZR", 0, false)) {
                scene.selection.rotation.identity();
            }
            if(manager.label("rot-x-45-label", 5, "RX45", 0, false)) {
                scene.selection.rotation.rotate((float)Math.PI / 4, 1, 0, 0);
            }
            if(manager.label("rot-y-45-label", 5, "RY45", 0, false)) {
                scene.selection.rotation.rotate((float)Math.PI / 4, 0, 1, 0);
            }
            if(manager.label("rot-z-45-label", 5, "RZ45", 0, false)) {
                scene.selection.rotation.rotate((float)Math.PI / 4, 0, 0, 1);
            }
            if(manager.label("unit-scale-label", 5, "US", 0, false)) {
                scene.selection.scale.set(1, 1, 1);
            }
        }
        manager.addRow(5);
        if(showScenes) {
            if((r = manager.list("scene-list", 0, sceneList, 10, 8, -2)) != null) {
                load(IO.file(IO.file("assets/scenes"), sceneList.get((Integer)r) + ".xml"), game, true);
                showScenes = false;
            }
        }
        int sel = -2;
        if(resetFactories) {
            sel = -1;
            resetFactories = false;
        }
        if((r = manager.list("node-factory-list", (showScenes) ? 5 : 0, factoryNames, 15, 8, sel)) != null) {
            factory = factories.get((Integer)r);
        }
        if(factory != null) {
            if(manager.label("add-node-label", 5, "+", 0, false)) {
                Node node = factory.newInstance(game, scene);

                if(scene.selection != null) {
                    scene.selection.addChild(node);
                } else {
                    scene.root.addChild(node);
                }
                scene.selection = node;
                resetComponents = true;
            }
        }
        if(scene.selection != null) {
            if(manager.label("del-node-label", 5, "-", 0, false)) {
                scene.selection.detachFromParent();
                scene.selection = null;
            }
            handleSnap();
            if(scene.selection != null) {
                manager.moveTo(game.getRenderTargetWidth() - 250, 10);
                for(int i = 0; i != scene.selection.getComponentCount(); i++) {
                    scene.selection.componentAt(i).handleUI(manager, resetComponents);
                }
            }
            resetComponents = false;
        } else {
            handleSnap();
        }
        return manager.end();
    }

    private static void handleSnap() {
        Object r;

        manager.addRow(5);
        if((r = manager.textField("snap-field", 0, "Snap", snap + "", resetSnap, 5)) != null) {
            snap = Parser.parse(((String)r).split("\\s+"), 0, snap);
            snap = Math.max(1, snap);
        }
        resetSnap = false;
    }

    private static void select(Node node, Vector3f origin, Vector3f direction, float[] time) {
        if(node.getTriangleCount() != 0) {
            for(int i = 0; i != node.getTriangleCount(); i++) {
                node.triangleAt(i, triangle);
                if(direction.dot(triangle.n) < 0) {
                    if(triangle.intersects(origin, direction, 0, time)) {
                        scene.selection = node;
                    }
                }
            }
        } else if(node != scene.root && !(node.renderable instanceof Lines)) {
            bounds.min.set(node.absolutePosition);
            bounds.max.set(node.absolutePosition);
            bounds.min.sub(8, 8, 8);
            bounds.max.add(8, 8, 8);

            if(bounds.intersects(origin, direction, time)) {
                scene.selection = node;
            }
        }
        for(Node child : node) {
            select(child, origin, direction, time);
        }
    }
}
