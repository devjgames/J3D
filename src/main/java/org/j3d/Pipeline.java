package org.j3d;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;

public class Pipeline extends Resource {

    private int program = 0;
    private FloatBuffer buf = BufferUtils.createFloatBuffer(16);

    public Pipeline(String vertexSource, String fragmentSource, String... attributes) throws Exception {
        int vs = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        int fs = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        
        GL20.glShaderSource(vs, vertexSource);
        GL20.glCompileShader(vs);

        int status = GL20.glGetShaderi(vs, GL20.GL_COMPILE_STATUS);
        if (status == GL11.GL_FALSE) {
            System.out.println(GL20.glGetShaderInfoLog(vs));
            GL20.glDeleteShader(vs);
            GL20.glDeleteShader(fs);
            destroy();
            throw new Exception("failed to compile vertex shader");
        }

        GL20.glShaderSource(fs, fragmentSource);
        GL20.glCompileShader(fs);
        status = GL20.glGetShaderi(fs, GL20.GL_COMPILE_STATUS);
        if (status == GL11.GL_FALSE) {
            System.out.println(GL20.glGetShaderInfoLog(fs));
            GL20.glDeleteShader(vs);
            GL20.glDeleteShader(fs);
            destroy();
            throw new Exception("failed to compile fragment shader");
        }

        program = GL20.glCreateProgram();
        GL20.glAttachShader(program, vs);
        GL20.glAttachShader(program, fs);
        GL20.glDeleteShader(vs);
        GL20.glDeleteShader(fs);
        GL20.glLinkProgram(program);
        status = GL20.glGetProgrami(program, GL20.GL_LINK_STATUS);
        if (status == GL11.GL_FALSE) {
            System.out.println(GL20.glGetProgramInfoLog(program));
            GL20.glDeleteProgram(program);
            program = 0;
            destroy();
            throw new Exception("failed to link shader program");
        }
        for (int i = 0; i != attributes.length; i++) {
            GL20.glBindAttribLocation(program, i, attributes[i]);
            int attr = GL20.glGetAttribLocation(program, attributes[i]);
            if (attr < 0) {
                System.out.println("attribute '" + attributes[i] + "' not found!");
            }
        }
        GL20.glLinkProgram(program);
        status = GL20.glGetProgrami(program, GL20.GL_LINK_STATUS);
        if (status == GL11.GL_FALSE) {
            System.out.println(GL20.glGetProgramInfoLog(program));
            GL20.glDeleteProgram(program);
            program = 0;
            destroy();
            throw new Exception("failed to link shader program");
        }
    }

    public int getProgram() {
        return program;
    }

    public int getUniformLocation(String name) {
        int l = GL20.glGetUniformLocation(program, name);
        if (l < 0) {
            System.out.println("location '" + name + "' not found!");
        }
        return l;
    }

    public void begin() {
        GL20.glUseProgram(program);
    }

    public void end() {
        GL20.glUseProgram(0);
    }

    public void set(int location, boolean value) {
        GL20.glUniform1i(location, (value) ? 1 : 0);
    }

    public void set(int location, int value) {
        GL20.glUniform1i(location, value);
    }

    public void set(int location, float value) {
        GL20.glUniform1f(location, value);
    }

    public void set(int location, float x, float y) {
        GL20.glUniform2f(location, x, y);
    }

    public void set(int location, Vector2f value) {
        set(location, value.x, value.y);
    }

    public void set(int location, float x, float y, float z) {
        GL20.glUniform3f(location, x, y, z);
    }

    public void set(int location, Vector3f value) {
        set(location, value.x, value.y, value.z);
    }

    public void set(int location, float x, float y, float z, float w) {
        GL20.glUniform4f(location, x, y, z, w);
    }

    public void set(int location, Vector4f value) {
        set(location, value.x, value.y, value.z, value.w);
    }

    public void set(int location, Matrix4f value) {
        value.get(buf);
        GL20.glUniformMatrix4fv(location, false, buf);
    }

    public void set(int location, int unit, Texture value) {
        GL15.glActiveTexture(GL15.GL_TEXTURE0 + unit);
        set(location, unit);
        value.bind();
        GL15.glActiveTexture(GL15.GL_TEXTURE0);
    }

    @Override
    public void destroy() throws Exception {
        if(program != 0) {
            GL20.glDeleteProgram(program);
        }
        super.destroy();
    }
}
