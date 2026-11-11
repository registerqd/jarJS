package jarjs.juc;

import java.util.concurrent.Callable;

public interface ExecutorService {
    void execute(Runnable command);
    <T> Future<T> submit(Callable<T> task);
    Future<?> submit(Runnable task);
    void shutdown();
    java.util.List<Runnable> shutdownNow();
    boolean isShutdown();
    boolean isTerminated();
    boolean awaitTermination(long timeout, jarjs.juc.TimeUnit unit) throws InterruptedException;
}
