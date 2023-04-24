package org.j3d.demo;

import org.j3d.GameEditor;
import org.j3d.Utils;

public class App {

    public static void main(String[] args) throws Exception {
        Utils.setNimbusLookAndFeel();

        new GameEditor(250, 175, 3, true);

    }
}