package org.j3d.scene.demo1;

import java.io.File;
import java.util.Vector;

import org.j3d.Font;
import org.j3d.Game;
import org.j3d.IO;
import org.j3d.MeshPart;
import org.j3d.Resource;
import org.j3d.UIManager;
import org.j3d.lm.DualTextureMaterial;
import org.j3d.scene.MeshRenderable;
import org.j3d.scene.Renderer;
import org.j3d.scene.Scene;
import org.j3d.scene.demo1.Tile.TileSize;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class App {

    private static Game game = null;
    private static Scene scene = null;
    private static UIManager manager;
    private static Vector<Tile> tiles = new Vector<>();
    private static Vector<Tile> tileFactories = new Vector<>();
    private static Vector<String> tileFactoryNames = new Vector<>();
    private static Vector<String> sceneList = new Vector<>();
    private static String[] modes = new String[] { "Zoom", "Rot", "Mov", "+", "-" };
    private static int mode = 1;
    private static boolean showScenes = false;
    private static File tileFile = null;
    private static Tile tileFactory = null;
    
    public static void main(String[] args) throws Exception {

        try {
            game = new Game(1000, 700, true);

            tileFactories.add(new Tile.Light(game, null, TileSize.X32, 0, 0, 0));
            tileFactories.add(new Tile.Floor(game, null, TileSize.X128, 0, 0, 0));
            tileFactories.add(new Tile.Corner1(game, null, TileSize.X128, 0, 0, 0));
            tileFactories.add(new Tile.Corner2(game, null, TileSize.X128, 0, 0, 0));
            tileFactories.add(new Tile.Corner3(game, null, TileSize.X128, 0, 0, 0));
            tileFactories.add(new Tile.Corner4(game, null, TileSize.X128, 0, 0, 0));
            tileFactories.add(new Tile.Side1(game, null, TileSize.X128, 0, 0, 0));
            tileFactories.add(new Tile.Side2(game, null, TileSize.X128, 0, 0, 0));
            tileFactories.add(new Tile.Side3(game, null, TileSize.X128, 0, 0, 0));
            tileFactories.add(new Tile.Side4(game, null, TileSize.X128, 0, 0, 0));
            tileFactories.add(new Tiles2.LightOrange(game, null, TileSize.X32, 0, 0, 0));
            tileFactories.add(new Tiles2.LightBlue(game, null, TileSize.X32, 0, 0, 0));
            tileFactories.add(new Tiles2.LedgeFlatL(game, null, TileSize.X64, 0, 0, 0));
            tileFactories.add(new Tiles2.LedgeFlatH(game, null, TileSize.X64, 0, 0, 1));
            tileFactories.add(new Tiles2.LedgeCornerL1(game, null, TileSize.X64, 0, 0, 0));
            tileFactories.add(new Tiles2.LedgeCornerH1(game, null, TileSize.X64, 0, 0, 1));
            tileFactories.add(new Tiles2.LedgeCornerL2(game, null, TileSize.X64, 0, 0, 0));
            tileFactories.add(new Tiles2.LedgeCornerH2(game, null, TileSize.X64, 0, 0, 1));
            tileFactories.add(new Tiles2.LedgeCornerL3(game, null, TileSize.X64, 0, 0, 0));
            tileFactories.add(new Tiles2.LedgeCornerH3(game, null, TileSize.X64, 0, 0, 1));
            tileFactories.add(new Tiles2.LedgeCornerL4(game, null, TileSize.X64, 0, 0, 0));
            tileFactories.add(new Tiles2.LedgeCornerH4(game, null, TileSize.X64, 0, 0, 1));
            tileFactories.add(new Tiles2.LedgeSideL1(game, null, TileSize.X64, 0, 0, 0));
            tileFactories.add(new Tiles2.LedgeSideH1(game, null, TileSize.X64, 0, 0, 1));
            tileFactories.add(new Tiles2.LedgeSideL2(game, null, TileSize.X64, 0, 0, 0));
            tileFactories.add(new Tiles2.LedgeSideH2(game, null, TileSize.X64, 0, 0, 1));
            tileFactories.add(new Tiles2.LedgeSideL3(game, null, TileSize.X64, 0, 0, 0));
            tileFactories.add(new Tiles2.LedgeSideH3(game, null, TileSize.X64, 0, 0, 1));
            tileFactories.add(new Tiles2.LedgeSideL4(game, null, TileSize.X64, 0, 0, 0));
            tileFactories.add(new Tiles2.LedgeSideH4(game, null, TileSize.X64, 0, 0, 1));
            for(Tile tile : tileFactories) {
                tileFactoryNames.add(tile.toString());
            }
            scene = load(IO.file("assets/tile-scenes/scene1.txt"), game, true);

            File[] files = IO.file("assets/tile-scenes").listFiles();

            if(files != null) {
                for(File sceneFile : files) {
                    if(sceneFile.isFile() && IO.extension(sceneFile).equals(".txt")) {
                        sceneList.add(IO.fileNameWithOutExtension(sceneFile));
                    }
                }
            }
            sceneList.sort((a, b) -> a.compareTo(b));

            manager = new UIManager(game, game.getResources().manage(new Font(IO.file("assets/pics/font.fnt"))));

            Renderer renderer = new Renderer();
            boolean spaceKeyDown = false;
            boolean sKeyDown = false;
            boolean sync = true;

            GLFW.glfwSwapInterval(1);

            game.resetTimer();

            while(game.run()) {
                boolean handled = true;

                game.beginRenderTarget();
                renderer.render(scene, game);
                if(scene.inDesign) {
                    pushInfo(game, scene, renderer);
                    handled = handleUI();
                }
                game.nextFrame();

                if(!handled) {
                    if(game.isButtonDown(0)) {
                        if(mode == 0) {
                            scene.zoom(game.getDeltaY());
                        } else if(mode == 1) {
                            scene.rotateAroundTarget(game);
                        } else if(mode == 2) {
                            scene.move(scene.target, game.getDeltaX(), game.getDeltaY(), null);
                        } 
                    }
                    scene.lines.cellSize = 0;
                    
                    if(tileFactory != null && (mode == 3 || mode == 4)) {
                        Vector3f origin = new Vector3f();
                        Vector3f direction = new Vector3f();
                        int h = game.getRenderTargetHeight();
                        int x = game.getMouseX();
                        int y = h - game.getMouseY() - 1;
                        int cs = tileFactory.getCellSize();

                        scene.unProject(x, y, 0, game, origin);
                        scene.unProject(x, y, 1, game, direction);
                        direction.sub(origin).normalize();

                        float t = direction.dot(0, 1, 0);

                        if(Math.abs(t) > 0.0000001) {
                            t = -origin.dot(0, 1, 0) / t;
                            origin.add(direction.mul(t));

                            int r = (int)Math.floor(origin.z / cs);
                            int c = (int)Math.floor(origin.x / cs);
                            Tile existing = null;

                            scene.lines.row = r;
                            scene.lines.col = c;
                            scene.lines.cellSize = cs;

                            if(game.isButtonDown(0)) {
                                for(Tile tile : tiles) {
                                    if(tile.row == r && tile.col == c && tile.getCellSize() == cs && tile.layer == tileFactory.layer) {
                                        existing = tile;
                                    }
                                }
                                if(existing != null) {
                                    tiles.remove(existing);
                                    existing.node.detachFromParent();
                                }
                                if(mode == 3) {
                                    Tile tile = tileFactory.newInstance(game, scene, r, c);

                                    scene.root.addChild(tile.node);
                                    tiles.add(tile);
                                }
                            }
                        }
                    }
                }

                if(game.isKeyDown(GLFW.GLFW_KEY_SPACE)) {
                    if(!spaceKeyDown) {
                        spaceKeyDown = true;
                        game.toggleFullscreen();
                    }
                } else {
                    spaceKeyDown = false;
                }
                if(game.isKeyDown(GLFW.GLFW_KEY_S)) {
                    if(!sKeyDown) {
                        sKeyDown = true;
                        sync = !sync;
                        if(sync) {
                            GLFW.glfwSwapInterval(1);
                        } else {
                            GLFW.glfwSwapInterval(0);
                        }
                    }
                } else {
                    sKeyDown = false;
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

    public static void pushInfo(Game game, Scene scene, Renderer renderer) {
        String info = "";

        info += "F=" + game.getFPS() + ", ";
        info += "R=" + Resource.getInstances() + ", ";
        info += "T=" + scene.root.countTriangles() + ", ";
        info += "B=" + renderer.getBinds() + ", ";
        info += "N=" + renderer.getNodes() + ", ";
        info += "L=" + renderer.getLights() + ", ";
        info += "O=" + renderer.getObjects() + ", ";
        info += "SPC=FS, ";
        info += "S=Sync";

        game.getSpritePipeline().begin(game.getRenderTargetWidth(), game.getRenderTargetHeight());
        game.getSpritePipeline().beginSprite(manager.getFont());
        game.getSpritePipeline().push(manager.getFont(), info, 5, 10, game.getRenderTargetHeight() - 10 - manager.getFont().getCharHeight(), 1, 1, 1, 1);
        game.getSpritePipeline().endSprite();
        game.getSpritePipeline().end();
    }

    private static Scene load(File tileFile, Game game, boolean inDesign) throws Exception {
        game.getAssets().clear();
        
        Scene scene = new Scene(game, inDesign);

        scene.eye.set(400, 400, 400);

        tiles = Tile.load(tileFile, game, scene, 1024, 1024, 1);

        App.tileFile = tileFile;

        return scene;
    }

    private static boolean handleUI() throws Exception {
        Object r = null;

        manager.begin();
        manager.moveTo(10, 10);
        for(int i = 0; i != modes.length; i++) {
            if(manager.label("mode-" + modes[i] + "-label", (i == 0) ? 0 : 5, modes[i], 0, mode == i)) {
                mode = i;
            }
        }
        manager.addRow(5);
        if(manager.label("load-label", 0, "Load", 0, showScenes)) {
            showScenes = !showScenes;
        }
        if(manager.label("save-label", 5, "Save", 0, false)) {
            try {
                Tile.save(tiles, tileFile);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
        if(manager.label("map-label", 5, "Map", 0, false)) {
            Tile.map(game, tiles, 1024, 1024, 1, tileFile, true);
        }
        if(manager.label("map-clear-label", 5, "Clear", 0, false)) {
            File file = IO.file(tileFile.getParentFile(), IO.fileNameWithOutExtension(tileFile) + ".png");

            if(file.exists()) {
                file.delete();
            }
            for(Tile tile : tiles) {
                if(tile.node.renderable instanceof MeshRenderable) {
                    MeshRenderable mesh = (MeshRenderable)tile.node.renderable;

                    for(MeshPart part : mesh.mesh) {
                        if(part.material instanceof DualTextureMaterial) {
                            ((DualTextureMaterial)part.material).texture2 = null;
                        }
                    }
                }
            }
        }
        manager.addRow(5);
        if(showScenes) {
            if((r = manager.list("scene-list", 0, sceneList, 10, 8, -2)) != null) {
                scene = load(IO.file(IO.file("assets/tile-scenes"), sceneList.get((Integer)r) + ".txt"), game, true);
                showScenes = false;
            }
        }
        if((r = manager.list("tile-factory-list", (showScenes) ? 5 : 0, tileFactoryNames, 15, 8, -2)) != null) {
            tileFactory = tileFactories.get((Integer)r);
        }
        return manager.end();
    }
}
