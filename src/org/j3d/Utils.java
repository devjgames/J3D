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
            }
        }
    }

    public static void append(Object o, String prefix, StringBuilder b) throws Exception {
        Field[] fields = o.getClass().getFields();

        b.append(prefix + " " + o.getClass().getName() + "\n");

        for(Field field : fields) {
            Class<?> type = field.getType();

            if(boolean.class.isAssignableFrom(type) || 
                int.class.isAssignableFrom(type) || 
                float.class.isAssignableFrom(type) ||
                Vec2.class.isAssignableFrom(type) || 
                Vec3.class.isAssignableFrom(type) || 
                Vec4.class.isAssignableFrom(type) ||
                type.isEnum()) {
                b.append("property "  + field.getName() + " " + field.get(o) + "\n");
            } else if(String.class.isAssignableFrom(type)) {
                b.append("property " + field.getName() + " " + ((String)field.get(o)).replace("\n", "@") + "\n");
            } 
        }
    }

    public static void parse(Object o, String[] tokens, String line) throws Exception {
        Field field = o.getClass().getField(tokens[1]);
        Class<?> type = field.getType(); 

        if(boolean.class.isAssignableFrom(type)) {
            field.set(o, Boolean.parseBoolean(tokens[2]));
        } else if(int.class.isAssignableFrom(type)) {
            field.set(o, Integer.parseInt(tokens[2]));
        } else if(float.class.isAssignableFrom(type)) {
            field.set(o, Float.parseFloat(tokens[2]));
        } else if(String.class.isAssignableFrom(type)) {
            field.set(o, line.substring(line.indexOf(tokens[1]) + tokens[1].length()).trim().replace("@", "\n"));
        } else if(Vec2.class.isAssignableFrom(type)) {
            ((Vec2)field.get(o)).parse(tokens, 2);
        } else if(Vec3.class.isAssignableFrom(type)) {
            ((Vec3)field.get(o)).parse(tokens, 2);
        } else if(Vec4.class.isAssignableFrom(type)) {
            ((Vec4)field.get(o)).parse(tokens, 2);
        } else if(type.isEnum()) {
            Object[] constants = type.getEnumConstants();
            for(int i = 0; i != constants.length; i++) {
                String name = constants[i].toString();
                if(name.equals(tokens[2])) {
                    field.set(o, constants[i]);
                    break;
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
