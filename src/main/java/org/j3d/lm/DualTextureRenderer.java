package org.j3d.lm;

import java.io.File;

import org.j3d.AssetManager;
import org.j3d.Mesh;
import org.j3d.MeshData;
import org.j3d.MeshDataPart;
import org.j3d.MeshDataVertex;
import org.j3d.MeshPart;
import org.j3d.Texture;
import org.j3d.Utils;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class DualTextureRenderer implements Renderer {

    public final Vector4f backgroundColor = new Vector4f(0, 0, 0, 1);

    private Mesh mesh = new Mesh();

    @Override
    public Mesh getMesh() {
        return mesh;
    }

    public void push(int part, float x, float y, float z, float s, float t, float u, float v) {
        mesh.meshPartAt(part).push(x, y, z);
        mesh.meshPartAt(part).push(s, t);
        mesh.meshPartAt(part).push(u, v);
    }

    public void load(File file, AssetManager assets) throws Exception {
        MeshData data = new MeshData(file);

        for(MeshDataPart dataPart : data.parts) {
            MeshPart part = new MeshPart(mesh, 7);
            DualTextureMaterial material = assets.getResources().manage(new DualTextureMaterial());

            if(dataPart.texture != null) {
                material.texture = assets.load(dataPart.texture);
            }
            for(MeshDataVertex vertex : dataPart.vertices) {
                part.push(vertex.position.x, vertex.position.y, vertex.position.z);
                part.push(vertex.textureCoordinate.x, vertex.textureCoordinate.y);
                part.push(0, 0);
            }
            for(int[] face : dataPart.faces) {
                part.pushFace(face);
            }
            part.material = material;
            part.trim();
            part.bufferVertices(false);
            part.bufferIndices();
            part.calcBounds();
            mesh.addMeshPart(part);
        }
        mesh.calcBounds();
    }

    @Override
    public void render(Matrix4f projection, Matrix4f view, Texture texture2) {
        for(MeshPart part : mesh) {
            DualTextureMaterial material = (DualTextureMaterial)part.material;

            material.texture2 = texture2;
        }
        Utils.clear(backgroundColor.x, backgroundColor.y, backgroundColor.z, backgroundColor.w);
        mesh.render(projection, view);
    }
    
}
