package org.j3d;

import java.io.File;
import java.util.Vector;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;

public class NodeLoader implements AssetLoader {

    @Override
    public Object load(File file, AssetManager assets) throws Exception {
        String[] lines = new String(IO.readAllBytes(file)).split("\\n+");
        Vector<Vec4> vList = new Vector<>();
        Vector<Vec2> tList = new Vector<>();
        Vector<Vec3> nList = new Vector<>();
        Hashtable<String, Vector<Vertex>> keyedVertices = new Hashtable<>();
        Hashtable<String, Vector<Integer>> keyedIndices = new  Hashtable<>();
        Hashtable<String, Vector<Vector<Integer>>> keyedPolygons = new Hashtable<>();
        Hashtable<String, String> materials = new Hashtable<>();
        HashSet<String> textures = new HashSet<>();
        String material = "";

        for (String line : lines) {
            String tLine = line.trim();
            String[] tokens = tLine.split("\\s+");
            if(tLine.startsWith("mtllib ")) {
                loadMaterials(new File(file.getParent(), tLine.substring(6).trim()), materials);
            } else if(tLine.startsWith("usemtl ")) {
                material = materials.get(tLine.substring(6).trim());
                if(!keyedVertices.containsKey(material)) {
                    textures.add(material);
                    keyedVertices.put(material, new Vector<>());
                    keyedIndices.put(material, new Vector<>());
                    keyedPolygons.put(material, new Vector<>());
                }
            } else if (tLine.startsWith("v ")) {
                vList.add(new Vec4(new Vec3().parse(tLine.substring(1).trim()), 1));
            } else if (tLine.startsWith("vt ")) {
                tList.add(new Vec2(new Vec2().parse(tLine.substring(2).trim())));
                tList.lastElement().y = 1 - tList.lastElement().y;
            } else if (tLine.startsWith("vn ")) {
                nList.add(new Vec3().parse(tLine.substring(2).trim()));
            } else if (tLine.startsWith("f ")) {
                if(!keyedVertices.containsKey(material)) {
                    keyedVertices.put(material, new Vector<>());
                    keyedIndices.put(material, new Vector<>());
                    keyedPolygons.put(material, new Vector<>());
                }
                Vector<Vertex> vertices = keyedVertices.get(material);
                Vector<Integer> indices = keyedIndices.get(material);
                Vector<Vector<Integer>> polygons = keyedPolygons.get(material);
                int bV = vertices.size();
                int tris = tokens.length - 3;
                polygons.add(new Vector<>());
                for (int i = 1; i != tokens.length; i++) {
                    String[] iTokens = tokens[i].split("[/]+");
                    int vI = Integer.parseInt(iTokens[0]) - 1;
                    int tI = Integer.parseInt(iTokens[1]) - 1;
                    int nI = Integer.parseInt(iTokens[2]) - 1;
                    Vertex vertex = new Vertex(vList.get(vI), tList.get(tI), new Vec2(0, 0), new Vec4(1, 1, 1, 1), nList.get(nI));
                    vertices.add(vertex);
                    polygons.lastElement().add(bV + i - 1);
                }
                for (int i = 0; i != tris; i++) {
                    indices.add(bV);
                    indices.add(bV + i + 1);
                    indices.add(bV + i + 2);
                }
            }
        }
        Enumeration<String> keys = keyedVertices.keys();
        Vector<String> sortedKeys = new Vector<>();
        while(keys.hasMoreElements()) {
            sortedKeys.add(keys.nextElement());
        }
        sortedKeys.sort((a, b) -> a.compareTo(b));
        Node root = new Node();
        root.name = IO.fileNameWithOutExtension(file);
        for(String key : sortedKeys) {
            Vector<Integer> vIndices = keyedIndices.get(key);
            Vector<Vector<Integer>> vPolygons = keyedPolygons.get(key);
            if(vPolygons.size() != 0) {
                Vector<Vertex> vertices = keyedVertices.get(key);
                int[] indices = new int[vIndices.size()];
                int[][] polygons = new int[vPolygons.size()][];
                for(int i = 0; i != indices.length; i++) {
                    indices[i] = vIndices.get(i);
                }
                for(int i = 0; i != polygons.length; i++) {
                    polygons[i] = new int[vPolygons.get(i).size()];
                    for(int j = 0; j != polygons[i].length; j++) {
                        polygons[i][j] = vPolygons.get(i).get(j);
                    }
                }
                Node node = new Node();
                AABB bounds = new AABB();

                for(Vertex v : vertices) {
                    bounds.add(new Vec3(v.position));
                }
                Vec3 center = new Vec3();

                bounds.center(center);
                center.neg();
                
                for(Vertex v : vertices) {
                    v.position.add(center.x, center.y, center.z, 0);
                }
                center.neg();

                node.position.set(center);

                node.renderable = new Mesh(vertices.toArray(new Vertex[vertices.size()]), indices, polygons);
                if(textures.contains(key)) {
                    node.texture = assets.load(IO.file(key));
                    node.name = IO.fileNameWithOutExtension(node.texture.file);
                }
                root.add(node);
            }
        }
        return root;
    }

    private void loadMaterials(File file, Hashtable<String, String> materials) throws Exception {
        String[] lines = new String(IO.readAllBytes(file)).split("\\n+");
        String name = null;

        for(String line : lines) {
            String tLine = line.trim();
            if(tLine.startsWith("newmtl ")) {
                name = tLine.substring(6).trim();
            } else if(tLine.startsWith("map_Kd ")) {
                materials.put(name, new File(file.getParent(), tLine.substring(6).trim()).getPath());
            }
        }
    }
}
