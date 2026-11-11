package jarjs.lwjgl.opengl;

/**
 * org.lwjgl.opengl.GL13 兼容层 —— 多重纹理单元（Multitexturing）。
 * 将桌面 GL13 纹理单元操作映射到 WebGL activeTexture。
 */
public final class GL13 {

    private GL13() {}

    // 纹理单元常量 (GL_TEXTURE0 ~ GL_TEXTURE31)
    public static final int GL_TEXTURE0 = 0x84C0;
    public static final int GL_TEXTURE1 = 0x84C1;
    public static final int GL_TEXTURE2 = 0x84C2;
    public static final int GL_TEXTURE3 = 0x84C3;
    public static final int GL_TEXTURE4 = 0x84C4;
    public static final int GL_TEXTURE5 = 0x84C5;
    public static final int GL_TEXTURE6 = 0x84C6;
    public static final int GL_TEXTURE7 = 0x84C7;
    public static final int GL_TEXTURE8 = 0x84C8;
    public static final int GL_TEXTURE9 = 0x84C9;
    public static final int GL_TEXTURE10 = 0x84CA;
    public static final int GL_TEXTURE11 = 0x84CB;
    public static final int GL_TEXTURE12 = 0x84CC;
    public static final int GL_TEXTURE13 = 0x84CD;
    public static final int GL_TEXTURE14 = 0x84CE;
    public static final int GL_TEXTURE15 = 0x84CF;
    public static final int GL_TEXTURE16 = 0x84D0;
    public static final int GL_TEXTURE17 = 0x84D1;
    public static final int GL_TEXTURE18 = 0x84D2;
    public static final int GL_TEXTURE19 = 0x84D3;
    public static final int GL_TEXTURE20 = 0x84D4;
    public static final int GL_TEXTURE21 = 0x84D5;
    public static final int GL_TEXTURE22 = 0x84D6;
    public static final int GL_TEXTURE23 = 0x84D7;
    public static final int GL_TEXTURE24 = 0x84D8;
    public static final int GL_TEXTURE25 = 0x84D9;
    public static final int GL_TEXTURE26 = 0x84DA;
    public static final int GL_TEXTURE27 = 0x84DB;
    public static final int GL_TEXTURE28 = 0x84DC;
    public static final int GL_TEXTURE29 = 0x84DD;
    public static final int GL_TEXTURE30 = 0x84DE;
    public static final int GL_TEXTURE31 = 0x84DF;

    public static final int GL_ACTIVE_TEXTURE = 0x84E0;
    public static final int GL_CLIENT_ACTIVE_TEXTURE = 0x84E1;
    public static final int GL_MAX_TEXTURE_UNITS = 0x84E2;

    /** 激活指定的纹理单元。 */
    public static void glActiveTexture(int texture) {
        GLBackend.activeTexture(texture);
    }

    public static void glClientActiveTexture(int texture) {
        // WebGL 不需要切换 clientActiveTexture
    }

    /** 多重纹理坐标赋值（立即模式扩展）。 */
    public static void glMultiTexCoord2f(int target, float s, float t) {
        GL.texCoord(s, t);
    }

    public static void glMultiTexCoord2d(int target, double s, double t) {
        GL.texCoord((float) s, (float) t);
    }
}
