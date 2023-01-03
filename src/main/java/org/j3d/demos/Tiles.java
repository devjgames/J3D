package org.j3d.demos;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;

import org.j3d.BoundingBox;
import org.j3d.Collider;
import org.j3d.Game;
import org.j3d.IO;
import org.j3d.LightPipeline;
import org.j3d.Log;
import org.j3d.Parser;
import org.j3d.Collider.TriangleSelector;
import org.j3d.LightPipeline.Light;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Tiles implements TriangleSelector {

    private static Vector<Tile> factories = new Vector<>();
    private static String textureName = "";

    public static void addTileFactories(String textureName) {
        if(factories.isEmpty()) {
            File[] files = IO.file("assets/meshes").listFiles();

            Arrays.sort(files);
            for(File file : files) {
                if(IO.extension(file).equals(".obj")) {
                    String name = IO.fileNameWithOutExtension(file);

                    Log.log(1, "adding tile factory '" + name + "' ...");

                    factories.add(new Tile(name, name.endsWith("_1") ? 1 : 0));
                }
            }
            Tiles.textureName = textureName;
        }
    }

    public static File getTextureFile() {
        return IO.file(IO.file("assets/meshes"), textureName + ".png");
    }

    public static File getDecalFile() {
        return IO.file(IO.file("assets/meshes"), textureName + "-decal.png");
    }

    public static int getTileFactoryCount() {
        return factories.size();
    }

    public static Tile getTileFactory(int i) {
        return factories.get(i);
    }

    public final File file;
    public final Vector3f position = new Vector3f();
    public final Game game;
    public final int playerRow;
    public final int playerCol;
    public final Vector<Light> lights = new Vector<>();
    
    private Hashtable<String, Tile> tiles = new Hashtable<>();
    private Vector<Tile> sortedTiles = new Vector<>();
    private Vector<Light> sortedLights = new Vector<>();
    private boolean enabled = true;
    private int binds = 0;
    private int rendered = 0;
    private FrustumIntersection frustum = new FrustumIntersection();
    private Matrix4f matrix = new Matrix4f();
    private BoundingBox bounds = new BoundingBox();

    public Tiles(Game game, File file) throws Exception {
        String[] lines = new String(IO.readAllBytes(file)).split("\\n+");
        int pr = 0;
        int pc = 0;

        this.file = file;
        this.game = game;

        for(String line : lines) {
            String tLine = line.trim();
            String[] tokens = tLine.split("\\s+");

            if(tLine.startsWith("player ")) {
                pr = Parser.parse(tokens, 1, 0);
                pc = Parser.parse(tokens, 2, 0);
            } else if(tLine.startsWith("light ")) {
                Light light = new Light();

                light.directional = Parser.parse(tokens, 1, true);
                Parser.parse(tokens, 2, light.vector);
                Parser.parse(tokens, 5, light.color);
                light.radius = Parser.parse(tokens, 9, 50.0f);
                lights.add(light);
            } else if(tLine.startsWith("tile ")) {
                Tile tile = new Tile(tokens[1], Parser.parse(tokens, 2, 0));
                int row = Parser.parse(tokens, 3, 0);
                int col = Parser.parse(tokens, 4, 0);
                int rotation = Parser.parse(tokens, 5, 0);

                tile.setTransform(game, row, col, rotation);
                if(tile.name.startsWith("t_")) {
                    tile.getSelector(game).setEnabled(false);
                }
                add(tile);
            }
        }
        playerRow = pr;
        playerCol = pc;
        position.set(playerCol * Tile.SIZE, Tile.SIZE * 2, playerRow * Tile.SIZE);
    }

    public int getBinds() {
        return binds;
    }

    public int getRendered() {
        return rendered;
    }

    public Collection<Tile> getTiles() {
        return tiles.values();
    }

    public Tile find(int row, int col, int layer) {
        String key = Tile.keyFor(row, col, layer);

        if(tiles.containsKey(key)) {
            return tiles.get(key);
        }
        return null;
    }

    public void remove(Tile tile) {
        String key = tile.key();

        if(tiles.containsKey(key)) {
            tiles.remove(key);
        }
    }

    public void add(Tile tile) throws Exception {
        remove(tile);
        tiles.put(tile.key(), tile);
    }

    public void render(Matrix4f projection, Matrix4f view, Tile cell) throws Exception {
        frustum.set(matrix.set(projection).mul(view));

        for(Light light : lights) {
            bounds.min.set(light.vector).sub(light.radius, light.radius, light.radius);
            bounds.max.set(light.vector).add(light.radius, light.radius, light.radius);
            if(frustum.testAab(bounds.min, bounds.max)) {
                sortedLights.add(light);
            }
        }

        sortedLights.sort((a, b) -> {
            if(a == b) {
                return 0;
            } else {
                float d1 = a.vector.distance(position);
                float d2 = b.vector.distance(position);

                return Float.compare(d1, d2);
            }
        });
        sortedLights.setSize(Math.min(sortedLights.size(), LightPipeline.MAX_LIGHTS));

        for(Tile tile : tiles.values()) {
            if(tile.visible) {
                bounds.min.set(tile.getSelector(game).mesh.getBounds().min);
                bounds.max.set(tile.getSelector(game).mesh.getBounds().max);
                bounds.transform(tile.getSelector(game).model);
                if(frustum.testAab(bounds.min, bounds.max)) {
                    sortedTiles.add(tile);
                }
            }
        }
        sortedTiles.sort((a, b) -> {
            if(a == b) {
                return 0;
            } else {
                return a.name.compareTo(b.name);
            }
        });

        Tile last = null;

        binds = 0;
        rendered = 0;

        for(Tile tile : sortedTiles) {
            if(last == null) {
                last = tile;
                tile.getSelector(game).mesh.lights.clear();
                tile.getSelector(game).mesh.lights.addAll(sortedLights);
                tile.getSelector(game).begin(projection, view);
                binds++;
            } else if(!last.name.equals(tile.name)) {
                last.getSelector(game).end();
                last = tile;
                tile.getSelector(game).mesh.lights.clear();
                tile.getSelector(game).mesh.lights.addAll(sortedLights);
                tile.getSelector(game).begin(projection, view);
                binds++;
            }
            tile.getSelector(game).render();
            rendered++;
        }
        if(last != null) {
            last.getSelector(game).end();
        }

        if(cell != null) {
            cell.getSelector(game).mesh.lights.clear();
            cell.getSelector(game).mesh.lights.addAll(sortedLights);
            cell.getSelector(game).render(projection, view);
            binds++;
            rendered++;
        }
        sortedTiles.clear();
        sortedLights.clear();
    }

    public void save() throws Exception {
        StringBuilder b = new StringBuilder(1000);

        b.append("player " + playerRow + " " + playerCol + "\n");

        for(Light light : lights) {
            b.append(
                "light " + 
                light.directional + " " +
                Parser.toString(light.vector) + " " + 
                Parser.toString(light.color) + " " + 
                light.radius + "\n"
            );
        }
        for(Tile tile : tiles.values()) {
            b.append(
                "tile " + 
                tile.name + " " + 
                tile.getLayer() + " " + 
                tile.getRow() +  " " + 
                tile.getCol() + " " + 
                tile.getRotation() + "\n"
                );
        }
        IO.writeAllBytes(b.toString().getBytes(), file);
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
    public boolean intersect(Collider collider) throws Exception {
        boolean hit = false;
        int row = calcIndex(position.z);
        int col = calcIndex(position.x);

        for(int r = -1; r != 2; r++) {
            for(int c = -1; c != 2; c++) {
                for(int l = 0; l != 2; l++) {
                    Tile tile = find(row + r, col + c, l);

                    if(tile != null) {
                        if(tile.getSelector(game).getEnabled()) {
                            if(tile.getSelector(game).intersect(collider)) {
                                hit = true;
                            }
                        }
                    }
                }
            }
        }
        return hit;
    }

    @Override
    public boolean resolve(Collider collider) throws Exception {
        boolean hit = false;
        int row = calcIndex(position.z);
        int col = calcIndex(position.x);

        for(int r = -1; r != 2; r++) {
            for(int c = -1; c != 2; c++) {
                for(int l = 0; l != 2; l++) {
                    Tile tile = find(row + r, col + c, l);

                    if(tile != null) {
                        if(tile.getSelector(game).getEnabled()) {
                            if(tile.getSelector(game).resolve(collider)) {
                                hit = true;
                            }
                        }
                    }
                }
            }
        }
        return hit;
    }

    public static int calcIndex(float value) {
        return (int)Math.floor(value) / Tile.SIZE;
    }
}
