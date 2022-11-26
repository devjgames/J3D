package org.j3d;

import java.io.*;

public class IO {

    public static byte[] readAllBytes(InputStream input) throws IOException {
        ByteArrayOutputStream output = null;
        byte[] bytes = new byte[1000];
        int n;
        try {
            output = new ByteArrayOutputStream(1000);
            while ((n = input.read(bytes)) >= 0) {
                if (n != 0) {
                    output.write(bytes, 0, n);
                }
            }
        } finally {
            if (output != null) {
                output.close();
            }
        }
        return output.toByteArray();
    }

    public static byte[] readAllBytes(File file) throws IOException {
        FileInputStream input = null;
        byte[] bytes = null;
        try {
            bytes = readAllBytes(input = new FileInputStream(file));
        } finally {
            if (input != null) {
                input.close();
            }
        }
        return bytes;
    }

    public static byte[] readAllBytes(Class<?> type, String name) throws IOException {
        InputStream input = null;
        byte[] bytes = null;
        try {
            bytes = readAllBytes(input = type.getResourceAsStream(name));
        } finally {
            if (input != null) {
                input.close();
            }
        }
        return bytes;
    }

    public static void writeAllBytes(byte[] bytes, File file) throws IOException {
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(file);
            output.write(bytes);
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    public static File file(String path) {
        return new File(path.replace('\\', File.separatorChar).replace('/', File.separatorChar));
    }

    public static File file(File parent, String path) {
        return new File(parent, path.replace('\\', File.separatorChar).replace('/', File.separatorChar));
    }

    public static String extension(File file) {
        String name = file.getName();
        int i = name.lastIndexOf('.');
        if (i >= 0) {
            return name.substring(i);
        }
        return "";
    }

    public static String fileNameWithOutExtension(File file) {
        String name = file.getName();
        int i = name.lastIndexOf('.');
        if (i >= 0) {
            name = name.substring(0, i);
        }
        return name;
    }
}
