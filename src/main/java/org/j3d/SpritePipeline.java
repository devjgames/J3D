package org.j3d;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class SpritePipeline extends Resource {

    private int vao = 0;
    private int vbo = 0;
    private int veo = 0;
    private IntBuffer iBuf;
    private FloatBuffer vBuf;
    private Texture texture = null;
    private int uProjection;
    private int uTexture;
    private Pipeline pipeline;
    private Matrix4f projection = new Matrix4f();
    private Game game;

    public SpritePipeline(Game game) throws Exception {
        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();
        veo = GL15.glGenBuffers();
        iBuf = BufferUtils.createIntBuffer(10 * 6);
        vBuf = BufferUtils.createFloatBuffer(10 * 4 * 8);
        pipeline = new Pipeline(
                new String(IO.readAllBytes(Pipeline.class, "/SpriteVertexShader.glsl")),
                new String(IO.readAllBytes(Pipeline.class, "/SpriteFragmentShader.glsl")),
                "vsInPosition", "vsInTextureCoordinate", "vsInColor"
        );
        uProjection = pipeline.getUniformLocation("uProjection");
        uTexture = pipeline.getUniformLocation("uTexture");
        this.game = game;
    }

    public void begin(int width, int height) {
        projection.identity().ortho(0, width, height, 0, -1, 1);
        texture = null;
        Utils.setCullState(CullState.NONE);
        Utils.setDepthState(DepthState.NONE);
        Utils.setBlendState(BlendState.ALPHA);
        pipeline.begin();
        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, veo);
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glEnableVertexAttribArray(2);
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 8 * 4, 0);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 8 * 4, 2 * 4);
        GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, 8 * 4, 4 * 4);
        pipeline.set(uProjection, projection);
    }

    public void beginSprite(Texture texture) {
        this.texture = texture;
        iBuf.limit(iBuf.capacity());
        iBuf.position(0);
        vBuf.limit(vBuf.capacity());
        vBuf.position(0);
    }

    public void beginSprite(Font font) {
        beginSprite(font.getTexture());
    }

    public void push(int sx, int sy, int sw, int sh, int dx, int dy, int dw, int dh, float r, float g, float b, float a, boolean flip) {
        if (texture != null) {

            iBuf = Utils.ensureCapacity(iBuf, 1000 * 6);
            vBuf = Utils.ensureCapacity(vBuf, 1000 * 4 * 8);

            float tw = texture.width;
            float th = texture.height;
            float sx1 = sx / tw;
            float sy1 = sy / th;
            float sx2 = (sx + sw) / tw;
            float sy2 = (sy + sh) / th;
            float dx1 = dx;
            float dy1 = dy;
            float dx2 = dx + dw;
            float dy2 = dy + dh;
            int baseVertex = vBuf.position() / 8;

            if(flip) {
                float temp = sy1;
                sy1 = sy2;
                sy2 = temp;
            }

            vBuf.put(dx1);
            vBuf.put(dy1);
            vBuf.put(sx1);
            vBuf.put(sy1);
            vBuf.put(r);
            vBuf.put(g);
            vBuf.put(b);
            vBuf.put(a);

            vBuf.put(dx2);
            vBuf.put(dy1);
            vBuf.put(sx2);
            vBuf.put(sy1);
            vBuf.put(r);
            vBuf.put(g);
            vBuf.put(b);
            vBuf.put(a);

            vBuf.put(dx2);
            vBuf.put(dy2);
            vBuf.put(sx2);
            vBuf.put(sy2);
            vBuf.put(r);
            vBuf.put(g);
            vBuf.put(b);
            vBuf.put(a);

            vBuf.put(dx1);
            vBuf.put(dy2);
            vBuf.put(sx1);
            vBuf.put(sy2);
            vBuf.put(r);
            vBuf.put(g);
            vBuf.put(b);
            vBuf.put(a);

            iBuf.put(baseVertex);
            iBuf.put(baseVertex + 1);
            iBuf.put(baseVertex + 2);
            iBuf.put(baseVertex + 2);
            iBuf.put(baseVertex + 3);
            iBuf.put(baseVertex);
        }
    }

    public void push(String text, int scale, int cw, int ch, int cols, int lineSpacing, int x, int y, float r, float g, float b, float a) {
        int sx = x;

        for (int i = 0; i != text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                x = sx * scale;
                y += lineSpacing + (int)(ch * scale);
            } else {
                int j = (int) c - (int) ' ';
                if (j >= 0 && j < 100) {
                    int sr = j / cols;
                    int sc = j % cols;
                    push(sc * cw, sr * ch, cw, ch, x, y, cw * scale, ch * scale, r, g, b, a, false);
                    x += cw * scale;
                }
            }
        }
    }

    public void push(Font font, String text, int lineSpacing, int x, int y, float r, float g, float b, float a) {
        int s = game.getScale();

        push(text, s, font.getCharWidth() / s, font.getCharHeight() / s, font.getColumns(), lineSpacing, x, y, r, g, b, a);
    }

    public void endSprite() {
        if (vBuf.position() != 0) {
            int indexCount = iBuf.position();
            vBuf.flip();
            iBuf.flip();
            pipeline.set(uTexture, 0, texture);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vBuf, GL15.GL_STREAM_DRAW);
            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, iBuf, GL15.GL_STREAM_DRAW);
            GL11.glDrawElements(GL11.GL_TRIANGLES, indexCount, GL11.GL_UNSIGNED_INT, 0);
            vBuf.limit(vBuf.capacity());
            vBuf.position(0);
            iBuf.limit(iBuf.capacity());
            iBuf.position(0);
        }
        texture = null;
    }

    public void end() {
        pipeline.end();
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        texture = null;
    }

    @Override
    public void destroy() throws Exception {
        GL30.glDeleteVertexArrays(vao);
        GL15.glDeleteBuffers(vbo);
        GL15.glDeleteBuffers(veo);
        pipeline.destroy();
        super.destroy();
    }
}
