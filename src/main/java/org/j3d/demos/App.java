package org.j3d.demos;

import org.j3d.Font;
import org.j3d.Game;
import org.j3d.IO;
import org.j3d.UIManager;
import org.j3d.Utils;
import org.lwjgl.glfw.GLFW;

public class App {
    
    private Game game = null;
    private Font font;
    private UIManager manager;
    private boolean sync = true;
    private Demo[] demos;
    private String[] demoNames;
    private Demo demo = null;
    private int skip = 0;
    private int select = -2;

    public Game getGame() {
        return game;
    }

    public Font getFont() {
        return font;
    }

    public UIManager getManager() {
        return manager;
    }

    public void run(Demo ... list) throws Exception {
        try {
            game = new Game(1000, 800, 0, false);
            font = game.getResources().manage(new Font(IO.file("assets/pics/font.fnt")));
            manager = new UIManager(game, font);
            GLFW.glfwSwapInterval(1);

            demos = list;
            demoNames = new String[demos.length];
            for(int i = 0; i != demos.length; i++) {
                demoNames[i] =  demos[i].toString();
            }

            game.resetTimer();
            while(game.run()) {
                if(demo != null) {
                    if(skip > 0) {
                        skip--;
                        Utils.clear(0, 0, 0, 1);
                        game.nextFrame();
                    } else {
                        if(!demo.run(this)) {
                            game.getAssets().clear();
                            demo = null;
                            select = -1;
                            game.disableFPSMouse();
                        }
                    }
                } else {
                    Object r;
                    manager.begin();
                    Utils.clear(0, 0, 0, 1);
                    manager.moveTo(10, 10);
                    if(manager.label("App-full-screen-label", 0, "FS", 0, game.isFullscreen())) {
                        game.toggleFullscreen();
                    }
                    if(manager.label("App-sync-label", 5, "Sync", 0, sync)) {
                        sync = !sync;
                        if(sync) {
                            GLFW.glfwSwapInterval(1);
                        } else {
                            GLFW.glfwSwapInterval(0);
                        }
                    }
                    manager.addRow(5);
                    if((r = manager.list("App-demo-list", 0, demoNames, 20, 8, select)) != null) {
                        Utils.clearConsole();
                        game.getAssets().clear();
                        demo = demos[(Integer)r];
                        demo.init(this);
                        skip = 6;
                    }
                    select = -2;
                    manager.end();
                    game.nextFrame();
                }
            }
        } finally {
            if(game != null) {
                game.destroy();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        new App().run(
            new Editor(),
            new Play("scene1", new Logic(), true),
            new Play("scene1", new Logic(), false)
        );
    }
}
