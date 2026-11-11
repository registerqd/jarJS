package jarjs.juc.locks;

/** 单线程下 park/unpark 直接空转，避免 AQS 链路依赖。 */
public final class LockSupport {
    private LockSupport() {}

    public static void park() {}
    public static void park(Object blocker) {}
    public static void parkNanos(long nanos) {}
    public static void parkNanos(Object blocker, long nanos) {}
    public static void parkUntil(long deadline) {}
    public static void parkUntil(Object blocker, long deadline) {}
    public static void unpark(Thread thread) {}
    public static Object getBlocker(Thread t) { return null; }
}
