package jarjs.lwjgl.opengl;

/**
 * org.lwjgl.opengl.ARBFramebufferObject 兼容层。
 * 转发到 GL30 的帧缓冲实现。
 */
public final class ARBFramebufferObject {

    private ARBFramebufferObject() {}

    public static final int GL_FRAMEBUFFER = 0x8D40;
    public static final int GL_RENDERBUFFER = 0x8D41;
    public static final int GL_COLOR_ATTACHMENT0 = 0x8CE0;
    public static final int GL_DEPTH_ATTACHMENT = 0x8D00;
    public static final int GL_STENCIL_ATTACHMENT = 0x8D20;
    public static final int GL_DEPTH_STENCIL_ATTACHMENT = 0x821A;
    public static final int GL_FRAMEBUFFER_COMPLETE = 0x8CD5;

    public static int glGenFramebuffers() { return GL30.glGenFramebuffers(); }
    public static void glBindFramebuffer(int target, int framebuffer) { GL30.glBindFramebuffer(target, framebuffer); }
    public static void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level) {
        GL30.glFramebufferTexture2D(target, attachment, textarget, texture, level);
    }
    public static int glCheckFramebufferStatus(int target) { return GL30.glCheckFramebufferStatus(target); }
    public static void glDeleteFramebuffers(int framebuffer) { GL30.glDeleteFramebuffers(framebuffer); }

    public static int glGenRenderbuffers() { return GL30.glGenRenderbuffers(); }
    public static void glBindRenderbuffer(int target, int renderbuffer) { GL30.glBindRenderbuffer(target, renderbuffer); }
    public static void glRenderbufferStorage(int target, int internalformat, int width, int height) {
        GL30.glRenderbufferStorage(target, internalformat, width, height);
    }
    public static void glFramebufferRenderbuffer(int target, int attachment, int renderbuffertarget, int renderbuffer) {
        GL30.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer);
    }
    public static void glDeleteRenderbuffers(int renderbuffer) { GL30.glDeleteRenderbuffers(renderbuffer); }
}
