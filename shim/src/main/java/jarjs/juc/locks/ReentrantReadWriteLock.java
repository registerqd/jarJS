package jarjs.juc.locks;

/** 单线程下读写锁直接复用可重入锁语义。 */
public class ReentrantReadWriteLock implements ReadWriteLock {
    private final ReentrantLock readLock = new ReentrantLock();
    private final ReentrantLock writeLock = new ReentrantLock();

    public Lock readLock() { return readLock; }
    public Lock writeLock() { return writeLock; }

    public int getReadHoldCount() { return readLock.getHoldCount(); }
    public int getWriteHoldCount() { return writeLock.getHoldCount(); }
    public boolean isWriteLocked() { return writeLock.isLocked(); }
    public boolean isWriteLockedByCurrentThread() { return writeLock.isHeldByCurrentThread(); }
}
