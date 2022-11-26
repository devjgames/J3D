package org.j3d;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Parser {

    public static String toString(Object obj) {
        Class<? extends Object> type = obj.getClass();

        if(Vector2f.class.isAssignableFrom(type)) {
            return Parser.toString((Vector2f)obj);
        } else if(Vector3f.class.isAssignableFrom(type)) {
            return Parser.toString((Vector3f)obj);
        } else if(Vector4f.class.isAssignableFrom(type)) {
            return Parser.toString((Vector4f)obj);
        } else if(Matrix4f.class.isAssignableFrom(type)) {
            return Parser.toString((Matrix4f)obj);
        } else {
            return obj.toString();
        }
    }

    public static String toString(Matrix4f m) {
        return 
        m.m00() + " " + m.m01() + " " + m.m02() + " " + m.m03() + " " + 
        m.m10() + " " + m.m11() + " " + m.m12() + " " + m.m13() + " " + 
        m.m20() + " " + m.m21() + " " + m.m22() + " " + m.m23() + " " + 
        m.m30() + " " + m.m31() + " " + m.m32() + " " + m.m33();
    }

    public static String toString(Vector2f v) {
        return v.x + " " + v.y;
    }

    public static String toString(Vector3f v) {
        return v.x + " " + v.y + " " + v.z;
    }

    public static String toString(Vector4f v) {
        return v.x + " " + v.y + " " + v.z + " " + v.w;
    }

    public static boolean parse(String[] tokens, int i, boolean devValue) {
        try {
            return Boolean.parseBoolean(tokens[i]);
        } catch (Exception ex) {
        }
        return devValue;
    }

    public static int parse(String[] tokens, int i, int defValue) {
        try {
            return Integer.parseInt(tokens[i]);
        } catch (Exception ex) {
        }
        return defValue;
    }

    public static float parse(String[] tokens, int i, float defValue) {
        try {
            return Float.parseFloat(tokens[i]);
        } catch (Exception ex) {
        }
        return defValue;
    }

    public static Vector2f parse(String[] tokens, int i, Vector2f v) {
        try {
            float x = Float.parseFloat(tokens[i++]);
            float y = Float.parseFloat(tokens[i++]);
            v.set(x, y);
        } catch (Exception ex) {
        }
        return v;
    }

    public static Vector3f parse(String[] tokens, int i, Vector3f v) {
        try {
            float x = Float.parseFloat(tokens[i++]);
            float y = Float.parseFloat(tokens[i++]);
            float z = Float.parseFloat(tokens[i++]);
            v.set(x, y, z);
        } catch (Exception ex) {
        }
        return v;
    }

    public static Vector4f parse(String[] tokens, int i, Vector4f v) {
        try {
            float x = Float.parseFloat(tokens[i++]);
            float y = Float.parseFloat(tokens[i++]);
            float z = Float.parseFloat(tokens[i++]);
            float w = Float.parseFloat(tokens[i++]);
            v.set(x, y, z, w);
        } catch (Exception ex) {
        }
        return v;
    }

    public static Matrix4f parse(String[] tokens, int i, Matrix4f m) {
        try {
            float m00 = Float.parseFloat(tokens[i++]);
            float m01 = Float.parseFloat(tokens[i++]);
            float m02 = Float.parseFloat(tokens[i++]);
            float m03 = Float.parseFloat(tokens[i++]);
            float m10 = Float.parseFloat(tokens[i++]);
            float m11 = Float.parseFloat(tokens[i++]);
            float m12 = Float.parseFloat(tokens[i++]);
            float m13 = Float.parseFloat(tokens[i++]);
            float m20 = Float.parseFloat(tokens[i++]);
            float m21 = Float.parseFloat(tokens[i++]);
            float m22 = Float.parseFloat(tokens[i++]);
            float m23 = Float.parseFloat(tokens[i++]);
            float m30 = Float.parseFloat(tokens[i++]);
            float m31 = Float.parseFloat(tokens[i++]);
            float m32 = Float.parseFloat(tokens[i++]);
            float m33 = Float.parseFloat(tokens[i++]);
            m.set(m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33);
        } catch(Exception ex) {
        }
        return m;
    }

    public static Object parseObject(String[] tokens, int i, Object defValue) {
        if(defValue instanceof Boolean) {
            return parse(tokens, i, (Boolean)defValue);
        } else if(defValue instanceof Integer) {
            return parse(tokens, i, (Integer)defValue);
        } else if(defValue instanceof Float) {
            return parse(tokens, i, (Float)defValue);
        } else if(defValue instanceof String) {
            if(i >= 0 && i < tokens.length) {
                return tokens[i];
            }
            return defValue;
        } else if(defValue instanceof Vector2f) {
            return parse(tokens, i, (Vector2f)defValue);
        } else if(defValue instanceof Vector3f) {
            return parse(tokens, i, (Vector3f)defValue);
        } else if(defValue instanceof Vector4f) {
            return parse(tokens, i, (Vector4f)defValue);
        } else if(defValue instanceof Matrix4f) {
            return parse(tokens, i, (Matrix4f)defValue);
        } else {
            try {
                return defValue.getClass().getMethod("valueOf", String.class).invoke(null, tokens[i]);
            } catch(Exception ex) {
            }
        }
        return defValue;
    }
}
