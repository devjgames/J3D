package org.j3d;

import java.io.*;
import javax.swing.*;
import java.awt.*;

public class Controller implements Game.GameLoop {

    public static void run(LightMapper lightMapper, File startMap, int w, int h, int scale, boolean resizable) throws Exception {

        JFrame frame = new JFrame("Sample");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setResizable(resizable);

        Game game = null;

        if(startMap == null) {
            MapEditor editor = new MapEditor(frame, lightMapper, w, h, scale);

            frame.add(editor, BorderLayout.CENTER);
            game = editor.game();
        } else {
            frame.add((game = new Game(w, h, scale, new Controller(lightMapper, startMap))).getPanel(), BorderLayout.CENTER);
        }

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        game.run();
    }

    private LightMapper lightMapper;
    private File startMap;
    private Map map;

    private Controller(LightMapper lightMapper, File startMap) {
        this.lightMapper = lightMapper;
        this.startMap = startMap;
    }

    @Override
    public void resize(Game game) throws Exception {
    }

    @Override
    public void init(Game game) throws Exception {
        map = Map.load(startMap, lightMapper, game);
        map.light(game, false);
        map.init(game);
    }

    @Override
    public void render(Game game) throws Exception {
        map.scene().render(game);
        map.renderSprites();
    }

    @Override
    public void update(Game game) throws Exception {
        map.update();
        if(map.loadFile != null) {
            game.assets().clear();
            map = Map.load(map.loadFile, lightMapper, game);
            map.light(game, false);
            map.init(game);
        }
    }
}
