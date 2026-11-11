package jarjs.lwjgl.opengl;

import org.teavm.jso.JSBody;

/**
 * WebGL 后端桥 (v0.3 - 含现代着色器管线)。
 * 所有方法在 JS 侧由 runtime/jarjs-gl.js 实现。
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

    @JSBody(params = {"target", "id"}, script = "window.JARJS_GL.bindTexture(target, id);")
    public static native void bindTexture(int target, int id);

    @JSBody(params = {"unit"}, script = "window.JARJS_GL.activeTexture(unit);")
    public static native void activeTexture(int unit);

    @JSBody(params = {"r", "g", "b", "a", "mask"}, script = "window.JARJS_GL.clear(r, g, b, a, mask);")
    public static native void clear(float r, float g, float b, float a, int mask);

    @JSBody(params = {"flags"}, script = "window.JARJS_GL.setState(flags);")
    public static native void setState(int flags);

    @JSBody(params = {"src", "dst"}, script = "window.JARJS_GL.blendFunc(src, dst);")
    public static native void blendFunc(int src, int dst);

    @JSBody(params = {"sR", "dR", "sA", "dA"}, script = "window.JARJS_GL.blendFuncSeparate(sR, dR, sA, dA);")
    public static native void blendFuncSeparate(int sR, int dR, int sA, int dA);

    @JSBody(params = {"mode"}, script = "window.JARJS_GL.blendEquation(mode);")
    public static native void blendEquation(int mode);

    @JSBody(params = {"w"}, script = "window.JARJS_GL.lineWidth(w);")
    public static native void lineWidth(float w);

    @JSBody(params = {"s"}, script = "window.JARJS_GL.pointSize(s);")
    public static native void pointSize(float s);

    @JSBody(params = {"x", "y", "w", "h"}, script = "window.JARJS_GL.viewport(x, y, w, h);")
    public static native void viewport(int x, int y, int w, int h);

    @JSBody(params = {"x", "y", "w", "h"}, script = "window.JARJS_GL.scissor(x, y, w, h);")
    public static native void scissor(int x, int y, int w, int h);

    @JSBody(params = {"cap"}, script = "window.JARJS_GL.enable(cap);")
    public static native void enable(int cap);

    @JSBody(params = {"cap"}, script = "window.JARJS_GL.disable(cap);")
    public static native void disable(int cap);

    @JSBody(params = {"f"}, script = "window.JARJS_GL.depthFunc(f);")
    public static native void depthFunc(int f);

    @JSBody(params = {"m"}, script = "window.JARJS_GL.depthMask(m);")
    public static native void depthMask(boolean m);

    @JSBody(params = {"r", "g", "b", "a"}, script = "window.JARJS_GL.colorMask(r, g, b, a);")
    public static native void colorMask(boolean r, boolean g, boolean b, boolean a);

    @JSBody(params = {"m"}, script = "window.JARJS_GL.cullFace(m);")
    public static native void cullFace(int m);

    @JSBody(params = {"m"}, script = "window.JARJS_GL.frontFace(m);")
    public static native void frontFace(int m);

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

    // =========================================================
    // ---------- 现代 OpenGL 2.0 着色器桥 ----------
    // =========================================================

    @JSBody(params = {"type"}, script = "return window.JARJS_GL.createShader(type);")
    public static native int createShader(int type);

    @JSBody(params = {"id", "src"}, script = "window.JARJS_GL.shaderSource(id, src);")
    public static native void shaderSource(int id, String src);

    @JSBody(params = {"id"}, script = "window.JARJS_GL.compileShader(id);")
    public static native void compileShader(int id);

    @JSBody(params = {"id", "pname"}, script = "return window.JARJS_GL.getShaderi(id, pname);")
    public static native int getShaderi(int id, int pname);

    @JSBody(params = {"id"}, script = "return window.JARJS_GL.getShaderInfoLog(id);")
    public static native String getShaderInfoLog(int id);

    @JSBody(params = {"id"}, script = "window.JARJS_GL.deleteShader(id);")
    public static native void deleteShader(int id);

    // Program
    @JSBody(params = {}, script = "return window.JARJS_GL.createProgram();")
    public static native int createProgram();

    @JSBody(params = {"pid", "sid"}, script = "window.JARJS_GL.attachShader(pid, sid);")
    public static native void attachShader(int pid, int sid);

    @JSBody(params = {"pid", "sid"}, script = "window.JARJS_GL.detachShader(pid, sid);")
    public static native void detachShader(int pid, int sid);

    @JSBody(params = {"pid"}, script = "window.JARJS_GL.linkProgram(pid);")
    public static native void linkProgram(int pid);

    @JSBody(params = {"pid"}, script = "window.JARJS_GL.validateProgram(pid);")
    public static native void validateProgram(int pid);

    @JSBody(params = {"pid", "pname"}, script = "return window.JARJS_GL.getProgrami(pid, pname);")
    public static native int getProgrami(int pid, int pname);

    @JSBody(params = {"pid"}, script = "return window.JARJS_GL.getProgramInfoLog(pid);")
    public static native String getProgramInfoLog(int pid);

    @JSBody(params = {"pid"}, script = "window.JARJS_GL.useProgram(pid);")
    public static native void useProgram(int pid);

    @JSBody(params = {"pid"}, script = "window.JARJS_GL.deleteProgram(pid);")
    public static native void deleteProgram(int pid);

    // Uniform
    @JSBody(params = {"pid", "name"}, script = "return window.JARJS_GL.getUniformLocation(pid, name);")
    public static native int getUniformLocation(int pid, String name);

    @JSBody(params = {"lid", "v"}, script = "window.JARJS_GL.uniform1i(lid, v);")
    public static native void uniform1i(int lid, int v);

    @JSBody(params = {"lid", "v0", "v1"}, script = "window.JARJS_GL.uniform2i(lid, v0, v1);")
    public static native void uniform2i(int lid, int v0, int v1);

    @JSBody(params = {"lid", "v0", "v1", "v2"}, script = "window.JARJS_GL.uniform3i(lid, v0, v1, v2);")
    public static native void uniform3i(int lid, int v0, int v1, int v2);

    @JSBody(params = {"lid", "v0", "v1", "v2", "v3"}, script = "window.JARJS_GL.uniform4i(lid, v0, v1, v2, v3);")
    public static native void uniform4i(int lid, int v0, int v1, int v2, int v3);

    @JSBody(params = {"lid", "v"}, script = "window.JARJS_GL.uniform1f(lid, v);")
    public static native void uniform1f(int lid, float v);

    @JSBody(params = {"lid", "v0", "v1"}, script = "window.JARJS_GL.uniform2f(lid, v0, v1);")
    public static native void uniform2f(int lid, float v0, float v1);

    @JSBody(params = {"lid", "v0", "v1", "v2"}, script = "window.JARJS_GL.uniform3f(lid, v0, v1, v2);")
    public static native void uniform3f(int lid, float v0, float v1, float v2);

    @JSBody(params = {"lid", "v0", "v1", "v2", "v3"}, script = "window.JARJS_GL.uniform4f(lid, v0, v1, v2, v3);")
    public static native void uniform4f(int lid, float v0, float v1, float v2, float v3);

    @JSBody(params = {"lid", "arr"}, script = "window.JARJS_GL.uniform1fv(lid, arr);")
    public static native void uniform1fv(int lid, float[] arr);

    @JSBody(params = {"lid", "arr"}, script = "window.JARJS_GL.uniform2fv(lid, arr);")
    public static native void uniform2fv(int lid, float[] arr);

    @JSBody(params = {"lid", "arr"}, script = "window.JARJS_GL.uniform3fv(lid, arr);")
    public static native void uniform3fv(int lid, float[] arr);

    @JSBody(params = {"lid", "arr"}, script = "window.JARJS_GL.uniform4fv(lid, arr);")
    public static native void uniform4fv(int lid, float[] arr);

    @JSBody(params = {"lid", "t", "m"}, script = "window.JARJS_GL.uniformMatrix2fv(lid, t, m);")
    public static native void uniformMatrix2fv(int lid, boolean t, float[] m);

    @JSBody(params = {"lid", "t", "m"}, script = "window.JARJS_GL.uniformMatrix3fv(lid, t, m);")
    public static native void uniformMatrix3fv(int lid, boolean t, float[] m);

    @JSBody(params = {"lid", "t", "m"}, script = "window.JARJS_GL.uniformMatrix4fv(lid, t, m);")
    public static native void uniformMatrix4fv(int lid, boolean t, float[] m);

    // Attribute
    @JSBody(params = {"pid", "name"}, script = "return window.JARJS_GL.getAttribLocation(pid, name);")
    public static native int getAttribLocation(int pid, String name);

    @JSBody(params = {"pid", "index", "name"}, script = "window.JARJS_GL.bindAttribLocation(pid, index, name);")
    public static native void bindAttribLocation(int pid, int index, String name);

    @JSBody(params = {"index"}, script = "window.JARJS_GL.enableVertexAttribArray(index);")
    public static native void enableVertexAttribArray(int index);

    @JSBody(params = {"index"}, script = "window.JARJS_GL.disableVertexAttribArray(index);")
    public static native void disableVertexAttribArray(int index);

    @JSBody(params = {"idx", "sz", "tp", "norm", "stride", "offset"},
            script = "window.JARJS_GL.vertexAttribPointer(idx, sz, tp, norm, stride, offset);")
    public static native void vertexAttribPointer(int idx, int sz, int tp, boolean norm, int stride, int offset);

    @JSBody(params = {"idx", "x"}, script = "window.JARJS_GL.vertexAttrib1f(idx, x);")
    public static native void vertexAttrib1f(int idx, float x);

    @JSBody(params = {"idx", "x", "y"}, script = "window.JARJS_GL.vertexAttrib2f(idx, x, y);")
    public static native void vertexAttrib2f(int idx, float x, float y);

    @JSBody(params = {"idx", "x", "y", "z"}, script = "window.JARJS_GL.vertexAttrib3f(idx, x, y, z);")
    public static native void vertexAttrib3f(int idx, float x, float y, float z);

    @JSBody(params = {"idx", "x", "y", "z", "w"}, script = "window.JARJS_GL.vertexAttrib4f(idx, x, y, z, w);")
    public static native void vertexAttrib4f(int idx, float x, float y, float z, float w);

    // =========================================================
    // ---------- 缓冲区对象 Buffer Objects (GL15) ----------
    // =========================================================

    @JSBody(params = {}, script = "return window.JARJS_GL.createBuffer();")
    public static native int createBuffer();

    @JSBody(params = {"target", "id"}, script = "window.JARJS_GL.bindBuffer(target, id);")
    public static native void bindBuffer(int target, int id);

    @JSBody(params = {"target", "arr", "usage"}, script = "window.JARJS_GL.bufferDataFloat(target, arr, usage);")
    public static native void bufferDataFloat(int target, float[] arr, int usage);

    @JSBody(params = {"target", "arr", "usage"}, script = "window.JARJS_GL.bufferDataInt(target, arr, usage);")
    public static native void bufferDataInt(int target, int[] arr, int usage);

    @JSBody(params = {"target", "arr", "usage"}, script = "window.JARJS_GL.bufferDataShort(target, arr, usage);")
    public static native void bufferDataShort(int target, short[] arr, int usage);

    @JSBody(params = {"target", "arr", "usage"}, script = "window.JARJS_GL.bufferDataByte(target, arr, usage);")
    public static native void bufferDataByte(int target, byte[] arr, int usage);

    @JSBody(params = {"target", "offset", "arr"}, script = "window.JARJS_GL.bufferSubDataFloat(target, offset, arr);")
    public static native void bufferSubDataFloat(int target, int offset, float[] arr);

    @JSBody(params = {"id"}, script = "window.JARJS_GL.deleteBuffer(id);")
    public static native void deleteBuffer(int id);

    @JSBody(params = {"mode", "first", "count"}, script = "window.JARJS_GL.drawArraysNative(mode, first, count);")
    public static native void drawArraysNative(int mode, int first, int count);

    @JSBody(params = {"mode", "count", "type", "offset"},
            script = "window.JARJS_GL.drawElementsNative(mode, count, type, offset);")
    public static native void drawElementsNative(int mode, int count, int type, int offset);

    // =========================================================
    // ---------- 帧缓冲 Framebuffer Objects (GL30) ----------
    // =========================================================

    @JSBody(params = {}, script = "return window.JARJS_GL.createFramebuffer();")
    public static native int createFramebuffer();

    @JSBody(params = {"target", "id"}, script = "window.JARJS_GL.bindFramebuffer(target, id);")
    public static native void bindFramebuffer(int target, int id);

    @JSBody(params = {"target", "attach", "textarget", "texId", "level"},
            script = "window.JARJS_GL.framebufferTexture2D(target, attach, textarget, texId, level);")
    public static native void framebufferTexture2D(int target, int attach, int textarget, int texId, int level);

    @JSBody(params = {"target"}, script = "return window.JARJS_GL.checkFramebufferStatus(target);")
    public static native int checkFramebufferStatus(int target);

    @JSBody(params = {"id"}, script = "window.JARJS_GL.deleteFramebuffer(id);")
    public static native void deleteFramebuffer(int id);
}
