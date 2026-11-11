package jarjs.lwjgl.opengl;

/**
 * org.lwjgl.opengl.GLContext 兼容层。
 * 提供全局 OpenGL 运行上下文及其硬件能力查询。
 */
public final class GLContext {

    private static final ContextCapabilities CAPABILITIES = new ContextCapabilities();

    private GLContext() {}

    /**
     * 获取当前活动上下文的硬件特性集。
     * 在 jarJS 环境下，将常用的核心 OpenGL 1.1~3.0 及常用扩展全部预置为 true。
     */
    public static ContextCapabilities getCapabilities() {
        return CAPABILITIES;
    }

    public static void useContext(Object context) {}

    public static void useContext(Object context, boolean forwardCompatible) {}

    public static Object getContext() {
        return "jarJS-GL-Context";
    }
}
