package org.j3d.scene.demo1.components;

import org.j3d.UIManager;
import org.j3d.scene.NodeComponent;
import org.j3d.scene.Renderer;
import org.j3d.scene.demo1.App;

public class InfoComponent extends NodeComponent {

    public String extra = "ESC=Edit";
    

    @Override
    public void pushSprites(Renderer renderer) throws Exception {
        if(!getScene().inDesign) {
            App.pushInfo(getGame(), getScene(), renderer, extra);
        }
    }

    @Override
    public void handleUI(UIManager manager, boolean reset) throws Exception {
        Object r;

        if((r = manager.textField("InfoComponent-extra-field", 0, "Extra", extra, reset, 14)) != null) {
            extra = (String)r;
        }
    }
}
