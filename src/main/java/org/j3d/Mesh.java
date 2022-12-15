package org.j3d;

import java.util.Iterator;
import java.util.Vector;

import org.joml.Matrix4f;

public class Mesh implements Iterable<MeshPart> {
    
    public final Matrix4f model = new Matrix4f();
    public final BoundingBox bounds = new BoundingBox();
    public Object data = null;

    private Vector<MeshPart> parts = new Vector<>();

    public int getMeshPartCount() {
        return parts.size();
    }

    public MeshPart meshPartAt(int i) {
        return parts.get(i);
    }

    public void addMeshPart(MeshPart part) {
        parts.add(part);
    }

    public void calcBounds() {
        bounds.clear();
        for(MeshPart part : this) {
            part.calcBounds();
            bounds.add(part.bounds);
        }
    }

    public void render(Matrix4f projection, Matrix4f view) {
        for(MeshPart part : this) {
            part.begin(null);
            part.render(projection, view);
            part.end();
        }
        if(getMeshPartCount() != 0) {
            meshPartAt(getMeshPartCount() - 1).material.end();
        }
    }

    @Override
    public Iterator<MeshPart> iterator() {
        return parts.iterator();
    }

    public Mesh newInstance() {
        Mesh mesh = new Mesh();

        for(MeshPart part : this) {
            mesh.addMeshPart(part.newInstance(mesh));
        }
        mesh.calcBounds();
        
        return mesh;
    }
}
