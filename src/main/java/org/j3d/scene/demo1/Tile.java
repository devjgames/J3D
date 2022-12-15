package org.j3d.scene.demo1;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.j3d.Game;
import org.j3d.IO;
import org.j3d.MeshPart;
import org.j3d.Parser;
import org.j3d.Texture;
import org.j3d.lm.DualTextureMaterial;
import org.j3d.lm.LightMapper;
import org.j3d.lm.Model;
import org.j3d.lm.Surface;
import org.j3d.scene.MeshRenderable;
import org.j3d.scene.Node;
import org.j3d.scene.Renderable;
import org.j3d.scene.Scene;

public abstract class Tile {

    public enum TileSize {
        X32,
        X64,
        X128,
        X256
    }

    public final TileSize size;
    public final int row;
    public final int col;
    public final Node node = new Node();
    public final int layer;

    public Tile(Game game, Scene scene, TileSize size, int row, int col, int layer) {
        this.size = size;
        this.row = row;
        this.col = col;
        this.layer = layer;

        node.data = this;
        node.position.set(col * getCellSize() + getCellSize() / 2, 0 , row * getCellSize() + getCellSize() / 2);
    }

    public int getCellSize() {
        if(size == TileSize.X32) {
            return 32;
        } else if(size == TileSize.X64) {
            return 64;
        } else if(size == TileSize.X128) {
            return 128;
        } else {
            return 256;
        }
    }

    public Tile newInstance(Game game, Scene scene, int row, int col) throws Exception {
        return (Tile)getClass().getConstructors()[0].newInstance(game, scene, size, row, col, layer);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    public static void save(Vector<Tile> tiles, File file) throws IOException {
        StringBuilder b = new StringBuilder(1000);  

        for(Tile tile : tiles) {
            b.append(tile.getClass().getName() + " " + tile.row + " " + tile.col + " " + tile.size + " " + tile.layer + "\n");
        }
        IO.writeAllBytes(b.toString().getBytes(), file);
    }

    public static Vector<Tile> load(File file, Game game, Scene scene, int width, int height, float ao) throws Exception {
        Vector<Tile> tiles = new Vector<>();
        String[] lines = new String(IO.readAllBytes(file)).split("\\n+");

        for(String line : lines) {
            String tLine = line.trim();

            if(!tLine.isEmpty()) {
                String[] tokens = tLine.split("\\s+");

                try {
                    Tile tile = (Tile)Class.forName(tokens[0]).getConstructors()[0].newInstance(
                        game,
                        scene,
                        (TileSize)Parser.parseObject(tokens, 3, TileSize.X256),
                        Integer.parseInt(tokens[1]),
                        Integer.parseInt(tokens[2]),
                        Parser.parse(tokens, 4, 0)
                    );

                    scene.root.addChild(tile.node);
                    tiles.add(tile);
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        scene.calcTransform(game);
        scene.root.calcBoundsAndTransform();
        scene.root.init();
        scene.calcTransform(game);
        scene.root.calcBoundsAndTransform();
        scene.root.start();


        map(game, tiles, width, height, ao, file, false);

        return tiles;
    }

    public static void map(Game game, Vector<Tile> tiles, int width, int height, float ao, File file, boolean delete) throws Exception {
        File texture2File = IO.file(IO.file("assets/tile-scenes"), IO.fileNameWithOutExtension(file) + ".png");
        Vector<org.j3d.Light> lights = new Vector<>();
        Vector<Model> models = new Vector<>();

        if(texture2File.exists() && delete) {
            texture2File.delete();
        }

        for(Tile tile : tiles) {
            if(tile.node.isLight) {
                org.j3d.Light light = new org.j3d.Light();

                light.position.set(tile.node.position);
                light.color.set(tile.node.lightColor);
                light.radius = tile.node.lightRadius;

                lights.add(light);
            } else if(tile.node.renderable instanceof MeshRenderable) {
                MeshRenderable renderable = (MeshRenderable)tile.node.renderable;
                boolean isDualTexture =  false;

                for(MeshPart part : renderable.mesh) {
                    if(part.material instanceof DualTextureMaterial) {
                        isDualTexture = true;
                        break;
                    }
                }
                if(isDualTexture) {
                    models.add(new Model(tile.node.model, renderable.mesh));
                }
            }
        }

        if(!models.isEmpty()) {
            if(new LightMapper().map(texture2File, lights, width, height, ao, models)) {
                game.getAssets().unload(texture2File);

                Texture texture = game.getAssets().load(texture2File);

                texture.bind();
                texture.toLinear(true);
                texture.unBind();;
                for(Model model : models) {
                    for(MeshPart part : model.mesh) {
                        DualTextureMaterial material = (DualTextureMaterial)part.material;

                        material.color.set(2, 2, 2, 1);
                        material.texture2 = texture;
                    }
                }
            }
        }
    }

    public static void setSurface(Renderable renderable) {
        Surface surface = new Surface();

        for(MeshPart part : ((MeshRenderable)renderable).mesh) {
            for(int i = 0; i != part.getFaceCount(); i++) {
                part.setFaceData(i, surface);
            }
        }
    }

    public static void setupLightMapped(Tile tile, Game game, int row, int col, int rotation, File file) throws Exception {
        int cs = tile.getCellSize();

        tile.node.renderable = new MeshRenderable(file, DualTextureMaterial.load(file, game.getAssets()));
        tile.node.renderable = tile.node.renderable.newInstance();
        tile.node.position.set(col * cs + cs * 0.5f, 0, row * cs + cs * 0.5f);
        tile.node.collidable = true;
        tile.node.rotation.rotate(rotation * (float)Math.PI / 180, 0, 1, 0);

        setSurface(tile.node.renderable);
    }

    public static class Light extends Tile {

        public Light(Game game, Scene scene, TileSize size, int row, int col, int layer) {
            super(game, scene, size, row, col, layer);

            int cs = getCellSize();

            node.isLight = true;
            node.lightColor.set(0.5f, 0.75f, 1);
            node.lightRadius = 300;
            node.position.set(col * cs + cs * 0.5f, 128, row * cs + cs * 0.5f);
        }
    }

    public static class Floor extends Tile {

        public Floor(Game game, Scene scene, TileSize size, int row, int col, int layer) throws Exception {
            super(game, scene, size, row, col, layer);

            setupLightMapped(this, game, row, col, 0, IO.file("assets/tiles/floor.obj"));
        }
    }

    public static class Corner1 extends Tile {

        public Corner1(Game game, Scene scene, TileSize size, int row, int col, int layer) throws Exception {
            super(game, scene, size, row, col, layer);

            setupLightMapped(this, game, row, col, 0, IO.file("assets/tiles/corner.obj"));
        }
    }

    public static class Corner2 extends Tile {

        public Corner2(Game game, Scene scene, TileSize size, int row, int col, int layer) throws Exception {
            super(game, scene, size, row, col, layer);

            setupLightMapped(this, game, row, col, 90, IO.file("assets/tiles/corner.obj"));
        }
    }

    public static class Corner3 extends Tile {

        public Corner3(Game game, Scene scene, TileSize size, int row, int col, int layer) throws Exception {
            super(game, scene, size, row, col, layer);

            setupLightMapped(this, game, row, col, 180, IO.file("assets/tiles/corner.obj"));
        }
    }

    public static class Corner4 extends Tile {

        public Corner4(Game game, Scene scene, TileSize size, int row, int col, int layer) throws Exception {
            super(game, scene, size, row, col, layer);

            setupLightMapped(this, game, row, col, 270, IO.file("assets/tiles/corner.obj"));
        }
    }

    public static class Side1 extends Tile {

        public Side1(Game game, Scene scene, TileSize size, int row, int col, int layer) throws Exception {
            super(game, scene, size, row, col, layer);

            setupLightMapped(this, game, row, col, 0, IO.file("assets/tiles/side.obj"));
        }
    }

    public static class Side2 extends Tile {

        public Side2(Game game, Scene scene, TileSize size, int row, int col, int layer) throws Exception {
            super(game, scene, size, row, col, layer);

            setupLightMapped(this, game, row, col, 90, IO.file("assets/tiles/side.obj"));
        }
    }

    public static class Side3 extends Tile {

        public Side3(Game game, Scene scene, TileSize size, int row, int col, int layer) throws Exception {
            super(game, scene, size, row, col, layer);

            setupLightMapped(this, game, row, col, 180, IO.file("assets/tiles/side.obj"));
        }
    }

    public static class Side4 extends Tile {

        public Side4(Game game, Scene scene, TileSize size, int row, int col, int layer) throws Exception {
            super(game, scene, size, row, col, layer);

            setupLightMapped(this, game, row, col, 270, IO.file("assets/tiles/side.obj"));
        }
    }
}
