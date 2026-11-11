package jarjs.lwjgl.opengl;

/** org.lwjgl.opengl.EXTFramebufferObject 兼容层。 */
public final class EXTFramebufferObject {

    private EXTFramebufferObject() {}

    public static final int GL_FRAMEBUFFER_EXT = 0x8D40;
    public static final int GL_RENDERBUFFER_EXT = 0x8D41;
    public static final int GL_COLOR_ATTACHMENT0_EXT = 0x8CE0;
    public static final int GL_DEPTH_ATTACHMENT_EXT = 0x8D00;
    public static final int GL_FRAMEBUFFER_COMPLETE_EXT = 0x8CD5;

    public static int glGenFramebuffersEXT() { return GL30.glGenFramebuffers(); }
    public static void glBindFramebufferEXT(int target, int framebuffer) { GL30.glBindFramebuffer(target, framebuffer); }
    public static void glFramebufferTexture2DEXT(int target, int attachment, int textarget, int texture, int level) {
        GL30.glFramebufferTexture2D(target, attachment, textarget, texture, level);
    }
    public static int glCheckFramebufferStatusEXT(int target) { return GL30.glCheckFramebufferStatus(target); }
    public static void glDeleteFramebuffersEXT(int framebuffer) { GL30.glDeleteFramebuffers(framebuffer); }

    public static int glGenRenderbuffersEXT() { return GL30.glGenRenderbuffers(); }
    public static void glBindRenderbufferEXT(int target, int renderbuffer) { GL30.glBindRenderbuffer(target, renderbuffer); }
    public static void glRenderbufferStorageEXT(int target, int internalformat, int width, int height) {
        GL30.glRenderbufferStorage(target, internalformat, width, height);
    }
    public static void glFramebufferRenderbufferEXT(int target, int attachment, int renderbuffertarget, int renderbuffer) {
        GL30.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer);
    }
    public static void glDeleteRenderbuffersEXT(int renderbuffer) { GL30.glDeleteRenderbuffers(renderbuffer); }
}
