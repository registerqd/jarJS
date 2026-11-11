package jarjs.lwjgl.opengl;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * org.lwjgl.opengl.GL15 兼容层 —— 顶点缓冲对象（VBO）与索引缓冲对象（IBO）。
 * 直接桥接到 WebGL 标准缓冲对象，零额外开销。
 */
public final class GL15 {

    private GL15() {}

    // 缓冲区绑定目标
    public static final int GL_ARRAY_BUFFER = 0x8892;
    public static final int GL_ELEMENT_ARRAY_BUFFER = 0x8893;
    public static final int GL_ARRAY_BUFFER_BINDING = 0x8894;
    public static final int GL_ELEMENT_ARRAY_BUFFER_BINDING = 0x8895;

    // 缓冲区使用方式
    public static final int GL_STREAM_DRAW = 0x88E0;
    public static final int GL_STATIC_DRAW = 0x88E4;
    public static final int GL_DYNAMIC_DRAW = 0x88E8;

    // 缓冲区参数查询
    public static final int GL_BUFFER_SIZE = 0x8764;
    public static final int GL_BUFFER_USAGE = 0x8765;
    public static final int GL_BUFFER_ACCESS = 0x88BB;
    public static final int GL_BUFFER_MAPPED = 0x88BC;

    /** 创建单个缓冲对象 ID。 */
    public static int glGenBuffers() {
        return GLBackend.createBuffer();
    }

    /** 批量创建缓冲对象 ID。 */
    public static void glGenBuffers(IntBuffer buffers) {
        int pos = buffers.position();
        int limit = buffers.limit();
        for (int i = pos; i < limit; i++) {
            buffers.put(i, GLBackend.createBuffer());
        }
    }

    /** 绑定当前活动缓冲对象。 */
    public static void glBindBuffer(int target, int buffer) {
        GLBackend.bindBuffer(target, buffer);
    }

    /** 上传 Float 顶点数据到缓冲对象。 */
    public static void glBufferData(int target, FloatBuffer data, int usage) {
        int n = data.remaining();
        float[] arr = new float[n];
        int pos = data.position();
        for (int i = 0; i < n; i++) arr[i] = data.get(pos + i);
        GLBackend.bufferDataFloat(target, arr, usage);
    }

    /** 上传 Int 顶点/索引数据到缓冲对象。 */
    public static void glBufferData(int target, IntBuffer data, int usage) {
        int n = data.remaining();
        int[] arr = new int[n];
        int pos = data.position();
        for (int i = 0; i < n; i++) arr[i] = data.get(pos + i);
        GLBackend.bufferDataInt(target, arr, usage);
    }

    /** 上传 Short 索引数据到缓冲对象。 */
    public static void glBufferData(int target, ShortBuffer data, int usage) {
        int n = data.remaining();
        short[] arr = new short[n];
        int pos = data.position();
        for (int i = 0; i < n; i++) arr[i] = data.get(pos + i);
        GLBackend.bufferDataShort(target, arr, usage);
    }

    /** 上传 Byte 数据到缓冲对象。 */
    public static void glBufferData(int target, ByteBuffer data, int usage) {
        int n = data.remaining();
        byte[] arr = new byte[n];
        int pos = data.position();
        for (int i = 0; i < n; i++) arr[i] = data.get(pos + i);
        GLBackend.bufferDataByte(target, arr, usage);
    }

    /** 预分配指定尺寸的空缓冲。 */
    public static void glBufferData(int target, long size, int usage) {
        GLBackend.bufferDataByte(target, new byte[(int) size], usage);
    }

    /** 更新缓冲区局部子区域数据。 */
    public static void glBufferSubData(int target, long offset, FloatBuffer data) {
        int n = data.remaining();
        float[] arr = new float[n];
        int pos = data.position();
        for (int i = 0; i < n; i++) arr[i] = data.get(pos + i);
        GLBackend.bufferSubDataFloat(target, (int) offset, arr);
    }

    /** 删除单个缓冲对象。 */
    public static void glDeleteBuffers(int buffer) {
        GLBackend.deleteBuffer(buffer);
    }

    /** 批量删除缓冲对象。 */
    public static void glDeleteBuffers(IntBuffer buffers) {
        int pos = buffers.position();
        int limit = buffers.limit();
        for (int i = pos; i < limit; i++) {
            GLBackend.deleteBuffer(buffers.get(i));
        }
    }
}
