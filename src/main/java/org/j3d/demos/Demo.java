package org.j3d.demos;

import org.joml.Matrix4f;

public abstract class Demo {

    public final Matrix4f projection = new Matrix4f();
    public final Matrix4f view = new Matrix4f();
    
    public abstract void init(App app) throws Exception;

    public abstract boolean update(App app) throws Exception;

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
