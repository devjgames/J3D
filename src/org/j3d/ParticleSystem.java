package org.j3d;

import java.io.File;
import java.util.Vector;

public class ParticleSystem extends Renderable {

    public final Vec3 emitPosition = new Vec3();

    private SceneVertex[] vertices;
    private int[] indices;
    private float[] particles;
    private float[] temp;
    private int count;
    private AABB bounds = new AABB();
    private Mat4 m = new Mat4();
    private Vec3 r = new Vec3();
    private Vec3 u = new Vec3();
    private Vec3 f = new Vec3();
    private float time = 0;

    public ParticleSystem(int maxParticles) {
        vertices = new SceneVertex[maxParticles * 4];
        indices = new int[maxParticles * 6];
        particles = new float[maxParticles * 20];
        temp = new float[maxParticles * 20];
        count = 0;

        for(int i = 0; i != vertices.length; ) {
            vertices[i++] = new SceneVertex(0, 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0);
            vertices[i++] = new SceneVertex(0, 0, 0, 1, 1, 0, 1, 0, 1, 1, 1, 1, 0, 0, 0);
            vertices[i++] = new SceneVertex(0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0);
            vertices[i++] = new SceneVertex(0, 0, 0, 1, 0, 1, 0, 1, 1, 1, 1, 1, 0, 0, 0);
        }
        for(int i = 0, j = 0; i != vertices.length; i += 4) {
            indices[j++] = i;
            indices[j++] = i + 1;
            indices[j++] = i + 2;
            indices[j++] = i + 2;
            indices[j++] = i + 3;
            indices[j++] = i;
        }
    }

    @Override
    public File file() {
        return null;
    }

    @Override
    public AABB getBounds(Node node, Camera camera, AABB bounds) {
        return bounds.set(this.bounds);
    }

    @Override
    public int triangleCount() {
        return 0;
    }

    @Override
    public Triangle triangleAt(Node node, Camera camera, int i, Triangle triangle) {
        return null;
    }

    @Override
    public void buffer(Node node, Camera camera) {
        calcTransform(camera, node);
        bounds.clear();
        for(int i = 0, j = 0; i != count; ) {
            float vX = particles[i++];
            float vY = particles[i++];
            float vZ = particles[i++];
            float pX = particles[i++];
            float pY = particles[i++];
            float pZ = particles[i++];
            float sR = particles[i++];
            float sG = particles[i++];
            float sB = particles[i++];
            float sA = particles[i++];
            float eR = particles[i++];
            float eG = particles[i++];
            float eB = particles[i++];
            float eA = particles[i++];
            float sX = particles[i++];
            float sY = particles[i++];
            float eX = particles[i++];
            float eY = particles[i++];
            float t = time - particles[i++];
            float n = t / particles[i++];
            float cR = sR + n * (eR - sR);
            float cG = sG + n * (eG - sG);
            float cB = sB + n * (eB - sB);
            float cA = sA + n * (eA - sA);
            float x = (sX + n * (eX - sX)) * 0.5f;
            float y = (sY + n * (eY - sY)) * 0.4f;

            pX += t * vX;
            pY += t * vY;
            pZ += t * vZ;
            vertices[j + 0].position.set(pX - r.x * x - u.x * y, pY - r.y * x - u.y * y, pZ - r.z * x - u.z * y, 1);
            vertices[j + 0].color.set(cR, cG, cB, cA);
            vertices[j + 0].normal.set(f);
            vertices[j + 1].position.set(pX + r.x * x - u.x * y, pY + r.y * x - u.y * y, pZ + r.z * x - u.z * y, 1);
            vertices[j + 1].color.set(cR, cG, cB, cA);
            vertices[j + 1].normal.set(f);
            vertices[j + 2].position.set(pX + r.x * x + u.x * y, pY + r.y * x + u.y * y, pZ + r.z * x + u.z * y, 1);
            vertices[j + 2].color.set(cR, cG, cB, cA);
            vertices[j + 2].normal.set(f);
            vertices[j + 3].position.set(pX - r.x * x + u.x * y, pY - r.y * x + u.y * y, pZ - r.z * x + u.z * y, 1);
            vertices[j + 3].color.set(cR, cG, cB, cA);
            vertices[j + 3].normal.set(f);
            for(int k = 0; k != 4; k++) {
                SceneVertex v = vertices[j++];
                bounds.add(v.position.x, v.position.y, v.position.z);
            }
        }
    }

    @Override
    public void light(Vector<Node> lights, int lightCount, Node node, Camera camera, Vec4 ambientColor, Vec4 diffuseColor) {
        calcTransform(camera, node);

        int count = this.count / 20 * 4;

        for(int i = 0; i != count; i++) {
            vertices[i].light(lights, lightCount, node.model, node.modelIT, ambientColor, diffuseColor);
        }
    }

    @Override
    public int render(Node node, Camera camera, Renderer renderer) {
        return renderer.render(vertices, indices, count / 20 * 6);
    }

    @Override
    public void update(Game game) {
        time = game.totalTime();
        int count = 0;
        for(int i = 0; i != this.count; i += 20) {
            float n = (time - particles[i + 18]) / particles[i + 19];
            if(n <= 1) {
                for(int j = 0; j != 20; j++) {
                    temp[count++] = particles[i + j];
                }
            }
        }
        float[] t = particles;
        particles = temp;
        temp = t;
        this.count = count;
    }

    @Override
    public Renderable newInstance() {
        return null;
    }
    
    public void emit(Particle particle, Game game) {
        if(count != particles.length) {
            particles[count++] = particle.velocityX;
            particles[count++] = particle.velocityY;
            particles[count++] = particle.velocityZ;
            particles[count++] = particle.positionX + emitPosition.x;
            particles[count++] = particle.positionY + emitPosition.y;
            particles[count++] = particle.positionZ + emitPosition.z;
            particles[count++] = particle.startR;
            particles[count++] = particle.startG;
            particles[count++] = particle.startB;
            particles[count++] = particle.startA;
            particles[count++] = particle.endR;
            particles[count++] = particle.endG;
            particles[count++] = particle.endB;
            particles[count++] = particle.endA;
            particles[count++] = particle.startX;
            particles[count++] = particle.startY;
            particles[count++] = particle.endX;
            particles[count++]  = particle.endY;
            particles[count++] = game.totalTime();
            particles[count++]  = particle.lifeSpan;
        }
    }

    private void calcTransform(Camera camera, Node node) {
        m.set(camera.view).mul(node.model);
        r.set(m.m00, m.m01, m.m02);
        u.set(m.m10, m.m11, m.m12);
        f.set(m.m20, m.m21, m.m22).normalize();
    }
}
