package org.j3d;

import java.io.File;
import java.util.Vector;

public class MeshDataPart {
    
    public File texture = null;
    public final Vector<int[]> faces = new Vector<>();
    public final Vector<MeshDataVertex> vertices = new Vector<>();
}
