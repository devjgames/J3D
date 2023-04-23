package org.j3d;

import java.io.*;
import java.util.Vector;

public class MD2Mesh extends Renderable {

    public static int readByte(byte[] bytes, int[] i) {
        return bytes[i[0]++] & 0xFF;
    }

    public static int readShort(byte[] bytes, int[] i) {
        int b1 = bytes[i[0]++];
        int b2 = bytes[i[0]++];

        return ((b2 << 8) & 0xFF00) | (b1 & 0xFF);
    }

    public static int readInt(byte[] bytes, int[] i) {
        int b1 = bytes[i[0]++];
        int b2 = bytes[i[0]++];
        int b3 = bytes[i[0]++];
        int b4 = bytes[i[0]++];

        return ((b4 << 24) & 0xFF000000) | ((b3 << 16) & 0xFF0000) | ((b2 << 8) & 0xFF00) | (b1 & 0xFF);
    }

    public static float readFloat(byte[] bytes, int[] i) {
        return Float.intBitsToFloat(readInt(bytes, i));
    }

    public static String readString(byte[] bytes, int[] i, int length) {
        int j = i[0];
        int s = j;

        i[0] += length;
        for(; j != length; j++) {
            if(bytes[j] == 0) {
                break;
            }  
        }
        length = j - s;

        return new String(bytes, s, length);
    }
    
    public static class MD2Header {
        public final int id;
        public final int version;
        public final int skinW;
        public final int skinH;
        public final int frameSize;
        public final int numSkins;
        public final int numXYZ;
        public final int numST;
        public final int numTris;
        public final int numGLCmds;
        public final int numFrames;
        public final int offSkins;
        public final int offST;
        public final int offTris;
        public final int offFrames;
        public final int offGLCmds;
        public final int offEnd;

        public MD2Header(byte[] bytes, int[] i) {
            id = readInt(bytes, i);
            version = readInt(bytes, i);
            skinW = readInt(bytes, i);
            skinH = readInt(bytes, i);
            frameSize = readInt(bytes, i);
            numSkins = readInt(bytes, i);
            numXYZ = readInt(bytes, i);
            numST = readInt(bytes, i);
            numTris = readInt(bytes, i);
            numGLCmds = readInt(bytes, i);
            numFrames = readInt(bytes, i);
            offSkins = readInt(bytes, i);
            offST = readInt(bytes, i);
            offTris = readInt(bytes, i);
            offFrames = readInt(bytes, i);
            offGLCmds = readInt(bytes, i);
            offEnd = readInt(bytes, i);
        }
    }

    public static class MD2TextureCoordinate {
        public final short s;
        public final short t;

        public MD2TextureCoordinate(byte[] bytes, int[] i) {
            s = (short)readShort(bytes, i);
            t = (short)readShort(bytes, i);
        }
    }

    public static class MD2Triangle {
        public final short[] xyz;
        public final short[] st;

        public MD2Triangle(byte[] bytes, int[] i) {
            xyz = new short[3];
            st = new short[3];
            xyz[0] = (short)readShort(bytes, i);
            xyz[1] = (short)readShort(bytes, i);
            xyz[2] = (short)readShort(bytes, i);
            st[0] = (short)readShort(bytes, i);
            st[1] = (short)readShort(bytes, i);
            st[2] = (short)readShort(bytes, i);
        }
    }

    public static class MD2Vertex {
        public final short[] xyz;
        public final short n;

        public MD2Vertex(byte[] bytes, int[] i) {
            xyz = new short[3];
            xyz[0] = (short)readByte(bytes, i);
            xyz[1] = (short)readByte(bytes, i);
            xyz[2] = (short)readByte(bytes, i);
            n = (short)readByte(bytes, i);
        }
    }

    public static class MD2Frame {
        public final float[] scale;
        public final float[] translation;
        public final String name;
        public final MD2Vertex[] vertices;
        public final AABB bounds;

        public MD2Frame(byte[] bytes, int[] i, MD2Header header) {
            scale = new float[3];
            translation = new float[3];
            scale[0] = readFloat(bytes, i);
            scale[1] = readFloat(bytes, i);
            scale[2] = readFloat(bytes, i);
            translation[0] = readFloat(bytes, i);
            translation[1] = readFloat(bytes, i);
            translation[2] = readFloat(bytes, i);
            name = readString(bytes, i, 16);
            vertices = new MD2Vertex[header.numXYZ];
            bounds = new AABB();
            for(int j = 0; j != header.numXYZ; j++) {
                vertices[j] = new MD2Vertex(bytes, i);
                float x = vertices[j].xyz[0] * scale[0] + translation[0];
                float y = vertices[j].xyz[1] * scale[1] + translation[1];
                float z = vertices[j].xyz[2] * scale[2] + translation[2];
                bounds.add(x, y, z);
            }
        }
    }

    private MD2Header header;
    private MD2TextureCoordinate[] textureCoordinates;
    private MD2Triangle[] triangles;
    private MD2Frame[] frames;
    private Vertex[] vertices;
    private int[] indices;
    private AABB bounds = new AABB();
    private boolean done;
    private int frame;
    private int start;
    private int end;
    private int speed;
    private boolean looping;
    private float amount;
    private float[][] normals;

    public MD2Mesh(File file) throws IOException {
        byte[] bytes = IO.readAllBytes(file);
        int[] i = new int[] { 0 };

        header = new MD2Header(bytes, i);
        i[0] = header.offST;
        textureCoordinates = new MD2TextureCoordinate[header.numST];
        for(int j = 0; j != header.numST; j++) {
            textureCoordinates[j] = new MD2TextureCoordinate(bytes, i);
        }
        triangles = new MD2Triangle[header.numTris];
        for(int j = 0; j != header.numTris; j++) {
            triangles[j] = new MD2Triangle(bytes, i);
        }
        frames = new MD2Frame[header.numFrames];
        for(int j = 0; j != header.numFrames; j++) {
            i[0] = header.offFrames + j * header.frameSize;
            frames[j] = new MD2Frame(bytes, i, header);
        }

        vertices = new Vertex[header.numTris * 3];
        indices = new int[header.numTris * 3];
        for(int j = 0; j != indices.length; j++) {
            vertices[j] = new Vertex();
            indices[j] = j;
        }
        normals = MD2Normals.cloneNormals();

        start = end = speed = 0;
        looping = false;

        reset();
    }

    public MD2Mesh(MD2Mesh mesh) {
        header = mesh.header;
        textureCoordinates = mesh.textureCoordinates;
        triangles = mesh.triangles;
        frames = mesh.frames;
        normals = mesh.normals;

        vertices = new Vertex[header.numTris * 3];
        indices = new int[header.numTris * 3];
        for(int i = 0; i != indices.length; i++) {
            vertices[i] = new Vertex();
            indices[i] = i;
        }

        start = end = speed = 0;
        looping = false;

        reset();
    }

    public boolean isDone() {
        return done;
    }

    public void reset() {
        frame = start;
        amount = 0;
        done = start == end;
        bounds.set(frames[frame].bounds);
        buffer(null, null);
    }

    public void setSequence(int start, int end, int speed, boolean looping) {
        if(start != this.start || end != this.end || speed != this.speed || looping != this.looping) {
            if(start >= 0 && start < header.numFrames && end >= 0 && end < header.numFrames && speed >= 0 && start <= end) {
                this.start = start;
                this.end = end;
                this.speed = speed;
                this.looping = looping;
                reset();
            }
        }
    }

    @Override
    public AABB getBounds(Node node, Camera camera, AABB bounds) {
        return bounds.set(this.bounds);
    }

    @Override
    public int triangleCount() {
        return header.numTris;
    }

    @Override
    public Triangle getTriangle(Node node, Camera camera, int i, Triangle triangle) {
        i *= 3;
        triangle.set(
            vertices[i + 0].position.x, vertices[i + 0].position.y, vertices[i + 0].position.z, 
            vertices[i + 1].position.x, vertices[i + 1].position.y, vertices[i + 1].position.z,
            vertices[i + 2].position.x, vertices[i + 2].position.y, vertices[i + 2].position.z
        );
        return triangle;
    }

    @Override
    public void buffer(Node node, Camera camera) {
        int f1 = frame;
        int f2 = f1 + 1;

        if(f1 == end) {
            f2 = start;
        }

        for(int i = 0, k = 0; i != header.numTris; i++) {
            for(int j = 2; j != -1; j--, k++) {
                MD2TextureCoordinate textureCoordinate = textureCoordinates[triangles[i].st[j]];
                float s = textureCoordinate.s / (float)header.skinW;
                float t = textureCoordinate.t / (float)header.skinH;
                float vx1 = frames[f1].vertices[triangles[i].xyz[j]].xyz[0] * frames[f1].scale[0] + frames[f1].translation[0];
                float vy1 = frames[f1].vertices[triangles[i].xyz[j]].xyz[1] * frames[f1].scale[1] + frames[f1].translation[1];
                float vz1 = frames[f1].vertices[triangles[i].xyz[j]].xyz[2] * frames[f1].scale[2] + frames[f1].translation[2];
                float vx2 = frames[f1].vertices[triangles[i].xyz[j]].xyz[0] * frames[f1].scale[0] + frames[f1].translation[0];
                float vy2 = frames[f1].vertices[triangles[i].xyz[j]].xyz[1] * frames[f1].scale[1] + frames[f1].translation[1];
                float vz2 = frames[f1].vertices[triangles[i].xyz[j]].xyz[2] * frames[f1].scale[2] + frames[f1].translation[2];
                float nx1 = normals[frames[f1].vertices[triangles[i].xyz[j]].n][0];
                float ny1 = normals[frames[f1].vertices[triangles[i].xyz[j]].n][1];
                float nz1 = normals[frames[f1].vertices[triangles[i].xyz[j]].n][2];
                float nx2 = normals[frames[f2].vertices[triangles[i].xyz[j]].n][0];
                float ny2 = normals[frames[f2].vertices[triangles[i].xyz[j]].n][1];
                float nz2 = normals[frames[f2].vertices[triangles[i].xyz[j]].n][2];
                Vertex v = vertices[k];

                v.position.x = vx1 + amount * (vx2 - vx1);
                v.position.y = vy1 + amount * (vy2 - vy1);
                v.position.z = vz1 + amount * (vz2 - vz1);
                v.position.w = 1;
                v.normal.x = nx1 + amount * (nx2 - nx1);
                v.normal.y = ny1 + amount * (ny2 - ny1);
                v.normal.z = nz1 + amount * (nz2 - nz1);
                v.textureCoordinate.x = s;
                v.textureCoordinate.y = t;
                v.color.set(1, 1, 1, 1);
            }
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
        return renderer.render(vertices, indices, indices.length);
    }

    @Override
    public void update(Game game) {
        if(done) {
            return;
        }
        amount += speed * game.elapsedTime();
        if(amount >= 1) {
            if(looping) {
                if(frame == end) {
                    frame = start;
                } else {
                    frame++;
                }
                amount = 0;
            } else if(frame == end - 1) {
                amount = 1;
                done = true;
            } else {
                frame++;
                amount = 0;
            }
        }

        int f1 = frame;
        int f2 = f1 + 1;

        if(f1 == end) {
            f2 = start;
        }
        frames[f1].bounds.min.lerp(frames[f2].bounds.min, amount, bounds.min);
        frames[f1].bounds.max.lerp(frames[f2].bounds.max, amount, bounds.max);
    }
}
