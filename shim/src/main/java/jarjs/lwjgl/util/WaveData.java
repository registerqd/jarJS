package jarjs.lwjgl.util;

import jarjs.lwjgl.openal.AL10;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * org.lwjgl.openal.WaveData 兼容层 —— WAV 文件解析（纯 Java 实现，不依赖 native）。
 */
public class WaveData {

    public final ByteBuffer data;
    public final int format;
    public final int samplerate;

    private WaveData(ByteBuffer data, int format, int samplerate) {
        this.data = data;
        this.format = format;
        this.samplerate = samplerate;
    }

    public void dispose() {}

    public static WaveData create(URL path) {
        try (InputStream in = path.openStream()) {
            return create(in);
        } catch (Exception e) {
            return null;
        }
    }

    public static WaveData create(String file) {
        try (InputStream in = new java.io.FileInputStream(file)) {
            return create(in);
        } catch (Exception e) {
            return null;
        }
    }

    public static WaveData create(InputStream is) {
        try {
            byte[] all = readAll(is);
            return parse(all);
        } catch (Exception e) {
            return null;
        }
    }

    private static byte[] readAll(InputStream is) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = is.read(buf)) > 0) bos.write(buf, 0, n);
        return bos.toByteArray();
    }

    /** 解析标准 RIFF/WAVE（PCM）。 */
    public static WaveData parse(byte[] b) {
        if (b.length < 44) return null;
        if (b[0] != 'R' || b[1] != 'I' || b[2] != 'F' || b[3] != 'F') return null;
        if (b[8] != 'W' || b[9] != 'A' || b[10] != 'V' || b[11] != 'E') return null;

        int channels = 0, sampleRate = 0, bits = 0;
        int dataOff = -1, dataLen = 0;

        int p = 12;
        while (p + 8 <= b.length) {
            String id = "" + (char) b[p] + (char) b[p + 1] + (char) b[p + 2] + (char) b[p + 3];
            int size = (b[p + 4] & 0xFF) | ((b[p + 5] & 0xFF) << 8)
                     | ((b[p + 6] & 0xFF) << 16) | ((b[p + 7] & 0xFF) << 24);
            if ("fmt ".equals(id)) {
                channels = (b[p + 10] & 0xFF) | ((b[p + 11] & 0xFF) << 8);
                sampleRate = (b[p + 12] & 0xFF) | ((b[p + 13] & 0xFF) << 8)
                           | ((b[p + 14] & 0xFF) << 16) | ((b[p + 15] & 0xFF) << 24);
                bits = (b[p + 22] & 0xFF) | ((b[p + 23] & 0xFF) << 8);
            } else if ("data".equals(id)) {
                dataOff = p + 8;
                dataLen = Math.min(size, b.length - dataOff);
                break;
            }
            p += 8 + size + (size & 1);
        }

        if (dataOff < 0 || channels == 0) return null;

        int format;
        if (channels == 1) format = (bits == 8) ? AL10.AL_FORMAT_MONO8 : AL10.AL_FORMAT_MONO16;
        else format = (bits == 8) ? AL10.AL_FORMAT_STEREO8 : AL10.AL_FORMAT_STEREO16;

        ByteBuffer bb = ByteBuffer.allocateDirect(dataLen).order(ByteOrder.LITTLE_ENDIAN);
        bb.put(b, dataOff, dataLen);
        bb.flip();
        return new WaveData(bb, format, sampleRate);
    }
}
