package org.j3d;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class MeshData {
    
    public final Vector<MeshDataPart> parts = new Vector<>();

    public MeshData() {
    }

    public MeshData(File file) throws IOException {
        File directory = file.getParentFile();
        String[] lines = new String(IO.readAllBytes(file)).split("\\n+");
        Vector<Vector3f> vList = new Vector<>();
        Vector<Vector2f> tList = new Vector<>();
        Vector<Vector3f> nList = new Vector<>();
        Hashtable<String, MeshDataPart> keyedParts = new Hashtable<>();
        Hashtable<String, String> textures = new Hashtable<>();
        MeshDataPart part = null;
        
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
                
                if(!keyedParts.containsKey(texture)) {
                    part = new MeshDataPart((texture.isEmpty()) ? null : IO.file(directory, texture));
                    keyedParts.put(texture, part);
                    parts.add(part);
                } else {
                    part = keyedParts.get(texture);
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

                if(part == null) {
                    part = new MeshDataPart(null);
                    keyedParts.put("", part);
                    parts.add(part);
                }

                for(int i = 1, bv = part.vertices.size(), j = 0; i != tokens.length; i++, bv++, j++) {
                    String[] iTokens = tokens[i].split("/");
                    int vi = Integer.parseInt(iTokens[0]) - 1;
                    int ti = Integer.parseInt(iTokens[1]) - 1;
                    int ni = Integer.parseInt(iTokens[2]) - 1;
                    MeshDataVertex vertex = new MeshDataVertex();

                    vertex.position.set(vList.get(vi));
                    vertex.textureCoordinate.set(tList.get(ti));
                    vertex.normal.set(nList.get(ni));

                    part.vertices.add(vertex);

                    indices[j] = bv;
                }
                part.faces.add(indices);
            }
        }
    }
}
