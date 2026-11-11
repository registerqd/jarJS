package jarjs.lwjgl.opengl;

/** org.lwjgl.opengl.EXTBlendFuncSeparate 兼容层。 */
public final class EXTBlendFuncSeparate {

    private EXTBlendFuncSeparate() {}

    public static final int GL_BLEND_DST_RGB_EXT = 0x80C8;
    public static final int GL_BLEND_SRC_RGB_EXT = 0x80C9;
    public static final int GL_BLEND_DST_ALPHA_EXT = 0x80CA;
    public static final int GL_BLEND_SRC_ALPHA_EXT = 0x80CB;

    public static void glBlendFuncSeparateEXT(int sfactorRGB, int dfactorRGB, int sfactorAlpha, int dfactorAlpha) {
        GLBackend.blendFuncSeparate(sfactorRGB, dfactorRGB, sfactorAlpha, dfactorAlpha);
    }
}
