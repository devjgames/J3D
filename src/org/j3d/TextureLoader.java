package org.j3d;

import java.io.File;
import java.io.IOException;

public class TextureLoader implements AssetLoader {

    @Override
    public Object load(File file, AssetManager assets) throws IOException {
        return Texture.load(file);
    }
}
