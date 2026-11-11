package jarjs.juc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * JS 单线程模型下，所有线程池/虚拟线程执行器都降级为「立即执行的微任务队列」。
 * 语义与 JVM 虚拟线程一致：提交即调度，没有物理线程争抢。
 */
public final class Executors {
    private Executors() {}

    public static ExecutorService newVirtualThreadPerTaskExecutor() { return new DirectExecutorService(); }
    public static ExecutorService newSingleThreadExecutor() { return new DirectExecutorService(); }
    public static ExecutorService newFixedThreadPool(int n) { return new DirectExecutorService(); }
    public static ExecutorService newCachedThreadPool() { return new DirectExecutorService(); }
    public static ExecutorService newWorkStealingPool() { return new DirectExecutorService(); }
    public static ExecutorService newWorkStealingPool(int parallelism) { return new DirectExecutorService(); }

    public static final class DirectExecutorService implements ExecutorService {
        private volatile boolean shutdown;

        public void execute(Runnable command) {
            if (command == null) throw new NullPointerException();
            if (shutdown) throw new IllegalStateException("执行器已关闭");
            command.run();
        }

        public <T> Future<T> submit(Callable<T> task) {
            T value = null;
            Throwable error = null;
            try { value = task.call(); } catch (Throwable t) { error = t; }
            return new CompletedFuture<>(value, error);
        }

        public Future<?> submit(Runnable task) {
            Throwable error = null;
            try { task.run(); } catch (Throwable t) { error = t; }
            return new CompletedFuture<>(null, error);
        }

        public void shutdown() { shutdown = true; }
        public List<Runnable> shutdownNow() { shutdown = true; return new ArrayList<>(); }
        public boolean isShutdown() { return shutdown; }
        public boolean isTerminated() { return shutdown; }
        public boolean awaitTermination(long timeout, TimeUnit unit) { return true; }
    }

    private static final class CompletedFuture<T> implements Future<T> {
        private final T value;
        private final Throwable error;
        private boolean cancelled;

        CompletedFuture(T value, Throwable error) { this.value = value; this.error = error; }

        public boolean cancel(boolean mayInterruptIfRunning) { cancelled = true; return true; }
        public boolean isCancelled() { return cancelled; }
        public boolean isDone() { return true; }

        public T get() throws InterruptedException {
            if (error != null) throw new RuntimeException(error);
            return value;
        }

        public T get(long timeout, TimeUnit unit) throws InterruptedException { return get(); }
    }
}
