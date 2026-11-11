package jarjs.lwjgl.opengl;

/**
 * org.lwjgl.opengl.GL14 兼容层 —— 混合分离与高级混合方程。
 */
public final class GL14 {

    private GL14() {}

    public static final int GL_FUNC_ADD = 0x8006;
    public static final int GL_MIN = 0x8007;
    public static final int GL_MAX = 0x8008;
    public static final int GL_FUNC_SUBTRACT = 0x800A;
    public static final int GL_FUNC_REVERSE_SUBTRACT = 0x800B;

    public static final int GL_BLEND_COLOR = 0x8005;
    public static final int GL_BLEND_EQUATION = 0x8009;
    public static final int GL_BLEND_EQUATION_RGB = 0x8009;
    public static final int GL_BLEND_EQUATION_ALPHA = 0x883D;

    public static final int GL_BLEND_DST_RGB = 0x80C8;
    public static final int GL_BLEND_SRC_RGB = 0x80C9;
    public static final int GL_BLEND_DST_ALPHA = 0x80CA;
    public static final int GL_BLEND_SRC_ALPHA = 0x80CB;

    public static final int GL_DEPTH_COMPONENT16 = 0x81A5;
    public static final int GL_DEPTH_COMPONENT24 = 0x81A6;
    public static final int GL_DEPTH_COMPONENT32 = 0x81A7;

    /** 分离设置 RGB 与 Alpha 的混合因子。 */
    public static void glBlendFuncSeparate(int sfactorRGB, int dfactorRGB, int sfactorAlpha, int dfactorAlpha) {
        GLBackend.blendFuncSeparate(sfactorRGB, dfactorRGB, sfactorAlpha, dfactorAlpha);
    }

    /** 设置混合方程（ADD, SUBTRACT 等）。 */
    public static void glBlendEquation(int mode) {
        GLBackend.blendEquation(mode);
    }

    public static void glBlendColor(float r, float g, float b, float a) {
        // WebGL 1.0 标准混合颜色
    }
}
