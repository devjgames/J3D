package org.j3d.tiles;

import java.io.File;

import org.j3d.Game;
import org.j3d.IO;
import org.j3d.MeshTriangleSelector;
import org.j3d.LightPipeline.Light;

public class Tile {
    
    public final String name;
    public final File mesh;
    public final File texture;
    public boolean visible = true;

    private int row = 0;
    private int col = 0;
    private int layer = 0;
    private int rotationDegrees = 0;
    private MeshTriangleSelector selector = null;

    public Tile(String name, String mesh, String texture) throws Exception {
        this.name = name;
        this.mesh = IO.file(IO.file("assets/meshes"), mesh);
        this.texture = IO.file(IO.file("assets/meshes"), texture);
    }

    public String getKey() {
        return key(row, col, layer);
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

    public int getRotationDegrees() {
        return rotationDegrees;
    }

    public void rotate() {
        rotationDegrees = (rotationDegrees + 90) % 360;
    }

    public MeshTriangleSelector getSelector() {
        return selector;
    }

    public void setLighting(Light ... lights) {
        if(lights.length == 0) {
            selector.mesh.ambientColor.set(1, 1, 1, 1);
            selector.mesh.diffuseColor.set(0, 0, 0, 1);
        } else {
            selector.mesh.ambientColor.set(0.5f, 0.5f, 0.5f, 1);
            selector.mesh.diffuseColor.set(1.0f, 1.0f, 1.0f, 1);
            selector.mesh.lights.clear();;
            for(Light light : lights) {
                selector.mesh.lights.add(light);
            }
        }
    }

    public void setLoctation(int row, int col, int layer, int rotationDegrees) {
        this.row = row;
        this.col = col;
        this.layer = layer;
        this.rotationDegrees = rotationDegrees;
        selector.setTransform(col * 64, layer * 64, row * 64, 0, rotationDegrees, 0, 1);
    }

    public Tile newInstance(App app) throws Exception {
        Tile tile = new Tile(name, mesh.getName(), texture.getName());
        Game game = app.getGame();

        tile.selector = new MeshTriangleSelector(game.getAssets().load(mesh));
        tile.selector.mesh.texture = game.getAssets().load(texture);
        tile.rotationDegrees = rotationDegrees;

        return tile;
    }

    public static String key(int row, int col, int layer) {
        return row + ":" + col + ":" + layer;
    }
}
