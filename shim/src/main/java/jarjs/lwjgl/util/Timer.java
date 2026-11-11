package jarjs.lwjgl.util;

/** org.lwjgl.util.Timer 的兼容层。 */
public class Timer {

    private static long lastTime;
    private static long resolution = 1000000L;
    private static boolean initialized = false;

    private long start;
    private long stop;
    private boolean running;

    public Timer() { reset(); }

    public static void tick() {
        lastTime = System.nanoTime();
        initialized = true;
    }

    public static float getTime() {
        if (!initialized) tick();
        return (System.nanoTime() - lastTime) / 1.0e9f;
    }

    public static float getResolution() {
        return resolution / 1.0e9f;
    }

    public void reset() {
        start = System.nanoTime();
        stop = 0;
        running = true;
    }

    public float getTimeLocal() {
        long now = running ? System.nanoTime() : stop;
        return (now - start) / 1.0e9f;
    }

    public void pause() {
        if (running) { stop = System.nanoTime(); running = false; }
    }

    public void resume() {
        if (!running) {
            start += System.nanoTime() - stop;
            running = true;
        }
    }

    public void set(float newTime) {
        long now = System.nanoTime();
        start = now - (long) (newTime * 1.0e9);
    }
}
