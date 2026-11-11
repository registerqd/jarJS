package jarjs.lwjgl.opengl;

import java.nio.IntBuffer;

/**
 * org.lwjgl.opengl.GL30 兼容层 —— 帧缓冲对象（FBO）与顶点数组对象（VAO）。
 * 支持离屏渲染、后处理通道与动态纹理生成。
 */
public final class GL30 {

    private GL30() {}

    // 帧缓冲绑定与附件
    public static final int GL_FRAMEBUFFER = 0x8D40;
    public static final int GL_READ_FRAMEBUFFER = 0x8CA8;
    public static final int GL_DRAW_FRAMEBUFFER = 0x8CA9;
    public static final int GL_RENDERBUFFER = 0x8D41;

    public static final int GL_COLOR_ATTACHMENT0 = 0x8CE0;
    public static final int GL_DEPTH_ATTACHMENT = 0x8D00;
    public static final int GL_STENCIL_ATTACHMENT = 0x8D20;
    public static final int GL_DEPTH_STENCIL_ATTACHMENT = 0x821A;

    public static final int GL_FRAMEBUFFER_COMPLETE = 0x8CD5;
    public static final int GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT = 0x8CD6;
    public static final int GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT = 0x8CD7;
    public static final int GL_FRAMEBUFFER_UNSUPPORTED = 0x8CDD;

    // =========================================================
    // ---------- 帧缓冲 Framebuffer ----------
    // =========================================================

    public static int glGenFramebuffers() {
        return GLBackend.createFramebuffer();
    }

    public static void glGenFramebuffers(IntBuffer buffers) {
        int pos = buffers.position();
        int limit = buffers.limit();
        for (int i = pos; i < limit; i++) {
            buffers.put(i, GLBackend.createFramebuffer());
        }
    }

    public static void glBindFramebuffer(int target, int framebuffer) {
        GLBackend.bindFramebuffer(target, framebuffer);
    }

    public static void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level) {
        GLBackend.framebufferTexture2D(target, attachment, textarget, texture, level);
    }

    public static int glCheckFramebufferStatus(int target) {
        return GLBackend.checkFramebufferStatus(target);
    }

    public static void glDeleteFramebuffers(int framebuffer) {
        GLBackend.deleteFramebuffer(framebuffer);
    }

    public static void glDeleteFramebuffers(IntBuffer buffers) {
        int pos = buffers.position();
        int limit = buffers.limit();
        for (int i = pos; i < limit; i++) {
            GLBackend.deleteFramebuffer(buffers.get(i));
        }
    }

    // =========================================================
    // ---------- 顶点数组对象 VAO (保留标准接口) ----------
    // =========================================================

    public static int glGenVertexArrays() { return 1; }
    public static void glBindVertexArray(int array) {}
    public static void glDeleteVertexArrays(int array) {}
}
