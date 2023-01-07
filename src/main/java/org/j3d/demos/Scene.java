package org.j3d.demos;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.j3d.BoundingBox;
import org.j3d.Collider;
import org.j3d.Game;
import org.j3d.IO;
import org.j3d.LightPipeline;
import org.j3d.MeshTriangleSelector;
import org.j3d.Parser;
import org.j3d.Utils;
import org.j3d.Collider.TriangleSelector;
import org.j3d.LightPipeline.Light;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Scene implements TriangleSelector {

    public static class Mesh {
        public final MeshTriangleSelector selector;
        public final Vector3f position = new Vector3f();
        public float rotationDegrees = 0;
        public final float scale;

        public Mesh(LightPipeline pipeline, float scale) {
            selector = new MeshTriangleSelector(pipeline);
            this.scale = scale;
        }

        public void setTransform() {
            selector.setTransform(position.x, position.y, position.z, 0, rotationDegrees, 0, scale);
        }
    }

    public final String name;
    public final String meshSet;
    public final Vector3f playerPosition = new Vector3f();
    public final Vector3f playerOffset = new Vector3f(100, 100, 100);
    public final Vector3f target = new Vector3f();
    public final Vector3f up = new Vector3f(0, 1, 0);
    public final Vector3f eye = new Vector3f();
    public float playerDegrees1 = 0;
    public float playerDegrees2 = 0;
    public final float playerScale;
    public final float playerSpeed;
    public final float playerRadius;
    public final float scale;
    public boolean lightingEnabled;
    public final Vector<Light> lights = new Vector<>();
    public final Vector4f backgroundColor = new Vector4f(0, 0, 0, 1);
    public final Hashtable<String, Boolean> meshCollidable = new Hashtable<>();
    public final Vector<Mesh> meshes = new Vector<>();
    public final LightPipeline playerPipeline;

    private final Hashtable<String, Vector<Mesh>> meshGroups = new Hashtable<>();
    private boolean enabled = true;

    public Scene(Game game, String name) throws Exception {
        String[] lines = new String(IO.readAllBytes(IO.file(IO.file("assets/scenes"), name + ".sh"))).split("\\n+");
        String meshSet = "";
        float playerScale = 1;
        float playerSpeed = 0;
        float scale = 1;
        float playerRadius = 1;

        this.name = name;

        for(String line : lines) {
            String tline = line.trim();
            String[] tokens = tline.split("\\s+");

            if(tline.startsWith("mesh-set ")) {
                meshSet = tokens[1]; 
            } else if(tline.startsWith("player ")) {
                Parser.parse(tokens, 1, playerPosition);
                Parser.parse(tokens, 4, playerOffset);
                playerScale = Parser.parse(tokens, 7, playerScale);
                playerSpeed = Parser.parse(tokens, 8, playerSpeed);
                playerRadius = Parser.parse(tokens, 9, playerRadius);
            } else if(tline.startsWith("scale ")) {
                scale = Parser.parse(tokens, 1, scale);
            } else if(tline.startsWith("lighting-enabled ")) {
                lightingEnabled = Parser.parse(tokens, 1, lightingEnabled);
            } else if(tline.startsWith("light ")) {
                Light light = new Light();

                light.directional = Parser.parse(tokens, 1, light.directional);
                Parser.parse(tokens, 2, light.vector);
                Parser.parse(tokens, 5, light.color);
                light.radius = Parser.parse(tokens, 9, light.radius);

                lights.add(light);
            } else if(tline.startsWith("background ")) {
                Parser.parse(tokens, 1, backgroundColor);
            } else if(tline.startsWith("mesh-cfg ")) {
                meshCollidable.put(tokens[1], Parser.parse(tokens, 2, true));
            } else if(tline.startsWith("mesh ")) {
                boolean collidable = true;

                if(meshCollidable.containsKey(tokens[1])) {
                    collidable = meshCollidable.get(tokens[1]);
                }

                LightPipeline pipeline = game.getAssets().load(
                    IO.file(IO.file(IO.file("assets/meshes"), meshSet), tokens[1] + ".obj")
                    );
                Mesh mesh = new Mesh(pipeline, scale);

                zeroCenter(pipeline);

                mesh.selector.setEnabled(collidable);
                mesh.position.set(
                    Parser.parse(tokens, 2, 0.0f), 
                    Parser.parse(tokens, 3, 0.0f),
                    Parser.parse(tokens, 4, 0.0f)
                );
                mesh.rotationDegrees = Parser.parse(tokens, 5, 0.0f);
                mesh.setTransform();

                meshes.add(mesh);

                if(!meshGroups.containsKey(tokens[1])) {
                    meshGroups.put(tokens[1], new Vector<>());
                }
                meshGroups.get(tokens[1]).add(mesh);
            }
        }
        this.meshSet = meshSet;
        this.playerScale = playerScale;
        this.playerSpeed = playerSpeed;
        this.playerRadius = playerRadius;
        this.scale = scale;

        playerPipeline = game.getAssets().load(IO.file(IO.file(IO.file("assets/meshes"), meshSet), "player.obj"));
        zeroCenter(playerPipeline);
    }


    public void render(Matrix4f projection, Matrix4f view) throws Exception {
        Enumeration<String> names = meshGroups.keys();
        float v = 1;

        if(playerPosition.y < 0) {
            v = 1 - Math.min(Math.abs(playerPosition.y) / 100, 1);
        }

        target.set(playerPosition);
        target.y = 0;
        target.add(playerOffset, eye);
        view.identity().lookAt(eye, target, up);

        Utils.clear(backgroundColor.x, backgroundColor.y, backgroundColor.z, backgroundColor.w);
        while(names.hasMoreElements()) {
            String name = names.nextElement();
            Vector<Mesh> group = meshGroups.get(name);

            group.get(0).selector.mesh.lights.clear();
            if(lightingEnabled) {
                group.get(0).selector.mesh.lights.addAll(lights);
                group.get(0).selector.mesh.ambientColor.set(0.2f, 0.2f, 0.2f, 1);
            } else {
                group.get(0).selector.mesh.ambientColor.set(1, 1, 1, 1);
            }
            group.get(0).selector.begin(projection, view);
            for(Mesh mesh : group) {
                mesh.setTransform();
                mesh.selector.render();
            }
            group.get(0).selector.end();
        }
        playerPipeline.ambientColor.set(v, v, v, 1);
        playerPipeline.model
            .identity()
            .translate(playerPosition)
            .rotate(Utils.toRadians(playerDegrees1), 0, 1, 0)
            .rotate(Utils.toRadians(playerDegrees2), 0, 0, 1)
            .scale(playerScale);
        playerPipeline.render(projection, view);
    }

    @Override
    public boolean getEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = true;
    }

    @Override
    public boolean intersect(Collider collider) throws Exception {
        boolean hit = false;

        for(Mesh mesh : meshes) {
            if(mesh.selector.getEnabled()) {
                mesh.setTransform();
                if(mesh.selector.intersect(collider)) {
                    hit = true;
                }
            }
        }
        return hit;
    }

    @Override
    public boolean resolve(Collider collider) throws Exception {
        boolean hit = false;

        for(Mesh mesh : meshes) {
            if(mesh.selector.getEnabled()) {
                mesh.setTransform();
                if(mesh.selector.resolve(collider)) {
                    hit = true;
                }
            }
        }
        return hit;
    }
    
    public void save() throws IOException {
        StringBuilder b = new StringBuilder(1000);
        Enumeration<String> keys = meshCollidable.keys();

        b.append("# mesh-set name\n");
        b.append("mesh-set " + meshSet + "\n");

        b.append("# player name x y z off-x off-y off-z scale speed radius\n");
        b.append(
            "player " + 
            Parser.toString(playerPosition) + " " + 
            Parser.toString(playerOffset) + " " + 
            playerScale + " " + 
            playerSpeed + " " + 
            playerRadius + "\n"
        );

        b.append("# scale s\n");
        b.append("scale " + scale + "\n");

        b.append("# lighting-enabled bool\n");
        b.append("lighting-enabled " + lightingEnabled + "\n");

        b.append("# light directional x y z r g b a radius\n");
        b.append("# ...\n");
        for(Light light : lights) {
            b.append(
                "light " + 
                light.directional + " " +
                Parser.toString(light.vector) + " " +
                Parser.toString(light.color) + " " + 
                light.radius + "\n"
            );
        }

        b.append("# background r g b a\n");
        b.append("background-color " + Parser.toString(backgroundColor) + "\n");

        b.append("# mesh-cfg name collidable\n");
        b.append("# ...\n");
        while(keys.hasMoreElements()) {
            String key = keys.nextElement();
            boolean collidable = meshCollidable.get(key);

            b.append("mesh-cfg " + key + " " + collidable + "\n");
        }

        b.append("# mesh name x y z rotation-degrees\n");
        b.append("# ...\n");
        for(Mesh mesh : meshes) {
            b.append(
                IO.fileNameWithOutExtension(mesh.selector.mesh.getFile()) + " " +
                mesh.position.x + " " +
                mesh.position.z + " " + 
                mesh.position.z + " " +
                mesh.rotationDegrees + "\n"
            );
        }
        IO.writeAllBytes(b.toString().getBytes(), IO.file(IO.file("assets/scenes"), name + ".sh"));
    }

    private void zeroCenter(LightPipeline pipeline) {
        Vector3f center = new Vector3f();
        BoundingBox bounds = pipeline.getBounds();

        bounds.max.add(bounds.min, center).mul(0.5f);
        for(int i = 0; i != pipeline.getVertexCount(); i++) {
            float x = pipeline.vertexX(i);
            float y = pipeline.vertexY(i);
            float z = pipeline.vertexZ(i);

            x -= center.x;
            z -= center.z;

            pipeline.setVertex(i, x, y, z);
        }
        bounds.max.sub(center.x, 0, center.z);
        bounds.min.sub(center.x, 0, center.z);

        pipeline.bufferVertices();
    }
}
