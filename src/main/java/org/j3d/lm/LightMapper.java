package org.j3d.lm;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Vector;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;

import org.j3d.Log;
import org.j3d.Mesh;
import org.j3d.MeshPart;
import org.j3d.PixelFormat;
import org.j3d.Texture;
import org.j3d.Triangle;
import org.joml.GeometryUtils;
import org.joml.Intersectionf;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

public class LightMapper {

    private final Vector3f normal = new Vector3f();
    private final Vector3f edgeNormal = new Vector3f();
    private final Vector3f v = new Vector3f();
    private final Vector3f origin = new Vector3f();
    private final Vector3f direction = new Vector3f();
    private final Vector3f offset = new Vector3f();
    private final Vector3f lightNormal = new Vector3f();
    private final Vector3f position = new Vector3f();
    private final Vector3f color = new Vector3f();
    private final Vector3f color2 = new Vector3f();
    private final float[] time = new float[1];
    
    public boolean map(File file, Vector<Light> lights, int width, int height, float ambientFactor, Mesh mesh) throws Exception {
        int px = 0;
        int py = 0;
        int mh = 0;
        int w = 0;
        int h = 0;
        Vector<Texel> texels = null;
        Vector<int[]> rects = null;
        Texture texture = null;
        ByteBuffer buf =  null;

        if(!file.exists()) {
            buf = BufferUtils.createByteBuffer(width * height * 4);
            for(int i = 0; i != buf.capacity(); i += 4) {
                buf.put((byte)255);
                buf.put((byte)0);
                buf.put((byte)255);
                buf.put((byte)255);
            }
            buf.flip();
            texture = new Texture(null, width, height, PixelFormat.COLOR, buf);
            rects = new Vector<int[]>();
            texels = new Vector<>(width * height);
        }

        for(MeshPart part : mesh) {
            for(int i = 0; i != part.getFaceCount(); i++) {
                int v1 = part.faceVertexAt(i, 0);
                int v2 = part.faceVertexAt(i, 1);
                int v3 = part.faceVertexAt(i, 2);
                float x1 = part.vertexAt(v1, 0);
                float y1 = part.vertexAt(v1, 1);
                float z1 = part.vertexAt(v1, 2);  
                float x2 = part.vertexAt(v2, 0);
                float y2 = part.vertexAt(v2, 1);
                float z2 = part.vertexAt(v2, 2); 
                float x3 = part.vertexAt(v3, 0);
                float y3 = part.vertexAt(v3, 1);
                float z3 = part.vertexAt(v3, 2);  
                float ux = x2 - x1;
                float uy = y2 - y1;
                float uz = z2 - z1;
                float vx = x3 - x2;
                float vy = y3 - y2;
                float vz = z3 - z2;
                float nx = uy * vz - uz * vy;
                float ny = uz * vx - ux * vz;
                float nz = ux * vy - uy * vx;
                float ul = Vector3f.length(ux, uy, uz);
                float vl = Vector3f.length(vx, vy, vz);
                float nl = Vector3f.length(nx, ny, nz);

                ux /= ul;
                uy /= ul;
                uz /= ul;
                vx /= vl;
                vy /= vl;
                vz /= vl;
                nx /= nl;
                ny /= nl;
                nz /= nl;

                boolean four = part.getFaceVertexCount(i) == 4;

                int minX = 0, minY = 0;

                if(four) {
                    w = (int)Math.ceil(ul / 16) + 1;
                    h = (int)Math.ceil(vl / 16) + 1;
                } else {
                    ux = vy * nz - vz * ny;
                    uy = vz * nx - vx * nz;
                    uz = vx * ny - vy * nx;

                    ul = Vector3f.length(ux, uy, uz);
                    ux /= ul;
                    uy /= ul;
                    uz /= ul;
        
                    float lx = Float.MAX_VALUE;
                    float ly = Float.MAX_VALUE;
                    float hx = -Float.MAX_VALUE;
                    float hy = -Float.MAX_VALUE;

                    for(int j = 0; j != part.getFaceVertexCount(i); j++) {
                        int v = part.faceVertexAt(i, j);
                        float x = part.vertexAt(v, 0);
                        float y = part.vertexAt(v, 1);
                        float z = part.vertexAt(v, 2);
                        float s = x * ux + y * uy + z * uz;
                        float t = x * vx + y * vy + z * vz;

                        lx = Math.min(s, lx);
                        ly = Math.min(t, ly);
                        hx = Math.max(s, hx);
                        hy = Math.max(t, hy);
                    }
                    lx = (float)Math.floor(lx / 16);
                    ly = (float)Math.floor(ly / 16);
                    hx = (float)Math.ceil(hx / 16);
                    hy = (float)Math.ceil(hy / 16);

                    minX = (int)(lx * 16);
                    minY = (int)(ly * 16);

                    w = (int)(hx - lx) + 1;
                    h = (int)(hy - ly) + 1;
                }

                if(px + w >= width) {
                    px = 0;
                    if(px + w >= width) {
                        if(texture != null) {
                            texture.destroy();
                        }
                        return false;
                    }
                    if(py + mh >= height) {
                        if(texture != null) {
                            texture.destroy();
                        }
                        return false;
                    }
                    py += mh;
                    mh = 0;
                } else if(py + h >= height) {
                    if(texture != null) {
                        texture.destroy();
                    }
                    return false;
                } else {
                    mh = Math.max(mh, h);
                }

                if(four) {
                    int v4 = part.faceVertexAt(i, 3);

                    part.setVertexAt(v1, 5, (px + 0.5f) / (float)width);
                    part.setVertexAt(v1, 6, (py + 0.5f) / (float)height);
                    part.setVertexAt(v2, 5, (px - 0.5f + w) / (float)width);
                    part.setVertexAt(v2, 6, (py + 0.5f) / (float)height);
                    part.setVertexAt(v3, 5, (px - 0.5f + w) / (float)width);
                    part.setVertexAt(v3, 6, (py - 0.5f + h) / (float)height);
                    part.setVertexAt(v4, 5, (px + 0.5f) / (float)width);
                    part.setVertexAt(v4, 6, (py - 0.5f + h) / (float)height);
                } else {
                    for(int j = 0; j != part.getFaceVertexCount(i); j++) {
                        int v = part.faceVertexAt(i, j);
                        float x = part.vertexAt(v, 0);
                        float y = part.vertexAt(v, 1);
                        float z = part.vertexAt(v, 2);
                        float s = x * ux + y * uy + z * uz;
                        float t = x * vx + y * vy + z * vz;

                        s -= minX;
                        s += px * 16;
                        s += 8;
                        s /= width * 16;

                        t -= minY;
                        t += py * 16;
                        t += 8;
                        t /= height * 16;

                        part.setVertexAt(v, 5, s);
                        part.setVertexAt(v, 6, t);
                    }
                }

                if(texture != null) {
                    rects.add(new int[] { px, py, w, h });
                }
                px += w;
            }
            part.bufferVertices(false);
        }

        if(texture == null) {
            return true;
        }

        Log.log(1, "calculating light map ...");

        int zero = 0;
        int irect = 0;
        Vector<Triangle> triangles = new Vector<>();

        for(MeshPart part : mesh) {
            for(int i = 0; i != part.getTriangleCount(); i++) {
                Triangle triangle = new Triangle();

                part.triangleAt(i, triangle);
                triangles.add(triangle);
            }
        }

        for(MeshPart part : mesh) {

            for(int i = 0; i != part.getFaceCount(); i++) {
                int v1 = part.faceVertexAt(i, 0);
                int v2 = part.faceVertexAt(i, 1);
                int v3 = part.faceVertexAt(i, 2);
                float x1 = part.vertexAt(v1, 0);
                float y1 = part.vertexAt(v1, 1);
                float z1 = part.vertexAt(v1, 2);  
                float x2 = part.vertexAt(v2, 0);
                float y2 = part.vertexAt(v2, 1);
                float z2 = part.vertexAt(v2, 2); 
                float x3 = part.vertexAt(v3, 0);
                float y3 = part.vertexAt(v3, 1);
                float z3 = part.vertexAt(v3, 2);  
                float t1x = part.vertexAt(v1, 5);
                float t1y = part.vertexAt(v1, 6);
                float t2x = part.vertexAt(v3, 5);
                float t2y = part.vertexAt(v3, 6);
                float t3x = part.vertexAt(v2, 5);
                float t3y = part.vertexAt(v2, 6);
                float area = (t3x - t1x) * (t2y - t1y) - (t3y - t1y) * (t2x - t1x);
                int[] rect = rects.get(irect++);
                float ux = x2 - x1;
                float uy = y2 - y1;
                float uz = z2 - z1;
                float vx = x3 - x2;
                float vy = y3 - y2;
                float vz = z3 - z2;
                float nx = uy * vz - uz * vy;
                float ny = uz * vx - ux * vz;
                float nz = ux * vy - uy * vx;
                float ul = Vector3f.length(ux, uy, uz);
                float vl = Vector3f.length(vx, vy, vz);
                float nl = Vector3f.length(nx, ny, nz);
                Surface surface = (Surface)part.faceDataAt(i);

                px = rect[0];
                py = rect[1];
                w = rect[2];
                h = rect[3];

                ux /= ul;
                uy /= ul;
                uz /= ul;
                vx /= vl;
                vy /= vl;
                vz /= vl;
                nx /= nl;
                ny /= nl;
                nz /= nl;

                if(area < 0) {
                    Log.log(0, "area < 0");
                }

                Log.log(4, px + ", " + py + " : " + w + " x " + h);

                for(int tx = px; tx != px + w; tx++) {
                    for(int ty = py; ty != py + h; ty++) {
                        float s = (tx + 0.5f) / (float)width;
                        float t = (ty + 0.5f) / (float)height;
                        float w0 = (s - t2x) * (t3y - t2y) - (t - t2y) * (t3x - t2x);
                        float w1 = (s - t3x) * (t1y - t3y) - (t - t3y) * (t1x - t3x);
                        float w2 = (s - t1x) * (t2y - t1y) - (t - t1y) * (t2x - t1x);
                        Texel texel = new Texel();                        

                        w0 /= area;
                        w1 /= area;
                        w2 /= area;

                        float ex = w0 * x1 + w1 * x3 + w2 * x2;
                        float ey = w0 * y1 + w1 * y3 + w2 * y2;
                        float ez = w0 * z1 + w1 * z3 + w2 * z2;

                        texel.x = tx;
                        texel.y = ty;
                        texel.position.set(ex, ey, ez);
                        texel.normal.set(nx, ny, nz);

                        boolean edge = false;

                        edgeNormal.zero();

                        for(MeshPart p : mesh) {
                            for(int k = 0; k != p.getFaceCount(); k++) {
                                if(p == part) {
                                    if(k == i) {
                                        continue;
                                    }
                                }
                
                                float kx1 = p.vertexAt(p.faceVertexAt(k, 0), 0);
                                float ky1 = p.vertexAt(p.faceVertexAt(k, 0), 1);
                                float kz1 = p.vertexAt(p.faceVertexAt(k, 0), 2);
                                float kx2 = p.vertexAt(p.faceVertexAt(k, 1), 0);
                                float ky2 = p.vertexAt(p.faceVertexAt(k, 1), 1);
                                float kz2 = p.vertexAt(p.faceVertexAt(k, 1), 2);
                                float kx3 = p.vertexAt(p.faceVertexAt(k, 2), 0);
                                float ky3 = p.vertexAt(p.faceVertexAt(k, 2), 1);
                                float kz3 = p.vertexAt(p.faceVertexAt(k, 2), 2);
                
                                GeometryUtils.normal(kx1, ky1, kz1, kx2, ky2, kz2, kx3, ky3, kz3, normal);
                
                                for(int m = 0; m != p.getFaceVertexCount(k); m++) {
                                    int mv1 = p.faceVertexAt(k, m);
                                    int mv2 = p.faceVertexAt(k, (m + 1) % p.getFaceVertexCount(k));
                                    float mx1 = p.vertexAt(mv1, 0);
                                    float my1 = p.vertexAt(mv1, 1);
                                    float mz1 = p.vertexAt(mv1, 2);
                                    float mx2 = p.vertexAt(mv2, 0);
                                    float my2 = p.vertexAt(mv2, 1);
                                    float mz2 = p.vertexAt(mv2, 2);
                                    
                                    Intersectionf.findClosestPointOnLineSegment(mx1, my1, mz1, mx2, my2, mz2, ex, ey, ez, this.v);
                
                                    if(this.v.distance(ex, ey, ez) < 8) {
                                        if(normal.dot(nx, ny, nz) < 0.4f) {
                                            edgeNormal.add(normal);
                                            edge = true;
                                        }
                                    }
                                }
                            }
                        }

                        if(edge) {
                            edgeNormal.normalize().mul(8);
                            zero++;
                        }
                        texel.edge = edge;

                        if(surface.emitsLight) {
                            color.set(1, 1, 1);
                        } else {
                            position.set(ex, ey, ez);
                            normal.set(nx, ny, nz);
                            color.set(surface.ambientColor);

                            for(Light light : lights) {
                                light.position.sub(position, offset);
                                offset.normalize(lightNormal);

                                float dI = Math.max(0, Math.min(1, lightNormal.dot(normal)));
                                float atten = 1 - Math.min(1, offset.length() / light.radius);
                                float shadow = 1;

                                for(int xs = -4; xs != 5; xs++) {
                                    for(int ys = -4; ys != 5; ys++) {
                                        origin.set(position).add(normal).add(edgeNormal).add(ux * xs * 4 + vx * ys * 4, uy * xs * 4 + vy * ys * 4, uz * xs * 4 + vz * ys * 4);
                                        light.position.sub(origin, direction);
                                        time[0] = direction.length();
                                        direction.normalize();
                                        for(Triangle triangle : triangles) {
                                            if(triangle.intersects(origin, direction, 0, time)) {
                                                shadow -= 1 / (9.0f * 9.0f);
                                                break;
                                            }
                                        }
                                    }
                                }
                                color2.set(surface.diffuseColor).mul(light.color).mul(atten * dI * shadow);
                                color.add(color2);
                            }
                        }

                        float max = Math.max(color.x, Math.max(color.y, color.z));

                        if(max > 1) {
                            color.div(max);
                        }
     
                        texel.color.set(color);
                        texel.emitsLight = surface.emitsLight;

                        texels.add(texel);
                    }
                }

                px += w;
            }
        }
        Log.log(1, "edge texels = " + zero);

        for(Texel texel : texels) {
            int i = texel.y * width * 4 + texel.x * 4;

            if(texel.edge && !texel.emitsLight) {
                texel.color.mul(ambientFactor);
            } 

            byte r = (byte)((int)(texel.color.x * 255) & 0xFF);
            byte g = (byte)((int)(texel.color.y * 255) & 0xFF);
            byte b = (byte)((int)(texel.color.z * 255) & 0xFF);

            buf.put(i++, r);
            buf.put(i++, g);
            buf.put(i, b);
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] rgba = new int[width * height];

        for(int x = 0; x != width; x++) {
            for(int y = 0; y != height; y++) {
                int i = y * width * 4 + x * 4;
                int r = ((int)buf.get(i++)) & 0xFF;
                int g = ((int)buf.get(i++)) & 0xFF;
                int b = ((int)buf.get(i)) & 0xFF;
                
                rgba[y * width + x] = 0xFF000000 | ((r << 16) & 0xFF0000) | ((g << 8) & 0xFF00) | (b & 0xFF);
            }
        }
        image.setRGB(0, 0, width, height, rgba, 0, width);
        
        ImageIO.write(image, "png", file);

        texture.destroy();

        return true;
    }
}
