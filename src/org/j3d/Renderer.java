package org.j3d;

import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;

public final class Renderer {

    private class Processor implements Callable<Void> {

        private final Renderer renderer;
        private final int processor;

        public Processor(Renderer renderer, int processor) {
            this.renderer = renderer;
            this.processor = processor;
        }

        @Override
        public Void call() {
            renderer.step = renderer.processors.size();
            renderer.fill(processor);
            return null;
        }
    }

    public CullState cullState = CullState.BACK;
    public boolean depthTestEnabled = true;
    public boolean depthWriteEnabled = true;
    public boolean blendEnabled = false;
    public boolean additiveBlend = false;
    public boolean maskEnabled = false;
    public Texture texture = null;
    public Texture texture2 = null;
    public final Vec3 backgroundColor = new Vec3(0.25f, 0.25f, 0.25f);
    public final Mat4 projection = new Mat4();
    public final Mat4 view = new Mat4();
    public final Mat4 model = new Mat4();
    public final Game game;

    private float[] zBuffer;
    private final Vec4 p1 = new Vec4();
    private final Vec4 p2 = new Vec4();
    private final Vec4 p3 = new Vec4();
    private final Vec2 t1 = new Vec2();
    private final Vec2 t2 = new Vec2();
    private final Vec2 t3 = new Vec2();
    private final Vec2 s1 = new Vec2();
    private final Vec2 s2 = new Vec2();
    private final Vec2 s3 = new Vec2();
    private final Frustum frustum = new Frustum();
    private final Vertex[] inVerts = new Vertex[32];
    private final Vertex[] outVerts = new Vertex[32];
    private final float[] side = new float[32];
    private final Mat4 pv = new Mat4();
    private final Vec4[] planes = new Vec4[6];
    private final Vertex v1 = new Vertex();
    private final Vertex v2 = new Vertex();
    private final Vertex v3 = new Vertex();
    private final Vector<Processor> processors;
    private final ForkJoinPool pool;

    private int step;
    private int x1, y1, x2, y2;
    private float r1, g1, b1, a1;
    private float r2, g2, b2, a2;
    private float r3, g3, b3, a3;
    private float p1w;
    private float p2w;
    private float p3w;
    private float z1;
    private float z2;
    private float z3;
    private int[] p;
    private int pixel;
    private int w;
    private int[] pixels;
    private int[] pixels2;
    private int tw, th, tw2, th2;
    private float area;

    public Renderer(Game game) {
        this.game = game;

        zBuffer = new float[game.w() * game.h()];

        for (int i = 0; i != side.length; i++) {
            inVerts[i] = new Vertex();
            outVerts[i] = new Vertex();
        }
        pool = ForkJoinPool.commonPool();
        processors = new Vector<>(Math.max(1, pool.getParallelism()));
        for(int i = 0; i != processors.capacity(); i++) {
            processors.add(new Processor(this, i));
        }
        System.out.println("render processors = " + processors.size());
    }

    void resize() {
        zBuffer = new float[game.w() * game.h()];
    }

    public void clear() {
        int[] colorBuffer = game.colorBuffer();
        int r = (int) (backgroundColor.x * 255) & 0xFF;
        int g = (int) (backgroundColor.y * 255) & 0xFF;
        int b = (int) (backgroundColor.z * 255) & 0xFF;

        cullState = CullState.BACK;
        depthTestEnabled = true;
        depthWriteEnabled = true;
        blendEnabled = false;
        additiveBlend = false;
        texture = null;
        texture2 = null;
        maskEnabled = false;
        for (int i = 0; i != zBuffer.length; i++) {
            zBuffer[i] = 1.0f;
            colorBuffer[i] = 0xFF000000 | ((r << 16) & 0xFF0000) | ((g << 8) & 0xFF00) | (b & 0xFF);
        }
    }

    public int render(Vertex[] vertices, int[] indices, int indexCount) {
        int rendered = 0;
        float w1, w2, w3;

        frustum.set(projection, view);
        pv.set(projection).mul(view);

        planes[0] = frustum.l;
        planes[1] = frustum.r;
        planes[2] = frustum.b;
        planes[3] = frustum.t;
        planes[4] = frustum.n;
        planes[5] = frustum.f;

        for (int i = 0; i != indexCount; i += 3) {
            int i1 = indices[i + 0];
            int i2 = indices[i + 1];
            int i3 = indices[i + 2];
            int n = 3;
            Vertex v1 = vertices[i1];
            Vertex v2 = vertices[i2];
            Vertex v3 = vertices[i3];

            Vertex[] in = inVerts;
            Vertex[] out = outVerts;
            Vertex[] tmp;
            inVerts[0].set(v1);
            inVerts[1].set(v2);
            inVerts[2].set(v3);

            inVerts[0].position.transform(model, inVerts[0].position);
            inVerts[1].position.transform(model, inVerts[1].position);
            inVerts[2].position.transform(model, inVerts[2].position);

            for (int j = 0; j != 6; j++) {
                n = clipVerts(planes[j], in, n, out, side);
                if (n == 0) {
                    break;
                }
                tmp = in;
                in = out;
                out = tmp;
            }

            if (n != 0) {
                int tris = n - 2;
                for (int j = 0; j != tris; j++) {
                    this.v1.set(in[0]);
                    this.v2.set(in[j + 1]);
                    this.v3.set(in[j + 2]);

                    this.v1.position.w = 1;
                    this.v2.position.w = 1;
                    this.v3.position.w = 1;
                    this.v1.position.transform(pv, this.v1.position);
                    this.v2.position.transform(pv, this.v2.position);
                    this.v3.position.transform(pv, this.v3.position);
                    w1 = this.v1.position.w;
                    w2 = this.v2.position.w;
                    w3 = this.v3.position.w;
                    this.v1.position.div(w1);
                    this.v2.position.div(w2);
                    this.v3.position.div(w3);
                    this.v1.position.w = w1;
                    this.v2.position.w = w2;
                    this.v3.position.w = w3;

                    this.v1.position.x = (this.v1.position.x * 0.5f + 0.5f) * game.w();
                    this.v1.position.y = game.h() - ((this.v1.position.y * 0.5f + 0.5f) * game.h());

                    this.v2.position.x = (this.v2.position.x * 0.5f + 0.5f) * game.w();
                    this.v2.position.y = game.h() - ((this.v2.position.y * 0.5f + 0.5f) * game.h());

                    this.v3.position.x = (this.v3.position.x * 0.5f + 0.5f) * game.w();
                    this.v3.position.y = game.h() - ((this.v3.position.y * 0.5f + 0.5f) * game.h());

                    rendered += render(this.v1, this.v2, this.v3);
                }
            }
        }
        return rendered;
    }

    public void render(Texture image, int sX, int sY, int sW, int sH, int dX, int dY, Vec4 color) {
        render(image, sX, sY, sW, sH, dX, dY, color.x, color.y, color.z, color.w);
    }

    public void render(Texture image, int sX, int sY, int sW, int sH, int dX, int dY, float r, float g, float b, float a) {
        int cR = (int) (r * 255) & 0xFF;
        int cG = (int) (g * 255) & 0xFF;
        int cB = (int) (b * 255) & 0xFF;
        int cA = (int) (a * 255) & 0xFF;
        int sX1 = sX;
        int sY1 = sY;
        int sX2 = sX + sW;
        int sY2 = sY + sH;
        int[] pixels = image.pixels;
        int[] dPixels = game.colorBuffer();
        int iW = image.w;
        int vW = game.w();
        int vH = game.h();

        for (int x = sX1, wX = dX; x < sX2; x++, wX++) {
            for (int y = sY1, wY = dY; y < sY2; y++, wY++) {
                if (wX >= 0 && wX < vW && wY >= 0 && wY < vH) {
                    int pixel = pixels[y * iW + x];
                    int sR = (pixel >> 16) & 0xFF;
                    int sG = (pixel >> 8) & 0xFF;
                    int sB = pixel & 0xFF;
                    int sA = (pixel >> 24) & 0xFF;
                    int i = wY * vW + wX;
                    int dPixel = dPixels[i];
                    int dR = (dPixel >> 16) & 0xFF;
                    int dG = (dPixel >> 8) & 0xFF;
                    int dB = dPixel & 0xFF;
                    sR = sR * cR / 255;
                    sG = sG * cG / 255;
                    sB = sB * cB / 255;
                    sA = sA * cA / 255;
                    dR = (255 - sA) * dR / 255 + sA * sR / 255;
                    dG = (255 - sA) * dG / 255 + sA * sG / 255;
                    dB = (255 - sA) * dB / 255 + sA * sB / 255;
                    dPixels[i] = 0xFF000000 | ((dR << 16) & 0xFF0000) | ((dG << 8) & 0xFF00) | (dB & 0xFF);
                }
            }
        }
    }

    public void render(Texture font, String text, int cW, int cH, int cols, int lineSpacing, int x, int y, Vec4 color) {
        render(font, text, cW, cH, cols, lineSpacing, x, y, color.x, color.y, color.z, color.w);
    }

    public void render(Texture font, String text, int cW, int cH, int cols, int lineSpacing, int x, int y, float r, float g, float b, float a) {
        int sX = x;
        for (int i = 0; i != text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                x = sX;
                y += lineSpacing;
                y += cH;
            } else {
                int j = (int) c - (int) ' ';
                if (j >= 0 && j < 100) {
                    int col = j % cols;
                    int row = j / cols;
                    render(font, col * cW, row * cH, cW, cH, x, y, r, g, b, a);
                    x += cW;
                }
            }
        }
    }

    private int render(Vertex v1, Vertex v2, Vertex v3) {
        area = (v3.position.x - v1.position.x) * (v2.position.y - v1.position.y) - (v3.position.y - v1.position.y) * (v2.position.x - v1.position.x);
        if (area <= 0) {
            if (cullState == CullState.BACK) {
                return 0;
            }
            Vertex t = v1;
            v1 = v3;
            v3 = t;
            area = (v3.position.x - v1.position.x) * (v2.position.y - v1.position.y) - (v3.position.y - v1.position.y) * (v2.position.x - v1.position.x);
        } else if (cullState == CullState.FRONT) {
            return 0;
        }
        area = 1 / area;

        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = -minX;
        float maxY = -minY;

        minX = Math.min(v1.position.x, minX);
        minX = Math.min(v2.position.x, minX);
        minX = Math.min(v3.position.x, minX);
        minY = Math.min(v1.position.y, minY);
        minY = Math.min(v2.position.y, minY);
        minY = Math.min(v3.position.y, minY);
        maxX = Math.max(v1.position.x, maxX);
        maxX = Math.max(v2.position.x, maxX);
        maxX = Math.max(v3.position.x, maxX);
        maxY = Math.max(v1.position.y, maxY);
        maxY = Math.max(v2.position.y, maxY);
        maxY = Math.max(v3.position.y, maxY);

        x1 = (int) Math.floor(minX);
        y1 = (int) Math.floor(minY);
        x2 = (int) Math.round(maxX);
        y2 = (int) Math.round(maxY);

        x1 = Math.max(0, Math.min(game.w(), x1));
        x2 = Math.max(0, Math.min(game.w(), x2));
        y1 = Math.max(0, Math.min(game.h(), y1));
        y2 = Math.max(0, Math.min(game.h(), y2));

        p1w = 1 / v1.position.w;
        p2w = 1 / v2.position.w;
        p3w = 1 / v3.position.w;
        z1 = v1.position.z;
        z2 = v2.position.z;
        z3 = v3.position.z;
        p = game.colorBuffer();
        w = game.w();
        pixels = null;
        pixels2 = null;

        if (texture != null) {
            tw = texture.w;
            th = texture.h;
            pixels = texture.pixels;
        }
        if(texture2 != null) {
            tw2 = texture2.w;
            th2 = texture2.h;
            pixels2 = texture2.pixels;
        }

        r1 = Math.max(0, Math.min(1, v1.color.x) * p1w);
        g1 = Math.max(0, Math.min(1, v1.color.y) * p1w);
        b1 = Math.max(0, Math.min(1, v1.color.z) * p1w);
        a1 = Math.max(0, Math.min(1, v1.color.w) * p1w);

        r2 = Math.max(0, Math.min(1, v2.color.x) * p2w);
        g2 = Math.max(0, Math.min(1, v2.color.y) * p2w);
        b2 = Math.max(0, Math.min(1, v2.color.z) * p2w);
        a2 = Math.max(0, Math.min(1, v2.color.w) * p2w);
        
        r3 = Math.max(0, Math.min(1, v3.color.x) * p3w);
        g3 = Math.max(0, Math.min(1, v3.color.y) * p3w);
        b3 = Math.max(0, Math.min(1, v3.color.z) * p3w);
        a3 = Math.max(0, Math.min(1, v3.color.w) * p3w);

        p1.set(v1.position);
        p2.set(v2.position);
        p3.set(v3.position);

        v1.textureCoordinate.scale(p1w, t1);
        v2.textureCoordinate.scale(p2w, t2);
        v3.textureCoordinate.scale(p3w, t3);
        v1.textureCoordinate2.scale(p1w, s1);
        v2.textureCoordinate2.scale(p2w, s2);
        v3.textureCoordinate2.scale(p3w, s3);

        int n = (x2 - x1) * (y2 - y1);

        if(n <= 0) {
            return 0;
        } else if(n > 1000 && processors.size() > 1) {
            pool.invokeAll(processors);
        } else {
            step = 1;
            fill(0);
        }
        return 1;
    }

    private int clipVerts(Vec4 plane, Vertex[] in, int n, Vertex[] out, float[] side) {
        int nout, i;
        float amount;
        if (n < 3) {
            return 0;
        }
        for (i = 0; i != n; i++) {
            side[i] = Vec3.dot(in[i].position.x, in[i].position.y, in[i].position.z, plane.x, plane.y, plane.z) + plane.w;
        }
        nout = 0;
        for (i = 0; i != n - 1; i++) {
            if (side[i] >= 0) {
                out[nout++].set(in[i]);
            }
            if (Math.signum(side[i]) != Math.signum(side[i + 1])) {
                amount = side[i] / (side[i] - side[i + 1]);
                in[i].position.lerp(in[i + 1].position, amount, out[nout].position);
                in[i].textureCoordinate.lerp(in[i + 1].textureCoordinate, amount, out[nout].textureCoordinate);
                in[i].textureCoordinate2.lerp(in[i + 1].textureCoordinate2, amount, out[nout].textureCoordinate2);
                in[i].color.lerp(in[i + 1].color, amount, out[nout].color);
                nout++;
            }
        }
        if (side[n - 1] >= 0) {
            out[nout++].set(in[n - 1]);
        }
        if (Math.signum(side[0]) != Math.signum(side[n - 1])) {
            amount = side[0] / (side[0] - side[n - 1]);
            in[0].position.lerp(in[n - 1].position, amount, out[nout].position);
            in[0].textureCoordinate.lerp(in[n - 1].textureCoordinate, amount, out[nout].textureCoordinate);
            in[0].textureCoordinate2.lerp(in[n - 1].textureCoordinate2, amount, out[nout].textureCoordinate2);
            in[0].color.lerp(in[n - 1].color, amount, out[nout].color);
            nout++;
        }
        return nout;
    }   

    private void fill(int off) {
        int xpix, ypix, sr, sg, sb, sa, dr, dg, db, x, y, max;
        float w0, w1, w2, z, d, u, v;
        float p4x, p4y;
        int n = (x2 - x1) * (y2 - y1);
        float r, g, b, a;
        for(int i = off; i < n; i += step) {
            x = i % (x2 - x1) + x1;
            y = i / (x2 - x1) + y1;
            p4x = (float) x + 0.5f;
            p4y = (float) y + 0.5f;
            w0 = (p4x - p2.x) * (p3.y - p2.y) - (p4y - p2.y) * (p3.x - p2.x);
            w1 = (p4x - p3.x) * (p1.y - p3.y) - (p4y - p3.y) * (p1.x - p3.x);
            w2 = (p4x - p1.x) * (p2.y - p1.y) - (p4y - p1.y) * (p2.x - p1.x);
            if (w0 >= 0 && w1 >= 0 && w2 >= 0) {
                w0 *= area;
                w1 *= area;
                w2 *= area;
                z = w0 * z1 + w1 * z2 + w2 * z3;
                if (z < zBuffer[y * w + x] || !depthTestEnabled) {
                    d = w0 * p1w + w1 * p2w + w2 * p3w;
                    r = w0 * r1 + w1 * r2 + w2 * r3;
                    g = w0 * g1 + w1 * g2 + w2 * g3;
                    b = w0 * b1 + w1 * b2 + w2 * b3;
                    a = w0 * a1 + w1 * a2 + w2 * a3;
                    r /= d;
                    g /= d;
                    b /= d;
                    a /= d;
                    sr = (int) (r * 255);
                    sg = (int) (g * 255);
                    sb = (int) (b * 255);
                    sa = (int) (a * 255);
                    if (pixels != null) {
                        u = w0 * t1.x + w1 * t2.x + w2 * t3.x;
                        v = w0 * t1.y + w1 * t2.y + w2 * t3.y;
                        u /= d;
                        v /= d;
                        xpix = (int) ((u - ((u < 0) ? (int) u - 1 : (int) u)) * tw) % tw;
                        ypix = (int) ((v - ((v < 0) ? (int) v - 1 : (int) v)) * th) % th;
                        pixel = pixels[ypix * tw + xpix];
                        sr = sr * ((pixel >> 16) & 0xFF) / 255;
                        sg = sg * ((pixel >> 8) & 0xFF) / 255;
                        sb = sb * (pixel & 0xFF) / 255;
                        sa = sa * ((pixel >> 24) & 0xFF) / 255;
                    }
                    if(pixels2 != null) {
                        u = w0 * s1.x + w1 * s2.x + w2 * s3.x;
                        v = w0 * s1.y + w1 * s2.y + w2 * s3.y;
                        u /= d;
                        v /= d;
                        xpix = (int) ((u - ((u < 0) ? (int) u - 1 : (int) u)) * tw2) % tw2;
                        ypix = (int) ((v - ((v < 0) ? (int) v - 1 : (int) v)) * th2) % th2;
                        pixel = pixels2[ypix * tw2 + xpix];
                        sr = sr * ((pixel >> 16) & 0xFF) / 255;
                        sg = sg * ((pixel >> 8) & 0xFF) / 255;
                        sb = sb * (pixel & 0xFF) / 255;
                    }
                    if (blendEnabled) {
                        pixel = p[y * w + x];
                        dr = (pixel >> 16) & 0xFF;
                        dg = (pixel >> 8) & 0xFF;
                        db = pixel & 0xFF;
                        if(additiveBlend) {
                            sr = dr + sr;
                            sg = dg + sg;
                            sb = db + sb;
                        } else {
                            sr = (255 - sa) * dr / 255 + sa * sr / 255;
                            sg = (255 - sa) * dg / 255 + sa * sg / 255;
                            sb = (255 - sa) * db / 255 + sa * sb / 255;
                        }
                    }

                    boolean mask = maskEnabled;

                    if(mask) {
                        mask = sa == 0;
                    }
                    if(!mask) {
                        max = sr;
                        max = (sg > max) ? sg : max;
                        max = (sb > max) ? sb : max;
                        if(max > 255) {
                            sr = sr * 255 / max;
                            sg = sg * 255 / max;
                            sb = sb * 255 / max;
                        }
                        p[y * w + x] = 0xFF000000 | ((sr << 16) & 0xFF0000) | ((sg << 8) & 0xFF00) | (sb & 0xFF);

                        if (depthWriteEnabled) {
                            zBuffer[y * w + x] = z;
                        }
                    }
                }
            }
        }
    }
}
