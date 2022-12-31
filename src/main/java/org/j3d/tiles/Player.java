package org.j3d.tiles;

import java.io.File;

public interface Player {
    
    void init(App app, File tileFile) throws Exception;
    
    boolean run(App app) throws Exception;
}
