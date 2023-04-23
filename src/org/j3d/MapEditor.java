package org.j3d;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.*;

public class MapEditor extends JPanel implements Game.GameLoop {

    private Game game;
    private JFrame frame;
    private LightMapper lightMapper;
    private Map map = null;
    private JButton loadButton;
    private JButton saveButton;
    private JButton playButton;
    private JComboBox<String> entityCombo;
    private JButton addEntityButton;
    private JButton addLightButton;
    private JButton deleteButton;
    private JButton mapButton;
    private JButton editMapButton;
    private JToggleButton rotateButton;
    private JToggleButton zoomButton;
    private JToggleButton panXZButton;
    private JToggleButton panYButton;
    private JToggleButton selectButton;
    private JToggleButton moveXZButton;
    private JToggleButton moveYButton;
    private JToggleButton sizeXButton;
    private JToggleButton sizeYButton;
    private JToggleButton sizeZButton;
    private JCheckBox showLightsButton;
    private JCheckBox showEntitiesButton;
    private JPanel editorPanel;
    private JScrollPane scrollPane;
    private boolean playing = false;
    private Mesh cubeMesh = null;
    private Node entitiesNode = null;
    private Node lightsNode = null;
    private Node arrowNode = null;
    private Sprite3D lightSprite = null;
    private Sprite3D arrowSprite = null;
    private boolean down = false;
    private JTextField snapField;
    private Node selection = null;
    private JButton snapShotButton;
    
    public MapEditor(JFrame frame, LightMapper lightMapper, int w, int h, int scale) {        
        setLayout(new BorderLayout());

        this.frame = frame;

        game = new Game(w, h, scale, this);
        add(game.getPanel(), BorderLayout.CENTER);

        this.lightMapper = lightMapper;

        JPanel toolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        loadButton = new JButton(new AbstractAction("Load") {
            @Override
            public void actionPerformed(ActionEvent e) {
                File file = Utils.selectFile(frame, IO.file("maps"), ".txt");
                if(file != null) {
                    loadMap(file);
                }
            }
        });
        toolPanel.add(loadButton);

        saveButton = new JButton(new AbstractAction("Save") {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    map.save(game);
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        toolPanel.add(saveButton);

        playButton = new JButton(new AbstractAction("Play") {
            @Override
            public void actionPerformed(ActionEvent e) {
                playing = !playing;
                loadMap(new File(new File("maps"), IO.fileNameWithOutExtension(map.getFile()) + ".txt"));
            }
        });
        toolPanel.add(playButton);

        entityCombo = new JComboBox<>();
        setEntityTypes();
        toolPanel.add(entityCombo);

        addEntityButton = new JButton(new AbstractAction("Add Entity") {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Node node = new Node();
                    Entity entity = (Entity)EntityFactory.newInstance().typeAt(entityCombo.getSelectedIndex()).getConstructors()[0].newInstance();

                    entity.position.set(map.scene().camera.target);
                    map.entities.add(entity);
                    setupEntityNode(node, entity);
                    entitiesNode.add(node);

                    select(node);
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        toolPanel.add(addEntityButton);

        addLightButton = new JButton(new AbstractAction("Add Light") {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Node node = new Node();
                    Node spriteNode = new Node();

                    node.isLight = true;
                    node.position.set(map.scene().camera.target);
                    setupLightNode(node, spriteNode);
                    node.add(spriteNode);
                    lightsNode.add(node);

                    select(spriteNode);
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        toolPanel.add(addLightButton);

        deleteButton = new JButton(new AbstractAction("Delete") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(selection != null) {
                    if(selection.getParent() != map.scene().root.childAt(0)) {
                        if(selection.tag instanceof Entity) {
                            map.entities.remove(selection.tag);
                        }
                        selection.detachFromParent();
                    }
                    select(null);
                }
            }
        });
        toolPanel.add(deleteButton);

        mapButton = new JButton(new AbstractAction("Map") {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    map.light(game, true);
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        toolPanel.add(mapButton);

        editMapButton = new JButton(new AbstractAction("Edit Map") {
            @Override
            public void actionPerformed(ActionEvent e) {
                editorPanel.removeAll();
                try {
                    selection = null;
                    addEditor(map.getClass().getField("lightMapWidth"), map);
                    addEditor(map.getClass().getField("lightMapHeight"), map);
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
                editorPanel.getParent().validate();
            }
        });
        toolPanel.add(editMapButton);

        snapField = new JTextField("1", 6);
        snapField.setToolTipText("Snap to integer grid");
        toolPanel.add(snapField);

        snapShotButton = new JButton(new AbstractAction("Snap Shot") {
            @Override
            public void actionPerformed(ActionEvent e) {
                game.takeSnapShot();
            }
        });
        toolPanel.add(snapShotButton);

        add(toolPanel, BorderLayout.SOUTH);

        toolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        rotateButton = new JToggleButton(new AbstractAction("Rot") {
            @Override
            public void actionPerformed(ActionEvent e) {
                setMode(rotateButton);
            }
        });
        rotateButton.setSelected(true);
        toolPanel.add(rotateButton);

        zoomButton = new JToggleButton(new AbstractAction("Zoom") {
            @Override
            public void actionPerformed(ActionEvent e) {
                setMode(zoomButton);
            }
        });
        toolPanel.add(zoomButton);

        panXZButton = new JToggleButton(new AbstractAction("PanXZ") {
            @Override
            public void actionPerformed(ActionEvent e) {
                setMode(panXZButton);
            }
        });
        toolPanel.add(panXZButton);

        panYButton = new JToggleButton(new AbstractAction("PanY") {
            @Override
            public void actionPerformed(ActionEvent e) {
                setMode(panYButton);
            }
        });
        toolPanel.add(panYButton);

        selectButton = new JToggleButton(new AbstractAction("Select") {
            @Override
            public void actionPerformed(ActionEvent e) {
                setMode(selectButton);
            }
        });
        toolPanel.add(selectButton);

        moveXZButton = new JToggleButton(new AbstractAction("MovXZ") {
            @Override
            public void actionPerformed(ActionEvent e) {
                setMode(moveXZButton);
            }
        });
        toolPanel.add(moveXZButton);

        moveYButton = new JToggleButton(new AbstractAction("MovY") {
            @Override
            public void actionPerformed(ActionEvent e) {
                setMode(moveYButton);
            }
        });
        toolPanel.add(moveYButton);

        sizeXButton = new JToggleButton(new AbstractAction("SizeX") {
            @Override
            public void actionPerformed(ActionEvent e) {
                setMode(sizeXButton);
            }
        });
        toolPanel.add(sizeXButton);

        sizeYButton = new JToggleButton(new AbstractAction("SizeY") {
            @Override
            public void actionPerformed(ActionEvent e) {
                setMode(sizeYButton);
            }
        });
        toolPanel.add(sizeYButton);

        sizeZButton = new JToggleButton(new AbstractAction("SizeZ") {
            @Override
            public void actionPerformed(ActionEvent e) {
                setMode(sizeZButton);
            }
        });
        toolPanel.add(sizeZButton);

        showLightsButton = new JCheckBox(new AbstractAction("Lights") {
            @Override
            public void actionPerformed(ActionEvent e) {
                lightsNode.visible = showLightsButton.isSelected();
            }
        });
        showLightsButton.setSelected(true);
        toolPanel.add(showLightsButton);

        showEntitiesButton = new JCheckBox(new AbstractAction("Entities") {
            @Override
            public void actionPerformed(ActionEvent e) {
                entitiesNode.visible = showEntitiesButton.isSelected();
            }
        });
        showEntitiesButton.setSelected(true);
        toolPanel.add(showEntitiesButton);

        add(toolPanel, BorderLayout.NORTH);

        editorPanel = new JPanel();

        BoxLayout layout = new BoxLayout(editorPanel, BoxLayout.Y_AXIS);
        JPanel parentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        editorPanel.setLayout(layout);
        scrollPane = new JScrollPane(parentPanel);
        parentPanel.add(editorPanel);
        scrollPane.setPreferredSize(new Dimension(250, 100));
        add(scrollPane, BorderLayout.EAST);

        enableUI(false);
    }

    public void setEntityTypes() {
        EntityFactory factory = EntityFactory.newInstance();
        entityCombo.removeAllItems();
        for(int i = 0; i != factory.count(); i++) {
            entityCombo.addItem(factory.typeAt(i).getSimpleName());
        }
        entityCombo.setSelectedIndex(0);
    }

    public Game game() {
        return game;
    }

    @Override
    public void resize(Game game) throws Exception {
    }

    @Override
    public void init(Game game) throws Exception {
    }

    @Override
    public void render(Game game) throws Exception {
        if(map != null) {
            map.scene().render(game);
        } else {
            game.renderer().clear();
        }
        if(playing) {
            map.renderSprites();
        }
    }

    @Override
    public void update(Game game) throws Exception {
        if(playing) {
            map.update();
            if(map.loadFile != null) {
                game.assets().clear();
                map = Map.load(map.loadFile, lightMapper, game);
                try {
                    map.light(game, false);
                    map.init(game);
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        } else if(map != null) {
            if(game.buttonDown(0)) {
                if(rotateButton.isSelected()) {
                    map.scene().camera.rotate(game.dX(), game.dY());
                } else if(zoomButton.isSelected()) {
                    map.scene().camera.zoom(game.dY());
                } else if(panXZButton.isSelected()) {
                    map.scene().camera.move(map.scene().camera.target, game.dX(), game.dY(), null);
                } else if(panYButton.isSelected()) {
                    map.scene().camera.move(map.scene().camera.target, -game.dY(), null);
                } else if(selectButton.isSelected()) {
                    if(!down) {
                        Vec3 origin = new Vec3();
                        Vec3 direction = new Vec3();

                        map.scene().camera.unproject(game.mouseX(), game.h() - game.mouseY() - 1, 0, game.w(), game.h(), origin);
                        map.scene().camera.unproject(game.mouseX(), game.h() - game.mouseY() - 1, 1, game.w(), game.h(), direction);
                        direction.sub(origin).normalize();

                        float[] time = new float[] { Float.MAX_VALUE };

                        selection = null;

                        select(map.scene().root, origin, direction, time);

                        select(selection);
                    }
                } else if(selection != null) {
                    Node node = selection;
                    if(node.getParent().isLight) {
                        node = node.getParent();
                    } else if(!(node.tag instanceof Entity)) {
                        node = null;
                    }
                    if(node != null) {
                        if(moveXZButton.isSelected()) {
                            map.scene().camera.move(node.position, game.dX(), game.dY(), null);
                        } else if(moveYButton.isSelected()) {
                            map.scene().camera.move(node.position, -game.dY(), null);
                        } else if(node.tag instanceof Entity) {
                            Entity entity = (Entity)node.tag;
                            if(sizeXButton.isSelected()) {
                                entity.size.x += game.dX();
                            } else if(sizeYButton.isSelected()) {
                                entity.size.y += game.dX();
                            } else if(sizeZButton.isSelected()) {
                                entity.size.z += game.dX();
                            }
                        }
                        if(node.tag instanceof Entity) {
                            ((Entity)node.tag).position.set(node.position);
                            setupEntityNode(node, (Entity)node.tag);
                        }
                    }
                }
                down = true;
            } else {
                if(selection != null && down) {
                    Node node = selection;
                    if(node.getParent().isLight) {
                        node = node.getParent();
                    } else if(!(node.tag instanceof Entity)) {
                        node = null;
                    }
                    if(node != null) {
                        int snap = 1;
                        try {
                            snap = Integer.parseInt(snapField.getText().trim());
                            snap = Math.max(1, snap);
                        } catch(NumberFormatException ex) {
                            snap = 1;
                        }
                        if(moveXZButton.isSelected() || moveYButton.isSelected()) {
                            Vec3 position = node.position;

                            position.x = (float)Math.floor(position.x / snap) * snap;
                            position.y = (float)Math.floor(position.y / snap) * snap;
                            position.z = (float)Math.floor(position.z / snap) * snap;
                        } else if(node.tag instanceof Entity) {
                            Vec3 size = ((Entity)node.tag).size;
                            if(sizeXButton.isSelected() || sizeYButton.isSelected() || sizeZButton.isSelected()) {
                                size.x = (float)Math.floor(size.x / snap) * snap;
                                size.y = (float)Math.floor(size.y / snap) * snap;
                                size.z = (float)Math.floor(size.z / snap) * snap;
                            }
                            size.x = Math.max(8, size.x);
                            size.y = Math.max(8, size.y);
                            size.z = Math.max(8, size.z);
                        }
                        if(node.tag instanceof Entity) {
                            ((Entity)node.tag).position.set(node.position);
                            setupEntityNode(node, (Entity)node.tag);
                        }
                    }
                }
                down = false;
            }
        }
    }

    private void select(Node node, Vec3 origin, Vec3 direction, float[] time) {
        if(node.getMesh() != null || node.getSprite3D() == lightSprite) {
            Triangle triangle = new Triangle();
            for(int i = 0; i != node.triangleCount(); i++) {
                node.getTriangle(map.scene().camera, i, triangle);
                if(triangle.n.dot(direction) < 0) {
                    if(triangle.intersects(origin, direction, 0, time)) {
                        selection = node;
                    }
                }
            }
        } 
        for(Node child : node) {
            select(child, origin, direction, time);
        }
    }

    private void setMode(JToggleButton button) {
        rotateButton.setSelected(rotateButton == button);
        zoomButton.setSelected(zoomButton == button);
        panXZButton.setSelected(panXZButton == button);
        panYButton.setSelected(panYButton == button);
        selectButton.setSelected(selectButton == button);
        moveXZButton.setSelected(moveXZButton == button);
        moveYButton.setSelected(moveYButton == button);
        sizeXButton.setSelected(sizeXButton == button);
        sizeYButton.setSelected(sizeYButton == button);
        sizeZButton.setSelected(sizeZButton == button);;
    }

    private void loadMap(File file) {
        try {
            game.assets().clear();

            map = Map.load(file, lightMapper, game);
            try {
                map.light(game, false);
                if(playing) {
                    map.init(game);
                    game.getPanel().grabFocus();
                }
            } catch(Exception ex) {
                ex.printStackTrace();
            }
            if(!playing) {
                lightSprite = new Sprite3D();
                lightSprite.size = 16;
                arrowSprite = new Sprite3D();
                entitiesNode = new Node();
                arrowNode = new Node();
                lightsNode = map.scene().root.childAt(1);

                Node cubeNode = game.assets().load(IO.file("assets/ui/cube.obj"));

                showLightsButton.setSelected(true);
                showEntitiesButton.setSelected(true);

                cubeMesh = cubeNode.childAt(0).getMesh();
                for(int i = 0; i != cubeMesh.vertexCount(); i++) {
                    Vertex v = cubeMesh.getVertex(i);

                    if(Math.abs(v.normal.x) > 0.5) {
                        v.color.set(1, 0, 0, 0.5f);
                    } else if(Math.abs(v.normal.y) > 0.5) {
                        v.color.set(0, 1, 0, 0.5f);
                    } else {
                        v.color.set(0, 0, 1, 0.5f);
                    }
                }

                for(Node node : lightsNode) {
                    Node spriteNode = new Node();

                    setupLightNode(node, spriteNode);
                    node.add(spriteNode);
                }
                for(Entity entity : map.entities) {
                    Node node = new Node();

                    setupEntityNode(node, entity);
                    entitiesNode.add(node);
                }
                map.scene().root.add(entitiesNode);

                Node arrowSpriteNode = new Node();

                arrowSpriteNode.renderable = arrowSprite;
                arrowSpriteNode.texture = game.assets().load(IO.file("assets/ui/arrow.png"));
                arrowSpriteNode.maskEnabled = true;
                arrowSpriteNode.position.y = 16;
                arrowNode.add(arrowSpriteNode);
                arrowNode.follow = FollowCamera.TARGET;
                map.scene().root.add(arrowNode);
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            map = null;
            playing = false;
        }
        select(null);
        enableUI(playing);
    }

    private void select(Node node) {
        editorPanel.removeAll();

        selection = node;
        if(node != null) {
            Node parent = node.getParent();

            if(parent.isLight) {
                try {
                    addEditor(parent.getClass().getField("position"), parent);
                    addEditor(parent.getClass().getField("lightColor"), parent);
                    addEditor(parent.getClass().getField("lightRadius"), parent);
                    addEditor(parent.getClass().getField("lightSampleCount"), parent);
                    addEditor(parent.getClass().getField("lightSampleRadius"), parent);
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            } else if(node.tag instanceof Entity) {
                Entity entity = (Entity)node.tag;
                Field[] fields = entity.getClass().getFields();
                for(Field field : fields) {
                    addEditor(field, entity);
                }
            } else {
                try {
                    Mesh mesh = node.getMesh();
                    if(mesh != null) {
                        addEditor(mesh.getClass().getField("color"), mesh);
                    }
                    addEditor(node.getClass().getField("ambientColor"), node);
                    addEditor(node.getClass().getField("diffuseColor"), node);
                    addEditor(node.getClass().getField("castsShadow"), node);
                    addEditor(node.getClass().getField("receivesShadow"), node);
                    addEditor(node.getClass().getField("blendEnabled"), node);
                    addEditor(node.getClass().getField("additiveBlend"), node);
                    addEditor(node.getClass().getField("zOrder"), node);
                    addEditor(node.getClass().getField("cullState"), node);
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        editorPanel.getParent().validate();
    }

    private void addEditor(Field field, Object o) {
        Class<?> type = field.getType();

        if(boolean.class.isAssignableFrom(type) ) {
            JCheckBox checkBox = new JCheckBox(new AbstractAction(field.getName()) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        field.set(o, ((JCheckBox)e.getSource()).isSelected());
                        if(field.getName().equals("blendEnabled") && o instanceof Node) {
                            ((Node)o).depthWriteEnabled = !(boolean)field.get(o);
                        }
                    } catch(Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            try {
                checkBox.setSelected((boolean)field.get(o));
            } catch(Exception ex) {
                ex.printStackTrace();;
            }
            JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            flowPanel.add(checkBox);
            editorPanel.add(flowPanel);
        } else if(
            int.class.isAssignableFrom(type) || 
            float.class.isAssignableFrom(type) || 
            Vec2.class.isAssignableFrom(type) || 
            Vec3.class.isAssignableFrom(type) || 
            Vec4.class.isAssignableFrom(type)) {
            JLabel label = new JLabel();
            JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            label.setText(field.getName());
            flowPanel.add(label);
            editorPanel.add(flowPanel);
            try {
                final JTextField textField = new JTextField(field.get(o).toString(), 15);
                
                flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                flowPanel.add(textField);
                editorPanel.add(flowPanel);
                textField.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyReleased(KeyEvent e) {
                        try {
                            String text = textField.getText().trim();
                            if(int.class.isAssignableFrom(type)) {
                                field.set(o, Integer.parseInt(text));
                            } else if(float.class.isAssignableFrom(type)) {
                                field.set(o, Float.parseFloat(text));
                            } else {
                                String[] tokens = text.split("\\s+");

                                if(Vec2.class.isAssignableFrom(type) && tokens.length >= 2) {
                                    ((Vec2)field.get(o)).parse(tokens, 0);
                                } else if(Vec3.class.isAssignableFrom(type) && tokens.length >= 3) {
                                    ((Vec3)field.get(o)).parse(tokens, 0);
                                } else if(Vec4.class.isAssignableFrom(type) && tokens.length >= 4) {
                                    ((Vec4)field.get(o)).parse(tokens, 0);

                                    if(field.getName().equals("color") && o instanceof Mesh) {
                                        Mesh mesh = (Mesh)o;
                                        for(int i = 0; i != mesh.vertexCount(); i++) {
                                            mesh.getVertex(i).color.set((Vec4)field.get(o));
                                        }
                                    }
                                }
                            }
                            if(o instanceof Entity) {
                                Entity entity = (Entity)o;

                                setupEntityNode((Node)entity.tag, entity);
                            } 
                        } catch(NumberFormatException ex) {
                        } catch(Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        } else if(String.class.isAssignableFrom(type)) {
            JButton button = new JButton(new AbstractAction(field.getName() + "...") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JDialog dialog = new JDialog(frame);

                    dialog.setModal(true);
                    dialog.setResizable(true);
                    dialog.setSize(600, 600);

                    JTextArea area = new JTextArea();
                    JScrollPane pane = new JScrollPane(area);
                    Font font = new Font(Font.MONOSPACED, Font.PLAIN, 12);

                    area.setFont(font);

                    dialog.setLayout(new BorderLayout());
                    dialog.add(pane, BorderLayout.CENTER);

                    try {
                        area.setText((String)field.get(o));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    area.addKeyListener(new KeyAdapter() {
                        @Override
                        public void keyReleased(KeyEvent e) {
                            try {
                                field.set(o, area.getText());
                            } catch(Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
                    dialog.setVisible(true);;
                }
            });
            JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            flowPanel.add(button);
            editorPanel.add(flowPanel);
        } else if(type.isEnum()) {
            try {
                final JComboBox<Object> comboBox = new JComboBox<>();
                Object[] constants = type.getEnumConstants();
                Object[] values = new Object[constants.length];
                int i = 0;
                for(Object constant : constants) {
                    comboBox.addItem(constant.toString());
                    values[i++] = constant;
                }
                for(i = 0; i != values.length; i++) {
                    if(values[i] == field.get(o)) {
                        comboBox.setSelectedIndex(i);
                        break;
                    }
                }
                JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                JLabel label = new JLabel(field.getName());
                flowPanel.add(label);
                editorPanel.add(flowPanel);
                flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                flowPanel.add(comboBox);
                editorPanel.add(flowPanel);
                comboBox.addItemListener(new ItemListener() {
                    @Override
                    public void itemStateChanged(ItemEvent e) {
                        try {
                            field.set(o, values[comboBox.getSelectedIndex()]);
                        } catch(Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void setupEntityNode(Node node, Entity entity) {
        node.renderable = cubeMesh;
        node.tag = entity;
        entity.tag = node;
        node.position.set(entity.position);
        node.scale.set(entity.size.x / 32.0f, entity.size.y / 32.0f, entity.size.z / 32.0f);
        node.cullState = CullState.NONE;
    }

    private void setupLightNode(Node node, Node spriteNode) throws Exception {
        spriteNode.texture = game.assets().load(IO.file("assets/ui/light.png"));
        spriteNode.maskEnabled = true;
        spriteNode.renderable = lightSprite;
    }

    private void enableUI(boolean playing) {
        playButton.setText((playing) ? "Stop" : "Play");
        if(map == null) {
            playButton.setEnabled(false);
            saveButton.setEnabled(false);
            addEntityButton.setEnabled(false);
            addLightButton.setEnabled(false);
            deleteButton.setEnabled(false);
            mapButton.setEnabled(false);
            editMapButton.setEnabled(false);
            rotateButton.setEnabled(false);
            zoomButton.setEnabled(false);
            panXZButton.setEnabled(false);
            panYButton.setEnabled(false);
            selectButton.setEnabled(false);
            moveXZButton.setEnabled(false);
            moveYButton.setEnabled(false);
            sizeXButton.setEnabled(false);
            sizeYButton.setEnabled(false);
            sizeZButton.setEnabled(false);
            showLightsButton.setEnabled(false);
            showEntitiesButton.setEnabled(false);
        } else {
            playButton.setEnabled(true);
            loadButton.setEnabled(!playing);
            saveButton.setEnabled(!playing);
            addEntityButton.setEnabled(!playing);
            addLightButton.setEnabled(!playing);
            deleteButton.setEnabled(!playing);
            mapButton.setEnabled(!playing);
            editMapButton.setEnabled(!playing);
            rotateButton.setEnabled(!playing);
            zoomButton.setEnabled(!playing);
            panXZButton.setEnabled(!playing);
            panYButton.setEnabled(!playing);
            selectButton.setEnabled(!playing);
            moveXZButton.setEnabled(!playing);
            moveYButton.setEnabled(!playing);
            sizeXButton.setEnabled(!playing);
            sizeYButton.setEnabled(!playing);
            sizeZButton.setEnabled(!playing);
            showLightsButton.setEnabled(!playing);
            showEntitiesButton.setEnabled(!playing);
        }
    }
}
