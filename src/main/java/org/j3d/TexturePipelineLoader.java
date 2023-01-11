package org.j3d;

import java.io.File;

public class TexturePipelineLoader implements AssetLoader {
    
    @Override
    public Object load(File file, AssetManager assets) throws Exception {
        TexturePipeline pipeline = new TexturePipeline(file);
        MeshData mesh = MeshData.load(file);
        int index = 0;

        for(MeshDataPart part : mesh.parts) {
            int i = -1;

            if(part.texture != null && index != TexturePipeline.MAX_TEXTURES) {
                i = index;
                pipeline.textures[index++] = assets.load(part.texture);
            }
            for(MeshDataVertex v : part.vertices) {
                pipeline.pushVertex(v.position.x, v.position.y, v.position.z, v.textureCoordinate.x, v.textureCoordinate.y, i);
            }
            for(int[] face : part.faces) {
                pipeline.pushFace(face);
            }
        }
        pipeline.buffer();

        return pipeline;
    }
}
