package org.j3d;

import java.io.File;
import java.util.Hashtable;

public class AssetManager {

    private Hashtable<String, Object> assets = new Hashtable<>();
    private Hashtable<String, AssetLoader> assetLoaders = new Hashtable<>();

    public AssetManager() {
        registerAssetLoader(".wav", new SoundLoader());
        registerAssetLoader(".png", new TextureLoader());
        registerAssetLoader(".obj", new NodeLoader());
        registerAssetLoader(".md2", new MD2MeshLoader());
    }

    public void registerAssetLoader(String extension, AssetLoader assetLoader) {
        assetLoaders.put(extension, assetLoader);
    }

    @SuppressWarnings("unchecked")
    public <T extends Object> T load(File file) throws Exception {
        String key = file.getPath();
        
        if (!assets.containsKey(key)) {
            System.out.println("loading asset - '" + file + "'");
            assets.put(key, assetLoaders.get(IO.extension(file)).load(file, this));
        }
        return (T) assets.get(key);
    }

    public void unload(File file) throws Exception {
        String key = file.getPath();

        if(assets.containsKey(key)) {
            Object asset = assets.get(key);
            if(asset instanceof Resource) {
                ((Resource)asset).destroy();
            }
            assets.remove(key);
        }
    }

    public void clear() {
        for (Object value : assets.values()) {
            if (value instanceof Resource) {
                try {
                    ((Resource) value).destroy();
                } catch(Exception ex) {
                    ex.printStackTrace(System.out);
                }
            }
        }
        assets.clear();
    }
}
