package org.j3d;

import java.io.File;
import java.util.Hashtable;
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
        Hashtable<String, Vector3f> colors = new Hashtable<>();
        Vector3f color = new Vector3f(1, 1, 1);

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
                        } else if(tmtlLine.startsWith("Kd ")) {
                            colors.put(name, Parser.parse(tmtlLine.split("\\s+"), 1, new Vector3f(1, 1, 1)));
                        } else if(tmtlLine.startsWith("map_Kd ")) {
                            File texFile = IO.file(file.getParentFile(), tmtlLine.substring(6).trim());

                            mesh.texture = assets.load(texFile);
                        }
                    }
                }
            } else if(tLine.startsWith("usemtl ")) {
                String key = tLine.substring(6).trim();


                if(colors.containsKey(key)) {
                    color = colors.get(key);
                    mesh.vertexColorEnabled = true;

                    Log.log(1, "setting obj mesh vertex color enabled = true");
                }
            } else if(tLine.startsWith("v ")) {
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

                    mesh.pushVertex(v.x, v.y, v.z, t.x, t.y, n.x, n.y, n.z, color.x, color.y, color.z);

                    indices[i - 1] = vCount++;
                }
                mesh.pushFace(indices);
            }
        }
        mesh.buffer();

        return mesh;
    }
}
