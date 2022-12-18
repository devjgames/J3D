package org.j3d.scene.demo1.components;

import org.j3d.Parser;
import org.j3d.UIManager;
import org.j3d.scene.Node;
import org.j3d.scene.NodeComponent;

public class LightComponent extends NodeComponent {
    
    @Override
    public void handleUI(UIManager manager, boolean reset) throws Exception {
        Object r;
        String key1 = "LightComponent-color-field";
        String key2 = "LightComponent-radius-slider";
        Node node = getNode();

        if((r = manager.textField(key1, 0, "Color", Parser.toString(node.lightColor), reset, 14)) != null) {
            Parser.parse(((String)r).split("\\s+"), 0, node.lightColor);
        }
        manager.addRow(5);
        if((r = manager.slider(key2, 0, "Radius", (node.lightRadius - 100) / 900.0f, 14, reset)) != null) {
            node.lightRadius = 100 + (Float)r * 900.0f;
        }
    }
}
