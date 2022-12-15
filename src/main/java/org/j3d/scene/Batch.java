package org.j3d.scene;

import java.util.Vector;

import org.j3d.Material;

public class Batch {

    public static class BatchRecord {
        public Node node;
        public Material material;
        public int zOrder;
        public int order = -1;
        
        public BatchRecord(Node node, Material material, int zOrder) {
            set(node, material, zOrder);
        }

        public void set(Node node, Material material, int zOrder) {
            this.node = node;
            this.material = material;
            this.zOrder = zOrder;
        }
    }
    
    public final Vector<BatchRecord> records = new Vector<>();
}
