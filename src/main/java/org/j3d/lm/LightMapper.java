package org.j3d.lm;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Vector;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;

import org.j3d.Log;
import org.j3d.Mesh;
import org.j3d.MeshData;
import org.j3d.MeshDataVertex;
import org.j3d.MeshPart;
import org.j3d.OctTree;
import org.j3d.PixelFormat;
import org.j3d.Texture;
import org.j3d.Triangle;
import org.joml.GeometryUtils;
import org.joml.Intersectionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

public class LightMapper {

    private final Vector3f normal = new Vector3f();
    private final Vector3f edgeNormal = new Vector3f();
    private final Vector3f v = new Vector3f();
    private final Vector3f origin = new Vector3f();
    private final Vector3f direction = new Vector3f();
    private final Vector3f color = new Vector3f();
    private final float[] time = new float[1];
    
    public boolean map(File file, File sampleFile, int width, int height, int blurPasses, float ambientFactor, Mesh mesh) throws Exception {
        int px = 0;
        int py = 0;
        int mh = 0;
        int w = 0;
        int h = 0;
        Vector<Texel> texels = null;
        Vector<int[]> rects = null;
        Texture texture = null;
        ByteBuffer buf =  null;
        MeshData samples = null;

        if(!file.exists()) {
            buf = BufferUtils.createByteBuffer(width * height * 4);
            for(int i = 0; i != buf.capacity(); i += 4) {
                buf.put((byte)255);
                buf.put((byte)255);
                buf.put((byte)255);
                buf.put((byte)255);
            }
            buf.flip();
            texture = new Texture(null, width, height, PixelFormat.COLOR, buf);
            samples = new MeshData(sampleFile);
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

                boolean calc = part.getFaceVertexCount(i) == 4;

                if(calc) {
                    calc = false;
                    for(int j = 0; j != 4; j++) {
                        int e1 = part.faceVertexAt(i, j);
                        int e2 = part.faceVertexAt(i, (j + 1) % 4);
                        int e3 = part.faceVertexAt(i, (j + 2) % 4);
                        float ex1 = part.vertexAt(e1, 0);
                        float ey1 = part.vertexAt(e1, 1);
                        float ez1 = part.vertexAt(e1, 2);
                        float ex2 = part.vertexAt(e2, 0);
                        float ey2 = part.vertexAt(e2, 1);
                        float ez2 = part.vertexAt(e2, 2);
                        float ex3 = part.vertexAt(e3, 0);
                        float ey3 = part.vertexAt(e3, 1);
                        float ez3 = part.vertexAt(e3, 2);

                        ex1 = ex1 - ex2;
                        ey1 = ey1 - ey2;
                        ez1 = ez1 - ez2;
                        ex2 = ex2 - ex3;
                        ey2 = ey2 - ey3;
                        ez2 = ez2 - ez3;

                        float el1 = Vector3f.length(ex1, ey1, ez1);
                        float el2 = Vector3f.length(ex2, ey2, ez2);

                        ex1 /= el1;
                        ey1 /= el1;
                        ez1 /= el1;
                        ex2 /= el2;
                        ey2 /= el2;
                        ez2 /= el2;

                        float angle = (float)(Math.acos(Math.max(-0.99f, Math.min(0.99f, ex1 * ex2 + ey1 * ey2 + ez1 * ez2))) * 180 / Math.PI);

                        if(Math.abs(angle - 90) > 1) {
                            calc = true;
                            break;
                        }
                    }
                }

                int minX = 0, minY = 0;

                if(calc) {
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

                if(calc) {
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
        int samplesTested = 0;
        Vector<Triangle> triangles =new Vector<>();

        for(MeshPart part : mesh) {
            for(int i = 0; i != part.getFaceCount(); i++) {
                Triangle triangle = new Triangle();

                part.triangleAt(i, triangle);
                triangle.data = part.faceDataAt(i);
                triangles.add(triangle);
            }
        }

        OctTree tree = OctTree.create(triangles, 16);

        for(MeshPart part : mesh) {
            DualTextureMaterial material = (DualTextureMaterial)part.material;

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
                        int pix = ty * width * 4 + tx * 4;
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

                        if(material.emitsLight) {
                            Vector3f lightColor = ((Surface)part.faceDataAt(i)).color;
                            byte r = (byte)((int)(lightColor.x * 255) & 0xFF);
                            byte g = (byte)((int)(lightColor.y * 255) & 0xFF);
                            byte b = (byte)((int)(lightColor.z * 255) & 0xFF);
                            float max = Math.max(lightColor.x, Math.max(lightColor.y, lightColor.z));

                            buf.put(pix++, r);
                            buf.put(pix++, g);
                            buf.put(pix, b);
                            texel.color.set(lightColor);
                            texel.emitsLight = true;

                            if(max > 1) {
                                texel.color.div(max);
                            }
                        } else {
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
                                                edgeNormal.set(normal);
                                                edge = true;
                                                break;
                                            }
                                        }
                                    }
                                    if(edge) {
                                        break;
                                    }
                                }
                                if(edge) {
                                    break;
                                }
                            }

                            if(edge) {
                                edgeNormal.mul(8);
                                zero++;
                            }
                            texel.edge = edge;

                            int count = 0;

                            origin.set(ex, ey, ez).add(edgeNormal).add(normal.set(nx, ny, nz));
                            color.zero();

                            for(MeshDataVertex sample : samples.parts.get(0).vertices) {
                                float d = direction.set(sample.normal).dot(normal);

                                if(d > 0.1f) {
                                    Triangle triangle;

                                    time[0] = Float.MAX_VALUE;
                                    if((triangle = intersect(tree)) != null) {
                                        Surface surface = (Surface)triangle.data;
                                        float atten = (d - 0.1f) / 0.9f;

                                        if(surface.emitsLight) {
                                            atten *= 1 - Math.min(1, time[0] / surface.lightRadius);
                                        }

                                        float r = surface.color.x * atten;
                                        float g = surface.color.y * atten;
                                        Float b = surface.color.z * atten;

                                        color.add(r, g, b);
                                        count++;
                                    }
                                }
                            }
                            samplesTested = count;

                            color.div(count);

                            float max = Math.max(color.x, Math.max(color.y, color.z));

                            if(max > 1) {
                                color.div(max);
                            }
                            texel.color.set(color);

                            byte r = (byte)((int)(color.x * 255) & 0xFF);
                            byte g = (byte)((int)(color.y * 255) & 0xFF);
                            byte b = (byte)((int)(color.z * 255) & 0xFF);

                            buf.put(pix++, r);
                            buf.put(pix++, g);
                            buf.put(pix, b);
                        }
                        texels.add(texel);
                    }
                }

                px += w;
            }
        }
        Log.log(1, "edge texels = " + zero + ", samples tested = " + samplesTested);

        float radius = Vector2f.length(16, 16) + 0.01f;

        for(Texel texel : texels) {
            int i = 0;
            for(Texel texel2 : texels) {
                if(texel != texel2) {
                    if(texel.normal.dot(texel2.normal) > 0.9) {
                        if(texel.position.distance(texel2.position) <= radius) {
                            texel.adjacent.add(i);
                        }
                    }
                }
                i++;
            }
        }

        Vector<Texel> newTexels = new Vector<>(texels.size());

        for(int i = 0; i < blurPasses; i++) {
            for(Texel texel : texels) {
                Texel newTexel = new Texel();

                newTexel.color.set(texel.color);
                newTexel.x = texel.x;
                newTexel.y = texel.y;
                newTexel.emitsLight = texel.emitsLight;
                newTexel.edge = texel.edge;
                newTexel.position.set(texel.position);
                newTexel.normal.set(texel.normal);
                newTexel.adjacent.addAll(texel.adjacent);
                if(!texel.emitsLight) {
                    int count = 1;

                    for(Integer j : texel.adjacent) {
                        Texel texel2 = texels.get(j);

                        if(!texel2.emitsLight) {
                            Vector3f c = texel2.color;

                            newTexel.color.add(c);
                            count++;
                        }
                    }
                    newTexel.color.div(count);
                }
                newTexels.add(newTexel);
            }
            Vector<Texel> temp = texels;

            texels = newTexels;
            newTexels = temp;
        }

        for(Texel texel : texels) {
            if(!texel.emitsLight) {
                int i = texel.y * width * 4 + texel.x * 4;

                if(texel.edge) {
                    texel.color.mul(ambientFactor);
                } 

                byte r = (byte)((int)(texel.color.x * 255) & 0xFF);
                byte g = (byte)((int)(texel.color.y * 255) & 0xFF);
                byte b = (byte)((int)(texel.color.z * 255) & 0xFF);

                buf.put(i++, r);
                buf.put(i++, g);
                buf.put(i, b);
            }
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

    private Triangle intersect(OctTree tree) {
        float t = time[0];
        Triangle hit = null;

        time[0] = Float.MAX_VALUE;
        if(tree.getBounds().intersects(origin, direction, time)) {
            time[0] = t;
            for(Triangle triangle : tree.getTriangles()) {
                if(triangle.intersects(origin, direction, 0, time)) {
                    hit = triangle;
                }
            }
            for(OctTree child : tree.getChildren()) {
                Triangle r = intersect(child);

                if(r != null) {
                    hit = r;
                }
            }
        } else {
            time[0] = t;
        }
        return hit;
    }
}
