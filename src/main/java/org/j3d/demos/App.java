package org.j3d.demos;

import java.util.Vector;

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
    private Vector<Demo> demos = new Vector<>();
    private Vector<String> demoNames = new Vector<>();

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
        Demo demo = null;
        boolean sync = true;
        int select = -2;
        int skipCount = 0;
        try {
            game = new Game(1000, 800, true);
            font = game.getResources().manage(new Font(IO.file("assets/pics/font.fnt")));
            manager = new UIManager(game, font);
            GLFW.glfwSwapInterval(1);

            for(Demo iDemo : list) {
                demos.add(iDemo);
            }
            for(Demo iDemo : demos) {
                demoNames.add(iDemo.toString());
            }

            game.resetTimer();
            while(game.run()) {
                if(demo != null) {
                    if(skipCount > 0) {
                        skipCount--;
                        game.beginRenderTarget();
                        Utils.clear(0, 0, 0, 1);
                        game.nextFrame();
                    } else {
                        if(!demo.update(this)) {
                            select = -1;
                            demo = null;
                            game.disableFPSMouse();
                            game.getAssets().clear();
                        }
                    }
                } else {
                    Object r;

                    game.beginRenderTarget();
                    Utils.clear(0, 0, 0, 1);
                    manager.begin();
                    manager.moveTo(10, 10);
                    if(manager.label("App-full-screen", 0, "Full Screen", 0, game.isFullscreen())) {
                        game.toggleFullscreen();
                    }
                    if(manager.label("App-sync", 5, "Sync", 0, sync)) {
                        sync = !sync;
                        if(sync) {
                            GLFW.glfwSwapInterval(1);
                        } else {
                            GLFW.glfwSwapInterval(0);
                        }
                    }
                    manager.addRow(5);
                    if((r = manager.list("App-demos", 0, demoNames, 20, 7, select)) != null) {
                        game.getAssets().clear();
                        demo = demos.get((Integer)r);
                        demo.init(this);
                        skipCount = 6;
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
            new MeshDemo(),
            new LitMeshDemo(),
            new InstanceDemo(),
            new LitInstanceDemo(),
            new CollisionDemo(),
            new LitCollisionDemo(),
            new FPSDemo(),
            new CameraDemo()
        );
    }
}
