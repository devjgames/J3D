package org.j3d.demos;

import java.io.File;
import java.util.Vector;

import org.j3d.BoundingBox;
import org.j3d.Game;
import org.j3d.IO;
import org.j3d.LightPipeline;
import org.j3d.Parser;
import org.j3d.Triangle;
import org.j3d.UIManager;
import org.j3d.Utils;
import org.j3d.demos.Scene.Mesh;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class Editor extends Demo {

    private Scene scene;
    private Mesh mesh;
    private Vector<String> sceneNames = new Vector<>();
    private Vector<String> meshNames = new Vector<>();
    private String[] modes = new String[] {
        "ZOOM",
        "ROT",
        "PAN",
        "+",
        "-"
    };
    private int mode;
    private int selMesh;
    private int selScene;
    private boolean down;
    private boolean down2;
    private boolean showScenes;
    private boolean showMeshes;
    private int snap;
    private boolean resetSnap;
    private Vector3f f = new  Vector3f();
    private Vector3f u = new Vector3f();
    private Vector3f r = new Vector3f();
    private Vector3f origin = new Vector3f();
    private Vector3f direction = new Vector3f();
    private Vector3f p = new Vector3f();
    private float[] time = new float[1];
    private BoundingBox bounds = new BoundingBox();
    private Triangle triangle = new Triangle();


    @Override
    public void init(App app) throws Exception {
        File[] files = IO.file("assets/scenes").listFiles();

        sceneNames.clear();
        for(File file : files) {
            if(IO.extension(file).equals(".sh")) {
                sceneNames.add(IO.fileNameWithOutExtension(file));
            }
        }
        sceneNames.sort((a, b) -> a.compareTo(b));

        scene = null;
        mesh = null;
        mode = 0;
        selMesh = -1;
        selScene = -1;
        down = false;
        down2 = false;
        showScenes = false;
        showMeshes = false;
        snap = 8;
        resetSnap = true;
    }

    @Override
    public boolean run(App app) throws Exception {
        Game game = app.getGame();
        UIManager manager = app.getManager();
        Object result;
        int gap = 0;

        projection.identity().perspective(Utils.toRadians(60), game.getAspectRatio(), 1, 50000);
        if(scene != null) {
            scene.render(projection, view, false);
            if(mode == 3) {
                int w = game.getRenderTargetWidth();
                int h = game.getRenderTargetHeight();
                int x = game.getMouseX();
                int y = h - game.getMouseY() - 1;

                Utils.unProject(x, y, 0, 0, 0, w, h, projection, view, origin);
                Utils.unProject(x, y, 1, 0, 0, w, h, projection, view, direction);

                direction.sub(origin).normalize();

                float t = -origin.dot(0, 1, 0) / direction.dot(0, 1, 0);
                int s = Math.max(1, snap);

                direction.mul(t, p);
                origin.add(p, p);

                mesh.selector.pipeline.lights.clear();
                mesh.selector.pipeline.ambientColor.set(1, 1, 1, 1);
                mesh.position.x = (int)Math.round(p.x / s) * s;
                mesh.position.z = (int)Math.round(p.z / s) * s;
                mesh.setTransform();
                mesh.selector.render(projection, view);
            }
        } else {
            Utils.clear(0, 0, 0, 1);
        }

        game.getSpritePipeline().begin(game.getRenderTargetWidth(), game.getRenderTargetHeight());
        pushInfo(app, null);
        game.getSpritePipeline().end();
        manager.begin();
        manager.moveTo(10, 10);
        if(manager.label("Editor-load-label", 0, "Load", 0, showScenes)) {
            showScenes = !showScenes;
            showMeshes = false;
            selScene = -1;
        }
        if(scene != null) {
            if(manager.label("Editor-save-label", 5, "Save", 0, false)) {
                scene.save();
            }
            if(manager.label("Editor-mesh-label", 5, "Mesh", 0, showMeshes)) {
                showMeshes = !showMeshes;
                showScenes = false;
            }
            for(int i = 0; i != modes.length; i++) {
                if(manager.label("Editor-mode-" + i + "-label", 5, modes[i], 0, i == mode)) {
                    mode = i;
                }
            }
            manager.addRow(5);
            if(showMeshes) {
                if((result = manager.list("Editor-mesh-list", 0, meshNames, 20, 8, selMesh)) != null) {
                    LightPipeline pipeline = game.getAssets().load(
                        IO.file(IO.file(IO.file("assets/meshes"), scene.meshSet), meshNames.get((Integer)result) + ".obj")
                        );
                    mesh = new Mesh(pipeline, scene.scale);
                    showMeshes = false;

                    Scene.zeroCenter(mesh.selector.pipeline);
                }
                selMesh = -2;
                gap = 5;
            }  
        } else {
            manager.addRow(5);
        }
        if(showScenes) {
            if((result = manager.list("Editor-scene-list", 0, sceneNames, 20, 8, selScene)) != null) {
                game.getAssets().clear();
                Utils.clearConsole();
                scene = new Scene(game, sceneNames.get((Integer)result));
                selMesh = 0;

                File[] list = IO.file(IO.file("assets/meshes"), scene.meshSet).listFiles();

                meshNames.clear();
                for(File file : list) {
                    if(IO.extension(file).equals(".obj")) {
                        meshNames.add(IO.fileNameWithOutExtension(file));
                    }
                }
                meshNames.sort((a, b) -> a.compareTo(b));

                LightPipeline pipeline = game.getAssets().load(
                    IO.file(IO.file(IO.file("assets/meshes"), scene.meshSet), meshNames.get(selMesh) + ".obj")
                    );
                mesh = new Mesh(pipeline, scene.scale);
                showScenes = false;

                Scene.zeroCenter(mesh.selector.pipeline);
            }
            selScene = -2;
            gap = 5;
        }
        if((result = manager.textField("Editor-snap-field", gap, "Snap", "" + snap, resetSnap, 6)) != null) {
            snap = Parser.parse(((String)result).split("\\s+"), 0, 1);
        }
        resetSnap = false;
        boolean handled = manager.end();
        game.nextFrame();

        if(!handled && scene != null) {
            if(game.isButtonDown(0)) {
                if(mode == 0) {
                    float length = scene.playerOffset.length();

                    scene.playerOffset.normalize().mul(length + game.getDeltaY());
                } else if(mode == 1) {
                    Utils.rotateOffsetAndUp(scene.playerOffset, scene.up, game);
                } else if(mode == 2) {
                    float dx = game.getDeltaX();
                    float dy = game.getDeltaY();
                    float dl = Vector2f.length(dx, dy);

                    f.set(scene.playerOffset).mul(1, 0, 1);
                    if(f.length() > 0.0000001 && dl > 0.1) {
                        f.normalize().cross(u.set(0, 1, 0), r).mul(dx);
                        f.mul(dy).add(r);
                        scene.playerPosition.add(f);
                    }
                } else if(!down) {
                    if(mode == 3) {
                        int s = Math.max(1, snap);

                        mesh.position.x = (int)Math.round(mesh.position.x / s) * s;
                        mesh.position.z = (int)Math.round(mesh.position.z / s) * s;
                        scene.add(mesh);
                    } else  if(mode == 4) {
                        int w = game.getRenderTargetWidth();
                        int h = game.getRenderTargetHeight();
                        int x = game.getMouseX();
                        int y = h - game.getMouseY() - 1;
                        Mesh hit = null;

                        Utils.unProject(x, y, 0, 0, 0, w, h, projection, view, origin);
                        Utils.unProject(x, y, 1, 0, 0, w, h, projection, view, direction);

                        direction.sub(origin).normalize();
                        time[0] = Float.MAX_VALUE;

                        for(int i = 0; i != scene.getMeshCount(); i++) {
                            Mesh iMesh = scene.meshAt(i);
                            float t = time[0];

                            bounds.min.set(iMesh.selector.pipeline.getBounds().min);
                            bounds.max.set(iMesh.selector.pipeline.getBounds().max);
                            bounds.transform(iMesh.selector.model);
                            bounds.min.sub(1, 1, 1);
                            bounds.max.add(1, 1, 1);
                            time[0] = Float.MAX_VALUE;
                            if(bounds.intersects(origin, direction, time)) {
                                time[0] = t;
                                iMesh.selector.pipeline.model.set(iMesh.selector.model);
                                for(int j = 0; j != iMesh.selector.pipeline.getTriangleCount(); j++) {
                                    iMesh.selector.pipeline.triangleAt(j, triangle);
                                    if(triangle.intersects(origin, direction, 0, time)) {
                                        hit = iMesh;
                                    }
                                }
                            } else {
                                time[0] = t;
                            }
                        }
                        if(hit != null) {
                            scene.remove(hit);
                        }
                    }
                }
                down = true;
            } else {
                down = false;
            }
            if(game.isButtonDown(1)) {
                if(!down2) {
                    mesh.rotationDegrees = (mesh.rotationDegrees + 90) % 360;
                }
                down2 = true;
            } else {
                down2 = false;
            }
        }
        return !game.isKeyDown(GLFW.GLFW_KEY_ESCAPE);
    }
    
}
