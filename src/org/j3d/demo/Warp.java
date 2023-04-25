package org.j3d.demo;

import org.j3d.Mesh;
import org.j3d.NodeComponent;
import org.j3d.Vec4;
import org.j3d.Vertex;

public class Warp extends NodeComponent {

    private Vertex[] baseVertices = null;

    @Override
    public void start() throws Exception {
        if(scene().inDesign()) {
            return;
        }

        Mesh mesh = node().getMesh();

        if(mesh != null) {
            baseVertices = new Vertex[mesh.vertexCount()];
            for(int i = 0; i != baseVertices.length; i++) {
                baseVertices[i] = new Vertex(mesh.vertexAt(i));
            }
        }
    }

    @Override
    public void update() throws Exception {
        if(scene().inDesign()) {
            return;
        }

        Mesh mesh = node().getMesh();

        if(mesh != null) {
            float t = game().totalTime();

            for(int i = 0; i != baseVertices.length; i++) {
                float x = baseVertices[i].position.x;
                float y = baseVertices[i].position.y;
                float z = baseVertices[i].position.z;
                Vec4 p = mesh.vertexAt(i).position;

                p.x = x + (float)(8 * Math.sin(0.05 * z + t) * Math.cos(0.05 * y + t));
                p.y = y + (float)(8 * Math.cos(0.05 * x + t) * Math.sin(0.05 * z + t));
                p.z = z + (float)(8 * Math.sin(0.05 * x + t) * Math.cos(0.05 * y + t));
            }
        }
    }
}