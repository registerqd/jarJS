package jarjs.sound.sampled;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

/**
 * javax.sound.sampled.AudioSystem 兼容层。
 */
public final class AudioSystem {

    private AudioSystem() {}

    public static AudioInputStream getAudioInputStream(InputStream stream) throws Exception {
        if (stream == null) return null;
        AudioFormat format = new AudioFormat(44100, 16, 2, true, false);
        return new AudioInputStream(stream, format, -1);
    }

    public static AudioInputStream getAudioInputStream(File file) throws Exception {
        return getAudioInputStream(new FileInputStream(file));
    }

    public static AudioInputStream getAudioInputStream(URL url) throws Exception {
        return getAudioInputStream(url.openStream());
    }

    public static Clip getClip() {
        return new Clip();
    }

    public static Line getLine(Line.Info info) {
        return new Clip();
    }

    public static boolean isLineSupported(Line.Info info) {
        return true;
    }
}
