package org.j3d;

import java.io.File;
import java.util.Vector;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class MeshLoader implements AssetLoader {
    
    @Override
    public Object load(File file, AssetManager assets) throws Exception {
        LightPipeline mesh = new LightPipeline(file);
        String[] lines = new String(IO.readAllBytes(file)).split("\\n+");
        Vector<Vector3f> vList = new Vector<>(1000);
        Vector<Vector2f> tList = new Vector<>(1000);
        Vector<Vector3f> nList = new Vector<>(1000);
        int vCount = 0;

        for(String line : lines) {
            String tLine = line.trim();
            String[] tokens = tLine.split("\\s+");

            if(tLine.startsWith("v ")) {
                vList.add(Parser.parse(tokens, 1, new Vector3f()));
            } else if(tLine.startsWith("vt ")) {
                tList.add(Parser.parse(tokens, 1, new Vector2f()));
                tList.lastElement().y = 1 - tList.lastElement().y;
            } else if(tLine.startsWith("vn ")) {
                nList.add(Parser.parse(tokens, 1, new Vector3f()));
            } else if(tLine.startsWith("f ")) {
                int[] indices = new int[tokens.length - 1];

                for(int i = 1; i != tokens.length; i++) {
                    String[] iTokens = tokens[i].split("/");
                    int vI = Integer.parseInt(iTokens[0]) - 1;
                    int tI = Integer.parseInt(iTokens[1]) - 1;
                    int nI = Integer.parseInt(iTokens[2]) - 1;
                    Vector3f v = vList.get(vI);
                    Vector2f t = tList.get(tI);
                    Vector3f n = nList.get(nI);

                    mesh.pushVertex(v.x, v.y, v.z, t.x, t.y, n.x, n.y, n.z);

                    indices[i - 1] = vCount++;
                }
                mesh.pushFace(indices);
            }
        }
        mesh.buffer();

        return mesh;
    }
}
