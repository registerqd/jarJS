package jarjs.juc.locks;

/** 单线程下倒计时闸门：countDown 递减，await 立即通过。 */
public class CountDownLatch {
    private int count;

    public CountDownLatch(int count) { this.count = count; }

    public void await() throws InterruptedException { count = 0; }
    public boolean await(long timeout, jarjs.juc.TimeUnit unit) throws InterruptedException { count = 0; return true; }
    public void countDown() { if (count > 0) count--; }
    public long getCount() { return count; }
}
