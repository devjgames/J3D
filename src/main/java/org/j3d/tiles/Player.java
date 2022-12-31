package org.j3d.tiles;

import java.io.File;

import org.j3d.LightPipeline.Light;

public interface Player {
    
    void init(App app, File tileFile, Light ... lights) throws Exception;
    
    boolean run(App app) throws Exception;
}
