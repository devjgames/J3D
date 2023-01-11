package org.j3d.demos;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import org.j3d.BoundingBox;
import org.j3d.Collider;
import org.j3d.DepthState;
import org.j3d.Game;
import org.j3d.IO;
import org.j3d.TexturePipeline;
import org.j3d.TrianglePipeline;
import org.j3d.PipelineTriangleSelector;
import org.j3d.Texture;
import org.j3d.Parser;
import org.j3d.Utils;
import org.j3d.Collider.TriangleSelector;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Scene implements TriangleSelector {

    public static class Mesh {
        public final Scene scene;
        public final PipelineTriangleSelector selector;
        public final Vector3f position = new Vector3f();
        public float rotationDegrees = 0;
        public final float scale;
        public Animator animator = null;
        public boolean visible = true;

        private final Vector4f color = new Vector4f();

        public Mesh(Scene scene, TrianglePipeline pipeline, float scale) {
            this.scene = scene;
            selector = new PipelineTriangleSelector(pipeline);
            this.scale = scale;

            scene.updateVertices(selector.pipeline);

            for(Texture texture : ((TexturePipeline)pipeline).textures) {
                if(texture != null) {
                    texture.bind();
                    if(scene.textureLinear) {
                        texture.toLinear(scene.textureClampToEdge);
                    } else {
                        texture.toNearest(scene.textureClampToEdge);
                    }
                    texture.unBind();
                }
            }
        }

        public void setTransform() {
            selector.setTransform(position.x, position.y, position.z, 0, rotationDegrees, 0, scale);
        }

        public Mesh newInstance() {
            Mesh mesh = new Mesh(scene, selector.pipeline, scale);

            mesh.rotationDegrees = rotationDegrees;
            mesh.position.set(position);
            mesh.setTransform();

            return mesh;
        }
    }

    private static class MeshConfig {
        public final boolean collidable;
        public final boolean cameraCollidable;
        public final Vector4f color = new Vector4f(1, 1, 1, 1);

        public MeshConfig(boolean collidable, boolean cameraCollidable) {
            this.collidable = collidable;
            this.cameraCollidable = cameraCollidable;
        }
    }

    public final String name;
    public final String meshSet;
    public final Vector3f playerPosition = new Vector3f();
    public final Vector3f playerOffset = new Vector3f(100, 100, 100);
    public final Vector3f playerDirection = new Vector3f(1, 0, 0);
    public final Vector3f up = new Vector3f(0, 1, 0);
    public float playerDegrees1 = 0;
    public float playerDegrees2 = 0;
    public final float playerScale;
    public final float playerSpeed;
    public final float playerRadius;
    public final float scale;
    public final Vector4f backgroundColor = new Vector4f(0, 0, 0, 1);
    
    private final Hashtable<String, Vector<Mesh>> meshGroups = new Hashtable<>();
    private boolean enabled = true;
    private final Vector<Mesh> meshes = new Vector<>();
    private final Vector3f target = new Vector3f();
    private final Vector3f eye = new Vector3f();
    private int binds = 0;
    private Hashtable<String, MeshConfig> meshConfig = new Hashtable<>();
    private final HashSet<String> updated = new HashSet<>();
    private boolean insetTextureCoordinates = true;
    private boolean textureLinear = false;
    private boolean textureClampToEdge = false;
    private TexturePipeline playerPipeline;
    private TexturePipeline skyPipeline = null;

    public Scene(Game game, String name, boolean inDesign) throws Exception {
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
                Parser.parse(tokens, 7, playerDirection);
                playerScale = Parser.parse(tokens, 10, playerScale);
                playerSpeed = Parser.parse(tokens, 11, playerSpeed);
                playerRadius = Parser.parse(tokens, 12, playerRadius);
            } else if(tline.startsWith("texture ")) {
                textureLinear = Parser.parse(tokens, 1, false);
                textureClampToEdge = Parser.parse(tokens, 2, false);
                insetTextureCoordinates = Parser.parse(tokens, 3, true);
            } else if(tline.startsWith("scale ")) {
                scale = Parser.parse(tokens, 1, scale);
            } else if(tline.startsWith("background ")) {
                Parser.parse(tokens, 1, backgroundColor);
            } else if(tline.startsWith("mesh-cfg ")) {
                meshConfig.put(
                    tokens[1], 
                    new MeshConfig(
                        Parser.parse(tokens, 2, true),
                        Parser.parse(tokens, 3, true)
                    )
                );
            } else if(tline.startsWith("mesh ")) {
                boolean collidable = true;
                boolean cameraCollidable = true;
                Vector4f color = new Vector4f(1, 1, 1, 1);

                if(meshConfig.containsKey(tokens[1])) {
                    MeshConfig config = meshConfig.get(tokens[1]);

                    collidable = config.collidable;
                    cameraCollidable = config.cameraCollidable;
                    color.set(config.color);
                }

                TexturePipeline pipeline = game.getAssets().load(
                    IO.file(IO.file(IO.file("assets/meshes"), meshSet), tokens[1] + ".obj")
                    );
                Mesh mesh = new Mesh(this, pipeline, scale);

                mesh.selector.setEnabled(collidable);
                if(!cameraCollidable) {
                    mesh.selector.pipeline.setTriangleTag(2);
                }
                ((TexturePipeline)mesh.selector.pipeline).color.set(color);
                mesh.color.set(color);
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
        updateVertices(playerPipeline);

        if(!inDesign) {
            File file = IO.file(IO.file(IO.file("assets/meshes"), meshSet), "sky.obj");

            if(file.exists()) {
                skyPipeline = game.getAssets().load(file);
                updateVertices(skyPipeline);
            }
        }
    }

    public int getBinds() {
        return binds;
    }

    public int getMeshCount() {
        return meshes.size();
    }

    public Mesh meshAt(int i) {
        return meshes.get(i);
    }

    public void add(Mesh mesh) {
        String name = IO.fileNameWithOutExtension(mesh.selector.pipeline.getFile());

        mesh = mesh.newInstance();
        meshes.add(mesh);
        if(!meshGroups.containsKey(name)) {
            meshGroups.put(name, new Vector<>());
        }
        meshGroups.get(name).add(mesh);
    }

    public void remove(Mesh mesh) {
        String name = IO.fileNameWithOutExtension(mesh.selector.pipeline.getFile());

        meshes.remove(mesh);
        meshGroups.get(name).remove(mesh);
        if(meshGroups.get(name).isEmpty()) {
            meshGroups.remove(name);
        }
    }

    public void resetColor(Mesh mesh) {
        ((TexturePipeline)mesh.selector.pipeline).color.set(mesh.color);
    }

    public void render(Matrix4f projection, Matrix4f view, boolean fpsCamera) throws Exception {
        Enumeration<String> names = meshGroups.keys();

        if(fpsCamera) {
            playerPosition.add(playerDirection, target);
            view.identity().lookAt(playerPosition, target, up);
            eye.set(playerPosition);
        } else {
            playerPosition.add(playerOffset, eye);
            view.identity().lookAt(eye, playerPosition, up);
        }

        binds = 0;

        Utils.clear(backgroundColor.x, backgroundColor.y, backgroundColor.z, backgroundColor.w);

        if(skyPipeline != null) {
            Utils.setDepthState(DepthState.NONE);
            skyPipeline.setTransform(eye.x, eye.y, eye.z, 0, 0, 0, 1);
            skyPipeline.render();
            Utils.setDepthState(DepthState.READ_WRITE);
        }
        while(names.hasMoreElements()) {
            String name = names.nextElement();
            Vector<Mesh> group = meshGroups.get(name);

            group.get(0).selector.begin(projection, view);
            for(Mesh mesh : group) {
                if(mesh.visible) {
                    mesh.setTransform();
                    mesh.selector.render();
                }
            }
            group.get(0).selector.end();
            binds++;
        }
        if(!fpsCamera) {
            playerPipeline.getModel()
                .identity()
                .translate(playerPosition)
                .rotate(Utils.toRadians(playerDegrees1), 0, 1, 0)
                .rotate(Utils.toRadians(playerDegrees2), 0, 0, 1)
                .scale(playerScale);
            playerPipeline.render(projection, view);
        }
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
        Enumeration<String> keys = meshConfig.keys();

        b.append("# mesh-set name\n");
        b.append("mesh-set " + meshSet + "\n");
        b.append("\n");

        b.append("# player x y z off-x off-y off-z dir-x dir-y dir-z scale speed radius\n");
        b.append(
            "player " + 
            Parser.toString(playerPosition) + " " + 
            Parser.toString(playerOffset) + " " + 
            Parser.toString(playerDirection) + " " +
            playerScale + " " + 
            playerSpeed + " " + 
            playerRadius + "\n"
        );
        b.append("\n");

        b.append("# texture linear clamp-to-edge inset-texture-coordinates\n");
        b.append("texture " + textureLinear + " " + textureClampToEdge + " " + insetTextureCoordinates + "\n");
        b.append("\n");

        b.append("# scale s\n");
        b.append("scale " + scale + "\n");
        b.append("\n");

        b.append("# background r g b a\n");
        b.append("background-color " + Parser.toString(backgroundColor) + "\n");
        b.append("\n");

        b.append("# mesh-cfg name collidable camera-collidable r g b a\n");
        b.append("# ...\n");
        while(keys.hasMoreElements()) {
            String key = keys.nextElement();
            MeshConfig config = meshConfig.get(key);

            b.append(
                "mesh-cfg " + 
                key + " " + 
                config.collidable + " " + 
                config.cameraCollidable + " " + 
                Parser.toString(config.color) + " "  + "\n"
                );
        }
        b.append("\n");

        b.append("# mesh name x y z rotation-degrees\n");
        b.append("# ...\n");
        for(Mesh mesh : meshes) {
            b.append(
                "mesh " +
                IO.fileNameWithOutExtension(mesh.selector.pipeline.getFile()) + " " +
                mesh.position.x + " " +
                mesh.position.y + " " + 
                mesh.position.z + " " +
                mesh.rotationDegrees + "\n"
            );
        }
        b.append("\n");

        IO.writeAllBytes(b.toString().getBytes(), IO.file(IO.file("assets/scenes"), name + ".sh"));
    }

    private void updateVertices(TrianglePipeline pipeline) {
        String name = IO.fileNameWithOutExtension(pipeline.getFile());

        if(updated.contains(name)) {
            return;
        }
        updated.add(name);

        if(insetTextureCoordinates) {
            TexturePipeline texturePipeline = (TexturePipeline)pipeline;
            
            for(int i = 0; i != pipeline.getFaceCount(); i++) {
                float minx = Float.MAX_VALUE;
                float miny = Float.MAX_VALUE;
                float maxx = -minx;
                float maxy = -miny;
                int index = (int)pipeline.vertexAt(pipeline.faceVertexAt(i, 0), 5);
                int w = 1;
                int h = 1;

                if(index >= 0 && index < TexturePipeline.MAX_TEXTURES) {
                    Texture texture = texturePipeline.textures[index];

                    if(texture != null) {
                        w = texture.width;
                        h = texture.height;
                    }
                }

                for(int j = 0; j != pipeline.getFaceVertexCount(i); j++) {
                    int k = pipeline.faceVertexAt(i, j);
                    float x = pipeline.vertexAt(k, 3);
                    float y = pipeline.vertexAt(k, 4);

                    minx = Math.min(x, minx);
                    miny = Math.min(y, miny);
                    maxx = Math.max(x, maxx);
                    maxy = Math.max(y, maxy);
                }

                int tilew = (int)(maxx * w - minx * w);
                int tileh = (int)(maxy * h - miny * h);
                
                if(tilew > 0 && tileh > 0) {
                    for(int j = 0; j != pipeline.getFaceVertexCount(i); j++) {
                        int k = pipeline.faceVertexAt(i, j);
                        float x = pipeline.vertexAt(k, 3);
                        float y = pipeline.vertexAt(k, 4);

                        if(Math.abs(x - minx) > Math.abs(x - maxx)) {
                            x -= 1.0f / w * 0.05f;
                        } else {
                            x += 1.0f / w * 0.05f;
                        }
                        if(Math.abs(y - miny) > Math.abs(y - maxy)) {
                            y -= 1.0f / h * 0.05f;
                        } else {
                            y += 1.0f / h * 0.05f;
                        }
                        pipeline.setVertexAt(k, 3, x);
                        pipeline.setVertexAt(k, 4, y);
                    }
                }
            }
        }

        Vector3f center = new Vector3f();
        BoundingBox bounds = pipeline.getBounds();

        bounds.max.add(bounds.min, center).mul(0.5f);
        for(int i = 0; i != pipeline.getVertexCount(); i++) {
            float x = pipeline.vertexAt(i, 0);
            float y = pipeline.vertexAt(i, 1);
            float z = pipeline.vertexAt(i, 2);

            x -= center.x;
            z -= center.z;

            pipeline.setVertexAt(i, 0, x);
            pipeline.setVertexAt(i, 1, y);
            pipeline.setVertexAt(i, 2, z);
        }
        bounds.max.sub(center.x, 0, center.z);
        bounds.min.sub(center.x, 0, center.z);

        pipeline.bufferVertices();
    }
}
