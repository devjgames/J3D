package org.j3d.lm;

import java.io.File;
import java.util.Vector;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;

import org.j3d.Log;
import org.j3d.Mesh;
import org.j3d.MeshPart;
import org.joml.GeometryUtils;
import org.joml.Intersectionf;
import org.joml.Vector3f;

public class LightMapper {

    private final Vector3f v1 = new Vector3f();
    private final Vector3f v2 = new Vector3f();
    private final Vector3f normal = new Vector3f();
    private final Vector3f edgeNormal = new Vector3f();
    
    @SuppressWarnings("unchecked")
    public boolean map(File file, int width, int height, int viewSize, int iterations, Renderer renderer) throws Exception {
        Mesh mesh = renderer.getMesh();
        Texel texel = null;

        if(!file.exists()) {
            texel = new Texel(renderer, width, height, viewSize);
        }

        if(!calcTextureCoordinate(renderer, width, height, texel)) {
            return false;
        }

        if(texel == null) {
            return true;
        }

        texel.projection.identity().perspective(120 * (float)Math.PI / 180, 1, 0.1f, 10000);

        texel.updateTexels();

        for(int iter = 0; iter < iterations; iter++) {
            Log.log(1, "mapping iterarion " + iter);

            int zero = 0;
            int irect = 0;
            Vector<int[]> rects = (Vector<int[]>)mesh.data;

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
                    int px = rect[0];
                    int py = rect[1];
                    int w = rect[2];
                    int h = rect[3];
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

                    if(area < 0) {
                        Log.log(0, "area < 0");
                    }

                    for(int tx = px; tx != px + w; tx++) {
                        for(int ty = py; ty != py + h; ty++) {
                            float s = (tx + 0.5f) / (float)width;
                            float t = (ty + 0.5f) / (float)height;
                            float w0 = (s - t2x) * (t3y - t2y) - (t - t2y) * (t3x - t2x);
                            float w1 = (s - t3x) * (t1y - t3y) - (t - t3y) * (t1x - t3x);
                            float w2 = (s - t1x) * (t2y - t1y) - (t - t1y) * (t2x - t1x);

                            w0 /= area;
                            w1 /= area;
                            w2 /= area;

                            float ex = w0 * x1 + w1 * x3 + w2 * x2;
                            float ey = w0 * y1 + w1 * y3 + w2 * y2;
                            float ez = w0 * z1 + w1 * z3 + w2 * z2;

                            texel.setPixel(tx, ty);
                            texel.direction.set(nx, ny, nz);
                            texel.eye.set(ex, ey, ez);

                            boolean edge = isEdge(renderer, part, irect, ex, ey, ez, nx, ny, nz, edgeNormal);

                            if(edge) {
                                edgeNormal.mul(8);
                                texel.eye.add(edgeNormal);
                                texel.setColor(0, 0, 0);
                                zero++;
                            } 
                            setColor(texel, edge);
                        }
                    }

                    px += w;
                }
            }
            texel.updateTexels();

            Log.log(1, "edge texels = " + zero);
        }


        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] rgba = new int[width * height];

        for(int x = 0; x != width; x++) {
            for(int y = 0; y != height; y++) {
                texel.setPixel(x, y);
                
                rgba[y * width + x] = texel.getColor();
            }
        }
        image.setRGB(0, 0, width, height, rgba, 0, width);
        
        ImageIO.write(image, "png", file);

        texel.destroy();

        return true;
    }

    protected boolean isEdge(Renderer renderer, MeshPart part, int i, float ex, float ey, float ez, float nx, float ny, float nz, Vector3f edgeNormal) {
        Mesh mesh = renderer.getMesh();

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
                    
                    Intersectionf.findClosestPointOnLineSegment(mx1, my1, mz1, mx2, my2, mz2, ex, ey, ez, this.v1);

                    if(this.v1.distance(ex, ey, ez) < 8) {
                        if(normal.dot(nx, ny, nz) < 0.1f) {
                            edgeNormal.set(normal);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    protected boolean calcTextureCoordinate(Renderer renderer, int width, int height, Texel texel) {
        int px = 0;
        int py = 0;
        int mh = 0;
        Mesh mesh = renderer.getMesh();
        Vector<int[]> rects = null;
        
        if(texel != null) {
            rects = new Vector<int[]>();
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

                int w, h, minX = 0, minY = 0;

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
                        return false;
                    }
                    if(py + mh >= height) {
                        return false;
                    }
                    py += mh;
                    mh = 0;
                } else if(py + h >= height) {
                    return false;
                } else {
                    mh = Math.max(mh, h);
                }

                Log.log(4, px + ", " + py + " : " + w + " x " + h);

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

                if(texel != null) {
                    for(int tx = px; tx != px + w; tx++) {
                        for(int ty = py; ty != py + h; ty++) {
                            texel.setPixel(tx, ty);
                            texel.setColor(0, 0, 0);
                        }
                    }
                    rects.add(new int[] { px, py, w, h });
                }
                px += w;
            }
            part.bufferVertices(false);
        }
        mesh.data = rects;

        return true;
    }

    protected void setColor(Texel texel, boolean edge) {
        GeometryUtils.perpendicular(texel.direction, v1, v2);
        v1.normalize();
        v2.normalize();

        texel.eye.add(texel.direction);
        texel.view.identity().lookAt(texel.eye.x, texel.eye.y, texel.eye.z, texel.eye.x + texel.direction.x, texel.eye.y + texel.direction.y, texel.eye.z + texel.direction.z, v1.x, v1.y, v1.z);
        texel.render();

        float r = 0;
        float g = 0;
        float b = 0;
        int n = 0;

        for(int j = 0; j != texel.pixels.capacity(); j++, n++) {
            r += texel.pixels.get(j++);
            g += texel.pixels.get(j++);
            b += texel.pixels.get(j++);
        }
        r /= n;
        g /= n;
        b /= n;

        if(edge) {
            r *= 0.75f;
            g *= 0.75f;
            b *= 0.75f;
        }

        texel.setColor(r, g, b);
    }
}
