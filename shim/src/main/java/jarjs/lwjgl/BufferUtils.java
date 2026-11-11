package jarjs.lwjgl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

/** org.lwjgl.BufferUtils 的兼容层。WebGL 没有直接内存概念，统一用堆缓冲区。 */
public final class BufferUtils {

    private BufferUtils() {}

    public static FloatBuffer createFloatBuffer(int capacity) {
        return ByteBuffer.allocateDirect(capacity * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    }

    public static IntBuffer createIntBuffer(int capacity) {
        return ByteBuffer.allocateDirect(capacity * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
    }

    public static ByteBuffer createByteBuffer(int capacity) {
        return ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder());
    }

    public static ShortBuffer createShortBuffer(int capacity) {
        return ByteBuffer.allocateDirect(capacity * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
    }

    public static CharBuffer createCharBuffer(int capacity) {
        return ByteBuffer.allocateDirect(capacity * 2).order(ByteOrder.nativeOrder()).asCharBuffer();
    }

    public static DoubleBuffer createDoubleBuffer(int capacity) {
        return ByteBuffer.allocateDirect(capacity * 8).order(ByteOrder.nativeOrder()).asDoubleBuffer();
    }

    public static LongBuffer createLongBuffer(int capacity) {
        return ByteBuffer.allocateDirect(capacity * 8).order(ByteOrder.nativeOrder()).asLongBuffer();
    }
}
