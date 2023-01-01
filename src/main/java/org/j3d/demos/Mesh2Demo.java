package org.j3d.demos;

public class Mesh2Demo extends Mesh1Demo {
    
    @Override
    public void init(App app) throws Exception {
        super.init(app);

        block.ambientColor.set(0.2f, 0.2f, 0.2f, 1);
        block.addLight(-1, -1, -1, 1, 0.75f, 0.5f, 1, 0, true);
        block.addLight(+1, +1, +1, 0.5f, 0.75f, 1, 1, 0, true);
    }
}
