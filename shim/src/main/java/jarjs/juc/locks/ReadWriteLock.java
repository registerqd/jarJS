package jarjs.juc.locks;

public interface ReadWriteLock {
    Lock readLock();
    Lock writeLock();
}
