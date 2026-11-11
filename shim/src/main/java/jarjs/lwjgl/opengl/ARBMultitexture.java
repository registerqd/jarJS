package jarjs.lwjgl.opengl;

/** org.lwjgl.opengl.ARBMultitexture 兼容层。 */
public final class ARBMultitexture {

    private ARBMultitexture() {}

    public static final int GL_TEXTURE0_ARB = 0x84C0;
    public static final int GL_TEXTURE1_ARB = 0x84C1;
    public static final int GL_TEXTURE2_ARB = 0x84C2;
    public static final int GL_TEXTURE3_ARB = 0x84C3;
    public static final int GL_ACTIVE_TEXTURE_ARB = 0x84E0;

    public static void glActiveTextureARB(int texture) { GLBackend.activeTexture(texture); }
    public static void glClientActiveTextureARB(int texture) {}
    public static void glMultiTexCoord2fARB(int target, float s, float t) { GL.texCoord(s, t); }
    public static void glMultiTexCoord2dARB(int target, double s, double t) { GL.texCoord((float) s, (float) t); }
}
