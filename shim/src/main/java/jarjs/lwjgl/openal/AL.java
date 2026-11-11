package jarjs.lwjgl.openal;

/**
 * org.lwjgl.openal.AL 兼容层 —— 音频设备与上下文生命周期。
 * 浏览器里没有真正的设备句柄，全部用占位句柄表示，实际初始化在首次播放时由 Web Audio 完成。
 */
public final class AL {

    private static boolean created = false;
    private static ALCcontext context;
    private static ALCdevice device;

    private AL() {}

    public static void create() throws jarjs.lwjgl.LWJGLException {
        create(null);
    }

    public static void create(String deviceArguments) throws jarjs.lwjgl.LWJGLException {
        if (created) return;
        device = ALC10.alcOpenDevice(null);
        context = ALC10.alcCreateContext(device, null);
        ALC10.alcMakeContextCurrent(context);
        created = true;
    }

    public static void create(String deviceArguments, int contextFrequency, int contextRefresh, boolean contextSynchronized) {
        try { create(deviceArguments); } catch (Exception e) { throw new RuntimeException(e); }
    }

    public static boolean isCreated() { return created; }

    public static void destroy() {
        if (!created) return;
        ALC10.alcMakeContextCurrent(null);
        ALC10.alcDestroyContext(context);
        ALC10.alcCloseDevice(device);
        context = null;
        device = null;
        created = false;
    }

    public static ALCcontext getContext() { return context; }
    public static ALCdevice getDevice() { return device; }

    public static void exit() { destroy(); }
}
