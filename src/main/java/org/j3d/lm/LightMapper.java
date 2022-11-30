package org.j3d.lm;

import java.io.File;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;

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
    
    public boolean map(File file, int width, int height, int viewSize, int iterations, Renderer renderer, int logLevel) throws Exception {
        Mesh mesh = renderer.getMesh();
        int px = 0;
        int py = 0;
        int mh = 0;
        Texel texel = null;

        if(!file.exists()) {
            texel = new Texel(renderer, width, height, viewSize);
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

                int minX = (int)(lx * 16);
                int minY = (int)(ly * 16);

                int w = (int)(hx - lx) + 1;
                int h = (int)(hy - ly) + 1;

                if(px + w >= width) {
                    px = 0;
                    if(px + w >= width) {
                        if(texel != null) {
                            texel.destroy();
                        }
                        return false;
                    }
                    if(py + mh >= height) {
                        if(texel != null) {
                            texel.destroy();
                        }
                        return false;
                    }
                    py += mh;
                    mh = 0;
                } else if(py + h >= height) {
                    if(texel != null) {
                        texel.destroy();
                    }
                    return false;
                } else {
                    mh = Math.max(mh, h);
                }

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

                if(texel != null) {
                    for(int tx = px; tx != px + w; tx++) {
                        for(int ty = py; ty != py + h; ty++) {
                            texel.setPixel(tx, ty);
                            texel.setColor(0, 0, 0);
                        }
                    }
                }

                px += w;
            }
            part.bufferVertices(false);
        }

        if(texel == null) {
            return true;
        }

        texel.projection.identity().perspective(120 * (float)Math.PI / 180, 1, 0.1f, 10000);

        texel.updateTexels();

        for(int iter = 0; iter < iterations; iter++) {
            if(logLevel > 0) {
                System.out.println("mapping iterarion " + iter);
            }

            px = 0;
            py = 0;
            mh = 0;

            int zero = 0;
            
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
    
                    int w = (int)(hx - lx) + 1;
                    int h = (int)(hy - ly) + 1;

                    if(px + w >= width) {
                        px = 0;
                        if(px + w >= width) {
                            if(texel != null) {
                                texel.destroy();
                            }
                            return false;
                        }
                        if(py + mh >= height) {
                            if(texel != null) {
                                texel.destroy();
                            }
                            return false;
                        }
                        py += mh;
                        mh = 0;
                    } else if(py + h >= height) {
                        if(texel != null) {
                            texel.destroy();
                        }
                        return false;
                    } else {
                        mh = Math.max(mh, h);
                    }
    
                    float t1x = part.vertexAt(v1, 5);
                    float t1y = part.vertexAt(v1, 6);
                    float t2x = part.vertexAt(v3, 5);
                    float t2y = part.vertexAt(v3, 6);
                    float t3x = part.vertexAt(v2, 5);
                    float t3y = part.vertexAt(v2, 6);
                    float area = (t3x - t1x) * (t2y - t1y) - (t3y - t1y) * (t2x - t1x);

                    if(area < 0) {
                        System.out.println("area < 0");
                    }

                    if(logLevel > 1) {
                        System.out.println(px + ", " + py + " : " + w + " x " + h);
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

                            boolean edge = false;

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
                                            edge = normal.dot(nx, ny, nz) < 0.1f;
                                            edgeNormal.set(normal);
                                            break;
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

                            texel.setPixel(tx, ty);
                            texel.direction.set(nx, ny, nz);
                            texel.eye.set(ex, ey, ez);

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

            if(logLevel > 0) {
                System.out.println("edge texels = " + zero);
            }
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
