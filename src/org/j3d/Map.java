package org.j3d;

import java.util.Vector;
import java.io.*;
import java.util.Hashtable;

public class Map {
    
    public final Vector<Entity> entities = new Vector<>();
    public int lightMapWidth = 128;
    public int lightMapHeight = 128;
    public File loadFile = null;

    private LightMapper lightMapper;
    private Scene scene = null;
    private File file = null;

    private Map(LightMapper lightMapper) {
        this.lightMapper = lightMapper;
    }

    public Scene scene() {
        return scene;
    }

    public File getFile() {
        return file;
    }

    public void init(Game game) throws Exception {
        scene.camera.calcTransforms(game.aspectRatio());
        scene.root.calcBoundsAndTransform(scene.camera);
        for(Entity entity : entities) {
            entity.init(game, scene, entities);
            entity.init();
        }
        scene.camera.calcTransforms(game.aspectRatio());
        scene.root.calcBoundsAndTransform(scene.camera);
        for(Entity entity : entities) {
            entity.start();
        }
    }
    
    public void renderSprites() throws Exception {
        for(Entity entity : entities) {
            entity.renderSprites();
        }
    }

    public void update() throws Exception {
        for(Entity entity : entities) {
            entity.update();
            loadFile = entity.loadFile();
        }
    }

    public void light(Game game, boolean deleteLightMap) throws Exception {
        lightMapper.light(new File(IO.file("maps"), IO.fileNameWithOutExtension(file) + ".png"), lightMapWidth, lightMapHeight, game, scene, deleteLightMap);
    }

    public void save(Game game) throws Exception {
        StringBuilder b = new StringBuilder(1000);
        scene.camera.calcTransforms(game.aspectRatio());

        b.append("map " + file.getPath() + "\n");
        b.append("lightMap "  + lightMapWidth + " " + lightMapHeight + "\n");
        b.append("camera " + scene.camera.eye + " " + scene.camera.target + " " + scene.camera.up + "\n");

        for(Node node : scene.root.childAt(0)) {
            b.append("material " + node.name + " ");
            b.append(node.getMesh().color + " ");
            b.append(node.ambientColor + " ");
            b.append(node.diffuseColor + " ");
            b.append(node.castsShadow + " ");
            b.append(node.receivesShadow + " ");
            b.append(node.blendEnabled + " ");
            b.append(node.additiveBlend + " ");
            b.append(node.zOrder + " ");
            b.append(node.cullState + "\n");
        }
        for(Node node : scene.root.childAt(1)) {
            if(node.isLight) {
                b.append("light " + node.position + " ");
                b.append(node.lightRadius + " ");
                b.append(node.lightColor + " ");
                b.append(node.lightSampleCount + " ");
                b.append(node.lightSampleRadius + "\n");
            }
        }
        for(Entity entity : entities) {
            Utils.append(entity, "entity", b);
        }
        IO.writeAllBytes(b.toString().getBytes(), new File(new File("maps"), IO.fileNameWithOutExtension(file) + ".txt"));
    }

    public static Map load(File file, LightMapper lightMapper, Game game) throws Exception {
        Map map = null;
        String[] lines = new String(IO.readAllBytes(file)).split("\\n+");
        Hashtable<String, Node> keyedNodes = new Hashtable<>();
        Entity entity = null;

        for(String line : lines) {
            String tLine = line.trim();
            String[] tokens = tLine.split("\\s+");
            if(tLine.startsWith("map ")) {
                File objFile = IO.file(tLine.substring(3).trim());
                map = new Map(lightMapper);
                map.file = objFile;
                map.scene = new Scene();
                map.scene.root.add(game.assets().load(objFile));
                for(Node node : map.scene.root.childAt(0)) {
                    node.lightMapEnabled = true;
                    keyedNodes.put(node.name, node);
                }
                map.scene.root.add(new Node());
            } else if(tLine.startsWith("lightMap ")) {
                map.lightMapWidth = Integer.parseInt(tokens[1]);
                map.lightMapHeight = Integer.parseInt(tokens[2]);
            } else if(tLine.startsWith("camera ")) {
                map.scene.camera.eye.parse(tokens, 1);
                map.scene.camera.target.parse(tokens, 4);
                map.scene.camera.up.parse(tokens, 7);
            } else if(tLine.startsWith("material ")) {
                if(keyedNodes.containsKey(tokens[1])) {
                    Node node = keyedNodes.get(tokens[1]);
                    Mesh mesh = node.getMesh();
                    mesh.color.parse(tokens, 2);
                    for(int i = 0; i != mesh.vertexCount(); i++) {
                        mesh.getVertex(i).color.set(mesh.color);
                    }
                    node.ambientColor.parse(tokens, 6);
                    node.diffuseColor.parse(tokens, 10);
                    node.castsShadow = Boolean.parseBoolean(tokens[14]);
                    node.receivesShadow = Boolean.parseBoolean(tokens[15]);
                    node.blendEnabled = Boolean.parseBoolean(tokens[16]);
                    node.depthWriteEnabled = !node.blendEnabled;
                    node.additiveBlend = Boolean.parseBoolean(tokens[17]);
                    node.zOrder = Integer.parseInt(tokens[18]);
                    node.cullState = CullState.valueOf(tokens[19]);
                }
            } else if(tLine.startsWith("light ")) {
                Node node = new Node();
                node.isLight = true;
                node.position.parse(tokens, 1);
                node.lightRadius = Float.parseFloat(tokens[4]);
                node.lightColor.parse(tokens, 5);
                node.lightSampleCount = Integer.parseInt(tokens[9]);
                node.lightSampleRadius = Float.parseFloat(tokens[10]);
                map.scene.root.childAt(1).add(node);
            } else if(tLine.startsWith("entity ")) {
                try {
                    entity = (Entity)EntityFactory.newInstance().findClass(tokens[1]).getConstructors()[0].newInstance();
                    map.entities.add(entity);
                } catch(Exception ex) {
                    entity = null;
                    ex.printStackTrace();
                }
            } else if(tLine.startsWith("property ") && entity != null) {
                try {
                    Utils.parse(entity, tokens, line);
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return map;
    }
}
