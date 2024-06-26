package org.j3d;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public final class Renderer implements Resource {

    public CullState cullState = CullState.BACK;
    public boolean depthTestEnabled = true;
    public boolean depthWriteEnabled = true;
    public boolean blendEnabled = false;
    public boolean additiveBlend = false;
    public Texture texture = null;
    public Texture texture2 = null;
    public final Vec3 backgroundColor = new Vec3(0.25f, 0.25f, 0.25f);
    public final Mat4 projection = new Mat4();
    public final Mat4 view = new Mat4();
    public final Mat4 model = new Mat4();

    private final Game game;
    private final FloatBuffer fBuf = BufferUtils.createFloatBuffer(16);
    private Texture image = null;
    private SceneVertex vertex = new SceneVertex();
    private FloatBuffer vBuf = BufferUtils.createFloatBuffer(11 * 6);
    private int program = 0;
    private int uProjection, uView, uModel;
    private int uTexture, uTextureEnabled;
    private int uTexture2, uTexture2Enabled;
    private int vao = 0;
    private int vbo = 0;
    private final Mat4 matrix = new Mat4();

    public Renderer(Game game) throws Exception {
        this.game = game;

        int vs = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        int fs = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        int s;

        GL20.glShaderSource(vs, new String(IO.readAllBytes(Renderer.class, "/org/j3d/glsl/Vertex.glsl")));
        GL20.glCompileShader(vs);

        GL20.glShaderSource(fs, new String(IO.readAllBytes(Renderer.class, "/org/j3d/glsl/Fragment.glsl")));
        GL20.glCompileShader(fs);

        s = GL20.glGetShaderi(vs, GL20.GL_COMPILE_STATUS);
        if(s == 0) {
            System.out.println(GL20.glGetShaderInfoLog(vs, GL20.glGetShaderi(vs, GL20.GL_INFO_LOG_LENGTH)));
            GL20.glDeleteShader(vs);
            GL20.glDeleteShader(fs);
            throw new Exception("failed to compile vertex shader");
        }

        s = GL20.glGetShaderi(fs, GL20.GL_COMPILE_STATUS);
        if(s == 0) {
            System.out.println(GL20.glGetShaderInfoLog(fs, GL20.glGetShaderi(fs, GL20.GL_INFO_LOG_LENGTH)));
            GL20.glDeleteShader(vs);
            GL20.glDeleteShader(fs);
            throw new Exception("failed to compile fragment shader");
        }

        program = GL20.glCreateProgram();
        GL20.glAttachShader(program, vs);
        GL20.glAttachShader(program, fs);
        GL20.glDeleteShader(vs);
        GL20.glDeleteShader(fs);
        GL20.glBindAttribLocation(program, 0, "vsInPosition");
        GL20.glBindAttribLocation(program, 1, "vsInTextureCoordinate");
        GL20.glBindAttribLocation(program, 2, "vsInTextureCoordinate2");
        GL20.glBindAttribLocation(program, 3, "vsInColor");
        GL20.glLinkProgram(program);
        s = GL20.glGetProgrami(program, GL20.GL_LINK_STATUS);
        if(s == 0) {
            System.out.println(GL20.glGetProgramInfoLog(program, GL20.glGetProgrami(program, GL20.GL_INFO_LOG_LENGTH)));
            GL20.glDeleteProgram(program);
            program = 0;
            throw new Exception("failed to link shader program");
        }
        uProjection = GL20.glGetUniformLocation(program, "uProjection");
        uView = GL20.glGetUniformLocation(program, "uView");
        uModel = GL20.glGetUniformLocation(program, "uModel");
        uTexture = GL20.glGetUniformLocation(program, "uTexture");
        uTextureEnabled = GL20.glGetUniformLocation(program, "uTextureEnabled");
        uTexture2 = GL20.glGetUniformLocation(program, "uTexture2");
        uTexture2Enabled = GL20.glGetUniformLocation(program, "uTexture2Enabled");

        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();
    }

    public void clear() {
        cullState = CullState.BACK;
        depthTestEnabled = true;
        depthWriteEnabled = true;
        blendEnabled = false;
        additiveBlend = false;
        texture = null;
        texture2 = null;
        image = null;
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDepthMask(true);
        GL11.glClearColor(backgroundColor.x, backgroundColor.y, backgroundColor.z, 1);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL20.glUseProgram(program);
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glEnableVertexAttribArray(2);
        GL20.glEnableVertexAttribArray(3);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 11 * 4, 0);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 11 * 4, 3 * 4);
        GL20.glVertexAttribPointer(2, 2, GL11.GL_FLOAT, false, 11 * 4, 3 * 4 + 2 * 4);
        GL20.glVertexAttribPointer(3, 4, GL11.GL_FLOAT, false, 11 * 4, 3 * 4 + 4 * 4);

        projection.put(fBuf);
        GL20.glUniformMatrix4fv(uProjection, false, fBuf);
        view.put(fBuf);
        GL20.glUniformMatrix4fv(uView, false, fBuf);
    }

    public void setState() {
        if(cullState  == CullState.NONE) {
            GL11.glDisable(GL11.GL_CULL_FACE);
        } else {
            GL11.glEnable(GL11.GL_CULL_FACE);
            if(cullState == CullState.BACK) {
                GL11.glCullFace(GL11.GL_BACK);
            } else {
                GL11.glCullFace(GL11.GL_FRONT);
            }
        }
        if(depthTestEnabled) {
            GL11.glEnable((GL11.GL_DEPTH_TEST));
        } else {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
        }
        GL11.glDepthMask(depthWriteEnabled);
        if(blendEnabled) {
            GL11.glEnable(GL11.GL_BLEND);
            if(additiveBlend) {
                GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
            } else {
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            }
        } else {
            GL11.glDisable(GL11.GL_BLEND);
        }
        if(texture != null) {
            GL20.glUniform1i(uTextureEnabled, 1);
            GL20.glUniform1i(uTexture, 0);
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getID());
        } else {
            GL20.glUniform1i(uTextureEnabled, 0);
        }
        if(texture2 != null) {
            GL20.glUniform1i(uTexture2Enabled, 1);
            GL20.glUniform1i(uTexture2, 1);
            GL13.glActiveTexture(GL13.GL_TEXTURE1);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture2.getID());
        } else {
            GL20.glUniform1i(uTexture2Enabled, 0);
        }
    }

    public int render(SceneVertex[] vertices, int[] indices, int indexCount) {

        int rendered = 0;

        model.put(fBuf);
        GL20.glUniformMatrix4fv(uModel,false, fBuf);

        vBuf.position(0);
        vBuf.limit(vBuf.capacity());

        for (int i = 0; i != indexCount; i += 3) {
            int i1 = indices[i + 0];
            int i2 = indices[i + 1];
            int i3 = indices[i + 2];
            SceneVertex v1 = vertices[i1];
            SceneVertex v2 = vertices[i2];
            SceneVertex v3 = vertices[i3];

            render(v1);
            render(v2);
            render(v3);

            rendered++;
        }
   
        vBuf.flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vBuf, GL15.GL_DYNAMIC_DRAW);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, rendered * 3);

        return rendered;
    }

    public void setupSprites() {
        matrix.toIdentity().ortho(0, game.w(), game.h(), 0, -1, 1);
        matrix.put(fBuf);
        GL20.glUniformMatrix4fv(uProjection, false, fBuf);

        matrix.toIdentity();
        matrix.put(fBuf);
        GL20.glUniformMatrix4fv(uView, false, fBuf);
        GL20.glUniformMatrix4fv(uModel, false, fBuf);

        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL20.glUniform1i(uTextureEnabled, 1);
        GL20.glUniform1i(uTexture, 0);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);

        GL20.glUniform1i(uTexture2Enabled, 0);

        image = null;

        vBuf.position(0);
        vBuf.limit(vBuf.capacity());
    }

    public void beginSprite(Texture image) {
        this.image = image;

        vBuf.position(0);
        vBuf.limit(vBuf.capacity());

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, image.getID());
    }

    public void render(int sX, int sY, int sW, int sH, int dX, int dY, int dW, int dH, Vec4 color) {
        render(sX, sY, sW, sH, dX, dY, dW, dH, color.x, color.y, color.z, color.w);
    }

    public void render(int sX, int sY, int sW, int sH, int dX, int dY, int dW, int dH, float r, float g, float b, float a) {
        float tw = image.w;
        float th = image.h;
        float sx1 = sX / tw;
        float sy1 = sY / th;
        float sx2 = (sX + sW) / tw;
        float sy2 = (sY + sH) / th;
        float dx1 = dX;
        float dy1 = dY;
        float dx2 = dX + dW;
        float dy2 = dY + dH;


        vertex.position.set(dx1, dy1, 0, 1);
        vertex.textureCoordinate.set(sx1, sy1);
        vertex.color.set(r, g, b, a);
        render(vertex);

        vertex.position.set(dx2, dy1, 0, 1);
        vertex.textureCoordinate.set(sx2, sy1);
        vertex.color.set(r, g, b, a);
        render(vertex);

        vertex.position.set(dx2, dy2, 0, 1);
        vertex.textureCoordinate.set(sx2, sy2);
        vertex.color.set(r, g, b, a);
        render(vertex);
        
        vertex.position.set(dx2, dy2, 0, 1);
        vertex.textureCoordinate.set(sx2, sy2);
        vertex.color.set(r, g, b, a);
        render(vertex);
        
        vertex.position.set(dx1, dy2, 0, 1);
        vertex.textureCoordinate.set(sx1, sy2);
        vertex.color.set(r, g, b, a);
        render(vertex);

        vertex.position.set(dx1, dy1, 0, 1);
        vertex.textureCoordinate.set(sx1, sy1);
        vertex.color.set(r, g, b, a);
        render(vertex);
    }

    public void render(String text, int scale, int cW, int cH, int cols, int lineSpacing, int x, int y, Vec4 color) {
        render(text, scale, cW, cH, cols, lineSpacing, x, y, color.x, color.y, color.z, color.w);
    }

    public void render(String text, int scale, int cW, int cH, int cols, int lineSpacing, int x, int y, float r, float g, float b, float a) {
        int sX = x;
        for (int i = 0; i != text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                x = sX;
                y += lineSpacing * scale;
                y += cH * scale;
            } else {
                int j = (int) c - (int) ' ';
                if (j >= 0 && j < 100) {
                    int col = j % cols;
                    int row = j / cols;
                    render(col * cW, row * cH, cW, cH, x, y, cW * scale, cH * scale, r, g, b, a);
                    x += cW * scale;
                }
            }
        }
    }

    public void endSprite() {
        int count = vBuf.position();

        vBuf.flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vBuf, GL15.GL_DYNAMIC_DRAW);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, count);
    }

    @Override
    public void destroy() throws Exception {
        if(program != 0) {
            GL20.glDeleteProgram(program);
            program = 0;
        }
        if(vbo != 0) {
            GL15.glDeleteBuffers(vbo);
            vbo = 0;
        }
        if(vao != 0) {
            GL30.glDeleteVertexArrays(vao);
            vao = 0;
        }
    }

    private void render(SceneVertex v) {
        int count = vBuf.position() + 11 * 6000;

        if(count > vBuf.capacity()) {
            FloatBuffer newBuf = BufferUtils.createFloatBuffer(count);

            System.out.println("increasing vertex buffer capacity " + count);

            newBuf.put(vBuf);
            vBuf = newBuf;
        }
        vBuf.put(v.position.x);
        vBuf.put(v.position.y);
        vBuf.put(v.position.z);
        vBuf.put(v.textureCoordinate.x);
        vBuf.put(v.textureCoordinate.y);
        vBuf.put(v.textureCoordinate2.x);
        vBuf.put(v.textureCoordinate2.y);
        vBuf.put(v.color.x);
        vBuf.put(v.color.y);
        vBuf.put(v.color.z);
        vBuf.put(v.color.w);
    }
}
