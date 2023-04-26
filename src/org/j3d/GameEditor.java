package org.j3d;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;

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

    private static final String PFX = "org.j3d.GameEditor.KEY.PFX";

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
    private Node clipboard = null;
    private String[] componentFactories;
    private JTree tree;
    private DefaultTreeModel model;
    private JPopupMenu menu;
    private JPanel editorPanel;
    private JPanel editorContainerPanel;
    private Vec3 origin = new Vec3();
    private Vec3 direction = new Vec3();
    private float[] time = new float[1];
    private Triangle triangle = new Triangle();
    private AABB bounds = new AABB();
    private Vec3 point = new Vec3();

    public GameEditor(int w, int h, int scale, boolean resizable, String ... componentFactories) {

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

        buttons.put("AddScene", new JButton(
            new AbstractAction("+") {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    createScene();
                };
            }
        ));
        bottomPanel.add(buttons.get("AddScene"));

        buttons.put("Load", new JButton(
            new AbstractAction("Load") {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    File file = Utils.selectFile(frame, IO.file("scenes"), ".xml");

                    if(file != null) {
                        try {
                            scene = null;
                            game.assets().clear();
                            scene = Serializer.deserialize(game, true, file);
                        } catch(Exception ex) {
                            ex.printStackTrace(System.out);
                        }
                        populateTree();
                        select(null);
                        enableUI();
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
                        scene = null;
                        game.assets().clear();
                        scene = Serializer.deserialize(game, inDesign, file);
                    } catch(Exception ex) {
                        ex.printStackTrace(System.out);
                    }
                    populateTree();
                    select(null);
                    enableUI();
                };
            }
        ));
        bottomPanel.add(buttons.get("Play"));

        buttons.put("Scene", new JButton(
            new AbstractAction("Scene") {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    edit(scene);
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


        buttons.put("X45", new JButton(
            new AbstractAction("RX 45") {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    selected.rotate(0, 45);
                };
            }
        ));
        bottomPanel.add(buttons.get("X45"));

        buttons.put("Y45", new JButton(
            new AbstractAction("RY 45") {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    selected.rotate(1, 45);
                };
            }
        ));
        bottomPanel.add(buttons.get("Y45"));

        buttons.put("Z45", new JButton(
            new AbstractAction("RZ 45") {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    selected.rotate(2, 45);
                };
            }
        ));
        bottomPanel.add(buttons.get("Z45"));

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
                        mapper.light(file, scene.lightMapWidth, scene.lightMapHeight, game, scene, true);
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
                    File file = new File(scene.file.getParentFile(), IO.fileNameWithOutExtension(scene.file) + ".png");

                    if(file.exists()) {
                        file.delete();
                    }
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
            new AbstractAction("Snap") {
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

        this.componentFactories = componentFactories;
        for(String factory : componentFactories) {
            System.out.println("found component factory: " + factory);
        }

        model = new DefaultTreeModel(new DefaultMutableTreeNode());
        tree = new JTree(model);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setRootVisible(false);

        tree.addTreeSelectionListener((e) -> {
            TreePath path = tree.getSelectionPath();

            if(path != null) {
                Object c = path.getLastPathComponent();

                if(c instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode tree = (DefaultMutableTreeNode)c;
                    
                    selected = (Node)tree.getUserObject();
                }
            }
            enableUI();
            edit(selected);
        });

        menu = new JPopupMenu();
        menu.add(new JMenuItem(new AbstractAction("Add Mesh") {
            @Override
            public void actionPerformed(ActionEvent e) {
                File file = Utils.selectFile(frame, IO.file("assets"), ".obj");

                if(file != null) {
                    Node parent = selected;
                    Node node = null;

                    if(parent == null) {
                        parent = scene.root;
                    }

                    try {
                        node = new Node(game, scene, game.assets().load(file));
                        for(Node child : node) {
                            child.lightMapEnabled = true;
                            child.lightingEnabled = false;
                        }
                        parent.add(node);
                    } catch(Exception ex) {
                        ex.printStackTrace(System.out);
                    }
                    populateTree();
                    select(node);
                    enableUI();
                }
            }
        }));
        menu.add(new JMenuItem(new AbstractAction("Add MD2 Mesh") {
            @Override
            public void actionPerformed(ActionEvent e) {
                File file = Utils.selectFile(frame, IO.file("assets"), ".md2");

                if(file != null) {
                    Node parent = selected;
                    Node node = new Node();

                    if(parent == null) {
                        parent = scene.root;
                    }

                    try {
                        node.renderable = game.assets().load(file);
                        node.renderable = node.renderable.newInstance();
                        parent.add(node);
                    } catch(Exception ex) {
                        ex.printStackTrace(System.out);
                    }
                    populateTree();
                    select(node);
                    enableUI();
                }
            }
        }));
        menu.add(new JMenuItem(new AbstractAction("Add Node") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Node parent = selected;
                Node node = new Node();

                if(parent == null) {
                    parent = scene.root;
                }
                parent.add(node);

                populateTree();
                select(node);
                enableUI();
            }
        }));
        menu.addSeparator();
        menu.add(new JMenuItem(new AbstractAction("Cut") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(selected != null) {
                    try {
                        clipboard = new Node(game, scene, selected);
                        selected.detachFromParent();
                    } catch(Exception ex) {
                        ex.printStackTrace(System.out);
                    }
                    populateTree();
                    select(null);
                    enableUI();
                }
            }
        }));
        menu.add(new JMenuItem(new AbstractAction("Copy") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(selected != null) {
                    try {
                        clipboard = new Node(game, scene, selected);
                    } catch(Exception ex) {
                        ex.printStackTrace(System.out);
                    }
                }
            }
        }));
        menu.add(new JMenuItem(new AbstractAction("Paste") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(clipboard != null) {
                    Node parent = selected;
                    Node node = null;

                    if(parent == null) {
                        parent = scene.root;
                    }
                    try {
                        node = new Node(game, scene, clipboard);
                        parent.add(node);
                    } catch(Exception ex) {
                        ex.printStackTrace(System.out);
                    }
                    populateTree();
                    select(node);
                    enableUI();
                }
            }
        }));
        menu.add(new JMenuItem(new AbstractAction("Delete") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(selected != null) {
                    selected.detachFromParent();
                    populateTree();
                    select(null);
                    enableUI();
                }
            }
        }));
        menu.addSeparator();
        menu.add(new JMenuItem(new AbstractAction("Clear Selection") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(selected != null) {
                    populateTree();
                    select(null);
                    enableUI();
                }
            }
        }));

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(e.getButton() == 3) {
                    boolean enabled = scene != null;

                    if(enabled) {
                        enabled = scene.inDesign();
                    }
                    if(enabled) {
                        menu.show(tree, e.getX(), e.getY());
                    }
                }
            }
        });

        BoxLayout box;

        editorContainerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        editorPanel = new JPanel();
        box = new BoxLayout(editorPanel, BoxLayout.Y_AXIS);
        editorPanel.setLayout(box);
        editorContainerPanel.add(editorPanel);

        JScrollPane treePane = new JScrollPane(tree);
        JScrollPane editorPane = new JScrollPane(editorContainerPanel);
        JScrollPane consolePane = new JScrollPane(consoleTextArea);
        JPanel consolePanel = new JPanel(new BorderLayout());

        treePane.setPreferredSize(new Dimension(200, 100));
        editorPane.setPreferredSize(new Dimension(300, 100));
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
            } else {
                File f = scene.getLoadFile();

                if(f != null) {
                    try {
                        scene = null;
                        game.assets().clear();
                        scene = Serializer.deserialize(game, false, file);
                    } catch(Exception ex) {
                        ex.printStackTrace(System.out);
                        enableUI();
                    }
                }
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
                scene.camera.move(scene.camera.target, game.dX(), game.dY(), null);
            } else if(mode == PANY) {
                scene.camera.move(scene.camera.target, -game.dY(), null);
            } else if(mode == SEL) {
                if(!lDown) {
                    int w = game.w();
                    int h = game.h();
                    int x = game.mouseX();
                    int y = h - game.mouseY() - 1;
                    
                    scene.camera.unproject(x, y, 0, w, h, origin);
                    scene.camera.unproject(x, y, 1, w, h, direction);

                    direction.sub(origin).normalize();
                    time[0] = Float.MAX_VALUE;

                    selected = null;
                    try {
                        scene.root.traverse((n) -> {
                            bounds.clear();
                            bounds.add(origin);
                            bounds.add(point.set(direction).scale(time[0]).add(origin));
                            if(n.bounds.touches(bounds)) {
                                for(int i = 0; i != n.triangleCount(); i++) {
                                    n.triangleAt(scene.camera, i, triangle);
                                    if(triangle.n.dot(direction) < 0) {
                                        if(triangle.intersects(origin, direction, 0, time)) {
                                            selected = n;
                                        }
                                    }
                                }
                            }
                            if(n.isLight) {
                                bounds.min.set(n.absolutePosition).sub(8, 8, 8);
                                bounds.max.set(n.absolutePosition).add(8, 8, 8);
                                if(bounds.intersects(origin, direction, time)) {
                                    selected = n;
                                }
                            }
                            return true;
                        });
                    } catch(Exception ex) {
                        ex.printStackTrace(System.out);
                    }
                    select(selected);
                    enableUI();
                }
            } else if(selected != null) {
                if(mode == MOVXZ) {
                    scene.camera.move(selected.position, game.dX(), game.dY(), null);
                } else if(mode == MOVY) {
                    scene.camera.move(selected.position, -game.dY(), null);
                } else if(mode == RX) {
                    selected.rotate(0, game.dX());
                } else if(mode == RY) {
                    selected.rotate(1, game.dX());
                } else if(mode == RZ) {
                    selected.rotate(2, game.dX());
                } else if(mode == SCALE) {
                    if(game.dX() < 0) {
                        selected.scale.scale(0.9f);
                    } else if(game.dX() > 0) {
                        selected.scale.scale(1.1f);
                    }
                }
            }
            lDown = true;
        } else {
            if(!lDown && selected != null && (mode == MOVXZ || mode == MOVY)) {
                Vec3 p = selected.position;

                p.x = Math.round(p.x / scene.snap) * scene.snap;
                p.y = Math.round(p.y / scene.snap) * scene.snap;
                p.z = Math.round(p.z / scene.snap) * scene.snap;
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
                    if(key.equals("Load") || key.equals("AddScene")) {
                        buttons.get(key).setEnabled(enabled || scene == null);
                    } else if(key.equals("SnapShot")) {
                        buttons.get(key).setEnabled(scene != null);
                    } else if(
                        key.equals("ToTarg") ||
                        key.equals("TargTo") ||
                        key.equals("ZRot") ||
                        key.equals("X45") ||
                        key.equals("Y45") ||
                        key.equals("Z45") || 
                        key.equals("UScale")
                    ) {
                        buttons.get(key).setEnabled(enabled && selected != null);
                    } else {
                        buttons.get(key).setEnabled(enabled);
                    }
                }
            } else {
                buttons.get(key).setEnabled(scene != null);
                buttons.get(key).setText((enabled) ? "Play" : "Stop");
            }
        }
    }

    private void populateTree() {
        DefaultMutableTreeNode tree = new DefaultMutableTreeNode();

        if(scene != null) {
            if(scene.inDesign()) {
                for(Node node : scene.root) {
                    populateTree(tree, node);
                }
            }
        }
        model.setRoot(tree);
    }

    private void populateTree(DefaultMutableTreeNode parent, Node node) {
        DefaultMutableTreeNode tree = new DefaultMutableTreeNode(node);

        parent.add(tree);
        for(Node child : node) {
            populateTree(tree, child);
        }
    }

    private void select(Node node) {
        selected = null;
        if(node != null) {
            select(node, (DefaultMutableTreeNode)model.getRoot());
        } else {
            tree.clearSelection();
            edit(null);
        }
    }

    private void select(Node node, DefaultMutableTreeNode treeNode) {
        if(treeNode.getUserObject() == node) {
            tree.setSelectionPath(new TreePath(treeNode.getPath()));
        }
        for(int i = 0; i != treeNode.getChildCount(); i++) {
            select(node, (DefaultMutableTreeNode)treeNode.getChildAt(i));
        }
    }

    @SuppressWarnings("unchecked")
    private void edit(Object o) {
        editorPanel.removeAll();

        if(o == null) {
            editorContainerPanel.getParent().validate();
            return;
        }
        if(o != selected) {
            selected = null;
            tree.clearSelection();
        } else {
            JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            JButton button = new JButton(new AbstractAction("texture") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    File file = Utils.selectFile(frame, IO.file("assets"), ".png");

                    try {
                        if(file == null) {
                            selected.texture = null;
                        } else {
                            selected.texture = game.assets().load(file);
                        }
                    } catch(Exception ex) {
                        ex.printStackTrace(System.out);
                    }
                }
            });
            flowPanel.add(button);
            editorPanel.add(flowPanel);

            MD2Mesh mesh = selected.getAnimatedMesh();

            if(mesh != null) {
                JTextField seqField = new JTextField(
                    "" + 
                    mesh.getStart() + " " +
                    mesh.getEnd() + " " +
                    mesh.getSpeed() + " " +
                    ((mesh.isLooping()) ? "1" : "2"),
                    10);

                flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                flowPanel.add(seqField);
                flowPanel.add(new JLabel("Sequence", JLabel.LEFT));
                editorPanel.add(flowPanel);

                seqField.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyReleased(KeyEvent e) {
                        JTextField fld = (JTextField)e.getSource();
                        String[] tokens = fld.getText().split("\\s+");

                        if(tokens.length == 4) {
                            try {
                                int start = Integer.parseInt(tokens[0]);
                                int end = Integer.parseInt(tokens[1]);
                                int speed = Integer.parseInt(tokens[2]);
                                int looping = Integer.parseInt(tokens[3]);

                                selected.getAnimatedMesh().setSequence(start, end, speed, (looping == 0) ? false : true);
                            } catch(NumberFormatException ex) {
                            }
                        }
                    }
                });
            }
        }

        addFields(o);

        if(o == selected) {
            for(int i = 0; i != selected.componentCount(); i++) {
                NodeComponent component = selected.componentAt(i);
                JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                JButton button = new JButton(new AbstractAction("Delete - " + component.getClass().getSimpleName()) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JButton b = (JButton)e.getSource();
                        NodeComponent c = (NodeComponent)b.getClientProperty(PFX + ".COMPONENT");

                        c.remove();

                        edit(selected);
                    }
                });
                String name = component.getClass().getName();
                JLabel label = new JLabel(name.substring(name.lastIndexOf(".") + 1));
                JPanel flowPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));

                label.setFont(new Font(label.getFont().getFontName(), Font.BOLD, 14));
                label.setForeground(Color.BLUE);
                button.putClientProperty(PFX + ".COMPONENT", component);
                flowPanel2.add(label);
                editorPanel.add(flowPanel2);
                addFields(component);
                flowPanel.add(button);
                editorPanel.add(flowPanel);
            }

            JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            JButton button = new JButton(new AbstractAction("+") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JButton b = (JButton)e.getSource();
                    JComboBox<String> combo = (JComboBox<String>)b.getClientProperty(PFX + ".COMBO");
                    
                    try {
                        NodeComponent component = (NodeComponent)Class.forName((String)combo.getSelectedItem()).getConstructors()[0].newInstance();

                        selected.addComponent(game, scene, component);

                        edit(selected);
                    } catch(Exception ex) {
                        ex.printStackTrace(System.out);
                    }
                }
            });
            JComboBox<String> combo = new JComboBox<>();

            combo.setPrototypeDisplayValue("-------------------");
            for(String c : componentFactories) {
                combo.addItem(c);
            }
            combo.setSelectedIndex(0);
            flowPanel.add(combo);
            flowPanel.add(button);
            editorPanel.add(flowPanel, o);

            button.putClientProperty(PFX + ".COMBO", combo);
        }
        editorContainerPanel.getParent().validate();
    }

    @SuppressWarnings("unchecked")
    private void addFields(Object o) {
        Class<? extends Object> cls = o.getClass();
        Field[] fields = cls.getFields();

        for(Field field : fields) {
            Class<? extends Object> type = field.getType();
            String name = field.getName();

            if(
                int.class.isAssignableFrom(type) ||
                float.class.isAssignableFrom(type) ||
                String.class.isAssignableFrom(type) ||
                Vec2.class.isAssignableFrom(type) ||
                Vec3.class.isAssignableFrom(type) ||
                Vec4.class.isAssignableFrom(type) 
            ) {
                try {
                    JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                    JTextField textField = new JTextField(Utils.toString(o, name), 10);

                    textField.putClientProperty(PFX + ".NAME", name);
                    textField.putClientProperty(PFX + ".OBJECT", o);

                    flowPanel.add(textField);
                    flowPanel.add(new JLabel(name, JLabel.LEFT));
                    editorPanel.add(flowPanel);

                    textField.addKeyListener(new KeyAdapter() {
                        @Override
                        public void keyReleased(KeyEvent e) {
                            try {
                                JTextField field = (JTextField)e.getSource();
                                String fname = (String)field.getClientProperty(PFX + ".NAME");
                                Object fo = field.getClientProperty(PFX + ".OBJECT");

                                Utils.parse(fo, field.getText(), fname);

                                if(fo instanceof Node && fname.equals("name")) {
                                    TreeModelEvent tme = new TreeModelEvent(model, tree.getSelectionPath());
                                    TreeModelListener[] listeners = model.getTreeModelListeners();
                                    
                                    for(TreeModelListener l : listeners) {
                                        l.treeNodesChanged(tme);
                                    }
                                }
                            } catch(Exception ex) {
                            }
                        }
                    });
                } catch(Exception ex) {
                    ex.printStackTrace(System.out);
                }
            } else if(boolean.class.isAssignableFrom(type)) {
                try {
                    JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                    JCheckBox checkBox = new JCheckBox(name, (Boolean)field.get(o));

                    checkBox.putClientProperty(PFX + ".NAME", name);
                    checkBox.putClientProperty(PFX + ".OBJECT", o);

                    flowPanel.add(checkBox);
                    editorPanel.add(flowPanel);

                    checkBox.addItemListener((e) -> {
                        JCheckBox cb = (JCheckBox)e.getSource();
                        String fname = (String)cb.getClientProperty(PFX + ".NAME");
                        Object fo = cb.getClientProperty(PFX + ".OBJECT");

                        try {
                            Utils.parse(fo, (cb.isSelected()) ? "true" : "false", fname);
                        } catch(Exception ex) {
                            ex.printStackTrace(System.out);
                        }
                    });
                } catch(Exception ex) {
                    ex.printStackTrace(System.out);
                }
            } else if(type.isEnum()) {
                try {
                    JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                    JComboBox<Object> combo = new JComboBox<>();
                    Object[] values = type.getEnumConstants();
                    int w = 1;
                    String item = Utils.toString(o, name);

                    for(Object v : values) {
                        String s = v.toString();

                        if(s.length() > w) {
                            w = s.length();
                            combo.setPrototypeDisplayValue(v);
                        }
                        combo.addItem(v);
                    }
                    combo.putClientProperty(PFX + ".NAME", name);
                    combo.putClientProperty(PFX + ".OBJECT", o);
                    for(int i = 0; i != values.length; i++) {
                        String v = values[i].toString();

                        if(v.equals(item)) {
                            combo.setSelectedIndex(i);
                            break;
                        }
                    }

                    flowPanel.add(combo);
                    flowPanel.add(new JLabel(name, JLabel.LEFT));
                    editorPanel.add(flowPanel);

                    combo.addItemListener((e) -> {
                        JComboBox<Object> cb = (JComboBox<Object>)e.getSource();
                        String fname = (String)cb.getClientProperty(PFX + ".NAME");
                        Object fo = cb.getClientProperty(PFX + ".OBJECT");

                        try {
                            Utils.parse(fo, cb.getSelectedItem().toString(), fname);
                        } catch(Exception ex) {
                            ex.printStackTrace(System.out);
                        }
                    });
                } catch(Exception ex) {
                    ex.printStackTrace(System.out);
                }
            }
        }
    }

    private void createScene() {
        Object r = JOptionPane.showInputDialog(frame, "Scene Name", "");

        if(r != null) {
            String name = ((String)r).trim();

            if(name.length() == 0) {
                System.out.println("name is blank");
                return;
            }
            
            for(int i = 0; i != name.length(); i++) {
                char c = name.charAt(i);

                if(!(Character.isLetter(c) || Character.isDigit(c) || c == '_')) {
                    System.out.println("name can only contain letter, digit or '_' characters");
                    return;
                }
            }

            File file = new File(new File("scenes"), name + ".xml");

            if(file.exists()) {
                System.out.println("scene already exists");
                return;
            }

            try {
                IO.writeAllBytes("<scene/>".getBytes(), file);
            } catch(Exception ex) {
                ex.printStackTrace(System.out);
            }
        }
    }
}