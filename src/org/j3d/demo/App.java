package org.j3d.demo;

import org.j3d.Game;
import org.j3d.IO;
import org.lwjgl.glfw.GLFW;

public class App {

    public static abstract class Demo {
    
        public abstract void init(Game game) throws Exception;

        public abstract void nextFrame(Game game) throws Exception;

        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }

    public static void run(Demo ... demos) throws Exception {

        Game game = null;
        Demo demo = null;
        int j = 0;
        boolean down = false;

        try {
            game = new Game(1000, 700);

            GLFW.glfwSetWindowTitle(game.window(), "J3D - down arrow key next, enter key run");

            while(game.run()) {
                if(demo != null) {
                    demo.nextFrame(game);
                } else {
                    game.renderer().clear();
                    game.renderer().setupSprites();
                    game.renderer().beginSprite(game.assets().load(IO.file("assets/font40x40_GS.png")));
                    for(int i = 0; i != demos.length; i++) {
                        float g = 1, b = 1;

                        if(j == i) {
                            g = 0.5f;
                            b = 0;
                        }
                        game.renderer().render(demos[i].toString(), game.getScale(), 40, 40, 15, 0, 10, 10 + i * 40 * game.getScale(), 1, g, b, 1);
                    }
                    game.renderer().endSprite();
                    game.renderer().end();
                }
                game.swapBuffers();

                if(demo == null) {
                    if(game.isKeyDown(GLFW.GLFW_KEY_DOWN)) {
                        if(!down) {
                            j = (j + 1) % demos.length;
                            down = true;
                        }
                    } else {
                        down = false;
                    }
                    if(game.isKeyDown(GLFW.GLFW_KEY_ENTER)) {
                        game.assets().clear();
                        demo = demos[j];
                        demo.init(game);
                    }
                } else {
                    if(game.isKeyDown(GLFW.GLFW_KEY_ESCAPE)) {
                        game.assets().clear();
                        demo = null;
                    }
                }
            }
        } finally {
            if(game != null) {
                game.destroy();
            }
        }
    }

    public static void main(String[] args) throws Exception {

        App.run(
            new Demo1(),
            new Demo2(),
            new Demo3(),
            new Demo4()
            );
    }
}