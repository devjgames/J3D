package org.j3d;

import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class PipelineLoader implements AssetLoader {
    
    @Override
    public Object load(File file, AssetManager assets) throws Exception {
        TexturePipeline pipeline = new TexturePipeline(file);
        String[] lines = new String(IO.readAllBytes(file)).split("\\n+");
        Vector<Vector3f> vList = new Vector<>(1000);
        Vector<Vector2f> tList = new Vector<>(1000);
        Hashtable<String, Integer> keyedTextures = new Hashtable<>();
        int vCount = 0;
        int texture = -1;

        for(String line : lines) {
            String tLine = line.trim();
            String[] tokens = tLine.split("\\s+");

            if(tLine.startsWith("mtllib ")) {
                File mtlFile = IO.file(file.getParentFile(), tLine.substring(6).trim());

                if(mtlFile.exists()) {
                    String[] mtlLines = new String(IO.readAllBytes(mtlFile)).split("\\n+");
                    String name = null;
                    int index = 0;

                    for(String mtlLine : mtlLines) {
                        String tmtlLine = mtlLine.trim();

                        if(tmtlLine.startsWith("newmtl ")) {
                            name = tmtlLine.substring(6).trim();
                        } else if(tmtlLine.startsWith("map_Kd ")) {
                            keyedTextures.put(name, index);
                            if(index < TexturePipeline.MAX_TEXTURES) {
                                pipeline.textures[index] = assets.load(IO.file(file.getParentFile(), tmtlLine.substring(6).trim()));
                            }
                            index++;
                        }
                    }
                }
            } else if(tLine.startsWith("usemtl ")) {
                String name = tLine.substring(6).trim();
                
                texture = keyedTextures.get(name);
            } else if(tLine.startsWith("v ")) {
                vList.add(Parser.parse(tokens, 1, new Vector3f()));
            } else if(tLine.startsWith("vt ")) {
                tList.add(Parser.parse(tokens, 1, new Vector2f()));
                tList.lastElement().y = 1 - tList.lastElement().y;
            } else if(tLine.startsWith("f ")) {
                int[] indices = new int[tokens.length - 1];

                for(int i = 1; i != tokens.length; i++) {
                    String[] iTokens = tokens[i].split("/");
                    int vI = Integer.parseInt(iTokens[0]) - 1;
                    int tI = Integer.parseInt(iTokens[1]) - 1;
                    Vector3f v = vList.get(vI);
                    Vector2f t = tList.get(tI);

                    pipeline.pushVertex(v.x, v.y, v.z, t.x, t.y, texture);

                    indices[i - 1] = vCount++;
                }
                pipeline.pushFace(indices);
            }
        }
        pipeline.buffer();

        return pipeline;
    }
}
