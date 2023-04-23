package org.j3d.sample;

import org.j3d.*;

public class App { 

    public static void main(String[] args) throws Exception {
        Utils.setNimbusLookAndFeel();

        EntityFactory factory = EntityFactory.newInstance();

        factory.addType(FireLight.class);
        factory.addType(Warp.class);
        factory.addType(Player.class);

        Controller.run(new LightMapper(), null, 200, 150, 4, true);
    }
}
