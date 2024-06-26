package org.j3d.demo;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import org.j3d.Game;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;

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
        JFrame frame = new JFrame("J3D");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);
        frame.setLayout(new BorderLayout());

        JList<Demo> demoList = new JList<>();
        DefaultListModel<Demo> model = new DefaultListModel<>();

        for(Demo demo : demos) {
            model.addElement(demo);
        }

        Game game = new Game(1000, 800);
        boolean[] init = new boolean[] { false };
        Demo[] demo = new Demo[] { null };

        demoList.setModel(model);
        demoList.getSelectionModel().addListSelectionListener((l) -> {
            if(!l.getValueIsAdjusting()) {
                demo[0] = null;
                init[0] = true;
                demo[0] = demoList.getSelectedValue();
            }
        });

        JScrollPane sPane = new JScrollPane(demoList);

        sPane.setPreferredSize(new Dimension(250, 50));

        frame.add(sPane, BorderLayout.WEST);
        frame.add(game.getCanvas(), BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);

        try {
            Display.setParent(game.getCanvas());
            Display.create(
                new PixelFormat()
                    .withBitsPerPixel(32)
                    .withAlphaBits(8)
                    .withDepthBits(32)
            );
            Display.makeCurrent();

            game.createRenderer();

            Mouse.create();
            Keyboard.create();

            float seconds = 0;

            while(!Display.isCloseRequested()) {
                Mouse.poll();
                Keyboard.poll();
                Display.makeCurrent();
                if(demo[0] != null) {
                    if(init[0]) {
                        game.assets().clear();
                        demo[0].init(game);
                        init[0] = false;
                        game.resetTimer();
                    }
                    demo[0].nextFrame(game);
                } else {
                    GL11.glClearColor(0.15f, 0.15f, 0.15f, 1);
                    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
                }
                Display.swapBuffers();
                game.tick();

                seconds += game.elapsedTime();
                if(seconds >= 1) {
                    seconds = 0;
                    frame.setTitle("J3D - " + game.frameRate());
                }
            }
        } finally {
            game.destroy();

            if(Mouse.isCreated()) {
                Mouse.destroy();
            }
            if(Keyboard.isCreated()) {
                Keyboard.destroy();
            }
            if(Display.isCreated()) {
                Display.destroy();
            }
        }
    }

    public static void main(String[] args) throws Exception {

        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

        App.run(
            new Demo1(),
            new Demo2(),
            new Demo3(),
            new Demo4()
            );
    }
}