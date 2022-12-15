package org.j3d.scene;

import java.io.File;
import java.util.Vector;

import org.j3d.BoundingBox;
import org.j3d.Material;
import org.j3d.Mesh;
import org.j3d.MeshPart;
import org.j3d.Triangle;

public class MeshRenderable implements Renderable {

    public final Mesh mesh;

    private final File file;
    private final int triangleCount;
    private final Vector<Material> materials = new Vector<>();

    public MeshRenderable(File file, Mesh mesh) throws Exception {
        this.mesh = mesh;
        this.file = file;

        int count = 0;

        for(MeshPart part : mesh) {
            count += part.getTriangleCount();
            materials.add(part.material);
            part.material.setSource(part);
        }
        triangleCount = count;
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public int getMaterialCount() {
        return mesh.getMeshPartCount();
    }

    @Override
    public Material materialAt(int i) {
        return materials.get(i);
    }

    @Override
    public BoundingBox getBounds() {
        return mesh.bounds;
    }

    @Override
    public int getTriangleCount() {
        return triangleCount;
    }

    @Override
    public void triangleAt(int i, Triangle triangle) {
        int count = 0;

        for(MeshPart part : mesh) {
            if(i >= count && i < count + part.getTriangleCount()) {
                part.triangleAt(i - count, triangle);
                return;
            }
            count += part.getTriangleCount();
        }
    }

    @Override
    public void update(Scene scene, Node node) {
        mesh.model.set(node.model);
        mesh.calcBounds();
    }

    @Override
    public Renderable newInstance() throws Exception {
        return new MeshRenderable(file, mesh.newInstance());
    }
    
}
