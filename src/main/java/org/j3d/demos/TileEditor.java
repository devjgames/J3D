package org.j3d.demos;

import java.io.File;
import java.util.Arrays;
import java.util.Vector;

import org.j3d.BoundingBox;
import org.j3d.Game;
import org.j3d.IO;
import org.j3d.LightPipeline;
import org.j3d.UIManager;
import org.j3d.Utils;
import org.j3d.LightPipeline.Light;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class TileEditor extends Demo {

    private Vector3f offset = new Vector3f(0, 200, 100);
    private Vector3f up = new Vector3f(0, 1, 0);
    private Vector3f target = new Vector3f();
    private Vector3f eye = new Vector3f();
    private Tiles tiles = null;
    private Vector<String> tileFactoryNames = new Vector<>();
    private Vector<String> tileSceneNames = new Vector<>();
    private int factorySelection = 0;
    private int iFactory = 0;
    private int sceneSelection = -2;
    private int iLight = -1;
    private String[] modes = new String[] {
        "PAN",
        "ZOOM",
        "+",
        "-",
        "+L",
        "-L",
        "SL"
    };
    private int mode = 0;
    private boolean showTiles = false;
    private boolean showScenes = false;
    private boolean down = false;
    private boolean down2 =  false;
    private boolean resetLight = false;
    private int rotation = 0;
    private Vector3f origin = new Vector3f();
    private Vector3f direction = new Vector3f();
    private float[] time = new float[1];
    private BoundingBox bounds = new BoundingBox();
    private LightPipeline cube = null;

    @Override
    public void init(App app) throws Exception {
        Game game = app.getGame();

        offset.set(0, 200, 100);
        target.zero();
        tiles = null;

        tileFactoryNames.clear();
        for(int i = 0; i != Tiles.getTileFactoryCount(); i++) {
            tileFactoryNames.add(Tiles.getTileFactory(i).name);
        }

        File[] files = IO.file("assets/tiles").listFiles();

        Arrays.sort(files);
        tileSceneNames.clear();
        for(File file : files) {
            if(IO.extension(file).equals(".txt")) {
                tileSceneNames.add(IO.fileNameWithOutExtension(file));
            }
        }
        factorySelection = 0;
        iFactory = 0;
        sceneSelection = -2;
        iLight = -1;
        mode = 0;
        showTiles = false;
        showScenes = false;
        down = false;
        down2 = false;
        resetLight = false;

        cube = game.getAssets().load(IO.file("assets/meshes/cube.obj"));
        cube.zeroCenter();
        cube.ambientColor.set(1, 1, 1, 1);
        cube.texture = game.getAssets().load(Tiles.getTextureFile());
    }

    @Override
    public boolean run(App app) throws Exception {
        Game game = app.getGame();
        UIManager manager = app.getManager();
        Object r;

        projection.identity().perspective(Utils.toRadians(60), game.getAspectRatio(), 1, 10000);
        target.add(offset, eye);
        view.identity().lookAt(eye, target, up);

        Utils.clear(0, 0, 0, 1);
        if(tiles != null) {
            if(mode == 2 || mode == 3) {
                Tile tile = Tiles.getTileFactory(iFactory);
                int w = game.getRenderTargetWidth();
                int h = game.getRenderTargetHeight();
                int x = game.getMouseX();
                int y = h - game.getMouseY() - 1;

                Utils.unProject(x, y, 0, 0, 0, w, h, projection, view, origin);
                Utils.unProject(x, y, 1, 0, 0, w, h, projection, view, direction);

                direction.sub(origin).normalize();
                time[0] = Float.MAX_VALUE;

                float t = -origin.dot(0, 1, 0) / direction.dot(0, 1, 0);
                float px = origin.x + t * direction.x;
                float pz = origin.z + t * direction.z;
                int row = Tiles.calcIndex(pz);
                int col = Tiles.calcIndex(px);
                Tile eTile = tiles.find(row, col, tile.getLayer());

                if(eTile != null) {
                    eTile.visible = false;
                }
                tile.setTransform(game, row, col, rotation);

                if(mode == 3) {
                    tile = null;
                }

                tiles.render(projection, view, tile);

                if(eTile != null) {
                    eTile.visible = true;
                }
            } else {
                tiles.render(projection, view, null);
            }
            cube.model
                .identity()
                .translate(tiles.playerCol * Tile.SIZE, Tile.SIZE / 2, tiles.playerRow * Tile.SIZE)
                .scale(16);
            cube.render(projection, view);
        }
        manager.begin();
        manager.moveTo(10, 10);
        if(manager.label("TileEditor-load-label", 0, "Load", 0, showScenes)) {
            showScenes = !showScenes;
            showTiles = false;
            iLight = -1;
            sceneSelection = -1;
        }
        if(tiles != null) {
            if(manager.label("TileEditor-save-label", 5, "Save", 0, false)) {
                tiles.save();
            }
            if(manager.label("TileEditor-tiles-label", 5, "Tiles", 0, showTiles)) {
                showTiles = !showTiles;
                showScenes = false;
                iLight = -1;
            }
            for(int i = 0; i != modes.length; i++) {
                if(manager.label("TileEditor-mode-" + i + "-label", 5, modes[i], 0, i ==  mode)) {
                    mode = i;
                }
            }
            if(showTiles) {
                manager.addRow(5);
                if((r = manager.list("TileEditor-tiles-list", 0, tileFactoryNames, 20, 8, factorySelection)) != null) {
                    iFactory = (Integer)r;
                }
                factorySelection = -2;
            } else if(iLight != -1) {
                Light light = tiles.lights.get(iLight);
                float value = Math.max(light.color.x, Math.max(light.color.y, light.color.z));
                
                manager.addRow(5);
                if((r = manager.slider("TileEditor-light-intensity-slider", 0, "Intensity", (value - 1) / 3, 10, resetLight)) != null) {
                    value = (Float)r * 3 + 1;
                    light.color.set(1, 0.5f, 0.25f, 1).mul(value).w = 1;
                }
                resetLight = false;
            }
        }
        if(showScenes) {
            manager.addRow(5);
            if((r = manager.list("TileEditor-tiles-list", 0, tileSceneNames, 20, 8, sceneSelection)) != null) {
                tiles = new Tiles(game, IO.file(IO.file("assets/tiles"), tileSceneNames.get((Integer)r) + ".txt"));
                showScenes = false;
            }
            sceneSelection = -2;
        }
        boolean handled = manager.end();
        game.getSpritePipeline().begin(game.getRenderTargetWidth(), game.getRenderTargetHeight());
        pushInfo(app, null);
        game.getSpritePipeline().end();
        game.nextFrame();
        
        if(!handled) {
            if(game.isButtonDown(0)) {
                if(mode == 0) {
                    target.add(game.getDeltaX(), 0, -game.getDeltaY());
                } else if(mode == 1) {
                    float length = offset.length();

                    offset.normalize().mul(length + game.getDeltaY());
                } else if(mode >= 2) {
                    if(!down) {
                        int w = game.getRenderTargetWidth();
                        int h = game.getRenderTargetHeight();
                        int x = game.getMouseX();
                        int y = h - game.getMouseY() - 1;

                        Utils.unProject(x, y, 0, 0, 0, w, h, projection, view, origin);
                        Utils.unProject(x, y, 1, 0, 0, w, h, projection, view, direction);

                        direction.sub(origin).normalize();
                        time[0] = Float.MAX_VALUE;

                        float t = -origin.dot(0, 1, 0) / direction.dot(0, 1, 0);
                        float px = origin.x + t * direction.x;
                        float pz = origin.z + t * direction.z;
                        int row = Tiles.calcIndex(pz);
                        int col = Tiles.calcIndex(px);

                        showScenes = false;
                        showTiles = false;
                        iLight = -1;

                        if(mode == 2) {
                            Tile tile = Tiles.getTileFactory(iFactory).newInstance();

                            tile.setTransform(game, row, col, rotation);
                            tiles.add(tile);
                        } else if(mode == 3) {
                            Tile tile = tiles.find(row, col, 0);

                            if(tile != null) {
                                tiles.remove(tile);
                            }
                            tile = tiles.find(row, col, 1);
                            if(tile != null) {
                                tiles.remove(tile);
                            }
                        } else if(mode == 4) {
                            Light light = new Light();

                            light.directional = false;
                            light.vector.set(px, 50, pz);
                            light.color.set(1, 0.5f, 0.25f, 1);
                            light.radius = 75;
                            
                            iLight = tiles.lights.size();
                            tiles.lights.add(light);

                            resetLight = true;
                        } else if(mode >= 5) {
                            int i = 0;
                            for(Light light : tiles.lights) {
                                bounds.min.set(light.vector).sub(50, 50, 50);
                                bounds.max.set(light.vector).add(50, 50, 50);

                                if(bounds.intersects(origin, direction, time)) {
                                    iLight = i;
                                }
                                i++;
                            }
                            if(iLight != -1) {
                                if(mode == 5) {
                                    tiles.lights.remove(iLight);
                                    iLight = -1;
                                } else {
                                    resetLight = true;
                                }
                            }
                        }
                    }
                }
                down = true;
            } else {
                down = false;
            }
            if(game.isButtonDown(1)) {
                if(!down2) {
                    rotation = (rotation + 90) % 360;
                }
                down2 = true;
            } else {
                down2 = false;
            }
        }
        return !game.isKeyDown(GLFW.GLFW_KEY_ESCAPE);
    }
    
}
