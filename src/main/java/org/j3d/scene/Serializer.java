package org.j3d.scene;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.j3d.AssetLoader;
import org.j3d.AssetManager;
import org.j3d.Game;
import org.j3d.IO;
import org.j3d.Mesh;
import org.j3d.MeshLoader;
import org.j3d.Parser;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Serializer implements AssetLoader {
    
    public static void serialize(Scene scene, File file) throws Exception {
        StringBuilder b = new StringBuilder(1000);

        b.append("<scene");
        appendAttributes(scene, b);
        if(scene.root.getCount() == 0) {
            b.append("/>\n");
        } else {
            b.append(">\n");
            for(Node node : scene.root) {
                appendNode(node, "\t", b);
            }
            b.append("</scene>\n");
        }
        IO.writeAllBytes(b.toString().getBytes(), file);
    }

    private static void appendNode(Node node, String indent, StringBuilder b) throws Exception {
        b.append(indent + "<node");
        appendAttributes(node, b);
        if(node.getCount() == 0 && node.getComponentCount() == 0) {
            b.append("/>\n");
        } else {
            b.append(">\n");
            for(int i = 0; i != node.getComponentCount(); i++) {
                b.append(indent + "\t" + "<component");
                appendAttributes(node.componentAt(i), b);
                b.append("/>\n");
            }
            for(Node child : node) {
                appendNode(child, indent + "\t", b);
            }
            b.append(indent + "</node>\n");
        }
    }

    private static void appendAttributes(Object obj, StringBuilder b) throws Exception {
        Field[] fields = obj.getClass().getFields();

        if(obj instanceof NodeComponent) {
            appendAttribute("component-type", obj.getClass().getName(), b);
        }
        if(obj instanceof Node) {
            Node node = (Node)obj;

            if(node.renderable != null) {
                appendAttribute("renderable", node.renderable.getFile().getPath(), b);
            }
        }
        for(Field field : fields) {
            Class<? extends Object> cls = field.getType();
            int m = field.getModifiers();

            if(
                boolean.class.isAssignableFrom(cls) ||
                int.class.isAssignableFrom(cls) ||
                float.class.isAssignableFrom(cls) ||
                String.class.isAssignableFrom(cls) ||
                Vector2f.class.isAssignableFrom(cls) ||
                Vector3f.class.isAssignableFrom(cls) ||
                Vector4f.class.isAssignableFrom(cls) ||
                Matrix4f.class.isAssignableFrom(cls) ||
                cls.isEnum()) {
                    if(Modifier.isPublic(m) && !Modifier.isStatic(m)) {
                        appendAttribute(field.getName(), Parser.toString(field.get(obj)), b);
                    }
                }
        }
    }

    private static void appendAttribute(String name, String value, StringBuilder b) {
        b.append(" " + name + "=\"" + value + "\"");
    }

    public static Scene deserialize(File file, Game game, boolean inDesign) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);
        Scene scene = create(document.getDocumentElement(), game, null, inDesign);


        scene.calcTransform(game);
        scene.root.calcBoundsAndTransform();
        scene.root.init();
        scene.calcTransform(game);
        scene.root.calcBoundsAndTransform();
        scene.root.start();

        return scene;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Object> T create(Element element, Game game, Scene ownerScene, boolean inDesign) throws Exception {
        NodeList nodes = element.getChildNodes();

        if(element.getTagName().equals("scene")) {
            Scene scene = new Scene(game, inDesign);

            parseAttributes(element, game, scene);
            for(int i = 0; i != nodes.getLength(); i++) {
                org.w3c.dom.Node n = nodes.item(i);

                if(n instanceof Element) {
                    Element element2 = (Element)n;

                    if(element2.getTagName().equals("node")) {
                        scene.root.addChild(create(element2, game, scene, inDesign));
                    }
                }
            }

            return (T)scene;
        } else if(element.getTagName().equals("node")) {
            Node node = new Node();

            parseAttributes(element, game, node);
            for(int i = 0; i != nodes.getLength(); i++) {
                org.w3c.dom.Node n = nodes.item(i);

                if(n instanceof Element) {
                    Element element2 = (Element)n;

                    if(element2.getTagName().equals("node")) {
                        node.addChild(create(element2, game, ownerScene, inDesign));
                    } else if(element2.getTagName().equals("component")) {
                        NodeComponent component = create(element2, game, ownerScene, inDesign);

                        if(component != null) {
                            node.addComponent(game, ownerScene, component);
                        }
                    }
                }
            }
            return (T)node;
        } else if(element.getTagName().equals("component")) {
            try {
                NodeComponent component = (NodeComponent)Class.forName(element.getAttribute("component-type")).getConstructors()[0].newInstance();

                parseAttributes(element, game, component);

                return (T)component;
            } catch(Exception ex) {
                ex.printStackTrace();

                return null;
            }
        } else {
            throw new Exception("invalid tag name");
        }
    }

    private static void parseAttributes(Element element, Game game, Object obj) throws Exception {
        Field[] fields = obj.getClass().getFields();

        if(obj instanceof Node) {
            Node node = (Node)obj;

            if(element.hasAttribute("renderable")) {
                try {
                    node.renderable = game.getAssets().load(IO.file(element.getAttribute("renderable")));
                    node.renderable = node.renderable.newInstance();
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        for(Field field : fields) {
            Class<? extends Object> cls = field.getType();
            int m = field.getModifiers();
            String name = field.getName();

            if(
                boolean.class.isAssignableFrom(cls) ||
                int.class.isAssignableFrom(cls) ||
                float.class.isAssignableFrom(cls) ||
                String.class.isAssignableFrom(cls) ||
                Vector2f.class.isAssignableFrom(cls) ||
                Vector3f.class.isAssignableFrom(cls) ||
                Vector4f.class.isAssignableFrom(cls) ||
                Matrix4f.class.isAssignableFrom(cls) ||
                cls.isEnum()) {
                if(Modifier.isPublic(m) && !Modifier.isStatic(m)) {
                    if(element.hasAttribute(name)) {
                        String value = element.getAttribute(name);
                        String[] tokens = value.split("\\s+");
                        Object oValue = Parser.parseObject(tokens, 0, field.get(obj));

                        if(!Modifier.isFinal(m)) {
                            field.set(obj, Parser.parseObject(tokens, 0, oValue));
                        }
                    }
                }
            }
        }
    }

    @Override
    public Object load(File file, AssetManager assets) throws Exception {
        return new MeshRenderable(file, (Mesh)new MeshLoader().load(file, assets));
    }
}
