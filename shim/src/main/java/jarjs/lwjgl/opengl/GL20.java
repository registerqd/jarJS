package jarjs.lwjgl.opengl;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * org.lwjgl.opengl.GL20 兼容层 —— 现代可编程管线核心（GLSL 着色器与程序对象）。
 * 将桌面 OpenGL 2.0 着色器完全映射为原生 WebGL 调用，并支持桌面 GLSL 自动预处理。
 */
public final class GL20 {

    private GL20() {}

    // 着色器类型
    public static final int GL_FRAGMENT_SHADER = 0x8B30;
    public static final int GL_VERTEX_SHADER = 0x8B31;
    public static final int GL_MAX_VERTEX_ATTRIBUTES = 0x8869;

    // 着色器参数
    public static final int GL_SHADER_TYPE = 0x8B4F;
    public static final int GL_DELETE_STATUS = 0x8B80;
    public static final int GL_COMPILE_STATUS = 0x8B81;
    public static final int GL_LINK_STATUS = 0x8B82;
    public static final int GL_VALIDATE_STATUS = 0x8B83;
    public static final int GL_INFO_LOG_LENGTH = 0x8B84;
    public static final int GL_ATTACHED_SHADERS = 0x8B85;
    public static final int GL_ACTIVE_UNIFORMS = 0x8B86;
    public static final int GL_ACTIVE_ATTRIBUTES = 0x8B89;
    public static final int GL_SHADING_LANGUAGE_VERSION = 0x8B8C;
    public static final int GL_CURRENT_PROGRAM = 0x8B8D;

    // =========================================================
    // ---------- Shader 对象管理 ----------
    // =========================================================

    /** 创建着色器对象（GL_VERTEX_SHADER 或 GL_FRAGMENT_SHADER）。 */
    public static int glCreateShader(int type) {
        return GLBackend.createShader(type);
    }

    /** 提交着色器 GLSL 源码字符串。 */
    public static void glShaderSource(int shader, CharSequence string) {
        GLBackend.shaderSource(shader, string == null ? "" : string.toString());
    }

    /** 提交着色器源码字节缓冲。 */
    public static void glShaderSource(int shader, ByteBuffer source) {
        int n = source.remaining();
        byte[] b = new byte[n];
        int pos = source.position();
        for (int i = 0; i < n; i++) b[i] = source.get(pos + i);
        GLBackend.shaderSource(shader, new String(b, java.nio.charset.StandardCharsets.UTF_8));
    }

    /** 编译着色器并执行自动 GLSL 兼容预处理。 */
    public static void glCompileShader(int shader) {
        GLBackend.compileShader(shader);
    }

    /** 查询着色器状态（GL_COMPILE_STATUS 等）。 */
    public static int glGetShaderi(int shader, int pname) {
        return GLBackend.getShaderi(shader, pname);
    }

    /** 查询着色器编译信息日志。 */
    public static String glGetShaderInfoLog(int shader, int maxLength) {
        return GLBackend.getShaderInfoLog(shader);
    }

    /** 删除着色器对象。 */
    public static void glDeleteShader(int shader) {
        GLBackend.deleteShader(shader);
    }

    // =========================================================
    // ---------- Program 对象管理 ----------
    // =========================================================

    /** 创建着色器程序对象。 */
    public static int glCreateProgram() {
        return GLBackend.createProgram();
    }

    /** 附加着色器到程序。 */
    public static void glAttachShader(int program, int shader) {
        GLBackend.attachShader(program, shader);
    }

    /** 解除着色器关联。 */
    public static void glDetachShader(int program, int shader) {
        GLBackend.detachShader(program, shader);
    }

    /** 链接着色器程序。 */
    public static void glLinkProgram(int program) {
        GLBackend.linkProgram(program);
    }

    /** 验证着色器程序。 */
    public static void glValidateProgram(int program) {
        GLBackend.validateProgram(program);
    }

    /** 查询程序参数（GL_LINK_STATUS 等）。 */
    public static int glGetProgrami(int program, int pname) {
        return GLBackend.getProgrami(program, pname);
    }

    /** 查询程序链接日志。 */
    public static String glGetProgramInfoLog(int program, int maxLength) {
        return GLBackend.getProgramInfoLog(program);
    }

    /** 激活着色器程序（传 0 恢复内置管线）。 */
    public static void glUseProgram(int program) {
        GL.currentProgram = program;
        GLBackend.useProgram(program);
    }

    /** 删除着色器程序。 */
    public static void glDeleteProgram(int program) {
        GLBackend.deleteProgram(program);
    }

    // =========================================================
    // ---------- Uniform 变量操作 ----------
    // =========================================================

    /** 获取 Uniform 变量槽位。 */
    public static int glGetUniformLocation(int program, CharSequence name) {
        return GLBackend.getUniformLocation(program, name == null ? "" : name.toString());
    }

    public static void glUniform1i(int location, int v0) { GLBackend.uniform1i(location, v0); }
    public static void glUniform2i(int location, int v0, int v1) { GLBackend.uniform2i(location, v0, v1); }
    public static void glUniform3i(int location, int v0, int v1, int v2) { GLBackend.uniform3i(location, v0, v1, v2); }
    public static void glUniform4i(int location, int v0, int v1, int v2, int v3) { GLBackend.uniform4i(location, v0, v1, v2, v3); }

    public static void glUniform1f(int location, float v0) { GLBackend.uniform1f(location, v0); }
    public static void glUniform2f(int location, float v0, float v1) { GLBackend.uniform2f(location, v0, v1); }
    public static void glUniform3f(int location, float v0, float v1, float v2) { GLBackend.uniform3f(location, v0, v1, v2); }
    public static void glUniform4f(int location, float v0, float v1, float v2, float v3) { GLBackend.uniform4f(location, v0, v1, v2, v3); }

    public static void glUniform1(int location, FloatBuffer values) {
        int n = values.remaining();
        float[] arr = new float[n];
        int pos = values.position();
        for (int i = 0; i < n; i++) arr[i] = values.get(pos + i);
        GLBackend.uniform1fv(location, arr);
    }

    public static void glUniformMatrix4(int location, boolean transpose, FloatBuffer matrices) {
        float[] arr = new float[16];
        int pos = matrices.position();
        for (int i = 0; i < 16; i++) arr[i] = matrices.get(pos + i);
        GLBackend.uniformMatrix4fv(location, transpose, arr);
    }

    public static void glUniformMatrix3(int location, boolean transpose, FloatBuffer matrices) {
        float[] arr = new float[9];
        int pos = matrices.position();
        for (int i = 0; i < 9; i++) arr[i] = matrices.get(pos + i);
        GLBackend.uniformMatrix3fv(location, transpose, arr);
    }

    public static void glUniformMatrix2(int location, boolean transpose, FloatBuffer matrices) {
        float[] arr = new float[4];
        int pos = matrices.position();
        for (int i = 0; i < 4; i++) arr[i] = matrices.get(pos + i);
        GLBackend.uniformMatrix2fv(location, transpose, arr);
    }

    // =========================================================
    // ---------- Attribute 顶点属性操作 ----------
    // =========================================================

    /** 获取顶点属性索引。 */
    public static int glGetAttribLocation(int program, CharSequence name) {
        return GLBackend.getAttribLocation(program, name == null ? "" : name.toString());
    }

    /** 预绑定顶点属性槽位索引。 */
    public static void glBindAttribLocation(int program, int index, CharSequence name) {
        GLBackend.bindAttribLocation(program, index, name == null ? "" : name.toString());
    }

    /** 启用顶点属性数组。 */
    public static void glEnableVertexAttribArray(int index) {
        GLBackend.enableVertexAttribArray(index);
    }

    /** 禁用顶点属性数组。 */
    public static void glDisableVertexAttribArray(int index) {
        GLBackend.disableVertexAttribArray(index);
    }

    /** 配置顶点属性指针（基于当前绑定的 VBO 偏移量）。 */
    public static void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, long buffer_buffer_offset) {
        GLBackend.vertexAttribPointer(index, size, type, normalized, stride, (int) buffer_buffer_offset);
    }

    public static void glVertexAttrib1f(int index, float v0) { GLBackend.vertexAttrib1f(index, v0); }
    public static void glVertexAttrib2f(int index, float v0, float v1) { GLBackend.vertexAttrib2f(index, v0, v1); }
    public static void glVertexAttrib3f(int index, float v0, float v1, float v2) { GLBackend.vertexAttrib3f(index, v0, v1, v2); }
    public static void glVertexAttrib4f(int index, float v0, float v1, float v2, float v3) { GLBackend.vertexAttrib4f(index, v0, v1, v2, v3); }

    // =========================================================
    // ---------- 原生绘制调用 ----------
    // =========================================================

    public static void glDrawArrays(int mode, int first, int count) {
        GLBackend.drawArraysNative(mode, first, count);
    }

    public static void glDrawElements(int mode, int count, int type, long indices) {
        GLBackend.drawElementsNative(mode, count, type, (int) indices);
    }

    // ================= v0.6 补齐：Uniform 数组重载 =================

    public static void glUniform1(int location, IntBuffer values) {
        if (values == null || values.remaining() < 1) return;
        GLBackend.uniform1i(location, values.get(values.position()));
    }

    public static void glUniform2(int location, IntBuffer values) {
        if (values == null || values.remaining() < 2) return;
        int p = values.position();
        GLBackend.uniform2i(location, values.get(p), values.get(p + 1));
    }

    public static void glUniform3(int location, IntBuffer values) {
        if (values == null || values.remaining() < 3) return;
        int p = values.position();
        GLBackend.uniform3i(location, values.get(p), values.get(p + 1), values.get(p + 2));
    }

    public static void glUniform4(int location, IntBuffer values) {
        if (values == null || values.remaining() < 4) return;
        int p = values.position();
        GLBackend.uniform4i(location, values.get(p), values.get(p + 1), values.get(p + 2), values.get(p + 3));
    }

    public static void glUniform2(int location, FloatBuffer values) {
        if (values == null || values.remaining() < 2) return;
        int p = values.position();
        GLBackend.uniform2f(location, values.get(p), values.get(p + 1));
    }

    public static void glUniform3(int location, FloatBuffer values) {
        if (values == null || values.remaining() < 3) return;
        int p = values.position();
        GLBackend.uniform3f(location, values.get(p), values.get(p + 1), values.get(p + 2));
    }

    public static void glUniform4(int location, FloatBuffer values) {
        if (values == null || values.remaining() < 4) return;
        int p = values.position();
        GLBackend.uniform4f(location, values.get(p), values.get(p + 1), values.get(p + 2), values.get(p + 3));
    }

    public static void glUniform1fv(int location, int count, FloatBuffer values) {
        if (values == null) return;
        int n = Math.min(count, values.remaining());
        float[] arr = new float[n];
        int p = values.position();
        for (int i = 0; i < n; i++) arr[i] = values.get(p + i);
        GLBackend.uniform1fv(location, arr);
    }

    public static void glUniform4fv(int location, int count, FloatBuffer values) {
        if (values == null) return;
        int n = Math.min(count * 4, values.remaining());
        float[] arr = new float[n];
        int p = values.position();
        for (int i = 0; i < n; i++) arr[i] = values.get(p + i);
        GLBackend.uniform4fv(location, arr);
    }
}
