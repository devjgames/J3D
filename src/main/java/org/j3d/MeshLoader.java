package org.j3d;

import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class MeshLoader implements AssetLoader {

    @Override
    public Object load(File file, AssetManager assets) throws Exception {
        File directory = file.getParentFile();
        Mesh mesh = new Mesh();
        String[] lines = new String(IO.readAllBytes(file)).split("\\n+");
        Vector<Vector3f> vList = new Vector<>();
        Vector<Vector2f> tList = new Vector<>();
        Vector<Vector3f> nList = new Vector<>();
        Hashtable<String, MeshPart> parts = new Hashtable<>();
        Hashtable<String, String> textures = new Hashtable<>();
        MeshPart part = null;
        
        for(String line : lines) {
            String tLine = line.trim();
            String[] tokens = tLine.split("\\s+");

            if(tLine.startsWith("mtllib ")) {
                String[] mLines = new String(IO.readAllBytes(IO.file(directory, tLine.substring(6).trim()))).split("\\n+");
                String name = null;

                for(String mLine : mLines) {
                    String tmLine = mLine.trim();

                    if(tmLine.startsWith("newmtl ")) {
                        name = tmLine.substring(6).trim();
                    } else if(tmLine.startsWith("map_Kd ")) {
                        textures.put(name, tmLine.substring(6).trim());
                    }
                }
            } else if(tLine.startsWith("usemtl ")) {
                String name = tLine.substring(6).trim();
                String texture = "";

                if(textures.containsKey(name)) {
                    texture = textures.get(name);
                }
                
                if(!parts.containsKey(texture)) {
                    PixelLightMaterial material = assets.getResources().manage(new PixelLightMaterial());

                    part = new MeshPart(mesh, 8);
                    if(!texture.isEmpty()) {
                        material.texture = assets.load(IO.file(directory, texture));
                    }
                    part.material = material;
                    mesh.addMeshPart(part);
                    parts.put(texture, part);
                } else {
                    part = parts.get(texture);
                }
            } else if(tLine.startsWith("v ")) {
                vList.add(Parser.parse(tokens, 1, new Vector3f()));
            } else if(tLine.startsWith("vt ")) {
                Vector2f t = Parser.parse(tokens, 1, new Vector2f());

                t.y = 1 - t.y;
                tList.add(t);
            } else if(tLine.startsWith("vn ")) {
                nList.add(Parser.parse(tokens, 1, new Vector3f()));
            } else if(tLine.startsWith("f ")) {
                int[] indices = new int[tokens.length - 1];

                for(int i = 1, bv = part.getVertexCount(), j = 0; i != tokens.length; i++, bv++, j++) {
                    String[] iTokens = tokens[i].split("/");
                    int vi = Integer.parseInt(iTokens[0]) - 1;
                    int ti = Integer.parseInt(iTokens[1]) - 1;
                    int ni = Integer.parseInt(iTokens[2]) - 1;
                    Vector3f v = vList.get(vi);
                    Vector2f t = tList.get(ti);
                    Vector3f n = nList.get(ni);

                    part.push(v.x, v.y, v.z);
                    part.push(t.x, t.y);
                    part.push(n.x, n.y, n.z);

                    indices[j] = bv;
                }
                part.push(indices);
            }
        }

        for(MeshPart p : mesh) {
            p.trim();
            p.bufferIndices();
            p.bufferVertices(false);
            p.calcBounds();
        }
        mesh.calcBounds();

        return mesh;
    }
    
}
