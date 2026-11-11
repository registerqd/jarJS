package jarjs.sound.sampled;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * javax.sound.sampled.AudioInputStream 兼容层。
 */
public class AudioInputStream extends InputStream {

    private final InputStream stream;
    protected AudioFormat format;
    protected long frameLength;

    public AudioInputStream(InputStream stream, AudioFormat format, long length) {
        this.stream = stream;
        this.format = format;
        this.frameLength = length;
    }

    public AudioFormat getFormat() { return format; }
    public long getFrameLength() { return frameLength; }

    @Override
    public int read() throws IOException {
        return stream.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return stream.read(b, off, len);
    }

    @Override
    public int available() throws IOException {
        return stream.available();
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }
}
