package org.j3d;

import java.util.Vector;

public class Sprite3D extends Renderable {

    public float size = 32;

    private Vertex[] vertices = new Vertex[] {
        new Vertex(0, 0, 0, 1, 0, 1, 0, 0, 1, 1, 1, 1, 0, 0, 0),
        new Vertex(0, 0, 0, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 0),
        new Vertex(0, 0, 0, 1, 1, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0),
        new Vertex(0, 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0)
    };
    private int[] indices = new int[] { 0, 1, 2, 2, 3, 0 };
    private Mat4 m = new Mat4();
    private Vec3 r = new Vec3();
    private Vec3 u = new Vec3();
    private Vec3 f = new Vec3();

    @Override
    public AABB getBounds(Node node, Camera camera, AABB bounds) {
        buffer(node, camera);
        bounds.clear();
        for(Vertex v : vertices) {
            bounds.add(v.position.x, v.position.y, v.position.z);
        }
        return bounds;
    }

    @Override
    public int triangleCount() {
        return 2;
    }

    @Override
    public Triangle getTriangle(Node node, Camera camera, int i, Triangle triangle) {
        buffer(node, camera);
        if(i == 0) {
            triangle.set(
                vertices[0].position.x, vertices[0].position.y, vertices[0].position.z,
                vertices[1].position.x, vertices[1].position.y, vertices[1].position.z,
                vertices[2].position.x, vertices[2].position.y, vertices[2].position.z 
            );
        } else  {
            triangle.set(
                vertices[2].position.x, vertices[2].position.y, vertices[2].position.z,
                vertices[3].position.x, vertices[3].position.y, vertices[3].position.z,
                vertices[0].position.x, vertices[0].position.y, vertices[0].position.z 
            );
        }
        return triangle;
    }

    @Override
    public void buffer(Node node, Camera camera) {
        float d = size / 2;

        m.set(camera.view).mul(node.model);
        r.set(m.m00, m.m01, m.m02);
        u.set(m.m10, m.m11, m.m12);
        f.set(m.m20, m.m21, m.m22).normalize();

        vertices[0].position.set(-r.x * d - u.x * d, -r.y * d - u.y * d, -r.z * d - u.z * d, 1);
        vertices[1].position.set(+r.x * d - u.x * d, +r.y * d - u.y * d, +r.z * d - u.z * d, 1);
        vertices[2].position.set(+r.x * d + u.x * d, +r.y * d + u.y * d, +r.z * d + u.z * d, 1);
        vertices[3].position.set(-r.x * d + u.x * d, -r.y * d + u.y * d, -r.z * d + u.z * d, 1);
        for(Vertex v : vertices) {
            v.normal.set(f);
        }
    }

    @Override
    public void light(Vector<Node> lights, int lightCount, Node node, Camera camera, Vec4 ambientColor, Vec4 diffuseColor) {
        for(Vertex v : vertices) {
            v.light(lights, lightCount, node.model, node.modelIT, ambientColor, diffuseColor);
        }
    }

    @Override
    public int render(Node node, Camera camera, Renderer renderer) {
        return renderer.render(vertices, indices, 6);
    }

    @Override
    public void update(Game game) {
    }
}
