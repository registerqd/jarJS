package jarjs.juc.locks;

/**
 * 单线程事件循环下的 ReentrantLock 直通实现。
 * JS 里不存在真正的并发争抢，锁只需维护「持有者 + 重入计数」状态机。
 */
public class ReentrantLock implements Lock {
    private Thread owner;
    private int count;
    private final boolean fair;

    public ReentrantLock() { this(false); }
    public ReentrantLock(boolean fair) { this.fair = fair; }

    public void lock() {
        Thread t = Thread.currentThread();
        if (owner == t) { count++; return; }
        owner = t;
        count = 1;
    }

    public void lockInterruptibly() throws InterruptedException { lock(); }

    public boolean tryLock() { lock(); return true; }

    public boolean tryLock(long time, jarjs.juc.TimeUnit unit) throws InterruptedException {
        lock();
        return true;
    }

    public void unlock() {
        Thread t = Thread.currentThread();
        if (owner != t) throw new IllegalMonitorStateException("当前线程未持有锁");
        if (--count == 0) owner = null;
    }

    public Condition newCondition() { return new SimpleCondition(); }

    public int getHoldCount() { return owner == Thread.currentThread() ? count : 0; }
    public boolean isLocked() { return count > 0; }
    public boolean isHeldByCurrentThread() { return owner == Thread.currentThread(); }
    public boolean isFair() { return fair; }
    public final boolean hasQueuedThreads() { return false; }
    public final int getQueueLength() { return 0; }

    private static final class SimpleCondition implements Condition {
        public void await() throws InterruptedException {}
        public void awaitUninterruptibly() {}
        public long awaitNanos(long nanosTimeout) throws InterruptedException { return 0; }
        public boolean await(long time, jarjs.juc.TimeUnit unit) throws InterruptedException { return true; }
        public void signal() {}
        public void signalAll() {}
    }
}
