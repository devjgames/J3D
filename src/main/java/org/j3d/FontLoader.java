package org.j3d;

import java.io.File;

public class FontLoader implements AssetLoader {

    @Override
    public Object load(File file, AssetManager assets) throws Exception {
        return new Font(file, assets.game);
    }
    
}
