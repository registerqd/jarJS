package jarjs.juc.locks;

/** 单线程下信号量直通：acquire 立即成功，release 无副作用。 */
public class Semaphore {
    private int permits;

    public Semaphore(int permits) { this.permits = permits; }
    public Semaphore(int permits, boolean fair) { this.permits = permits; }

    public void acquire() throws InterruptedException {}
    public void acquire(int n) throws InterruptedException {}
    public void acquireUninterruptibly() {}
    public boolean tryAcquire() { return true; }
    public boolean tryAcquire(long timeout, jarjs.juc.TimeUnit unit) throws InterruptedException { return true; }
    public void release() {}
    public void release(int n) {}
    public int availablePermits() { return permits; }
    public boolean hasQueuedThreads() { return false; }
    public int getQueueLength() { return 0; }
}
