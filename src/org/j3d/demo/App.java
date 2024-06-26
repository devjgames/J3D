package org.j3d.demo;

import java.io.InputStreamReader;
import java.io.LineNumberReader;

import org.j3d.Game;
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
        LineNumberReader reader = new LineNumberReader(new InputStreamReader(System.in));

        while(true) {
            for(int i = 0; i != demos.length; i++) {
                System.out.println(i + " - " + demos[i].toString());
            }
            System.out.println("q - quit");
            System.out.print("? ");

            String line = reader.readLine();

            if(line.equals("q")) {
                return;
            }

            try {
                int i = Integer.parseInt(line.trim());

                if(i >= 0 && i < demos.length) {
                    demo = demos[i];
                    break;
                }
            } catch(NumberFormatException ex) {
                continue;
            }
        }

        try {
            game = new Game(1000, 700);

            GLFW.glfwSetWindowTitle(game.window(), "J3D");

            demo.init(game);

            while(game.run()) {
                demo.nextFrame(game);
                game.swapBuffers();
            }
        } finally {
            if(game != null) {
                game.destroy();
                game = null;
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