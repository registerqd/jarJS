package jarjs.sound.sampled;

/**
 * javax.sound.sampled.Line 兼容层。
 */
public interface Line {
    void open() throws Exception;
    void close();
    boolean isOpen();
    void start();
    void stop();
    boolean isRunning();
    boolean isActive();

    class Info {
        private final Class<?> lineClass;
        public Info(Class<?> lineClass) { this.lineClass = lineClass; }
        public Class<?> getLineClass() { return lineClass; }
        public boolean matches(Info info) { return true; }
    }
}
