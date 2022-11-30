package org.j3d;

import java.io.File;
import java.util.Vector;

public class MeshDataPart {
    
    public final File texture;
    public final Vector<MeshDataVertex> vertices = new Vector<>();
    public final Vector<int[]> faces = new Vector<>();

    public MeshDataPart(File texture) {
        this.texture = texture;
    }
}
