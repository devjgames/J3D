package org.j3d;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;

public class AssetManager extends Resource {

    public final Game game;

    private final ResourceManager resources = new ResourceManager();
    private final Hashtable<String, Object> assets = new Hashtable<>();
    private final Hashtable<String, AssetLoader> assetLoaders = new Hashtable<>();

    public AssetManager(Game game) {
        registerAssetLoader(".png", new TextureLoader());
        registerAssetLoader(".jpeg", new TextureLoader());
        registerAssetLoader(".jpg", new TextureLoader());
        registerAssetLoader(".wav", new SoundLoader());
        registerAssetLoader(".fnt", new FontLoader());
        registerAssetLoader(".obj", new TexturePipelineLoader());

        this.game = game;
    }

    public ResourceManager getResources() {
        return resources;
    }

    public void registerAssetLoader(String extension, AssetLoader assetLoader) {
        assetLoaders.put(extension, assetLoader);
    }

    @SuppressWarnings("unchecked")
    public <T extends Object> T load(File file) throws Exception {
        String key = file.getPath();

        if (!assets.containsKey(key)) {
            Log.log(1, "loading '" + key + "' ...");
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

    public void clear() throws Exception {
        Enumeration<String> paths = assets.keys();

        while (paths.hasMoreElements()) {
            Object object = assets.get(paths.nextElement());

            if (object instanceof Resource) {
                ((Resource) object).destroy();
            }
        }
        assets.clear();
        getResources().clear();

        Utils.allocations = 0;
    }

    @Override
    public void destroy() throws Exception {
        clear();
        getResources().destroy();
        super.destroy();
    }
}
