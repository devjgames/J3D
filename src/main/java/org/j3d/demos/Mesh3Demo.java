package org.j3d.demos;

public class Mesh3Demo extends Mesh1Demo {
    
    @Override
    public void init(App app) throws Exception {
        super.init(app);

        block.ambientColor.set(0.2f, 0.2f, 0.2f, 1);
        block.addLight(-100, -100, -100, 3, 2, 1, 1, 200, false);
        block.addLight(+100, +100, +100, 1, 2, 3, 1, 200, false);
    }
}
