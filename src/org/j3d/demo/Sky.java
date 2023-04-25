package org.j3d.demo;

import org.j3d.FollowCamera;
import org.j3d.NodeComponent;

public class Sky extends NodeComponent {
    
    @Override
    public void start() throws Exception {
        if(scene().inDesign()) {
            return;
        }
        
        node().follow = FollowCamera.EYE;
        node().position.set(scene().camera.eye);
    }
}
