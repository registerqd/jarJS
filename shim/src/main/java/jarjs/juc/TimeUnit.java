package jarjs.juc;

public enum TimeUnit {
    NANOSECONDS(1L),
    MICROSECONDS(1000L),
    MILLISECONDS(1000_000L),
    SECONDS(1000_000_000L),
    MINUTES(60_000_000_000L),
    HOURS(3_600_000_000_000L),
    DAYS(86_400_000_000_000L);

    private final long nanos;

    TimeUnit(long nanos) { this.nanos = nanos; }

    public long toNanos(long d) { return d * nanos; }
    public long toMillis(long d) { return d * nanos / 1_000_000L; }
    public long toSeconds(long d) { return d * nanos / 1_000_000_000L; }
    public void sleep(long timeout) throws InterruptedException {}
}
