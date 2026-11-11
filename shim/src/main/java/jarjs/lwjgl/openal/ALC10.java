package jarjs.lwjgl.openal;

/** org.lwjgl.openal.ALC10 兼容层 —— 音频上下文管理。浏览器端全部为占位句柄。 */
public final class ALC10 {

    private ALC10() {}

    public static final int ALC_FALSE = 0;
    public static final int ALC_TRUE = 1;
    public static final int ALC_FREQUENCY = 0x1007;
    public static final int ALC_REFRESH = 0x1008;
    public static final int ALC_SYNC = 0x1009;
    public static final int ALC_DEFAULT_DEVICE_SPECIFIER = 0x1004;

    private static long next = 1;

    public static ALCdevice alcOpenDevice(String deviceName) {
        return new ALCdevice(next++);
    }

    public static boolean alcCloseDevice(ALCdevice device) { return true; }

    public static ALCcontext alcCreateContext(ALCdevice device, java.nio.IntBuffer attrList) {
        return new ALCcontext(next++);
    }

    public static boolean alcMakeContextCurrent(ALCcontext context) { return true; }

    public static void alcProcessContext(ALCcontext context) {}

    public static void alcSuspendContext(ALCcontext context) {}

    public static void alcDestroyContext(ALCcontext context) {}

    public static ALCcontext alcGetCurrentContext() { return null; }

    public static ALCdevice alcGetContextsDevice(ALCcontext context) { return null; }

    public static int alcGetError(ALCdevice device) { return 0; }

    public static String alcGetString(ALCdevice device, int param) {
        if (param == ALC_DEFAULT_DEVICE_SPECIFIER) return "JARJS WebAudio";
        return "";
    }

    public static void alcGetInteger(ALCdevice device, int param, java.nio.IntBuffer values) {}
}
