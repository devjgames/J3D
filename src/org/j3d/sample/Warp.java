package org.j3d.sample;

import org.j3d.*;

public class Warp extends Entity {

    public String name = "lava";
    
    private Mesh mesh;
    private Vertex[] baseVertices;

    @Override
    public void start() {
        Node root = scene().root.childAt(0);

        for(Node node : root) {
            if(node.name.equals(name.trim())) {
                mesh = node.getMesh();
                baseVertices = new Vertex[mesh.vertexCount()];
                for(int i = 0; i != baseVertices.length; i++) {
                    baseVertices[i] = new Vertex(mesh.getVertex(i));
                }
                break;
            }
        }
    }

    @Override
    public void update() {
        float t = game().totalTime();
        for(int i = 0; i != baseVertices.length; i++) {
            float x = baseVertices[i].position.x;
            float y = baseVertices[i].position.y;
            float z = baseVertices[i].position.z;
            Vertex v = mesh.getVertex(i);

            v.position.x = x + 8 * (float)(Math.cos(0.05f * z + t) * Math.sin(0.05f * y + t));
            v.position.y = y + 8 * (float)(Math.sin(0.05f * x + t) * Math.cos(0.05f * z + t));
            v.position.z = z + 8 * (float)(Math.sin(0.05f * x + t) * Math.cos(0.05f * y + t));
        }
    }
}
