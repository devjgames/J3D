package org.j3d;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class GameEditor implements org.j3d.Game.GameLoop {

    private static File file = IO.file("log.txt");
    private static boolean paused = false;

    private static final int ROT = 0;
    private static final int ZOOM = 1;
    private static final int PANXZ = 2;
    private static final int PANY = 3;
    private static final int SEL = 4;
    private static final int MOVXZ = 5;
    private static final int MOVY = 6;
    private static final int RX = 7;
    private static final int RY = 8;
    private static final int RZ = 9;
    private static final int SCALE = 10;

    private static class TextAreaStream extends OutputStream {

        private JTextArea textArea;

        public TextAreaStream(JTextArea textArea) {
            this.textArea = textArea;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            if(paused) {
                return;
            }
            if(SwingUtilities.isEventDispatchThread()) {
                String s = new String(b, off, len);

                textArea.append(s);

                try {
                    IO.appendAllBytes(s.getBytes(), file);
                } catch(Exception ex) {
                }
            } else {
                try {
                    SwingUtilities.invokeAndWait(() -> { 
                        try {
                            write(b, off, len); 
                        } catch(Exception ex) {
                        }
                    });
                } catch(Exception ex) {
                }
            }
        }

        @Override
        public void write(int b) throws IOException {
            write(new byte[] { (byte)b }, 0, 1);
        }
    }

    private JFrame frame;
    private String[] topBar = new String[] {
        "Rot", "Zoom", "PanXZ", "PanY", "Sel", "MovXZ", "MovY", "RX", "RY", "RZ", "Scale"
    };
    private int mode = 0;
    private boolean lDown = false;
    private Hashtable<String, JToggleButton> toggleButtons = new Hashtable<>();
    private Hashtable<String, JButton> buttons = new Hashtable<>();
    private JTextArea consoleTextArea;
    private Game game;
    private Scene scene = null;
    private Node selected = null;

    public GameEditor(int w, int h, int scale, boolean resizable) {

        if(file.exists()) {
            file.delete();
        }
        paused = false;

        frame = new JFrame("J3D-Editor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        for(String name : topBar) {
            toggleButtons.put(name, new JToggleButton(
                new AbstractAction(name) {
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        for(int i = 0; i != topBar.length; i++) {
                            toggleButtons.get(topBar[i]).setSelected(e.getSource() == toggleButtons.get(topBar[i]));
                            if(toggleButtons.get(topBar[i]).isSelected()) {
                                mode = i;
                            }
                        }
                    };
                }
            ));
            topPanel.add(toggleButtons.get(name));
        }
        toggleButtons.get("Rot").setSelected(true);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        buttons.put("Load", new JButton(
            new AbstractAction("Load") {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    File file = Utils.selectFile(frame, IO.file("scenes"), ".xml");

                    if(file != null) {
                        try {
                            selected = null;
                            scene = null;
                            enableUI();
                            game.assets().clear();
                            scene = Serializer.deserialize(game, true, file);
                            enableUI();
                        } catch(Exception ex) {
                            ex.printStackTrace(System.out);
                        }
                    }
                };
            }
        ));
        bottomPanel.add(buttons.get("Load"));

        buttons.put("Save", new JButton(
            new AbstractAction("Save") {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        Serializer.serialize(scene, scene.file);
                    } catch(Exception ex) {
                        ex.printStackTrace(System.out);
                    }
                };
            }
        ));
        bottomPanel.add(buttons.get("Save"));

        buttons.put("Play", new JButton(
            new AbstractAction("Play") {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    boolean inDesign = !scene.inDesign();
                    File file = scene.file;
                    try {
                        selected = null;
                        scene = null;
                        enableUI();
                        game.assets().clear();
                        scene = Serializer.deserialize(game, inDesign, file);
                        enableUI();
                        buttons.get("Play").setText((inDesign) ? "Play" : "Stop");
                    } catch(Exception ex) {
                        ex.printStackTrace(System.out);
                    }
                };
            }
        ));
        bottomPanel.add(buttons.get("Play"));

        buttons.put("Scene", new JButton(
            new AbstractAction("Scene") {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    // TODO
                };
            }
        ));
        bottomPanel.add(buttons.get("Scene"));

        buttons.put("ZRot", new JButton(
            new AbstractAction("Z Rot") {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    selected.rotation.toIdentity();
                };
            }
        ));
        bottomPanel.add(buttons.get("ZRot"));

        buttons.put("TargTo", new JButton(
            new AbstractAction("Targ To") {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    scene.camera.calcOffset();
                    scene.camera.target.set(selected.absolutePosition);
                    scene.camera.target.add(scene.camera.offset, scene.camera.eye);
                };
            }
        ));
        bottomPanel.add(buttons.get("TargTo"));

        buttons.put("ToTarg", new JButton(
            new AbstractAction("To Targ") {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    selected.position.set(scene.camera.target);
                };
            }
        ));
        bottomPanel.add(buttons.get("ToTarg"));

        buttons.put("UScale", new JButton(
            new AbstractAction("U Scale") {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    selected.scale.set(1, 1, 1);
                };
            }
        ));
        bottomPanel.add(buttons.get("UScale"));

        buttons.put("Map", new JButton(
            new AbstractAction("Map") {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    LightMapper mapper = new LightMapper();
                    File file = new File(scene.file.getParentFile(), IO.fileNameWithOutExtension(scene.file) + ".png");

                    try {
                        mapper.light(file, scene.lightMapWidth, scene.lightMapHeight, game, scene, resizable);
                    } catch(Exception ex) {
                        ex.printStackTrace(System.out);
                    }
                };
            }
        ));
        bottomPanel.add(buttons.get("Map"));

        buttons.put("ClearMap", new JButton(
            new AbstractAction("Clear Map") {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        scene.root.traverse((n) -> {
                            n.texture2 = null;
                            return true;
                        });
                    } catch(Exception ex) {
                        ex.printStackTrace(System.out);
                    }
                };
            }
        ));
        bottomPanel.add(buttons.get("ClearMap"));

        buttons.put("SnapShot", new JButton(
            new AbstractAction("Snap Shot") {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    game.takeSnapShot();
                };
            }
        ));
        bottomPanel.add(buttons.get("SnapShot"));

        buttons.put("Clear", new JButton(
            new AbstractAction("Clear") {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    consoleTextArea.setText("");
                };
            }
        ));
        bottomPanel.add(buttons.get("Clear"));

        toggleButtons.put("Pause", new JToggleButton(
            new AbstractAction("Pause") {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    paused = !paused;
                    toggleButtons.get("Pause").setSelected(paused);
                };
            }
        ));
        bottomPanel.add(toggleButtons.get("Pause"));

        consoleTextArea = new JTextArea();
        consoleTextArea.setEditable(false);
        consoleTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        System.setOut(new PrintStream(new TextAreaStream(consoleTextArea)));

        // TODO tree and editor panel

        JScrollPane treePane = new JScrollPane(new JTree(new DefaultTreeModel(new DefaultMutableTreeNode())));
        JScrollPane editorPane = new JScrollPane(new JPanel());
        JScrollPane consolePane = new JScrollPane(consoleTextArea);
        JPanel consolePanel = new JPanel(new BorderLayout());

        treePane.setPreferredSize(new Dimension(200, 100));
        editorPane.setPreferredSize(new Dimension(250, 100));
        consolePane.setPreferredSize(new Dimension(100, 150));

        game = new Game(w, h, scale, this);
        frame.add(game.getPanel(), BorderLayout.CENTER);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.add(treePane, BorderLayout.WEST);
        frame.add(editorPane, BorderLayout.EAST);

        consolePanel.add(consolePane, BorderLayout.CENTER);
        consolePanel.add(bottomPanel, BorderLayout.NORTH);

        frame.add(consolePanel, BorderLayout.SOUTH);

        enableUI();

        frame.pack();
        frame.setVisible(true);

        game.run();
    }


    @Override
    public void resize(Game game) throws Exception {
    }

    @Override
    public void init(Game game) throws Exception {

    }

    @Override
    public void render(Game game) throws Exception {
        if(scene == null) {
            game.renderer().clear();
        } else {
            try {
                scene.render(game);
            } catch(Exception ex) {
                ex.printStackTrace(System.out);
            }

            if(scene.inDesign()) {
                handleUI();
            }
        }
    }

    private void handleUI() {
        if(game.buttonDown(0)) {
            if(mode == ROT) {
                scene.camera.rotate(game.dX(), game.dY());
            } else if(mode == ZOOM) {
                scene.camera.zoom(game.dY());
            }  else if(mode == PANXZ) {

            } else if(mode == PANY) {

            } else if(mode == SEL) {
                if(!lDown) {

                }
            } else if(selected != null) {
                if(mode == MOVXZ) {

                } else if(mode == MOVY) {

                } else if(mode == RX) {

                } else if(mode == RY) {

                } else if(mode == RZ) {

                } else if(mode == SCALE) {

                }
            }
            lDown = true;
        } else {
            if(!lDown && selected != null && (mode == MOVXZ || mode == MOVY)) {

            }
            lDown = false;
        }
    }

    private void enableUI() {
        boolean enabled = scene != null;

        if(enabled) {
            enabled = scene.inDesign();
        }

        Enumeration<String> e = toggleButtons.keys();

        while(e.hasMoreElements()) {
            String key = e.nextElement();

            if(!key.equals("Pause")) {
                toggleButtons.get(key).setEnabled(enabled);
            }
        }

        e = buttons.keys();
        while(e.hasMoreElements()) {
            String key = e.nextElement();

            if(!key.equals("Play")) {
                if(!key.equals("Clear")) {
                    if(key.equals("Load")) {
                        buttons.get(key).setEnabled(enabled || scene == null);
                    } else if(
                        key.equals("ToTarg") ||
                        key.equals("TargTo") ||
                        key.equals("ZRot") ||
                        key.equals("UScale")
                    ) {
                        buttons.get(key).setEnabled(enabled && selected != null);
                    } else {
                        buttons.get(key).setEnabled(enabled);
                    }
                }
            } else {
                buttons.get(key).setEnabled(scene != null);
            }
        }

        // TODO tree and editor panel
    }
}