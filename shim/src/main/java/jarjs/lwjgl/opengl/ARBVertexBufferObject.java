package jarjs.lwjgl.opengl;

/** org.lwjgl.opengl.ARBVertexBufferObject 兼容层。 */
public final class ARBVertexBufferObject {

    private ARBVertexBufferObject() {}

    public static final int GL_ARRAY_BUFFER_ARB = 0x8892;
    public static final int GL_ELEMENT_ARRAY_BUFFER_ARB = 0x8893;
    public static final int GL_STATIC_DRAW_ARB = 0x88E4;
    public static final int GL_DYNAMIC_DRAW_ARB = 0x88E8;
    public static final int GL_STREAM_DRAW_ARB = 0x88E0;

    public static int glGenBuffersARB() { return GL15.glGenBuffers(); }
    public static void glBindBufferARB(int target, int buffer) { GL15.glBindBuffer(target, buffer); }
    public static void glBufferDataARB(int target, java.nio.FloatBuffer data, int usage) {
        GL15.glBufferData(target, data, usage);
    }
    public static void glBufferDataARB(int target, java.nio.IntBuffer data, int usage) {
        GL15.glBufferData(target, data, usage);
    }
    public static void glBufferDataARB(int target, java.nio.ShortBuffer data, int usage) {
        GL15.glBufferData(target, data, usage);
    }
    public static void glBufferDataARB(int target, java.nio.ByteBuffer data, int usage) {
        GL15.glBufferData(target, data, usage);
    }
    public static void glBufferDataARB(int target, long size, int usage) { GL15.glBufferData(target, size, usage); }
    public static void glBufferSubDataARB(int target, long offset, java.nio.FloatBuffer data) {
        GL15.glBufferSubData(target, offset, data);
    }
    public static void glDeleteBuffersARB(int buffer) { GL15.glDeleteBuffers(buffer); }
    public static void glDeleteBuffersARB(java.nio.IntBuffer buffers) { GL15.glDeleteBuffers(buffers); }
}
