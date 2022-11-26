package org.j3d;

import java.io.File;
import java.io.IOException;

public class BinReader {
    private byte[] bytes;
    private int position = 0;

    public BinReader(byte[] bytes) {
        this.bytes = bytes;
    }

    public BinReader(File file) throws IOException {
        this(IO.readAllBytes(file));
    }

    public int getLength() {
        return bytes.length;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void readBytes(byte[] bytes, int offset, int length) {
        for (int i = 0; i != length; i++, position++) {
            bytes[offset + i] = this.bytes[position];
        }
    }

    public int readByte() {
        return ((int) bytes[position++]) & 0xFF;
    }

    public int readShort() {
        int b1 = ((int) bytes[position++]) & 0xFF;
        int b2 = ((int) bytes[position++]) & 0xFF;
        return ((b2 << 8) & 0xFF00) | (b1 & 0xFF);
    }

    public int readInt() {
        int b1 = ((int) bytes[position++]) & 0xFF;
        int b2 = ((int) bytes[position++]) & 0xFF;
        int b3 = ((int) bytes[position++]) & 0xFF;
        int b4 = ((int) bytes[position++]) & 0xFF;
        return ((b4 << 24) & 0xFF000000) | ((b3 << 16) & 0xFF0000) | ((b2 << 8) & 0xFF00) | (b1 & 0xFF);
    }

    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    public String readString(int length) {
        int n = 0;
        for (int i = 0; i != length; i++, n++) {
            if (bytes[position + i] == 0) {
                break;
            }
        }
        byte[] b = new byte[n];
        for (int i = 0; i != n; i++) {
            b[i] = bytes[position + i];
        }
        position += length;
        return new String(b);
    }
}
