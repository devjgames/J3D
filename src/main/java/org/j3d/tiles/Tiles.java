package org.j3d.tiles;

import java.io.File;
import java.util.Hashtable;

import org.j3d.IO;
import org.j3d.Parser;
import org.j3d.Collider;
import org.j3d.Collider.TriangleSelector;
import org.j3d.LightPipeline.Light;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Tiles implements TriangleSelector {
    
    public final App app;
    public int playerRow = 0;
    public int playerCol = 0;
    public int playerLayer = 0;

    public final Vector3f position = new Vector3f();
    
    private Hashtable<String, Tile> tiles = new Hashtable<>();
    private boolean enabled = true;
    private int row = 0;
    private int col = 0;
    private int layer = 0;

    public Tiles(App app, File file, Vector3f offset, Vector3f target, Vector3f up, Light ... lights) throws Exception {
        String[] lines = new String(IO.readAllBytes(file)).split("\\n+");

        this.app = app;

        for(String line : lines) {
            String tLine = line.trim();
            String[] tokens = tLine.split("\\s+");

            if(tLine.startsWith("player ")) {
                playerRow = Parser.parse(tokens, 1, 0);
                playerCol = Parser.parse(tokens, 2, 0);
                playerLayer = Parser.parse(tokens, 3, 0);
            } else if(tLine.startsWith("tile ")) {
                Tile tile = new Tile(
                    tokens[1],
                    IO.file(tokens[2]), 
                    IO.file(tokens[3])
                    );
                tile = tile.newInstance(app);
                tile.setLoctation(
                    Parser.parse(tokens, 4, 0),
                    Parser.parse(tokens, 5, 0),
                    Parser.parse(tokens, 6, 0),
                    Parser.parse(tokens, 7, 0)
                );
                tile.setLighting(lights);
                addTile(tile);
            } else if(tLine.startsWith("camera ")) {
                Parser.parse(tokens, 1, offset);
                Parser.parse(tokens, 4, target);
                Parser.parse(tokens, 7, up);
            }
        }
    }

    public Tile findTile(int row, int col, int layer) {
        String key = Tile.key(row, col, layer);

        if(tiles.containsKey(key)) {
            return tiles.get(key);
        }
        return null;
    }

    public void addTile(Tile tile) {
        String key = tile.getKey();

        removeTile(tile);
        tiles.put(key, tile);
    }

    public void removeTile(Tile tile) {
        String key = tile.getKey();

        if(tiles.containsKey(key)) {
            tiles.remove(key);
        }
    }

    public void save(File file, Vector3f offset, Vector3f target, Vector3f up) throws Exception {
        StringBuilder b = new StringBuilder(1000);

        b.append("player " + playerRow + " " + playerCol + " " + playerLayer + "\n");
        b.append("camera " + Parser.toString(offset) + " " + Parser.toString(target) + " " + Parser.toString(up) + "\n");
        for(Tile tile : tiles.values()) {
            b.append(
                "tile " + 
                tile.name + " " + 
                tile.mesh + " " + 
                tile.texture + " " + 
                tile.getRow() + " " + 
                tile.getCol() + " " + 
                tile.getLayer() + " " + 
                tile.getRotationDegrees() + "\n"
                );
        }
        IO.writeAllBytes(b.toString().getBytes(), file);
    }

    public void render(Matrix4f projection, Matrix4f view) {
        for(Tile tile : tiles.values()) {
            if(tile.visible) {
                tile.getSelector().render(projection, view);
            }
        }
    }

    public void setLighting(Light ... lights) {
        for(Tile tile : tiles.values()) {
            tile.setLighting(lights);
        }
    }

    @Override
    public boolean getEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean intersect(Collider collider) {
        boolean hit = false;

        calcCell();

        for(int r = row - 2; r != row + 3; r++) {
            for(int c = col - 2; c != col + 3; c++) {
                for(int l = layer - 2; l != layer + 3; l++) {
                    Tile tile = findTile(r, c, l);

                    if(tile != null) {
                        if(tile.getSelector().intersect(collider)) {
                            hit = true;
                        }
                    }
                }
            }
        }
        return hit;
    }

    @Override
    public boolean resolve(Collider collider) {
        boolean hit = false;

        calcCell();

        for(int r = row - 2; r != row + 3; r++) {
            for(int c = col - 2; c != col + 3; c++) {
                for(int l = layer - 2; l != layer + 3; l++) {
                    Tile tile = findTile(r, c, l);

                    if(tile != null) {
                        if(tile.getSelector().resolve(collider)) {
                            hit = true;
                        }
                    }
                }
            }
        }
        return hit;
    }

    private void calcCell() {
        row = (int)(Math.floor(position.z / 64));
        col = (int)(Math.floor(position.x / 64));
        layer = (int)(Math.floor(layer / 64));
    }
}
