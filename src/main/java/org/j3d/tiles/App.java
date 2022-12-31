package org.j3d.tiles;

import java.io.File;
import java.util.Vector;

import org.j3d.Font;
import org.j3d.Game;
import org.j3d.IO;
import org.j3d.LightPipeline;
import org.j3d.Resource;
import org.j3d.UIManager;
import org.j3d.Utils;
import org.j3d.LightPipeline.Light;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class App {
    
    private Game game = null;
    private Font font;
    private UIManager manager;
    private boolean sync = true;
    private Vector<Tile> tileFactories = new Vector<>();
    private Vector<String> tileFactoryNames = new Vector<>();
    private Vector<String> tileFiles = new Vector<>();
    private Tiles tiles = null;
    private File tileFile = null;
    private Light[] lights = null;
    private String[] modes = new String[] {
        "ZM", 
        "RT",
        "PN",
        "+",
        "-"
    };
    private int mode = 0;
    private int factoryI = 0;
    private boolean showTileList = false;
    private boolean showFactoryList = false;
    private boolean lit = true;
    private Vector3f target = new Vector3f();
    private Vector3f offset = new Vector3f();
    private Vector3f up = new Vector3f();
    private Vector3f eye = new Vector3f();
    private Matrix4f projection = new Matrix4f();
    private Matrix4f view = new Matrix4f();
    private boolean down0 = false;
    private boolean down1 = false;
    private int row = 0;
    private int col = 0;
    private int layer = 0;
    private Tile marker = null;
    private Tile lastTile = null;
    private LightPipeline ball = null;
    private Vector3f f = new Vector3f();
    private Vector3f u = new Vector3f();
    private Vector3f r = new Vector3f();
    private final Player player;
    private boolean playing = false;

    public App(Player player) {
        this.player = player;
    }

    public Game getGame() {
        return game;
    }

    public Font getFont() {
        return font;
    }

    public UIManager getManager() {
        return manager;
    }

    public void run(Tile ... factories) throws Exception {
        try {
            game = new Game(1000, 800, true);
            font = game.getResources().manage(new Font(IO.file("assets/pics/font.fnt")));
            manager = new UIManager(game, font);
            GLFW.glfwSwapInterval(1);
            init(factories);
            game.resetTimer();
            while(game.run()) {
                if(playing) {
                    if(!player.run(this)) {
                        load();
                    }
                } else {
                    edit();
                }
            }
        } finally {
            if(game != null) {
                game.destroy();
            }
        }
    }

    private void init(Tile[] factories) throws Exception {
        File[] files = IO.file("assets/tiles").listFiles();

        for(File file : files) {
            tileFiles.add(IO.fileNameWithOutExtension(file));
        }
        tileFiles.sort((a, b) -> a.compareTo(b));

        for(Tile factory : factories) {
            tileFactoryNames.add(factory.name);
            tileFactories.add(factory);
        }
        lights = new Light[] {
            new Light(),
            new Light()
        };
        lights[0].vector.set(-1, -1, -1);
        lights[0].color.set(1, 0.75f, 0.5f, 1);
        lights[1].vector.set(+1, -1, +1);
        lights[1].color.set(0.5f, 0.75f, 1, 1);
    }

    private void edit() throws Exception {
        int h = game.getRenderTargetHeight();

        game.beginRenderTarget();
        Utils.clear(0.15f, 0.15f, 0.15f, 1);
        if(tileFile != null) {
            projection.identity().perspective(Utils.toRadians(60), game.getAspectRatio(), 1, 25000);
            target.add(offset, eye);
            view.identity().lookAt(eye, target, up);
            tiles.render(projection, view);
            if(mode == 3) {
                marker.getSelector().render(projection, view);
            }
            ball.lights.clear();;
            ball.ambientColor.set(1, 0, 1, 1);
            ball.render(projection, view);
        }
        boolean handled = handleUI();
        game.getSpritePipeline().begin(game.getRenderTargetWidth(), game.getRenderTargetHeight());
        game.getSpritePipeline().beginSprite(font);
        game.getSpritePipeline().push(
            font, 
            "FPS=" + game.getFPS() + ", RES=" + Resource.getInstances(), 
            5, 10, h - font.getCharHeight() - 10, 
            1, 1, 1, 1
            );
        game.getSpritePipeline().endSprite();
        game.getSpritePipeline().end();
        game.nextFrame();
        if(!handled) {
            handleMouse();
        }
    }

    private boolean handleUI() throws Exception {
        manager.begin();
        manager.moveTo(10, 10);
        if(manager.label("App-full-screen-label", 0, "FS", 0, game.isFullscreen())) {
            game.toggleFullscreen();
        }
        if(manager.label("App-sync-label", 5, "Sync", 0, sync)) {
            sync = !sync;
            if(sync) {
                GLFW.glfwSwapInterval(1);
            } else {
                GLFW.glfwSwapInterval(0);
            }
        }
        if(manager.label("App-load-label", 5, "LD", 0, showTileList)) {
            showTileList = !showTileList;
            showFactoryList =  false;
        }
        if(tileFile != null) {
            if(manager.label("App-save-label", 5, "SV", 0, false)) {
                tiles.save(tileFile, offset, target, up);
            }
            if(manager.label("App-play-label", 5, "Play", 0, false)) {
                game.getAssets().clear();
                playing = true;
                if(lit) {
                    player.init(this, tileFile, lights);
                } else {
                    player.init(this, tileFile);
                }
            }
            if(manager.label("App-tiles-label", 5, "Tiles", 0, showFactoryList)) {
                showFactoryList = !showFactoryList;
                showTileList = false;
            }
            if(manager.label("App-lighting-label", 5, "Lit", 0, lit)) {
                lit = !lit;
                if(lit) {
                    tiles.setLighting(lights);
                } else {
                    tiles.setLighting();
                }
            }
            for(int i = 0; i != modes.length; i++) {
                if(manager.label("App-mode-" + modes[i] + "-label", 5, modes[i], 0, i == mode)) {
                    mode = i;
                }
            }
            for(int i = 0; i != 3; i++) {
                if(manager.label("App-layer" + i + "-label", 5, "L" + (i + 1), 0, layer == i)) {
                    layer = i;
                }
            }
        }
        if(showTileList || showFactoryList) {
            Object r;

            manager.addRow(5);
            if(showTileList) {
                if((r = manager.list("App-file-list", 0, tileFiles, 20, 8, -2)) != null) {
                    tileFile = IO.file(IO.file("assets/tiles"), tileFiles.get((Integer)r) + ".txt");
                    load();
                }
            } else {
                if((r = manager.list("App-tile-list", 0, tileFactoryNames, 20, 8, -2)) != null) {
                    factoryI = (Integer)r;
                    marker = tileFactories.get(factoryI).newInstance(this);
                }
            }
        }
        return manager.end();
    }

    private void load() throws Exception {
        playing = false;
        target.zero();
        offset.set(400, 400, 400);
        up.set(0, 1, 0);
        game.getAssets().clear();
        if(lit) {
            tiles = new Tiles(this, tileFile, offset, target, up, lights);
        } else {
            tiles = new Tiles(this, tileFile, offset, target, up);
        }
        marker = tileFactories.get(factoryI).newInstance(this);
        lastTile = null;
        showTileList = false;
        showFactoryList = false;
        ball = game.getAssets().load(IO.file("assets/meshes/ball.obj"));
        ball.setTransform(tiles.playerCol * 64, tiles.playerLayer * 64 + 64 + 16, tiles.playerRow * 16, 0, 0, 0, 1.5f);
    }

    private void handleMouse() throws Exception {
        if(tileFile == null) {
            return;
        }
        unProject();
        if(game.isButtonDown(0)) {
            if(mode == 0) {
                float length = offset.length();

                offset = offset.normalize().mul(length + game.getDeltaY());
            } else if(mode == 1) {
                Utils.rotateOffsetAndUp(offset, up, game);
            } else if(mode == 2) {
                f.set(offset).mul(-1, 0, -1);
                if(f.length() > 0.0000001) {
                    f.normalize().cross(u.set(0, 1, 0), r).normalize();
                    f.mul(game.getDeltaY()).add(r.mul(game.getDeltaX()));
                    target.add(f);
                }
            } else if(!down0) {
                if(mode == 3) {
                    Tile tile = marker.newInstance(this);

                    if(lit) {
                        tile.setLighting(lights);
                    } else {
                        tile.setLighting();
                    }
                    tile.setLoctation(row, col, layer, tile.getRotationDegrees());
                    tiles.addTile(tile);
                } else if(mode == 4) {
                    Tile tile = tiles.findTile(row, col, layer);
                    
                    if(tile != null) {
                        tiles.removeTile(tile);
                    }
                }
            }
            down0 = true;
        } else {
            down0 = false;
        }
        if(game.isButtonDown(1)) {
            if(!down1) {
                marker.rotate();
            }
            down1 =  true;
        } else {
            down1 = false;
        }
        if(mode == 3 || mode == 4) {
            if(lastTile != null) {
                lastTile.visible = true;
            }
            lastTile = tiles.findTile(row, col, layer);
            if(lastTile != null) {
                lastTile.visible = false;
            }
            if(mode == 3) {
                if(lit) {
                    marker.setLighting(lights);
                } else {
                    marker.setLighting();
                }
                marker.setLoctation(row, col, layer, marker.getRotationDegrees());
            }
        }
    }

    private void unProject() {
        int w = game.getRenderTargetWidth();
        int h = game.getRenderTargetHeight();
        int x = game.getMouseX();
        int y = h - game.getMouseY() - 1;
        Vector3f o = new Vector3f();
        Vector3f d = new Vector3f();

        Utils.unProject(x, y, 0, 0, 0, w, h, projection, view, o);
        Utils.unProject(x, y, 1, 0, 0, w, h, projection, view, d);

        d.sub(o, d).normalize();

        float t = d.dot(0, 1, 0);
        float s = -layer * 64;

        row = 0;
        col = 0;

        if(Math.abs(t) > 0.0000001) {
            t = (-s - o.dot(0, 1, 0)) / t;

            d.mul(t);
            o.add(d, d);

            row = (int)Math.floor((d.z + 32) / 64);
            col = (int)Math.floor((d.x + 32) / 64);
        }
    }

    public static void main(String[] args) throws Exception {
        new App(
            new PlayerDemo()
        ).run(
            new Tile("ledge1", "ledge.obj", "ledge1.png"),
            new Tile("ledge-side1", "ledge-side.obj", "ledge1.png"),
            new Tile("block1", "block.obj", "stone1.png"),
            new Tile("ramp1", "ramp.obj", "stone1.png"),
            new Tile("tree1", "tree.obj", "tree1.png")
        );
    }
}
