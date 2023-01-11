package org.j3d;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class MeshData {
    
    public final Vector<MeshDataPart> parts = new Vector<>();

    public static MeshData load(File file) throws IOException {
        MeshData mesh = new MeshData();
        String[] lines = new String(IO.readAllBytes(file)).split("\\n+");
        Vector<Vector3f> vList = new Vector<>(1000);
        Vector<Vector2f> tList = new Vector<>(1000);
        Vector<Vector3f> nList = new Vector<>(1000);
        Hashtable<String, File> keyedTextures = new Hashtable<>();
        Hashtable<String, MeshDataPart> keyedParts = new Hashtable<>();
        String partKey = "";

        for(String line : lines) {
            String tLine = line.trim();
            String[] tokens = tLine.split("\\s+");

            if(tLine.startsWith("mtllib ")) {
                File mtlFile = IO.file(file.getParentFile(), tLine.substring(6).trim());

                if(mtlFile.exists()) {
                    String[] mtlLines = new String(IO.readAllBytes(mtlFile)).split("\\n+");
                    String name = null;

                    for(String mtlLine : mtlLines) {
                        String tmtlLine = mtlLine.trim();

                        if(tmtlLine.startsWith("newmtl ")) {
                            name = tmtlLine.substring(6).trim();
                        } else if(tmtlLine.startsWith("map_Kd ")) {
                            keyedTextures.put(name, IO.file(file.getParentFile(), tmtlLine.substring(6).trim()));
                        }
                    }
                }
            } else if(tLine.startsWith("usemtl ")) {
                String name = tLine.substring(6).trim();
                File texture = keyedTextures.get(name);
                
                partKey = "";
                if(texture != null) {
                    partKey = texture.getPath();
                }
                if(!keyedParts.containsKey(partKey)) {
                    mesh.parts.add(new MeshDataPart());
                    mesh.parts.lastElement().texture = texture;
                    keyedParts.put(partKey, mesh.parts.lastElement());
                }
            } else if(tLine.startsWith("v ")) {
                vList.add(Parser.parse(tokens, 1, new Vector3f()));
            } else if(tLine.startsWith("vt ")) {
                tList.add(Parser.parse(tokens, 1, new Vector2f()));
                tList.lastElement().y = 1 - tList.lastElement().y;
            } else if(tLine.startsWith("vn ")) {
                nList.add(Parser.parse(tokens, 1, new Vector3f(0, 1 ,0)));
            } else if(tLine.startsWith("f ")) {
                int[] indices = new int[tokens.length - 1];

                if(!keyedParts.containsKey(partKey)) {
                    mesh.parts.add(new MeshDataPart());
                    keyedParts.put(partKey, mesh.parts.lastElement());
                }

                MeshDataPart part = keyedParts.get(partKey);
                int count = part.vertices.size();

                for(int i = 1; i != tokens.length; i++) {
                    String[] iTokens = tokens[i].split("/");
                    int vI = Integer.parseInt(iTokens[0]) - 1;
                    int tI = Integer.parseInt(iTokens[1]) - 1;
                    int nI = Integer.parseInt(iTokens[2]) - 1;
                    MeshDataVertex v = new MeshDataVertex();

                    v.position.set(vList.get(vI));
                    v.textureCoordinate.set(tList.get(tI));
                    v.normal.set(nList.get(nI));

                    part.vertices.add(v);

                    indices[i - 1] = count++;
                }
                part.faces.add(indices);
            }
        }
        return mesh;
    }
}
