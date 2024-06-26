package org.j3d;

import java.io.File;
import java.lang.reflect.Field;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Serializer {

    public static Scene deserialize(Game game, boolean inDesign, File file) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document =  builder.parse(file);

        return (Scene)load(game, new Scene(file, inDesign, game), document.getDocumentElement());
    }

    private static Object load(Game game, Scene scene, Element element) throws Exception {
        NodeList nodes = element.getChildNodes();
        Object r = null;

        if(element.getTagName().equals("scene")) {
            load(scene, element);

            for(int i = 0; i != nodes.getLength(); i++) {
                org.w3c.dom.Node xmlNode = nodes.item(i);

                if(xmlNode instanceof Element) {
                    Element element2 = (Element)xmlNode;

                    if(element2.getTagName().equals("camera")) {
                        load(scene.camera, element2);
                    } else {
                        scene.root.add((Node)load(game, scene, element2));
                    }
                }
            }
            r = scene;
        } else {
            Node node = new Node();

            load(node, element);

            if(element.hasAttribute("renderable")) {
                try {
                    Renderable renderable = game.assets().load(IO.file(element.getAttribute("renderable")));

                    node.renderable = renderable.newInstance();

                    MD2Mesh mesh = node.getAnimatedMesh();

                    if(mesh != null && element.hasAttribute("sequence")) {
                        String[] tokens = element.getAttribute("sequence").split("\\s+");

                        mesh.setSequence(
                            Integer.parseInt(tokens[0]),
                            Integer.parseInt(tokens[1]),
                            Integer.parseInt(tokens[2]),
                            Boolean.parseBoolean(tokens[3])
                        );
                    }
                } catch(Exception ex) {
                    ex.printStackTrace(System.out);
                }
            }
            if(element.hasAttribute("texture")) {
                try {
                    node.texture = game.assets().load(IO.file(element.getAttribute("texture")));
                } catch(Exception ex) {
                    ex.printStackTrace(System.out);
                }
            }
            if(element.hasAttribute("texture2")) {
                try {
                    node.texture2 = game.assets().load(IO.file(element.getAttribute("texture2")));
                } catch(Exception ex) {
                    ex.printStackTrace(System.out);
                }
            }
            if(element.hasAttribute("vertices") && element.hasAttribute("indices") && element.hasAttribute("polygons")) {
                String[] svertices = element.getAttribute("vertices").split("\\s+");
                String[] sindices = element.getAttribute("indices").split("\\s");
                String[] spolygons = element.getAttribute("polygons").split("\\s+");
                SceneVertex[] vertices = new SceneVertex[svertices.length / 15];
                int[] indices = new int[sindices.length];
                int[][] polygons = new int[spolygons.length][];

                for(int i = 0, j = 0; i != vertices.length; i++) {
                    vertices[i] = new SceneVertex(
                        Float.parseFloat(svertices[j++]),
                        Float.parseFloat(svertices[j++]),
                        Float.parseFloat(svertices[j++]),
                        Float.parseFloat(svertices[j++]),
                        Float.parseFloat(svertices[j++]),
                        Float.parseFloat(svertices[j++]),
                        Float.parseFloat(svertices[j++]),
                        Float.parseFloat(svertices[j++]),
                        Float.parseFloat(svertices[j++]),
                        Float.parseFloat(svertices[j++]),
                        Float.parseFloat(svertices[j++]),
                        Float.parseFloat(svertices[j++]),
                        Float.parseFloat(svertices[j++]),
                        Float.parseFloat(svertices[j++]),
                        Float.parseFloat(svertices[j++])
                    );
                }
                for(int i = 0; i != sindices.length; i++) {
                    indices[i] = Integer.parseInt(sindices[i]);
                }
                for(int i = 0; i != spolygons.length; i++) {
                    String[] polygon = spolygons[i].split(":");

                    polygons[i] = new int[polygon.length];
                    for(int j = 0; j != polygon.length; j++) {
                        polygons[i][j] = Integer.parseInt(polygon[j]);
                    }
                }
                node.renderable = new Mesh(vertices, indices, polygons);
            }

            for(int i = 0; i != nodes.getLength(); i++) {
                org.w3c.dom.Node xmlNode = nodes.item(i);

                if(xmlNode instanceof Element) {
                    Element element2 = (Element)xmlNode;

                    if(element2.getTagName().equals("node")) {
                        node.add((Node)load(game, scene, element2));
                    } else {
                        try {
                            NodeComponent component = (NodeComponent)Class.forName(element2.getTagName()).getConstructors()[0].newInstance();

                            load(component, element2);
                            node.addComponent(game, scene, component);
                        } catch(Exception ex) {
                            ex.printStackTrace(System.out);
                        }
                    }
                }
            }
            r = node;
        }
        return r;
    }

    private static void load(Object o, Element element) throws Exception {
        Field[] fields = o.getClass().getFields();

        for(Field field : fields) {
            String name = field.getName();

            if(element.hasAttribute(name)) {
                try {
                    Utils.parse(o, element.getAttribute(name), name);
                } catch(Exception ex) {
                    ex.printStackTrace(System.out);
                }
            }
        }
    }
    
    public static void serialize(Scene scene, File file) throws Exception {
        StringBuilder b = new StringBuilder(10000);

        b.append("<scene");
        append(scene, false, b);
        b.append("\t<camera");
        append(scene.camera, true, b);
        for(Node node : scene.root) {
            append(node, "\t", b);
        }
        b.append("</scene>\n");

        IO.writeAllBytes(b.toString().getBytes(), file);
    }

    private static void append(Object o, boolean empty, StringBuilder b) throws Exception {
        Class<? extends Object> cls = o.getClass();
        Field[] fields = cls.getFields();

        for(Field field : fields) {
            String s = Utils.toString(o, field.getName());

            if(s != null) {
                b.append(" " + field.getName() + "=\"" + fix(s) + "\"");
            }
        }
        if(empty) {
            b.append("/>\n");
        } else {
            b.append(">\n");
        }
    }

    private static String fix(String value) {
        value = value.replace("&", "&amp;");
        value = value.replace("\"", "&quot;");
        value = value.replace("'", "&apos;");
        value = value.replace("<", "&lt;");
        value = value.replace(">", "&gt;");
        value = value.replace("\n", "&#10;");
        value = value.replace("\t", "&#09;");
        return value;
    }

    private static void append(Node node, String indent, StringBuilder b) throws Exception {
        boolean empty = node.count() == 0 && node.componentCount() == 0;
        Mesh mesh = node.getMesh();

        b.append(indent + "<node");
        if(mesh != null) {
            if(mesh.indexCount() != 0) {
                b.append(" vertices=\"");
                for(int i = 0; i != mesh.vertexCount(); i++) {
                    SceneVertex v = mesh.vertexAt(i);

                    if(i == 0) {
                        b.append(Utils.toString(v, "position"));
                    } else {
                        b.append(" " + Utils.toString(v, "position"));
                    }
                    b.append(" " + Utils.toString(v, "textureCoordinate"));
                    b.append(" " + Utils.toString(v, "textureCoordinate2"));
                    b.append(" " + Utils.toString(v, "color"));
                    b.append(" " + Utils.toString(v, "normal"));
                }
                b.append("\" indices=\"");
                for(int i = 0; i != mesh.indexCount(); i++) {
                    if(i == 0) {
                        b.append(mesh.indexAt(i));
                    } else {
                        b.append(" " + mesh.indexAt(i));
                    }
                }
                b.append("\" polygons=\"");
                for(int i = 0; i != mesh.polygonCount(); i++) {
                    String p = "";

                    for(int j = 0; j != mesh.polygonIndexCount(i); j++) {
                        p += ":" + mesh.polygonIndexAt(i, j);
                    }
                    p = p.substring(1);
                    if(i == 0) {
                        b.append(p);
                    } else {
                        b.append(" " + p);
                    }
                }
                b.append("\"");
            }
        } else if(node.renderable != null) {
            File file = node.renderable.file();

            if(file != null) {
                MD2Mesh md2Mesh = node.getAnimatedMesh();

                b.append(" renderable=\"" + file.getPath() + "\"");
                if(md2Mesh != null) {
                    b.append(
                        " sequence=\"" + 
                        md2Mesh.getStart() + " " + md2Mesh.getEnd() + " " + md2Mesh.getSpeed() + " " + md2Mesh.isLooping() + "\""
                        );
                }
            }
        }
        if(node.texture != null) {
            b.append(" texture=\"" + node.texture.file + "\"");
        }
        if(node.texture2 != null) {
            b.append(" texture2=\"" + node.texture2.file + "\"");
        }
        append(node, empty, b);
        if(!empty) {
            for(int i = 0; i != node.componentCount(); i++) {
                NodeComponent component = node.componentAt(i);

                b.append(indent + "\t<" + component.getClass().getName());
                append(component, true, b);
            }
            for(Node child : node) {
                append(child, indent + "\t", b);
            }
            b.append(indent + "</node>\n");
        }
    }
}
