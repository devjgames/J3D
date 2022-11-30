package org.j3d;

import java.io.File;

public class MeshLoader implements AssetLoader {

    @Override
    public Object load(File file, AssetManager assets) throws Exception {
        MeshData data = new MeshData(file);
        Mesh mesh = new Mesh();

        for(MeshDataPart dataPart : data.parts) {
            MeshPart part = new MeshPart(mesh, 8);
            PixelLightMaterial material = assets.getResources().manage(new PixelLightMaterial());

            part.material = material;
            if(dataPart.texture != null) {
                material.texture = assets.load(dataPart.texture);
            }
            for(MeshDataVertex vertex : dataPart.vertices) {
                part.push(vertex.position.x, vertex.position.y, vertex.position.z);
                part.push(vertex.textureCoordinate.x, vertex.textureCoordinate.y);
                part.push(vertex.normal.x, vertex.normal.y, vertex.normal.z);
            }
            for(int[] face: dataPart.faces) {
                part.pushFace(face);
            }
            part.trim();
            part.bufferIndices();;
            part.bufferVertices(false);
            part.calcBounds();
            mesh.addMeshPart(part);
        }
        mesh.calcBounds();

        return mesh;
    }
    
}
