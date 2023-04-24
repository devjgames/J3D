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

    public static int readByte(byte[] bytes, int[] i) {
        return bytes[i[0]++] & 0xFF;
    }

    public static int readShort(byte[] bytes, int[] i) {
        int b1 = bytes[i[0]++];
        int b2 = bytes[i[0]++];

        return ((b2 << 8) & 0xFF00) | (b1 & 0xFF);
    }

    public static int readInt(byte[] bytes, int[] i) {
        int b1 = bytes[i[0]++];
        int b2 = bytes[i[0]++];
        int b3 = bytes[i[0]++];
        int b4 = bytes[i[0]++];

        return ((b4 << 24) & 0xFF000000) | ((b3 << 16) & 0xFF0000) | ((b2 << 8) & 0xFF00) | (b1 & 0xFF);
    }

    public static float readFloat(byte[] bytes, int[] i) {
        return Float.intBitsToFloat(readInt(bytes, i));
    }

    public static String readString(byte[] bytes, int[] i, int length) {
        int j = i[0];
        int s = j;

        i[0] += length;
        for(; j != length; j++) {
            if(bytes[j] == 0) {
                break;
            }  
        }
        length = j - s;

        return new String(bytes, s, length);
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

    public static void appendAllBytes(byte[] bytes, File file) throws Exception {
        if(file.exists()) {
            byte[] ob = readAllBytes(file);
            byte[] nb = new byte[ob.length + bytes.length];

            for(int i = 0; i != ob.length; i++) {
                nb[i] = ob[i];
            }
            for(int i = ob.length; i != nb.length; i++) {
                nb[i] = bytes[i - ob.length];
            }
            bytes = nb;
        } 
        writeAllBytes(bytes, file);
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
