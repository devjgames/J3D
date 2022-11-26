package org.j3d;

import java.io.File;
import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;

public class Sound extends Resource {
    
    private int source = 0;
    private int buffer = 0;

    public Sound(File file) throws Exception {
        BinReader reader = new BinReader(file);
        int b1 = reader.readByte();
        int b2 = reader.readByte();
        int b3 = reader.readByte();
        int b4 = reader.readByte();
        String header = new String(new byte[]{(byte) b1, (byte) b2, (byte) b3, (byte) b4});
        if (!header.equals("RIFF")) {
            throw new Exception("Invalid WAV file format");
        }
        reader.setPosition(reader.getPosition() + 18);

        int chan = reader.readShort();
        int sampleRate = reader.readInt();

        reader.setPosition(reader.getPosition() + 6);

        int bps = reader.readShort();

        reader.setPosition(reader.getPosition() + 4);

        int size = reader.readInt();
        byte[] data = new byte[size];

        reader.readBytes(data, 0, data.length);

        int format;

        if (chan == 1) {
            if (bps == 8) {
                format = AL10.AL_FORMAT_MONO8;
            } else {
                format = AL10.AL_FORMAT_MONO16;
            }
        } else if (bps == 8) {
            format = AL10.AL_FORMAT_STEREO8;
        } else {
            format = AL10.AL_FORMAT_STEREO16;
        }

        ByteBuffer buf = BufferUtils.createByteBuffer(data.length);
        buf.put(data);
        buf.flip();
        buffer = AL10.alGenBuffers();
        AL10.alBufferData(buffer, format, buf, sampleRate);
        source = AL10.alGenSources();
        AL10.alSourcei(source, AL10.AL_BUFFER, buffer);
    }

    public void setVolume(float value) {
        AL10.alSourcef(source, AL10.AL_GAIN, Math.max(0, Math.min(1, value)));
    }

    public int getAlSource() {
        return source;
    }

    public int getAlBuffer() {
        return buffer;
    }

    public void stop()  {
        AL10.alSourceStop(source);
    }

    public void play(boolean looping) {
        stop();
        if (looping) {
            AL10.alSourcei(source, AL10.AL_LOOPING, AL10.AL_TRUE);
        }
        AL10.alSourcePlay(source);
    }

    @Override
    public void destroy() throws Exception {
        stop();
        AL10.alDeleteSources(source);
        AL10.alDeleteBuffers(buffer);
        source = buffer = 0;
        super.destroy();
    }
}
