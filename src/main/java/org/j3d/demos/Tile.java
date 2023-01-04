package org.j3d.demos;

import java.io.File;

import org.j3d.Game;
import org.j3d.IO;
import org.j3d.Log;
import org.j3d.MeshTriangleSelector;
import org.j3d.Utils;

public class Tile {

    public static final int SIZE = 32;
    
    public final String name;
    public final File meshFile;
    public boolean visible = true;

    private int row = 0;
    private int col = 0;
    private int layer = 0;
    private MeshTriangleSelector selector = null;
    private int rotation = 0;

    public Tile(String name, int layer) {
        File directory = IO.file("assets/meshes");
        
        this.name = name;
        this.meshFile = IO.file(directory, name + ".obj");

        this.layer = layer;
    }

    public String key() {
        return keyFor(row, col, layer);
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getLayer() {
        return layer;
    }

    public int getRotation() {
        return rotation;
    }

    public MeshTriangleSelector getSelector(Game game) throws Exception {
        if(selector == null) {
            Log.log(5, "creating selector for tile - " + this);
            selector = new MeshTriangleSelector(game.getAssets().load(meshFile));
            selector.mesh.zeroCenter();
            selector.mesh.texture = game.getAssets().load(Tiles.getTextureFile());

            File file = Tiles.getDecalFile();

            if(file.exists()) {
                selector.mesh.decal = game.getAssets().load(file);
            }
            Utils.allocate();
        }
        return selector;
    }

    public void reload() {
        selector = null;
    }

    public void setTransform(Game game, int row, int col, int rotation) throws Exception {
        getSelector(game).setTransform(col * SIZE, 0, row * SIZE, 0, rotation, 0, SIZE / 2);
        this.rotation = rotation;
        this.row = row;
        this.col = col;
    }

    public Tile newInstance() {
        return new Tile(name, layer);
    }

    public static String keyFor(int row, int col, int layer) {
        return row + ":" + col + ":" + layer;
    }

    @Override
    public String toString() {
        return name + "@" + row + ":" + col + ":" + layer;
    }
}
