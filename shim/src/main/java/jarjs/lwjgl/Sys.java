package jarjs.lwjgl;

/** org.lwjgl.Sys 的兼容层。 */
public final class Sys {

    private Sys() {}

    public static long getTime() { return System.nanoTime() / 1000L; }

    public static long getTimerResolution() { return 1000000L; }

    public static String getVersion() { return "JARJS-LWJGL 1.0 (WebGL)"; }

    public static String getClipboard() { return ""; }

    public static void openURL(String url) {}

    public static boolean openURL(String url, String logPath) { return false; }

    public static void alert(String title, String message) {}

    public static String getProperty(String key) { return null; }

    public static void log(String msg) { System.out.println("[JARJS] " + msg); }

    public static void log(Throwable t) { t.printStackTrace(); }
}
