package org.j3d.demo;

import org.j3d.IO;
import org.j3d.NodeComponent;

public class Info extends NodeComponent {

    public boolean visible = true;
    public String font = "assets/font40x40.png";
    public int cw = 40;
    public int ch = 40;
    public int cols = 15;
    
    @Override
    public void renderSprites() throws Exception {
        if(visible) {
            game().renderer().beginSprite(game().assets().load(IO.file(font)));
            game().renderer().render( 
                "FPS = " + game().frameRate() + "\nTRI = " + scene().getTrianglesRendered(), 
                cw, ch, 15, 5, 10, 10, 1, 1, 1, 1);
            game().renderer().endSprite();
        }
    }
}
