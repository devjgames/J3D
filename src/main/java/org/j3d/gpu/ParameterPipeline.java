package org.j3d.gpu;

import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.j3d.Parser;
import org.j3d.Pipeline;
import org.j3d.Resource;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

public class ParameterPipeline extends Resource {

    public static final int BOOL = 0;
    public static final int INTEGER = 1;
    public static final int FLOAT = 2;
    public static final int FLOAT2 = 3;
    public static final int FLOAT3 = 4;
    public static final int FLOAT4 = 5;

    public static class TokenException extends Exception {
        
        public TokenException(Tokenizer tokenizer, String message) {
            super("ERROR - " + message + ", line '" + tokenizer.getLine());
        }
    }

    public static class Tokenizer {
        private final String line;
        private String[] tokens;
        private int i = 0;

        public Tokenizer(String line) throws TokenException {
            this.line = line;
            if(line.length() <= 3) {
                throw new TokenException(this, "invalid parameter");
            }
            tokens = line.substring(3).trim().split("\\s+");
        }

        public String getLine() {
            return line;
        }

        public String next(String error) throws TokenException {
            if(i == tokens.length) {
                throw new TokenException(this, error);
            }
            return tokens[i++];
        }

        public int type() throws TokenException {
            String error = "expected bool, int, float, vec2, vec3 or vec4";
            String type = next(error);

            if(type.equals("bool")) {
                return BOOL;
            } else if(type.equals("int")) {
                return INTEGER; 
            } else if(type.equals("float")) {
                return FLOAT;
            } else if(type.equals("vec2")) {
                return FLOAT2;
            } else if(type.equals("vec3")) {
                return FLOAT3;
            } else if(type.equals("vec4")) {
                return FLOAT4;
            } else {
                throw new TokenException(this, error);
            }
        }

        public String name(Hashtable<String, Integer> types, int type) throws TokenException {
            String name = next("expected name");

            if(!types.containsKey(name)) {
                throw new TokenException(this, "uniform not found");
            }

            int uType = types.get(name);

            if(
                (type == BOOL && uType != GL15.GL_INT) ||
                (type == INTEGER && uType != GL15.GL_INT) ||
                (type == FLOAT && uType != GL11.GL_FLOAT) ||
                (type == FLOAT2 && uType != GL20.GL_FLOAT_VEC2) ||
                (type == FLOAT3 && uType != GL20.GL_FLOAT_VEC3) ||
                (type == FLOAT4 && uType != GL20.GL_FLOAT_VEC4)
            ) {
                throw new TokenException(this, "parameter type does not match uniform type");
            }
            return name;
        }

        public void eq() throws TokenException {
            String error = "expected '='";
            String eq = next(error);

            if(!eq.equals("=")) {
                throw new TokenException(this, error);
            }
        }

        public boolean nextBool() throws TokenException {
            String error = "expected true or false";
            String value = next(error);

            if(value.equals("true")) {
                return true;
            } else if(value.equals("false")) {
                return false;
            } else {
                throw new TokenException(this, error);
            }
        }

        public int nextInt() throws TokenException {
            String error = "expected integer";
            String value = next(error);

            try {
                return Integer.parseInt(value);
            } catch(Exception ex) {
                throw new TokenException(this, error);
            }
        }

        public float nextFloat() throws TokenException {
            String error = "expected number";
            String value = next(error);

            try {
                return Float.parseFloat(value);
            } catch(Exception ex) {
                throw new TokenException(this, error);
            }
        }
    }

    private Pipeline pipeline = null;
    private Hashtable<String, Integer> uniforms = new Hashtable<>();
    private Hashtable<String, Integer> uniformTypes = new Hashtable<>();
    private Hashtable<String, Object> uniformValues = new Hashtable<>();
    private Hashtable<String, Object> uniformDefValues = new Hashtable<>();
    private Vector<String> uniformNames = new Vector<>();
    private String error = null;

    public ParameterPipeline(byte[] vertexBytes, byte[] fragmentBytes, String ... attributes) {   
    
        try {
            pipeline = new Pipeline(new String(vertexBytes), new String(fragmentBytes), attributes);
        } catch(Exception ex) {
            error = "ERROR - " + ex.getMessage();
            return;
        }

        String[] lines = new String(fragmentBytes).split("\\n+");
        ByteBuffer buf = BufferUtils.createByteBuffer(GL20.glGetProgrami(pipeline.getProgram(), GL20.GL_ACTIVE_UNIFORM_MAX_LENGTH));
        int n = GL20.glGetProgrami(pipeline.getProgram(), GL20.GL_ACTIVE_UNIFORMS);
        Hashtable<String, Integer> types = new Hashtable<>();

        for(int i = 0; i != n; i++) {
            int[] type = new int[] { 0 };
            int[] len = new int[] { 0 };

            GL20.glGetActiveUniform(pipeline.getProgram(), i, len, new int[] { 0 }, type, buf);

            byte[] sb = new byte[len[0]];

            for(int j = 0; j != sb.length; j++) {
                sb[j] = buf.get(j);
            }
            types.put(new String(sb), type[0]);
        }

        for(String line : lines) {
            String tLine = line.trim();

            if(tLine.startsWith("///")) {
                try {
                    Tokenizer tokenizer = new Tokenizer(tLine);
                    int type = tokenizer.type();
                    String name = tokenizer.name(types, type);
                    int uniform = pipeline.getUniformLocation(name);
                    Object value = null;
                    Object defValue = null;
                
                    tokenizer.eq();

                    if(type == BOOL) {
                        defValue = value = tokenizer.nextBool();
                    } else if(type == INTEGER) {
                        defValue = value = tokenizer.nextInt();
                    } else if(type == FLOAT) {
                        defValue = value = tokenizer.nextFloat();
                    } else if(type == FLOAT2) {
                        value = new Vector2f(tokenizer.nextFloat(), tokenizer.nextFloat());
                        defValue = new Vector2f((Vector2f)value);
                    } else if(type == FLOAT3) {
                        value = new Vector3f(tokenizer.nextFloat(), tokenizer.nextFloat(), tokenizer.nextFloat());
                        defValue = new Vector3f((Vector3f)value);
                    } else {
                        value = new Vector4f(tokenizer.nextFloat(), tokenizer.nextFloat(), tokenizer.nextFloat(), tokenizer.nextFloat());
                        defValue = new Vector4f((Vector4f)value);
                    }
                    uniformValues.put(name, value);
                    uniformDefValues.put(name, defValue);
                    uniformTypes.put(name, type);
                    uniforms.put(name, uniform);
                    uniformNames.add(name);
                } catch(TokenException ex) {
                    error = ex.getMessage();
                    break;
                }
            }
        }
    }
    
    public String getError() {
        return error;
    }

    public Pipeline getPipeline() {
        return pipeline;
    }

    public int getUniformCount() {
        return uniformNames.size();
    }

    public String getUniformName(int i) {
        return uniformNames.get(i);
    }

    public int getUniformType(String name) {
        return uniformTypes.get(name);
    }

    public String getUniformValue(String name) {
        Object value = uniformValues.get(name);

        return Parser.toString(value);
    }

    public void setUniformValue(String name, String value) {
        uniformValues.put(name, Parser.parseObject(value.split("\\s+"), 0, uniformValues.get(name)));
    }

    public void resetParameters() {
        Enumeration<String> names = uniforms.keys();

        while(names.hasMoreElements()) {
            String name = names.nextElement();
            int type = uniformTypes.get(name);

            if(type == BOOL || type == INTEGER || type == FLOAT) {
                uniformValues.put(name, uniformDefValues.get(name));
            } else {
                Object value = uniformValues.get(name);
                Object defValue = uniformDefValues.get(name);

                if(type == FLOAT2) {
                    ((Vector2f)value).set((Vector2f)defValue);
                } else if(type == FLOAT3) {
                    ((Vector3f)value).set((Vector3f)defValue);
                } else {
                    ((Vector4f)value).set((Vector4f)defValue);
                }
            }
        }
    }

    public void bindParameters() {

        for(int i = 0; i != getUniformCount(); i++) {
            String name = getUniformName(i);
            int uniform = uniforms.get(name);
            int type = getUniformType(name);
            Object value = uniformValues.get(name);

            if(type == BOOL) {
                pipeline.set(uniform, (Boolean)value);
            } else if(type == INTEGER) {
                pipeline.set(uniform, (Integer)value);
            } else if(type == FLOAT) {
                pipeline.set(uniform, (Float)value);
            } else if(type == FLOAT2) {
                pipeline.set(uniform, (Vector2f)value);
            } else if(type == FLOAT3) {
                pipeline.set(uniform, (Vector3f)value);
            } else {
                pipeline.set(uniform, (Vector4f)value);
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        if(pipeline != null) {
            pipeline.destroy();
        }
        super.destroy();
    }
}