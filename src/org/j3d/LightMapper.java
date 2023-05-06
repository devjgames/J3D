package org.j3d;

import java.util.*;

import javax.imageio.ImageIO;

import java.io.*;
import java.awt.image.*;

public class LightMapper {

    private Vector<Node> renderables = new Vector<>();
    private Vector<Triangle> triangles = new Vector<>();
    private Vector<Node> lights = new Vector<>();
    private Triangle triangle = new Triangle();

    public void light(File file, Game game, Scene scene, boolean deleteLightMap) throws Exception {
        int[] xy = new int[] { 0, 0 };
        int[] maxH = new int[] { 0 };
        int width = scene.lightMapWidth;
        int height = scene.lightMapHeight;
        Vec2 pixelSize = new Vec2(1, 1).div(width, height);
        int[] pixels = null;
        BufferedImage image = null;

        renderables.clear();
        triangles.clear();
        lights.clear();

        scene.camera.calcTransforms(game.aspectRatio());
        scene.root.calcBoundsAndTransform(scene.camera);

        if(deleteLightMap) {
            if(file.exists()) {
                System.out.println("deleting - " + file);
                file.delete();
            }
        }
        if(!file.exists()) {
            pixels = new int[width * height];
            for(int i = 0; i != pixels.length; i++) {
                pixels[i] = 0xFF000000;
            }
        }

        scene.root.traverse((n) -> {
            if(n.visible) {
                if(n.getMesh() != null && n.lightMapEnabled) {
                    renderables.add(n);
                    if(n.castsShadow) {
                        for(int i = 0; i != n.triangleCount(); i++) {
                            triangles.add(n.triangleAt(scene.camera, i, new Triangle()));
                        }
                    }
                }
                if(n.isLight) {
                    lights.add(n);
                }
                return true;
            }
            return false;
        });

        OctTree tree = null;

        if(!file.exists()) {
            tree = OctTree.create(triangles, 16);
        }
        triangles.clear();

        for(Node renderable : renderables) {
            Mesh mesh = renderable.getMesh();

            for(int i = 0; i != mesh.polygonCount(); i++) {
                Vertex v1 = mesh.vertexAt(mesh.polygonIndexAt(i, 0));
                Vertex v2 = mesh.vertexAt(mesh.polygonIndexAt(i, 1));
                Vertex v3 = mesh.vertexAt(mesh.polygonIndexAt(i, 2));
                Vec3 e1 = new Vec3();
                Vec3 e2 = new Vec3();
                Vec3 p1 = new Vec3(v1.position).transform(renderable.model, new Vec3(v1.position));
                Vec3 p2 = new Vec3(v2.position).transform(renderable.model, new Vec3(v2.position));
                Vec3 p3 = new Vec3(v3.position).transform(renderable.model, new Vec3(v3.position));
                Vec3 n1 = new Vec3(v1.normal).transform(renderable.modelIT, v1.normal).normalize();
                Vec3 n2 = new Vec3(v2.normal).transform(renderable.modelIT, v2.normal).normalize();
                Vec3 n3 = new Vec3(v3.normal).transform(renderable.modelIT, v3.normal).normalize();
                Vec3 normal = new Vec3();

                p2.sub(p1, e1).normalize();
                p3.sub(p2, e2).normalize();
                e1.cross(e2, normal).normalize();
                normal.cross(e1, e2).normalize();

                float x1 = Float.MAX_VALUE;
                float y1 = Float.MAX_VALUE;
                float x2 = -Float.MAX_VALUE;
                float y2 = -Float.MAX_VALUE;

                for(int j = 0; j != mesh.polygonIndexCount(i); j++) {
                    Vertex v = mesh.vertexAt(mesh.polygonIndexAt(i, j));
                    Vec3 p = new Vec3(v.position).transform(renderable.model, new Vec3(v.position));
                    float x = p.dot(e1);
                    float y = p.dot(e2);

                    x1 = Math.min(x, x1);
                    y1 = Math.min(y, y1);
                    x2 = Math.max(x, x2);
                    y2 = Math.max(y, y2);
                }

                int w = (int)(x2 - x1) / 16 + 1;
                int h = (int)(y2 - y1) / 16 + 1;

                w = Math.max(1, w);
                h = Math.max(1, h);

                if(!allocate(xy, w, h, maxH, width, height)) {
                    throw new Exception("failed to allocate light map tile");
                }
                for(int j = 0; j != mesh.polygonIndexCount(i); j++) {
                    Vertex v = mesh.vertexAt(mesh.polygonIndexAt(i, j));
                    Vec3 p = new Vec3(v.position).transform(renderable.model, new Vec3(v.position));
                    float x = (int)p.dot(e1);
                    float y = (int)p.dot(e2);

                    x = (int)(x - x1) / 16;
                    y = (int)(y - y1) / 16;
                    x = (xy[0] + x) * pixelSize.x;
                    y = (xy[1] + y) * pixelSize.y;
                    v.textureCoordinate2.set(x, y);
                }

                Vec2 t1 = mesh.vertexAt(mesh.polygonIndexAt(i, 0)).textureCoordinate2;
                Vec2 t2 = mesh.vertexAt(mesh.polygonIndexAt(i, 2)).textureCoordinate2;
                Vec2 t3 = mesh.vertexAt(mesh.polygonIndexAt(i, 1)).textureCoordinate2;

                float area = (t3.x - t1.x) * (t2.y - t1.y) - (t3.y - t1.y) * (t2.x - t1.x);

                if(area < 0) {
                    System.out.println("area < 0");
                }

                Vec3 n = new Vec3();

                if(!file.exists()) {
                    System.out.println("rendering light " + w + " x " + h + " ...");

                    Vec3 p = new Vec3();
                    Vec4 c = new Vec4();
                    for(int x = xy[0]; x != xy[0] + w; x++) {
                        for(int y = xy[1]; y != xy[1] + h; y++) {
                            float tx = x / (float)width;
                            float ty = y / (float)height;
                            float w0 = (tx - t2.x) * (t3.y - t2.y) - (ty - t2.y) * (t3.x - t2.x);
                            float w1 = (tx - t3.x) * (t1.y - t3.y) - (ty - t3.y) * (t1.x - t3.x);
                            float w2 = (tx - t1.x) * (t2.y - t1.y) - (ty - t1.y) * (t2.x - t1.x);
                            w0 /= area;
                            w1 /= area;
                            w2 /= area;
                            p.x = w0 * p1.x + w1 * p3.x + w2 * p2.x;
                            p.y = w0 * p1.y + w1 * p3.y + w2 * p2.y;
                            p.z = w0 * p1.z + w1 * p3.z + w2 * p2.z;
                            n.x = w0 * n1.x + w1 * n3.x + w2 * n2.x;
                            n.y = w0 * n1.y + w1 * n3.y + w2 * n2.y;
                            n.z = w0 * n1.z + w1 * n3.z + w2 * n2.z;
                            n.normalize();
                            c.set(renderable.ambientColor);

                            for(Node light : lights) {
                                Vec3 lOffset = new Vec3();
                                Vec3 lNormal = new Vec3();

                                light.absolutePosition.sub(p, lOffset);
                                lNormal.normalize(lOffset);

                                float atten = 1.0f - Math.min(lOffset.length() / light.lightRadius, 1.0f);
                                float dI = n.dot(lNormal);

                                if(atten < 1.0f && dI > 0.0f) {
                                    float sV = 1.0f;
                                    if(renderable.receivesShadow) {
                                        lNormal.scale(2);
                                        Random random = new Random(1000);
                                        float sF = 1.0f / light.lightSampleCount;
                                        Vec3 lP = light.absolutePosition;
                                        AABB bounds = new AABB();
                                        Vec3 point = new Vec3();
                                        for(int s = 0; s != light.lightSampleCount; s++) {
                                            Vec3 origin = new Vec3(p).add(lNormal);
                                            float oX = random.nextFloat() * 2 - 1;
                                            Float oY = random.nextFloat() * 2 - 1;
                                            Float oZ = random.nextFloat() * 2 - 1;
                                            Vec3 direction = new Vec3(oX, oY, oZ).scale(light.lightSampleRadius).add(lP).sub(origin);
                                            float[] time = new float[] { direction.length() };
                                            direction.normalize();
                                            if(inShadow(tree, bounds, point, origin, direction, time, triangle)) {
                                                sV -= sF;
                                            }
                                        }
                                    }
                                    Vec4 diff = new Vec4(renderable.diffuseColor);
                                    
                                    diff.mul(light.lightColor);
                                    diff.scale(sV * dI * atten);
                                    c.add(diff);
                                }
                            }
                            float max = Math.max(c.x, Math.max(c.y, c.z));
                            if(max > 1) {
                                c.div(max);
                            }
                            int r = (int)(c.x * 255);
                            int g = (int)(c.y * 255);
                            int b = (int)(c.z * 255);
                            pixels[y * width + x] =  0xFF000000 | ((r << 16) & 0xFF0000) | ((g << 8) & 0xFF00) | (b & 0xFF);
                        }
                    }
                }
                xy[0] += w;
            }
        }
        if(!file.exists()) {
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            image.setRGB(0, 0, width, height, pixels, 0, width);
            ImageIO.write(image, "PNG", file);
        }
        game.assets().unload(file);
        for(Node renderable : renderables) {
            renderable.texture2 = game.assets().load(file);
        }

        renderables.clear();
        lights.clear();
    }

    private boolean inShadow(OctTree tree, AABB bounds, Vec3 point, Vec3 origin, Vec3 direction, float[] time, Triangle triangle) {
        bounds.clear();
        bounds.add(origin);
        bounds.add(point.set(direction).scale(time[0]).add(origin));
        if(tree.bounds.touches(bounds)) {
            for(int i = 0; i != tree.triangleCount(); i++) {
                if(tree.triangleAt(i, triangle).intersects(origin, direction, 0, time)) {
                    return true;
                }
            }
            for(OctTree child : tree) {
                if(inShadow(child, bounds, point, origin, direction, time, triangle)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean allocate(int[] xy, int w, int h, int[] maxH, int lmW, int lmH) {
        if(xy[0] + w < lmW) {
            maxH[0] = Math.max(maxH[0], h);
        } else {
            xy[0] = 0;
            xy[1] += maxH[0];
            maxH[0] = h;
        }
        return xy[1] + h < lmH;
    }
}