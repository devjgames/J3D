package org.j3d;

import java.util.Vector;

public class Vertex {
    
    private static Vec4 tPosition = new Vec4();
    private static Vec3 tNormal = new Vec3();

    public final Vec4 position = new Vec4();
    public final Vec2 textureCoordinate = new Vec2();
    public final Vec2 textureCoordinate2 = new Vec2();
    public final Vec4 color = new Vec4(1, 1, 1, 1);
    public final Vec3 normal = new Vec3();

    public Vertex() {
    }

    public Vertex(float x, float y, float z, float w, float s, float t, float u, float v, float r, float g, float b, float a, float nx, float ny, float nz) {
        set(x, y, z, w, s, t, u, v, r, g, b, a, nx, ny, nz);
    }

    public Vertex(Vec4 position, Vec2 textureCoordinate, Vec2 textureCoordinate2, Vec4 color, Vec3 normal) {
        set(position, textureCoordinate, textureCoordinate2, color, normal);
    }

    public Vertex(Vertex v) {
        set(v);
    }

    public void set(float x, float y, float z, float w, float s, float t, float u, float v, float r, float g, float b, float a, float nx, float ny, float nz) {
        position.set(x, y, z, w);
        textureCoordinate.set(s, t);
        textureCoordinate2.set(u, v);
        color.set(r, g, b, a);
        normal.set(nx, ny, nz);
    }

    public void set(Vec4 position, Vec2 textureCoordinate, Vec2 textureCoordinate2, Vec4 color, Vec3 normal) {
        this.position.set(position);
        this.textureCoordinate.set(textureCoordinate);
        this.textureCoordinate2.set(textureCoordinate2);
        this.color.set(color);
        this.normal.set(normal);
    }

    public void set(Vertex v) {
        position.set(v.position);
        textureCoordinate.set(v.textureCoordinate);
        textureCoordinate2.set(v.textureCoordinate2);
        color.set(v.color);
        normal.set(v.normal);
    }

    public void light(Vector<Node> lights, int lightCount, Mat4 model, Mat4 modelIT, Vec4 ambientColor, Vec4 diffuseColor) {
        float r = ambientColor.x;
        float g = ambientColor.y;
        float b = ambientColor.z;
        float a = ambientColor.w;
        float m;
        tPosition.transform(model, position);
        tNormal.transform(modelIT, normal).normalize();
        for(int i = 0; i != lightCount; i++) {
            Node light = lights.get(i);
            float ox = light.absolutePosition.x - tPosition.x;
            float oy = light.absolutePosition.y - tPosition.y;
            float oz = light.absolutePosition.z - tPosition.z;
            float l = (float)Math.sqrt(ox * ox + oy * oy + oz * oz);
            float lx = ox / l;
            float ly = oy / l;
            float lz = oz / l;
            float ln = Math.max(0, Math.min(1, tNormal.dot(lx, ly, lz)));
            float at = 1 - Math.min(1, l / light.lightRadius);

            r += at * ln * diffuseColor.x * light.lightColor.x;
            g += at * ln * diffuseColor.y * light.lightColor.y;
            b += at * ln * diffuseColor.z * light.lightColor.z;
            a += at * ln * diffuseColor.w * light.lightColor.w;

            m = Math.max(r, Math.max(g, b));
            if(m > 1) {
                r /= m;
                g /= m;
                b /= m;
            }
            a = Math.min(1, a);
        }
        m = Math.max(r, Math.max(g, b));
        if(m > 1) {
            r /= m;
            g /= m;
            b /= m;
        }
        a = Math.min(1, a);
        color.set(r, g, b, a);
    }
}
