package org.j3d;

import java.util.Comparator;
import java.util.Vector;

public final class Scene implements Comparator<Node> {

    private static final Vector<Node> renderables = new Vector<>(100000);
    private static final Vector<Node> lights = new Vector<>(1000);

    public final Camera camera = new Camera();
    public final Node root = new Node();
    public final Vec3 backgroundColor = new Vec3(0.15f, 0.15f, 0.15f);
    public int maxLights = 6;

    private int trianglesRendered = 0;


    public int getTrianglesRendered() {
        return trianglesRendered;
    }

    public void render(Game game) throws Exception {
        camera.calcTransforms(game.aspectRatio());
        root.calcBoundsAndTransform(camera);

        maxLights = Math.max(3, Math.min(16, maxLights));

        trianglesRendered = 0;

        game.renderer().backgroundColor.set(backgroundColor);
        game.renderer().projection.set(camera.projection);
        game.renderer().view.set(camera.view);
        game.renderer().clear();

        updateAndBuffer(root, game);

        add(root);
        addLights(root);
        renderables.sort(this);
        lights.sort((a, b) -> {
            float d1 = a.absolutePosition.distance(camera.target);
            float d2 = b.absolutePosition.distance(camera.target);

            return Float.compare(d1, d2);
        });
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
            }
            trianglesRendered += renderable.renderable.render(renderable, camera, game.renderer());
        }
        renderables.clear();
        lights.clear();
    }

    private void updateAndBuffer(Node node, Game game) {
        if(node.visible) {
            if(node.renderable != null) {
                node.renderable.update(game);
                node.renderable.buffer(node, camera);
            }
            for (Node child : node) {
                updateAndBuffer(child, game);
            }
        }
    }

    private void add(Node node) {
        if (node.visible) {
            if (node.renderable != null) {
                renderables.add(node);
            }
            for (Node child : node) {
                add(child);
            }
        }
    }

    private void addLights(Node node) {
        if (node.visible) {
            if(node.isLight) {
                lights.add(node);
            }
            for(Node child : node) {
                addLights(child);
            }
        }
    }

    @Override
    public int compare(Node o1, Node o2) {
        if (o1 == o2) {
            return 0;
        } else if (o1.zOrder == o2.zOrder) {
            float d1 = o1.absolutePosition.distance(camera.eye);
            float d2 = o2.absolutePosition.distance(camera.eye);

            return Float.compare(d2, d1);
        } else {
            return Integer.compare(o1.zOrder, o2.zOrder);
        }
    }
}
