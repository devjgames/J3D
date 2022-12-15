package org.j3d.scene;

import org.j3d.Asset;
import org.j3d.BoundingBox;
import org.j3d.Material;
import org.j3d.Triangle;

public interface Renderable extends Asset {

    int getMaterialCount();

    Material materialAt(int i);
    
    BoundingBox getBounds();

    int getTriangleCount();

    void triangleAt(int i, Triangle triangle);

    void update(Scene scene, Node node) throws Exception;

    Renderable newInstance() throws Exception;
}
