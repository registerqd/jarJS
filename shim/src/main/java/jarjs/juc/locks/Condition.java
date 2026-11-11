package jarjs.juc.locks;

public interface Condition {
    void await() throws InterruptedException;
    void awaitUninterruptibly();
    long awaitNanos(long nanosTimeout) throws InterruptedException;
    boolean await(long time, jarjs.juc.TimeUnit unit) throws InterruptedException;
    void signal();
    void signalAll();
}
