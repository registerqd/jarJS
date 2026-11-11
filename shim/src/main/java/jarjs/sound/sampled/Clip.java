package jarjs.sound.sampled;

import jarjs.media.MediaBackend;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * javax.sound.sampled.Clip 兼容层 —— 基于 Web Audio 驱动的声音剪辑。
 */
public class Clip implements DataLine {

    private AudioFormat format;
    private int bufferId = 0;
    private int sourceId = 0;
    private boolean open = false;

    public Clip() {}

    public void open(AudioInputStream stream) throws Exception {
        this.format = stream.getFormat();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = stream.read(buf)) > 0) bos.write(buf, 0, n);
        byte[] data = bos.toByteArray();

        int[] bIds = MediaBackend.alGenBuffers(1);
        this.bufferId = (bIds != null && bIds.length > 0) ? bIds[0] : 0;
        int alFmt = (format.getChannels() == 2) ? 0x1103 : 0x1101;
        MediaBackend.alBufferData(bufferId, base64(data), alFmt, (int) format.getSampleRate());

        int[] sIds = MediaBackend.alGenSources(1);
        this.sourceId = (sIds != null && sIds.length > 0) ? sIds[0] : 0;
        MediaBackend.alSourcei(sourceId, 0x1009, bufferId); // AL_BUFFER
        this.open = true;
    }

    public void setFramePosition(int frames) {}
    public void setMicrosecondPosition(long microseconds) {}
    public void loop(int count) {
        if (sourceId != 0) MediaBackend.alSourcei(sourceId, 0x1007, count != 0 ? 1 : 0);
        start();
    }

    @Override public void open() throws Exception { this.open = true; }
    @Override public void close() {
        stop();
        if (sourceId != 0) MediaBackend.alDeleteSources(new int[]{sourceId});
        if (bufferId != 0) MediaBackend.alDeleteBuffers(new int[]{bufferId});
        this.open = false;
    }
    @Override public boolean isOpen() { return open; }
    @Override public void start() {
        if (sourceId != 0) MediaBackend.alSourcePlay(sourceId);
    }
    @Override public void stop() {
        if (sourceId != 0) MediaBackend.alSourceStop(sourceId);
    }
    @Override public boolean isRunning() {
        return sourceId != 0 && MediaBackend.alIsSourcePlaying(sourceId) != 0;
    }
    @Override public boolean isActive() { return isRunning(); }
    @Override public int available() { return 0; }
    @Override public int getBufferSize() { return 65536; }
    @Override public AudioFormat getFormat() { return format; }
    @Override public int getFramePosition() { return 0; }
    @Override public float getLevel() { return 1.0f; }
    @Override public long getLongFramePosition() { return 0; }
    @Override public long getMicrosecondPosition() { return 0; }

    private static String base64(byte[] data) {
        final char[] T = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
        StringBuilder sb = new StringBuilder((data.length + 2) / 3 * 4);
        int i = 0;
        while (i + 2 < data.length) {
            int n = ((data[i] & 0xFF) << 16) | ((data[i + 1] & 0xFF) << 8) | (data[i + 2] & 0xFF);
            sb.append(T[(n >> 18) & 63]).append(T[(n >> 12) & 63])
              .append(T[(n >> 6) & 63]).append(T[n & 63]);
            i += 3;
        }
        int rem = data.length - i;
        if (rem == 1) {
            int n = (data[i] & 0xFF) << 16;
            sb.append(T[(n >> 18) & 63]).append(T[(n >> 12) & 63]).append("==");
        } else if (rem == 2) {
            int n = ((data[i] & 0xFF) << 16) | ((data[i + 1] & 0xFF) << 8);
            sb.append(T[(n >> 18) & 63]).append(T[(n >> 12) & 63]).append('=');
        }
        return sb.toString();
    }
}
