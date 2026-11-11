package jarjs.lwjgl.openal;

/**
 * org.lwjgl.openal.AL10 兼容层 —— OpenAL 1.0 核心音频 API。
 * 底层由 Web Audio API 驱动，PCM 数据直接构建 AudioBuffer 播放。
 */
public final class AL10 {

    private AL10() {}

    // 错误码
    public static final int AL_NO_ERROR = 0x0000;
    public static final int AL_INVALID_NAME = 0xA001;
    public static final int AL_INVALID_ENUM = 0xA002;
    public static final int AL_INVALID_VALUE = 0xA003;
    public static final int AL_INVALID_OPERATION = 0xA004;
    public static final int AL_OUT_OF_MEMORY = 0xA005;

    // 音频格式
    public static final int AL_FORMAT_MONO8 = 0x1100;
    public static final int AL_FORMAT_MONO16 = 0x1101;
    public static final int AL_FORMAT_STEREO8 = 0x1102;
    public static final int AL_FORMAT_STEREO16 = 0x1103;

    // 源参数
    public static final int AL_BUFFER = 0x1009;
    public static final int AL_GAIN = 0x100A;
    public static final int AL_PITCH = 0x1003;
    public static final int AL_POSITION = 0x1004;
    public static final int AL_VELOCITY = 0x1006;
    public static final int AL_LOOPING = 0x1007;
    public static final int AL_SOURCE_STATE = 0x1010;
    public static final int AL_INITIAL = 0x1011;
    public static final int AL_PLAYING = 0x1012;
    public static final int AL_PAUSED = 0x1013;
    public static final int AL_STOPPED = 0x1014;
    public static final int AL_MIN_GAIN = 0x100D;
    public static final int AL_MAX_GAIN = 0x100E;
    public static final int AL_REFERENCE_DISTANCE = 0x1020;
    public static final int AL_ROLLOFF_FACTOR = 0x1021;
    public static final int AL_MAX_DISTANCE = 0x1023;
    public static final int AL_CONE_INNER_ANGLE = 0x1001;
    public static final int AL_CONE_OUTER_ANGLE = 0x1002;
    public static final int AL_DIRECTION = 0x1005;

    // 监听器参数
    public static final int AL_ORIENTATION = 0x100F;
    public static final int AL_DISTANCE_MODEL = 0x0D00;
    public static final int AL_INVERSE_DISTANCE = 0xD002;

    public static int alGetError() {
        return jarjs.media.MediaBackend.alGetError();
    }

    // ---------- Buffer ----------
    public static int alGenBuffers() {
        int[] ids = jarjs.media.MediaBackend.alGenBuffers(1);
        return (ids != null && ids.length > 0) ? ids[0] : 0;
    }

    public static void alGenBuffers(java.nio.IntBuffer buffers) {
        int n = buffers.remaining();
        int[] ids = jarjs.media.MediaBackend.alGenBuffers(n);
        int pos = buffers.position();
        for (int i = 0; i < n && ids != null && i < ids.length; i++) {
            buffers.put(pos + i, ids[i]);
        }
    }

    public static void alBufferData(int buffer, int format, java.nio.ByteBuffer data, int freq) {
        if (data == null) return;
        int n = data.remaining();
        byte[] bytes = new byte[n];
        int pos = data.position();
        for (int i = 0; i < n; i++) bytes[i] = data.get(pos + i);
        jarjs.media.MediaBackend.alBufferData(buffer, base64(bytes), format, freq);
    }

    public static void alBufferData(int buffer, int format, byte[] data, int freq) {
        if (data == null) return;
        jarjs.media.MediaBackend.alBufferData(buffer, base64(data), format, freq);
    }

    public static void alDeleteBuffers(int buffer) {
        jarjs.media.MediaBackend.alDeleteBuffers(new int[]{buffer});
    }

    public static void alDeleteBuffers(java.nio.IntBuffer buffers) {
        jarjs.media.MediaBackend.alDeleteBuffers(toArray(buffers));
    }

    // ---------- Source ----------
    public static int alGenSources() {
        int[] ids = jarjs.media.MediaBackend.alGenSources(1);
        return (ids != null && ids.length > 0) ? ids[0] : 0;
    }

    public static void alGenSources(java.nio.IntBuffer sources) {
        int n = sources.remaining();
        int[] ids = jarjs.media.MediaBackend.alGenSources(n);
        int pos = sources.position();
        for (int i = 0; i < n && ids != null && i < ids.length; i++) {
            sources.put(pos + i, ids[i]);
        }
    }

    public static void alSourcei(int source, int param, int value) {
        jarjs.media.MediaBackend.alSourcei(source, param, value);
    }

    public static void alSourcef(int source, int param, float value) {
        jarjs.media.MediaBackend.alSourcef(source, param, value);
    }

    public static void alSource3f(int source, int param, float v1, float v2, float v3) {
        jarjs.media.MediaBackend.alSource3f(source, param, v1, v2, v3);
    }

    public static void alSourcePlay(int source) {
        jarjs.media.MediaBackend.alSourcePlay(source);
    }

    public static void alSourcePlay(java.nio.IntBuffer sources) {
        jarjs.media.MediaBackend.alSourcePlayv(toArray(sources));
    }

    public static void alSourceStop(int source) {
        jarjs.media.MediaBackend.alSourceStop(source);
    }

    public static void alSourceStop(java.nio.IntBuffer sources) {
        int[] a = toArray(sources);
        for (int id : a) jarjs.media.MediaBackend.alSourceStop(id);
    }

    public static void alSourcePause(int source) {
        jarjs.media.MediaBackend.alSourcePause(source);
    }

    public static void alSourceRewind(int source) {
        jarjs.media.MediaBackend.alSourceRewind(source);
    }

    public static void alDeleteSources(int source) {
        jarjs.media.MediaBackend.alDeleteSources(new int[]{source});
    }

    public static void alDeleteSources(java.nio.IntBuffer sources) {
        jarjs.media.MediaBackend.alDeleteSources(toArray(sources));
    }

    public static int alGetSourcei(int source, int param) {
        if (param == AL_SOURCE_STATE) {
            return jarjs.media.MediaBackend.alIsSourcePlaying(source) != 0 ? AL_PLAYING : AL_STOPPED;
        }
        return 0;
    }

    // ---------- Listener ----------
    public static void alListenerf(int param, float value) {
        jarjs.media.MediaBackend.alListenerf(param, value);
    }

    public static void alListener3f(int param, float v1, float v2, float v3) {
        jarjs.media.MediaBackend.alListener3f(param, v1, v2, v3);
    }

    public static void alListener(int param, java.nio.FloatBuffer values) {
        if (values == null) return;
        int p = values.position();
        if (values.remaining() >= 3) {
            jarjs.media.MediaBackend.alListener3f(param, values.get(p), values.get(p + 1), values.get(p + 2));
        } else if (values.remaining() >= 1) {
            jarjs.media.MediaBackend.alListenerf(param, values.get(p));
        }
    }

    public static void alDistanceModel(int model) {}
    public static void alDopplerFactor(float value) {}
    public static void alDopplerVelocity(float value) {}

    // ---------- 工具 ----------
    private static int[] toArray(java.nio.IntBuffer b) {
        if (b == null) return new int[0];
        int n = b.remaining();
        int[] a = new int[n];
        int p = b.position();
        for (int i = 0; i < n; i++) a[i] = b.get(p + i);
        return a;
    }

    /** 标准 Base64 编码（Java 8 兼容实现，不依赖 java.util.Base64 的 TeaVM 支持）。 */
    static String base64(byte[] data) {
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
            sb.append(T[(n >> 18) & 63]).append(T[(n >> 12) & 63])
              .append(T[(n >> 6) & 63]).append('=');
        }
        return sb.toString();
    }
}
