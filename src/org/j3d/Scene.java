package org.j3d;

import java.io.File;
import java.io.InputStream;
import java.util.Vector;

public final class Scene  {

    private static final Vector<Node> renderables = new Vector<>(100000);
    private static final Vector<Node> lights = new Vector<>(1000);

    public final File file;
    public final Camera camera = new Camera();
    public final Node root = new Node();
    public final Vec3 backgroundColor = new Vec3(0.15f, 0.15f, 0.15f);
    public int maxLights = 6;
    public boolean drawLights = true;
    public int snap = 1;
    public int lightMapWidth = 128;
    public int lightMapHeight = 128;
    public boolean lightMapQuadsMode = false;
    public boolean lightMapLinear = false;

    private int trianglesRendered = 0;
    private Node ui = null;
    private File loadFile = null;

    public Scene(File file, boolean inDesign, Game game) throws Exception {
        this.file = file;
        if(inDesign) {
            InputStream input = null;

            ui = NodeLoader.load(IO.file("ui.obj"), new String(IO.readAllBytes(Scene.class, "/org/j3d/resources/ui.obj")), false, game.assets());
            ui = ui.childAt(0);
            ui.scale.set(0.5f, 0.5f, 0.5f);

            try {
                ui.texture = Texture.load(input = Scene.class.getResourceAsStream("/org/j3d/resources/colors.png"));
            } finally {
                if(input != null) {
                    input.close();
                }
            }
        }
    }

    public Node getUI() {
        return ui;
    }

    public boolean inDesign() {
        return ui != null;
    }

    public int getTrianglesRendered() {
        return trianglesRendered;
    }

    public File getLoadFile() {
        return loadFile;
    }

    public void render(Game game) throws Exception {
        camera.calcTransforms(game.aspectRatio());
        root.calcBoundsAndTransform(camera);

        maxLights = Math.max(3, Math.min(8, maxLights));

        trianglesRendered = 0;

        root.traverse((n) -> {
            for(int i = 0; i != n.componentCount(); i++) {
                if(!n.componentAt(i).setup()) {
                    n.componentAt(i).init();
                }
            }
            return true;
        });

        camera.calcTransforms(game.aspectRatio());
        root.calcBoundsAndTransform(camera);

        root.traverse((n) -> {
            for(int i = 0; i != n.componentCount(); i++) {
                if(!n.componentAt(i).setup()) {
                    n.componentAt(i).start();
                }
            }
            return true;
        });

        camera.calcTransforms(game.aspectRatio());
        root.calcBoundsAndTransform(camera);

        root.traverse((n) -> {
            for(int i = 0; i != n.componentCount(); i++) {
                n.componentAt(i).complete();
            }
            return true;
        });

        root.traverse((n) -> {
            if(n.visible) {
                if(n.renderable != null) {
                    n.renderable.update(game);
                    n.renderable.buffer(n, camera);
                }
                for(int i = 0; i != n.componentCount(); i++) {
                    n.componentAt(i).update();
                }
                return true;
            }
            return false;
        });

        camera.calcTransforms(game.aspectRatio());
        root.calcBoundsAndTransform(camera);

        game.renderer().backgroundColor.set(backgroundColor);
        game.renderer().projection.set(camera.projection);
        game.renderer().view.set(camera.view);
        game.renderer().clear();

        root.traverse((n) -> {
            if (n.visible) {
                if (n.renderable != null) {
                    renderables.add(n);
                }
                if(n.isLight) {
                    lights.add(n);
                }
                return true;
            }
            return false;
        });
        renderables.sort((a, b) -> {
            if (a == b) {
                return 0;
            } else if (a.zOrder == b.zOrder) {
                float da = a.absolutePosition.distance(camera.eye);
                float db = b.absolutePosition.distance(camera.eye);
    
                return Float.compare(db, da);
            } else {
                return Integer.compare(a.zOrder, b.zOrder);
            }
        });
        lights.sort((a, b) -> {
            float d1 = a.absolutePosition.distance(camera.target);
            float d2 = b.absolutePosition.distance(camera.target);

            return Float.compare(d1, d2);
        });

        if(ui != null) {
            game.renderer().texture = ui.texture;
            if(drawLights) {
                for(Node light : lights) {
                    game.renderer().model.toIdentity().translate(light.absolutePosition).scale(ui.scale);
                    ui.getMesh().render(ui, camera, game.renderer());
                }
            }
            game.renderer().model.toIdentity().translate(camera.target).scale(ui.scale);
            ui.getMesh().render(ui, camera, game.renderer());
        }
        for (Node renderable : renderables) {
            game.renderer().model.set(renderable.model);
            game.renderer().depthWriteEnabled = renderable.depthWriteEnabled;
            game.renderer().depthTestEnabled = renderable.depthTestEnabled;
            game.renderer().maskEnabled = renderable.maskEnabled;
            game.renderer().blendEnabled = renderable.blendEnabled;
            game.renderer().additiveBlend = renderable.additiveBlend;
            game.renderer().texture = renderable.texture;
            game.renderer().texture2 = renderable.texture2;
            game.renderer().cullState = renderable.cullState;
            if(renderable.lightingEnabled) {
                renderable.renderable.light(lights, Math.min(maxLights, lights.size()), renderable, camera, renderable.ambientColor, renderable.diffuseColor);
            } else if(renderable.renderable instanceof Mesh) {
                Mesh mesh = renderable.getMesh();

                for(int i = 0; i != mesh.vertexCount(); i++) {
                    mesh.vertexAt(i).color.set(renderable.color);
                }
            }
            trianglesRendered += renderable.renderable.render(renderable, camera, game.renderer());
        }
        root.traverse((n) -> {
            if(n.visible) {
                for(int i = 0; i != n.componentCount(); i++) {
                    n.componentAt(i).renderSprites();
                }
                return true;
            }
            return false;
        });

        if(!inDesign()) {
            root.traverse((n) -> {
                if(n.visible && loadFile == null) {
                    for(int i = 0; i != n.componentCount(); i++) {
                        File f = n.componentAt(i).loadFile();

                        if(f != null) {
                            loadFile = f;
                            return false;
                        }
                    }
                    return true;
                }
                return false;
            });
        }
        
        renderables.clear();
        lights.clear();
    }
}
