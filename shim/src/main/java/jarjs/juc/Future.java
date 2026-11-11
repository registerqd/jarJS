package jarjs.juc;

public interface Future<T> {
    boolean cancel(boolean mayInterruptIfRunning);
    boolean isCancelled();
    boolean isDone();
    T get() throws InterruptedException;
    T get(long timeout, TimeUnit unit) throws InterruptedException;
}
