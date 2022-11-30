package org.j3d;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Utils {

    private static boolean hasError = false;

    public static void checkError(String tag) {
        if (!hasError) {
            int error = GL11.glGetError();
            if (error != GL11.GL_NO_ERROR) {
                hasError = true;
                System.out.println(tag + ":" + error);
            }
        }
    }

    public static void setCullState(CullState state) {
        if (state == CullState.NONE) {
            GL11.glDisable(GL11.GL_CULL_FACE);
        } else {
            GL11.glEnable(GL11.GL_CULL_FACE);
            if (state == CullState.BACK) {
                GL11.glCullFace(GL11.GL_BACK);
            } else {
                GL11.glCullFace(GL11.GL_FRONT);
            }
        }
    }

    public static void setDepthState(DepthState state) {
        if (state == DepthState.READ_WRITE) {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(true);
        } else {
            GL11.glDepthMask(false);
            if (state == DepthState.READONLY) {
                GL11.glEnable(GL11.GL_DEPTH_TEST);
            } else {
                GL11.glDisable(GL11.GL_DEPTH_TEST);
            }
        }
    }

    public static void setBlendState(BlendState state) {
        if (state == BlendState.OPAQUE) {
            GL11.glDisable(GL11.GL_BLEND);
        } else {
            GL11.glEnable(GL11.GL_BLEND);
            if (state == BlendState.ALPHA) {
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            } else {
                GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
            }
        }
    }

    public static void enablePolygonOffset(boolean enabled, float factor, float units) {
        if(enabled) {
            GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
            GL11.glPolygonOffset(factor, units);
        } else {
            GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
        }
    }

    public static void clear(float r, float g, float b, float a) {
        GL11.glClearColor(r, g, b, a);
        setCullState(CullState.BACK);
        setBlendState(BlendState.OPAQUE);
        setDepthState(DepthState.READ_WRITE);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    public static IntBuffer ensureCapacity(IntBuffer iBuf, int growBy) {
        if (iBuf.position() == iBuf.capacity()) {
            Log.log(2, "increasing index buffer capacity to " + (iBuf.capacity() + growBy) + " ...");
            IntBuffer nBuf = BufferUtils.createIntBuffer(iBuf.capacity() + growBy);
            int position = iBuf.position();
            iBuf.flip();
            nBuf.put(iBuf);
            nBuf.limit(nBuf.capacity());
            nBuf.position(position);
            iBuf = nBuf;
        }
        return iBuf;
    }

    public static FloatBuffer ensureCapacity(FloatBuffer vBuf, int growBy) {
        if (vBuf.position() == vBuf.capacity()) {
            Log.log(2, "increasing vertex buffer capacity to " + (vBuf.capacity() + growBy) + " ...");
            FloatBuffer nBuf = BufferUtils.createFloatBuffer(vBuf.capacity() + growBy);
            int position = vBuf.position();
            vBuf.flip();
            nBuf.put(vBuf);
            nBuf.limit(nBuf.capacity());
            nBuf.position(position);
            vBuf = nBuf;
        }
        return vBuf;
    }

    public static IntBuffer trimCapacity(IntBuffer iBuf) {
        if (iBuf.position() < iBuf.capacity()) {
            Log.log(2, "trimming index buffer capacity to " + iBuf.position() + " ...");
            IntBuffer nBuf = BufferUtils.createIntBuffer(iBuf.position());
            iBuf.flip();
            nBuf.put(iBuf);
            nBuf.flip();
            iBuf = nBuf;
        }
        return iBuf;
    }

    public static FloatBuffer trimCapacity(FloatBuffer vBuf) {
        if (vBuf.position() < vBuf.capacity()) {
            Log.log(2, "trimming vertex buffer capacity to " + vBuf.position() + " ...");
            FloatBuffer nBuf = BufferUtils.createFloatBuffer(vBuf.position());
            vBuf.flip();
            nBuf.put(vBuf);
            nBuf.flip();
            vBuf = nBuf;
        }
        return vBuf;
    }

    private static Matrix4f m = new Matrix4f();
    private static Vector4f v = new Vector4f();

    public static void unProject(float wx, float wy, float wz, int vx, int vy, int vw, int vh, Matrix4f projection, Matrix4f view, Vector3f point) {
        m.identity().mul(projection).mul(view).invert();

        v.x = (wx - vx) / (float) vw * 2 - 1;
        v.y = (wy - vy) / (float) vh * 2 - 1;
        v.z = 2 * wz - 1;
        v.w = 1;
        v.mul(m);
        v.div(v.w);
        point.set(v.x, v.y, v.z);
    }

    private static Vector3f r = new Vector3f();

    public static void rotateDirectionAndUp(Vector3f direction, Vector3f up, Game game) {
        rotateDirectionAndUp(direction, up, game.getDeltaX() * 0.025f, game.getDeltaY() * 0.025f);
    }

    public static void rotateDirectionAndUp(Vector3f direction, Vector3f up, float dx, float dy) {
        m.identity().rotate(dx, 0, 1, 0);
        direction.cross(up, r).mulDirection(m).normalize();
        direction.mulDirection(m).normalize();
        m.identity().rotate(dy, r);
        r.cross(direction, up).mulDirection(m).normalize();
        direction.mulDirection(m).normalize();
    }

    public static void rotateOffsetAndUp(Vector3f offset, Vector3f up, Game game) {
        rotateOffsetAndUp(offset, up, game.getDeltaX() * 0.025f, game.getDeltaY() * 0.025f);
    }

    public static void rotateOffsetAndUp(Vector3f offset, Vector3f up, float dx, float dy) {
        m.identity().rotate(dx, 0, 1, 0);
        offset.cross(up, r).mulDirection(m).normalize();
        offset.mulDirection(m);
        m.identity().rotate(dy, r);
        r.cross(offset, up).mulDirection(m).normalize();
        offset.mulDirection(m);
    }
}
