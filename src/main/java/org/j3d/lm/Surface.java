package org.j3d.lm;

import org.joml.Vector3f;

public class Surface {
    
    public final Vector3f ambientColor = new Vector3f(0.05f, 0.05f, 0.05f);
    public final Vector3f diffuseColor = new Vector3f(1, 1, 1);
    public boolean emitsLight = false;
}
