package jarjs.lwjgl.opengl;

import org.teavm.jso.JSBody;

/**
 * WebGL 后端桥。所有方法在 JS 侧由 runtime/jarjs-gl.js 实现。
 * Java 侧只负责固定管线语义（矩阵、光照、图元展开），这里只做搬运。
 */
public final class GLBackend {

    private GLBackend() {}

    @JSBody(params = {"w", "h", "dpr"}, script = "return (window.JARJS_GL ? window.JARJS_GL.init(w, h, dpr) : 0);")
    public static native int init(int w, int h, double dpr);

    @JSBody(params = {"mode", "verts", "count", "mvp", "texId", "flags"},
            script = "window.JARJS_GL.draw(mode, verts, count, mvp, texId, flags);")
    public static native void draw(int mode, float[] verts, int count, float[] mvp, int texId, int flags);

    @JSBody(params = {"id", "w", "h", "argb", "filter", "wrap"},
            script = "window.JARJS_GL.texImage(id, w, h, argb, filter, wrap);")
    public static native void texImage(int id, int w, int h, int[] argb, int filter, int wrap);

    @JSBody(params = {"id"}, script = "window.JARJS_GL.deleteTexture(id);")
    public static native void deleteTexture(int id);

    @JSBody(params = {"r", "g", "b", "a", "mask"}, script = "window.JARJS_GL.clear(r, g, b, a, mask);")
    public static native void clear(float r, float g, float b, float a, int mask);

    @JSBody(params = {"flags"}, script = "window.JARJS_GL.setState(flags);")
    public static native void setState(int flags);

    @JSBody(params = {"src", "dst"}, script = "window.JARJS_GL.blendFunc(src, dst);")
    public static native void blendFunc(int src, int dst);

    @JSBody(params = {"w"}, script = "window.JARJS_GL.lineWidth(w);")
    public static native void lineWidth(float w);

    @JSBody(params = {"s"}, script = "window.JARJS_GL.pointSize(s);")
    public static native void pointSize(float s);

    @JSBody(params = {"name"}, script = "return window.JARJS_GL.getString(name);")
    public static native String getString(int name);

    @JSBody(params = {"id"}, script = "return window.JARJS_GL.isTextureReady(id);")
    public static native boolean isTextureReady(int id);

    @JSBody(params = {}, script = "return window.JARJS_GL.getError();")
    public static native int getError();

    @JSBody(params = {}, script = "return window.JARJS_GL.contextLost() ? 1 : 0;")
    public static native int contextLost();

    @JSBody(params = {"msg"}, script = "if (window.console) console.warn('[JARJS GL] ' + msg);")
    public static native void warn(String msg);
}
