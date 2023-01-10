package org.j3d;

import java.io.File;

public class Font extends Resource implements Asset {
    
    private File file;
    private Texture texture;
    private int cw;
    private int ch;
    private int cols;
    private int whiteX;
    private int whiteY;

    public Font(File file, Game game) throws Exception {
        String[] lines = new String(IO.readAllBytes(file)).split("\\n+");

        this.file = file;

        texture = null;
        cw = -1;
        ch = -1;
        cols = -1;
        whiteX = -1;
        whiteY = -1;

        for(String line : lines) {
            String tLine = line.trim();

            if(tLine.startsWith("file ")) {
                texture = (Texture)new TextureLoader().load(IO.file(file.getParentFile(), tLine.substring(4).trim()), null);
            } else if(tLine.startsWith("cw ")) {
                cw = Integer.parseInt(tLine.substring(2).trim());
            } else if(tLine.startsWith("ch ")) {
                ch = Integer.parseInt(tLine.substring(2).trim());
            } else if(tLine.startsWith("cols ")) {
                cols = Integer.parseInt(tLine.substring(4).trim());
            } else if(tLine.startsWith("whiteX ")) {
                whiteX = Integer.parseInt(tLine.substring(6).trim());
            } else if(tLine.startsWith("whiteY ")) {
                whiteY = Integer.parseInt(tLine.substring(6).trim());
            }
        }
        if(texture == null || cw <= 0 || ch <= 0 || cols <= 0 || whiteX < 0 || whiteY < 0) {
            throw new Exception("invalid font file");
        }
        cw *= game.getScale();
        ch *= game.getScale();
    }

    @Override
    public File getFile() {
        return file;
    }

    public Texture getTexture() {
        return texture;
    }

    public int getCharWidth() {
        return cw;
    }

    public int getCharHeight() {
        return ch;
    }

    public int getColumns() {
        return cols;
    }

    public int getWhiteX() {
        return whiteX;
    }

    public int getWhiteY() {
        return whiteY;
    }

    @Override
    public void destroy() throws Exception {
        texture.destroy();
        super.destroy();
    }
}
