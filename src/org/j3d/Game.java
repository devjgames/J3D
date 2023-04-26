package org.j3d;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.*;

public final class Game {

    public static interface GameLoop {
        void resize(Game game) throws Exception;

        void init(Game game) throws Exception;

        void render(Game game) throws Exception;
    }

    public static class PlayLoop implements GameLoop {

        private File file;
        private Scene scene;

        public PlayLoop(File file) {
            this.file = file;
        }

        @Override
        public void resize(Game game) throws Exception {
        }
    
        @Override
        public void init(Game game) throws Exception {
            scene = Serializer.deserialize(game, false, file);
        }
    
        @Override
        public void render(Game game) throws Exception {
            scene.render(game);

            File f = scene.getLoadFile();

            if(f != null) {
                scene = null;
                game.assets().clear();
                scene = Serializer.deserialize(game, false, file);
            }
        }
    }

    public static void play(int w, int h, int scale, boolean resizable, File file) throws Exception {
        JFrame frame = new JFrame("J3D");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(resizable);
        frame.setLayout(new BorderLayout());

        Game game = new Game(w, h, scale, new PlayLoop(file));

        frame.add(game.getPanel(), BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);

        game.run();
    }

    private class Listener implements MouseListener, MouseMotionListener, KeyListener, ComponentListener {

        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {
            int c = e.getKeyCode();
            if (c >= 0 && c < keyState.length) {
                keyState[c] = true;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            int c = e.getKeyCode();
            if (c >= 0 && c < keyState.length) {
                keyState[c] = false;
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {

        }

        @Override
        public void mousePressed(MouseEvent e) {
            lastX = mouseX = e.getX();
            lastY = mouseY = e.getY();
            if (e.getButton() == MouseEvent.BUTTON1) {
                buttonState[0] = true;
            } else if (e.getButton() == MouseEvent.BUTTON2) {
                buttonState[1] = true;
            } else if (e.getButton() == MouseEvent.BUTTON3) {
                buttonState[2] = true;
            }
            panel.grabFocus();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                buttonState[0] = false;
            } else if (e.getButton() == MouseEvent.BUTTON2) {
                buttonState[1] = false;
            } else if (e.getButton() == MouseEvent.BUTTON3) {
                buttonState[2] = false;
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }

        @Override
        public void mouseDragged(MouseEvent e) {
            mouseX = e.getX();
            mouseY = e.getY();
            dX = lastX - mouseX;
            dY = mouseY - lastY;
            lastX = mouseX;
            lastY = mouseY;
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            mouseX = e.getX();
            mouseY = e.getY();
            dX = lastX - mouseX;
            dY = mouseY - lastY;
            lastX = mouseX;
            lastY = mouseY;
        }

        @Override
        public void componentResized(ComponentEvent e) {
            if (panel.getWidth() > 20 && panel.getHeight() > 20) {
                w = panel.getWidth() / scale;
                h = panel.getHeight() / scale;
                renderer.resize();
                colorBuffer = new int[w * h];
                image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

                try {
                    loop.resize(me);
                } catch (Exception ex) {
                    ex.printStackTrace(System.out);
                }
            }
        }

        @Override
        public void componentMoved(ComponentEvent e) {

        }

        @Override
        public void componentShown(ComponentEvent e) {

        }

        @Override
        public void componentHidden(ComponentEvent e) {

        }
    }

    private class GamePanel extends JPanel {

        @Override
        public void paintComponent(Graphics g) {
            if(!initialized) {
                try {
                    loop.init(me);
                    resetTimer();
                } catch (Exception ex) {
                    ex.printStackTrace(System.out);
                } finally {
                    initialized = true;
                }
            }
    
            Graphics2D g2D = (Graphics2D)g;
    
            try {
                loop.render(me);
            } catch (Exception ex) {
                ex.printStackTrace(System.out);
            }

            image.setRGB(0, 0, w(), h(), colorBuffer, 0, w());

            g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g2D.drawImage(image, 0, 0, getWidth(), getHeight(), null);

            if(takeSnapShot) {
                takeSnapShot = false;

                int w = getWidth();
                int h = getHeight();
                BufferedImage snapShot = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                
                g2D = null;

                try {
                    g2D = (Graphics2D)snapShot.getGraphics();
                    g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                    g2D.drawImage(image, 0, 0, getWidth(), getHeight(), null);

                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                    File file = IO.file("screen-shots/" + format.format(new Date()) + ".png");

                    file.getParentFile().mkdirs();
                    ImageIO.write(snapShot, "PNG", file);
                } catch(Exception ex) {
                    ex.printStackTrace(System.out);
                } finally {
                    if(g2D != null) {
                        g2D.dispose();
                    }
                }

            }
            dX = 0;
            dY = 0;

            tick();
        }
    }

    private Renderer renderer;
    private GameLoop loop;
    private AssetManager assets = new AssetManager();
    private int mouseX = 0;
    private int mouseY = 0;
    private int dX = 0;
    private int dY = 0;
    private int lastX = 0;
    private int lastY = 0;
    private boolean[] buttonState = new boolean[]{false, false, false};
    private boolean[] keyState = new boolean[256];
    private int[] colorBuffer;
    private double lastTime = 0;
    private double totalTime = 0;
    private double elapsedTime = 0;
    private double seconds = 0;
    private int frames = 0;
    private int fps = 0;
    private int w, h;
    private BufferedImage image;
    private boolean initialized = false;
    private int scale;
    private GamePanel panel = null;
    private Game me;
    private boolean takeSnapShot = false;

    public Game(int w, int h, int scale, GameLoop loop) {
        me = this;
        this.w = w;
        this.h = h;
        this.scale = scale;
        panel = new GamePanel();
        panel.setPreferredSize(new Dimension(w * scale, h * scale));
        this.loop = loop;
        panel.addKeyListener(new Listener());
        panel.addMouseListener(new Listener());
        panel.addMouseMotionListener(new Listener());
        panel.addComponentListener(new Listener());
        for (int i = 0; i != keyState.length; i++) {
            keyState[i] = false;
        }
        colorBuffer = new int[w * h];
        renderer = new Renderer(this);
        image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        panel.setFocusable(true);
        panel.grabFocus();
    }

    public JPanel getPanel() {
        return panel;
    }

    public Renderer renderer() {
        return renderer;
    }

    public AssetManager assets() { return assets; }

    public int mouseX() {
        return  (int)(mouseX / (panel.getWidth() / (float)w));
    }

    public int mouseY() {
        return (int)(mouseY / (panel.getHeight() / (float)h));
    }

    public int dX() {
        return dX;
    }

    public int dY() {
        return dY;
    }

    public boolean buttonDown(int i) {
        return buttonState[i];
    }

    public boolean keyDown(int i) {
        return keyState[i];
    }

    public int w() {
        return w;
    }

    public int h() {
        return h;
    }

    public float aspectRatio() {
        return w() / (float) h();
    }

    public int[] colorBuffer() {
        return colorBuffer;
    }

    public float totalTime() {
        return (float) totalTime;
    }

    public float elapsedTime() {
        return (float) elapsedTime;
    }

    public int frameRate() {
        return fps;
    }

    public void resetTimer() {
        lastTime = System.nanoTime() / 1000000000.0;
        totalTime = 0;
        elapsedTime = 0;
        seconds = 0;
        frames = 0;
        fps = 0;
    }

    void tick() {
        double nowTime = System.nanoTime() / 1000000000.0;
        elapsedTime = nowTime - lastTime;
        lastTime = nowTime;
        seconds += elapsedTime;
        totalTime += elapsedTime;
        frames++;
        if (seconds >= 1) {
            fps = frames;
            frames = 0;
            seconds = 0;
        }
    }

    public void takeSnapShot() {
        takeSnapShot = true;
    }

    public void run() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (!panel.isValid()) {
                    assets.clear();
                    return;
                }
                panel.repaint();
                SwingUtilities.invokeLater(this);
            }
        });
    }
}
