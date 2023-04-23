package org.j3d;


import java.io.*;

public class IO {

    public static byte[] readAllBytes(InputStream input) throws IOException {
        ByteArrayOutputStream output = null;
        byte[] bytes = new byte[1024];
        byte[] rBytes;
        int n;

        try {
            output = new ByteArrayOutputStream(1024);
            while ((n = input.read(bytes)) != -1) {
                if (n != 0) {
                    output.write(bytes, 0, n);
                }
            }
            rBytes = output.toByteArray();
        } finally {
            if (output != null) {
                output.close();
            }
        }
        return rBytes;
    }

    public static byte[] readAllBytes(File file) throws IOException {
        FileInputStream input = null;
        byte[] bytes;

        try {
            bytes = readAllBytes(input = new FileInputStream(file));
        } finally {
            if (input != null) {
                input.close();
            }
        }
        return bytes;
    }

    public static byte[] readAllBytes(Class<?> cls, String name) throws IOException {
        InputStream input = null;
        byte[] bytes;

        try {
            bytes = readAllBytes(input = cls.getResourceAsStream(name));
        } finally {
            if (input != null) {
                input.close();
            }
        }
        return bytes;
    }

    public static void writeAllBytes(byte[] bytes, File file) throws IOException {
        writeAllBytes(bytes, 0, bytes.length, file);
    }

    public static void writeAllBytes(byte[] bytes, int offset, int length, File file) throws IOException {
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(file);
            output.write(bytes, offset, length);
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    public static File file(String path) {
        return new File(path.replace('\\', File.separatorChar).replace('/', File.separatorChar));
    }

    public static String extension(File file) {
        String name = file.getName();
        String extension = "";
        int i = name.lastIndexOf('.');

        if (i != -1) {
            extension = name.substring(i);
        }
        return extension;
    }

    public static String fileNameWithOutExtension(File file) {
        String name = file.getName();
        int i = name.lastIndexOf('.');

        if (i != -1) {
            name = name.substring(0, i);
        }
        return name;
    }
}
