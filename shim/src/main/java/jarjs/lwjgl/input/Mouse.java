package jarjs.lwjgl.input;

import org.teavm.jso.JSBody;

/** org.lwjgl.input.Mouse 的兼容层。 */
public final class Mouse {

    public static final int BUTTON_LEFT = 0;
    public static final int BUTTON_RIGHT = 1;
    public static final int BUTTON_MIDDLE = 2;

    private static int eventX, eventY, eventDX, eventDY, eventButton = -1;
    private static boolean eventState = false;

    private Mouse() {}

    @JSBody(params = {"b"}, script = "return window.JARJS_GL ? !!window.JARJS_GL.isButtonDown(b) : false;")
    private static native boolean jsIsButtonDown(int b);

    @JSBody(params = {}, script = "return window.JARJS_GL ? window.JARJS_GL.getMouseX() : 0;")
    private static native int jsX();

    @JSBody(params = {}, script = "return window.JARJS_GL ? window.JARJS_GL.getMouseY() : 0;")
    private static native int jsY();

    @JSBody(params = {}, script = "return window.JARJS_GL ? window.JARJS_GL.getMouseDX() : 0;")
    private static native int jsDX();

    @JSBody(params = {}, script = "return window.JARJS_GL ? window.JARJS_GL.getMouseDY() : 0;")
    private static native int jsDY();

    @JSBody(params = {}, script = "return window.JARJS_GL ? window.JARJS_GL.nextMouseEvent() : null;")
    private static native String jsNextMouseEvent();

    @JSBody(params = {"g"}, script = "window.JARJS_GL && window.JARJS_GL.setGrabbed(g);")
    private static native void jsSetGrabbed(boolean g);

    @JSBody(params = {"x", "y"}, script = "window.JARJS_GL && window.JARJS_GL.setCursorPosition(x, y);")
    private static native void jsSetCursorPosition(int x, int y);

    public static void create() throws jarjs.lwjgl.LWJGLException {}
    public static void destroy() {}
    public static boolean isCreated() { return true; }
    public static void poll() {}
    public static int getButtonCount() { return 3; }

    public static boolean next() {
        String s = jsNextMouseEvent();
        if (s == null || s.isEmpty()) return false;
        String[] p = s.split(":");
        if (p.length < 5) return false;
        try {
            eventX = Integer.parseInt(p[0]);
            eventY = Integer.parseInt(p[1]);
            eventButton = Integer.parseInt(p[2]);
            eventState = "1".equals(p[3]);
            eventDX = Integer.parseInt(p[4]);
            eventDY = p.length > 5 ? Integer.parseInt(p[5]) : 0;
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static int getEventX() { return eventX; }
    public static int getEventY() { return eventY; }
    public static int getEventDX() { return eventDX; }
    public static int getEventDY() { return eventDY; }
    public static int getEventButton() { return eventButton; }
    public static boolean getEventButtonState() { return eventState; }
    public static long getEventNanoseconds() { return System.nanoTime(); }
    public static boolean isButtonDown(int button) { return jsIsButtonDown(button); }
    public static int getX() { return jsX(); }
    public static int getY() { return jsY(); }
    public static int getDX() { return jsDX(); }
    public static int getDY() { return jsDY(); }
    public static int getDWheel() { return 0; }
    public static void setGrabbed(boolean g) { jsSetGrabbed(g); }
    public static boolean isGrabbed() { return false; }
    public static void setCursorPosition(int x, int y) { jsSetCursorPosition(x, y); }
    public static boolean hasWheel() { return true; }
    public static int getNativeCursorCapabilities() { return 0; }

    // ================= v0.6 补齐 =================

    public static boolean isInsideWindow() { return true; }
    public static int getEventDWheel() { return 0; }
    public static long getEventNanosecondsPublic() { return System.nanoTime(); }
    public static void setNativeCursor(Object cursor) {}
    public static Object setNativeCursor(Object cursor, int hotSpotX, int hotSpotY) { return null; }
}
