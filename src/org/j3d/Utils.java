package org.j3d;

import javax.swing.UIManager.*;
import javax.swing.*;
import java.lang.reflect.*;
import java.io.*;
import java.util.Vector;
import java.awt.*;

public class Utils {
    
    public static void copy(Object src, Object dst) throws Exception {
        Field[] fields = src.getClass().getFields();

        for(Field field : fields) {
            int m = field.getModifiers();

            if(Modifier.isPublic(m) && !Modifier.isStatic(m)) {
                Class<?> type = field.getType();

                if(
                    boolean.class.isAssignableFrom(type) || 
                    int.class.isAssignableFrom(type) || 
                    float.class.isAssignableFrom(type) ||
                    String.class.isAssignableFrom(type) ||
                    type.isEnum()) {
                    field.set(dst, field.get(src));
                } else if(Vec2.class.isAssignableFrom(type)) {
                    ((Vec2)field.get(dst)).set((Vec2)field.get(src));
                } else if(Vec3.class.isAssignableFrom(type)) {
                    ((Vec3)field.get(dst)).set((Vec3)field.get(src));
                } else if(Vec4.class.isAssignableFrom(type)) {
                    ((Vec4)field.get(dst)).set((Vec4)field.get(src));
                } else if(Mat4.class.isAssignableFrom(type)) {
                    ((Mat4)field.get(dst)).set((Mat4)field.get(src));
                }
            }
        }
    }

    public static String toString(Object o, String name) throws Exception {
        Field field = o.getClass().getField(name);
        int m = field.getModifiers();

        if(Modifier.isPublic(m) && !Modifier.isStatic(m)) {
            Class<?> type = field.getType();

            if(boolean.class.isAssignableFrom(type) || 
                int.class.isAssignableFrom(type) || 
                float.class.isAssignableFrom(type) ||
                String.class.isAssignableFrom(type) ||
                Vec2.class.isAssignableFrom(type) || 
                Vec3.class.isAssignableFrom(type) || 
                Vec4.class.isAssignableFrom(type) ||
                Mat4.class.isAssignableFrom(type) ||
                type.isEnum()) {
                return field.get(o).toString();
            }
        }
        return null;
    }

    public static void parse(Object o, String text, String name) throws Exception {
        Field field = o.getClass().getField(name);
        int m = field.getModifiers();

        if(Modifier.isPublic(m) && !Modifier.isStatic(m)) {
            Class<?> type = field.getType(); 

            if(boolean.class.isAssignableFrom(type)) {
                field.set(o, Boolean.parseBoolean(text));
            } else if(int.class.isAssignableFrom(type)) {
                field.set(o, Integer.parseInt(text));
            } else if(float.class.isAssignableFrom(type)) {
                field.set(o, Float.parseFloat(text));
            } else if(String.class.isAssignableFrom(type)) {
                field.set(o, text);
            } else if(Vec2.class.isAssignableFrom(type)) {
                ((Vec2)field.get(o)).parse(text);
            } else if(Vec3.class.isAssignableFrom(type)) {
                ((Vec3)field.get(o)).parse(text);
            } else if(Vec4.class.isAssignableFrom(type)) {
                ((Vec4)field.get(o)).parse(text);
            } else if(Mat4.class.isAssignableFrom(type)) {
                ((Mat4)field.get(o)).parse(text);
            } else if(type.isEnum()) {
                Object[] constants = type.getEnumConstants();
                for(int i = 0; i != constants.length; i++) {
                    String ename = constants[i].toString();
                    if(ename.equals(text)) {
                        field.set(o, constants[i]);
                        break;
                    }
                }
            }
        }
    }

    public static void setNimbusLookAndFeel() {
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
        }
    }

    public static File selectFile(Window parent, File directory, String extension) {
        Vector<Object> paths = new Vector<>();

        appendFiles(directory, extension, paths);

        if(paths.size() == 0) {
            return null;
        }

        Object r = JOptionPane.showInputDialog(parent, "Select", "Select", JOptionPane.INFORMATION_MESSAGE, null, paths.toArray(), paths.get(0));

        if(r != null) {
            return IO.file((String)r);
        }
        return null;
    }

    private static void appendFiles(File directory, String extension, Vector<Object> paths) {
        File[] files = directory.listFiles();

        if(files != null) {
            for(File file : files) {
                if(file.isFile() && IO.extension(file).equals(extension)) {
                    paths.add(file.getPath());
                }
            }
            for(File file : files) {
                if(file.isDirectory()) {
                    appendFiles(file, extension, paths);
                }
            }
        }
    }
}
