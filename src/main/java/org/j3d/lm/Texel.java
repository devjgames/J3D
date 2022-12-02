package org.j3d.lm;

import java.util.Vector;

import org.joml.Vector3f;

public class Texel {
    
    public final Vector3f position = new Vector3f();
    public final Vector3f normal = new Vector3f();
    public final Vector3f color = new Vector3f();
    public boolean emitsLight = false;
    public boolean edge = false;
    public final Vector<Integer> adjacent = new Vector<Integer>();
    public int x = -1;
    public int y = -1;
}
