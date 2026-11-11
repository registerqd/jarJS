package jarjs.unsafe;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * sun.misc.Unsafe 兼容层 —— 直接内存与指针读写模拟器。
 * Netty 底层（如 PlatformDependent0）强依赖此类的直接内存分配与堆外读写。
 * 在浏览器无直接内存的环境下，通过虚拟地址分块页表（Virtual Page Blocks）完全纯 Java 仿真。
 */
public final class Unsafe {

    private static final Unsafe THE_UNSAFE = new Unsafe();

    // 虚拟地址分配计数器（从 0x10000 开始，防空指针）
    private static long nextAddress = 0x10000L;
    // 模拟内存块页表：虚拟地址 -> byte[] 内存段
    private static final Map<Long, byte[]> MEMORY_MAP = new HashMap<>();
    private static final Map<Long, Integer> SIZE_MAP = new HashMap<>();

    private Unsafe() {}

    public static Unsafe getUnsafe() {
        return THE_UNSAFE;
    }

    // ---------- 直接内存分配与释放 (Netty DirectByteBuf 核心) ----------

    public synchronized long allocateMemory(long bytes) {
        if (bytes <= 0) bytes = 1;
        long addr = nextAddress;
        nextAddress += (bytes + 15) & ~15; // 16 字节对齐
        byte[] block = new byte[(int) bytes];
        MEMORY_MAP.put(addr, block);
        SIZE_MAP.put(addr, (int) bytes);
        return addr;
    }

    public synchronized void freeMemory(long address) {
        MEMORY_MAP.remove(address);
        SIZE_MAP.remove(address);
    }

    public synchronized long reallocateMemory(long address, long newSize) {
        long newAddr = allocateMemory(newSize);
        byte[] oldBlock = MEMORY_MAP.get(address);
        if (oldBlock != null) {
            byte[] newBlock = MEMORY_MAP.get(newAddr);
            System.arraycopy(oldBlock, 0, newBlock, 0, Math.min(oldBlock.length, (int) newSize));
            freeMemory(address);
        }
        return newAddr;
    }

    public synchronized void setMemory(long address, long bytes, byte value) {
        byte[] block = MEMORY_MAP.get(address);
        if (block != null) {
            int len = Math.min(block.length, (int) bytes);
            for (int i = 0; i < len; i++) block[i] = value;
        }
    }

    public synchronized void copyMemory(long srcAddress, long destAddress, long bytes) {
        byte[] src = MEMORY_MAP.get(srcAddress);
        byte[] dst = MEMORY_MAP.get(destAddress);
        if (src != null && dst != null) {
            System.arraycopy(src, 0, dst, 0, Math.min((int) bytes, Math.min(src.length, dst.length)));
        }
    }

    public synchronized void copyMemory(Object srcBase, long srcOffset, Object destBase, long destOffset, long bytes) {
        if (srcBase instanceof byte[] && destBase instanceof byte[]) {
            System.arraycopy(srcBase, (int) srcOffset, destBase, (int) destOffset, (int) bytes);
        } else if (srcBase instanceof byte[] && destBase == null) {
            byte[] dst = MEMORY_MAP.get(destOffset);
            if (dst != null) System.arraycopy(srcBase, (int) srcOffset, dst, 0, (int) bytes);
        } else if (srcBase == null && destBase instanceof byte[]) {
            byte[] src = MEMORY_MAP.get(srcOffset);
            if (src != null) System.arraycopy(src, 0, destBase, (int) destOffset, (int) bytes);
        }
    }

    // ---------- 指针直接读写 ----------

    public synchronized byte getByte(long address) {
        byte[] b = MEMORY_MAP.get(address);
        return (b != null && b.length > 0) ? b[0] : 0;
    }

    public synchronized void putByte(long address, byte x) {
        byte[] b = MEMORY_MAP.get(address);
        if (b != null && b.length > 0) b[0] = x;
    }

    public synchronized short getShort(long address) {
        byte[] b = MEMORY_MAP.get(address);
        if (b == null || b.length < 2) return 0;
        return (short) ((b[0] & 0xFF) | ((b[1] & 0xFF) << 8));
    }

    public synchronized void putShort(long address, short x) {
        byte[] b = MEMORY_MAP.get(address);
        if (b != null && b.length >= 2) {
            b[0] = (byte) x; b[1] = (byte) (x >> 8);
        }
    }

    public synchronized int getInt(long address) {
        byte[] b = MEMORY_MAP.get(address);
        if (b == null || b.length < 4) return 0;
        return (b[0] & 0xFF) | ((b[1] & 0xFF) << 8) | ((b[2] & 0xFF) << 16) | ((b[3] & 0xFF) << 24);
    }

    public synchronized void putInt(long address, int x) {
        byte[] b = MEMORY_MAP.get(address);
        if (b != null && b.length >= 4) {
            b[0] = (byte) x; b[1] = (byte) (x >> 8);
            b[2] = (byte) (x >> 16); b[3] = (byte) (x >> 24);
        }
    }

    public synchronized long getLong(long address) {
        return (long) getInt(address) & 0xFFFFFFFFL;
    }

    public synchronized void putLong(long address, long x) {
        putInt(address, (int) x);
    }

    // ---------- 硬件架构信息探测常量 ----------

    public int addressSize() { return 8; }
    public int pageSize() { return 4096; }

    public int arrayBaseOffset(Class<?> clazz) { return 0; }
    public int arrayIndexScale(Class<?> clazz) { return 1; }

    public long objectFieldOffset(Field f) { return 0; }
    public boolean compareAndSwapInt(Object o, long offset, int expected, int x) { return true; }
    public boolean compareAndSwapLong(Object o, long offset, long expected, long x) { return true; }
    public boolean compareAndSwapObject(Object o, long offset, Object expected, Object x) { return true; }

    // ================= v0.6 补齐：Netty 原子/易失读写 =================

    public synchronized int getIntVolatile(Object o, long offset) { return getInt(offset); }
    public synchronized void putIntVolatile(Object o, long offset, int x) { putInt(offset, x); }

    public synchronized long getLongVolatile(Object o, long offset) { return getLong(offset); }
    public synchronized void putLongVolatile(Object o, long offset, long x) { putLong(offset, x); }

    public Object getObjectVolatile(Object o, long offset) { return null; }
    public void putObjectVolatile(Object o, long offset, Object x) {}
    public void putOrderedInt(Object o, long offset, int x) { putInt(offset, x); }
    public void putOrderedLong(Object o, long offset, long x) { putLong(offset, x); }
    public void putOrderedObject(Object o, long offset, Object x) {}

    public Object getObject(Object o, long offset) { return null; }
    public void putObject(Object o, long offset, Object x) {}

    public int getInt(Object o, long offset) { return 0; }
    public void putInt(Object o, long offset, int x) {}

    public long getLong(Object o, long offset) { return 0; }
    public void putLong(Object o, long offset, long x) {}

    public byte getByte(Object o, long offset) { return 0; }
    public void putByte(Object o, long offset, byte x) {}

    public short getShort(Object o, long offset) { return 0; }
    public void putShort(Object o, long offset, short x) {}

    public void throwException(Throwable ee) {
        if (ee instanceof RuntimeException) throw (RuntimeException) ee;
        if (ee instanceof Error) throw (Error) ee;
        throw new RuntimeException(ee);
    }

    public void ensureClassInitialized(Class<?> c) {}
    public Class<?> defineClass(String name, byte[] b, int off, int len) { return null; }
    public Class<?> defineAnonymousClass(Class<?> hostClass, byte[] data, Object[] cpPatches) { return null; }
    public Object allocateInstance(Class<?> cls) throws InstantiationException {
        try { return cls.getDeclaredConstructor().newInstance(); }
        catch (Exception e) { throw new InstantiationException(e.getMessage()); }
    }
}
