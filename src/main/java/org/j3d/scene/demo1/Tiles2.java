package org.j3d.scene.demo1;

import java.io.File;

import org.j3d.Game;
import org.j3d.IO;
import org.j3d.scene.MeshRenderable;
import org.j3d.scene.Scene;

public class Tiles2 {
    
    public static void setupTile(Tile tile, Game game, boolean high, int row, int col, int rotation, File file) throws Exception {
        int cs = tile.getCellSize();

        tile.node.renderable = new MeshRenderable(file, game.getAssets().load(file));
        tile.node.renderable = tile.node.renderable.newInstance();
        tile.node.collidable = true;
        tile.node.position.set(col * cs + cs * 0.5f, (high) ? 50 : 0, row * cs + cs * 0.5f);
        tile.node.rotation.rotate(rotation * (float)Math.PI / 180, 0, 1, 0);
    }

    public static class LightOrange extends Tile {

        public LightOrange(Game game, Scene scene, TileSize size, int row, int col, int layer) {
            super(game, scene, size, row, col, layer);

            int cs = getCellSize();

            node.isLight = true;
            node.lightColor.set(8, 4, 2);
            node.lightRadius = 300;
            node.position.set(col * cs + cs * 0.5f, 128, row * cs + cs * 0.5f);
        }
    }

    public static class LightBlue extends Tile {

        public LightBlue(Game game, Scene scene, TileSize size, int row, int col, int layer) {
            super(game, scene, size, row, col, layer);

            int cs = getCellSize();

            node.isLight = true;
            node.lightColor.set(2, 4, 8);
            node.lightRadius = 300;
            node.position.set(col * cs + cs * 0.5f, 128, row * cs + cs * 0.5f);
        }
    }

    public static class LedgeFlatL extends Tile {

        public LedgeFlatL(Game game, Scene scene, TileSize size, int row, int col, int layer) throws Exception {
            super(game, scene, size, row, col, layer);
            
            setupTile(this, game, false, row, col, 0, IO.file("assets/tiles/ledge-flat.obj"));
        }
    }

    public static class LedgeFlatH extends Tile {

        public LedgeFlatH(Game game, Scene scene, TileSize size, int row, int col, int layer) throws Exception {
            super(game, scene, size, row, col, layer);
            
            setupTile(this, game, true, row, col, 0, IO.file("assets/tiles/ledge-flat.obj"));
        }
    }

    public static class LedgeCornerL1 extends Tile {

        public LedgeCornerL1(Game game, Scene scene, TileSize size, int row, int col, int layer) throws Exception {
            super(game, scene, size, row, col, layer);
            
            setupTile(this, game, false, row, col, 0, IO.file("assets/tiles/ledge-corner.obj"));
        }
    }

    public static class LedgeCornerH1 extends Tile {

        public LedgeCornerH1(Game game, Scene scene, TileSize size, int row, int col, int layer) throws Exception {
            super(game, scene, size, row, col, layer);
            
            setupTile(this, game, true, row, col, 0, IO.file("assets/tiles/ledge-corner.obj"));
        }
    }

    public static class LedgeCornerL2 extends Tile {

        public LedgeCornerL2(Game game, Scene scene, TileSize size, int row, int col, int layer) throws Exception {
            super(game, scene, size, row, col, layer);
            
            setupTile(this, game, false, row, col, 90, IO.file("assets/tiles/ledge-corner.obj"));
        }
    }

    public static class LedgeCornerH2 extends Tile {

        public LedgeCornerH2(Game game, Scene scene, TileSize size, int row, int col, int layer) throws Exception {
            super(game, scene, size, row, col, layer);
            
            setupTile(this, game, true, row, col, 90, IO.file("assets/tiles/ledge-corner.obj"));
        }
    }

    public static class LedgeCornerL3 extends Tile {

        public LedgeCornerL3(Game game, Scene scene, TileSize size, int row, int col, int layer) throws Exception {
            super(game, scene, size, row, col, layer);
            
            setupTile(this, game, false, row, col, 180, IO.file("assets/tiles/ledge-corner.obj"));
        }
    }

    public static class LedgeCornerH3 extends Tile {

        public LedgeCornerH3(Game game, Scene scene, TileSize size, int row, int col, int layer) throws Exception {
            super(game, scene, size, row, col, layer);
            
            setupTile(this, game, true, row, col, 180, IO.file("assets/tiles/ledge-corner.obj"));
        }
    }

    public static class LedgeCornerL4 extends Tile {

        public LedgeCornerL4(Game game, Scene scene, TileSize size, int row, int col, int layer) throws Exception {
            super(game, scene, size, row, col, layer);
            
            setupTile(this, game, false, row, col, 270, IO.file("assets/tiles/ledge-corner.obj"));
        }
    }

    public static class LedgeCornerH4 extends Tile {

        public LedgeCornerH4(Game game, Scene scene, TileSize size, int row, int col, int layer) throws Exception {
            super(game, scene, size, row, col, layer);
            
            setupTile(this, game, true, row, col, 270, IO.file("assets/tiles/ledge-corner.obj"));
        }
    }

    public static class LedgeSideL1 extends Tile {

        public LedgeSideL1(Game game, Scene scene, TileSize size, int row, int col, int layer) throws Exception {
            super(game, scene, size, row, col, layer);
            
            setupTile(this, game, false, row, col, 0, IO.file("assets/tiles/ledge-side.obj"));
        }
    }

    public static class LedgeSideH1 extends Tile {

        public LedgeSideH1(Game game, Scene scene, TileSize size, int row, int col, int layer) throws Exception {
            super(game, scene, size, row, col, layer);
            
            setupTile(this, game, true, row, col, 0, IO.file("assets/tiles/ledge-side.obj"));
        }
    }

    public static class LedgeSideL2 extends Tile {

        public LedgeSideL2(Game game, Scene scene, TileSize size, int row, int col, int layer) throws Exception {
            super(game, scene, size, row, col, layer);
            
            setupTile(this, game, false, row, col, 90, IO.file("assets/tiles/ledge-side.obj"));
        }
    }

    public static class LedgeSideH2 extends Tile {

        public LedgeSideH2(Game game, Scene scene, TileSize size, int row, int col, int layer) throws Exception {
            super(game, scene, size, row, col, layer);
            
            setupTile(this, game, true, row, col, 90, IO.file("assets/tiles/ledge-side.obj"));
        }
    }

    public static class LedgeSideL3 extends Tile {

        public LedgeSideL3(Game game, Scene scene, TileSize size, int row, int col, int layer) throws Exception {
            super(game, scene, size, row, col, layer);
            
            setupTile(this, game, false, row, col, 180, IO.file("assets/tiles/ledge-side.obj"));
        }
    }

    public static class LedgeSideH3 extends Tile {

        public LedgeSideH3(Game game, Scene scene, TileSize size, int row, int col, int layer) throws Exception {
            super(game, scene, size, row, col, layer);
            
            setupTile(this, game, true, row, col, 180, IO.file("assets/tiles/ledge-side.obj"));
        }
    }

    public static class LedgeSideL4 extends Tile {

        public LedgeSideL4(Game game, Scene scene, TileSize size, int row, int col, int layer) throws Exception {
            super(game, scene, size, row, col, layer);
            
            setupTile(this, game, false, row, col, 270, IO.file("assets/tiles/ledge-side.obj"));
        }
    }

    public static class LedgeSideH4 extends Tile {

        public LedgeSideH4(Game game, Scene scene, TileSize size, int row, int col, int layer) throws Exception {
            super(game, scene, size, row, col, layer);
            
            setupTile(this, game, true, row, col, 270, IO.file("assets/tiles/ledge-side.obj"));
        }
    }
}
