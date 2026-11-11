package jarjs.lwjgl.opengl;

import jarjs.lwjgl.opengl.GL;
import jarjs.lwjgl.opengl.GLBackend;
import org.teavm.jso.JSBody;
import org.teavm.jso.browser.Window;

/**
 * org.lwjgl.opengl.Display 的兼容层。
 * 帧循环语义：update() 提交当前帧并让出到浏览器事件循环（Thread.sleep），
 * 浏览器在让出期间合成画面，因此 while(!isCloseRequested()){...update();} 的写法可以直接跑。
 */
public final class Display {

    private static DisplayMode mode = new DisplayMode(480, 480);
    private static boolean created = false;
    private static boolean closeRequested = false;
    private static boolean fullscreen = false;
    private static boolean vsync = true;
    private static String title = "JARJS";
    private static long lastFrame = 0;
    private static long startTime = 0;

    private Display() {}

    @JSBody(params = {}, script = "return window.JARJS_GL ? window.JARJS_GL.closeRequested() : false;")
    private static native boolean jsCloseRequested();

    @JSBody(params = {"w", "h", "t"}, script = "window.JARJS_GL.setSize(w, h, t);")
    private static native void jsSetSize(int w, int h, String title);

    @JSBody(params = {"v"}, script = "window.JARJS_GL.setVSync(v);")
    private static native void jsSetVSync(boolean v);

    public static void setDisplayMode(DisplayMode m) { mode = m; }
    public static DisplayMode getDisplayMode() { return mode; }
    public static DisplayMode[] getAvailableDisplayModes() { return new DisplayMode[]{mode}; }
    public static void setTitle(String t) {
        title = t == null ? "" : t;
        if (created) jsSetSize(mode.getWidth(), mode.getHeight(), title);
    }
    public static String getTitle() { return title; }

    public static void setFullscreen(boolean f) { fullscreen = f; }
    public static boolean isFullscreen() { return fullscreen; }
    public static void setDisplayModeAndFullscreen(DisplayMode m) { mode = m; }

    public static void create() throws jarjs.lwjgl.LWJGLException { create(new PixelFormat()); }

    public static void create(PixelFormat format) throws jarjs.lwjgl.LWJGLException {
        if (created) return;
        if (!GL.init(mode.getWidth(), mode.getHeight())) {
            throw new jarjs.lwjgl.LWJGLException("无法创建 WebGL 上下文（canvas#game 缺失或已被 2D 上下文占用）");
        }
        created = true;
        closeRequested = false;
        startTime = System.nanoTime();
        lastFrame = startTime;
        jsSetSize(mode.getWidth(), mode.getHeight(), title);
        jsSetVSync(vsync);
        Window.current().getDocument().setTitle(title);
        GL.viewport(0, 0, mode.getWidth(), mode.getHeight());
    }

    public static boolean isCreated() { return created; }

    public static boolean isCloseRequested() {
        if (jsCloseRequested()) closeRequested = true;
        return closeRequested;
    }

    public static void setCloseRequested(boolean b) { closeRequested = b; }

    public static void update() {
        update(true);
    }

    public static void update(boolean processMessages) {
        if (!created) return;
        long now = System.nanoTime();
        long elapsed = (now - lastFrame) / 1000000L;
        lastFrame = now;
        // 目标 60 FPS：把剩余时间让给浏览器合成
        long sleep = 16 - elapsed;
        if (sleep > 0) {
            try { Thread.sleep(sleep); } catch (Throwable ignored) {}
        } else {
            try { Thread.sleep(0); } catch (Throwable ignored) {}
        }
    }

    /** 限帧到指定 FPS。 */
    public static void sync(int fps) {
        if (fps <= 0) return;
        long now = System.nanoTime();
        long target = 1000000000L / fps;
        long elapsed = now - lastFrame;
        if (elapsed < target) {
            long ms = (target - elapsed) / 1000000L;
            if (ms > 0) {
                try { Thread.sleep(ms); } catch (Throwable ignored) {}
            }
        }
        lastFrame = System.nanoTime();
    }

    public static void swapBuffers() { update(); }

    public static void makeCurrent() throws jarjs.lwjgl.LWJGLException {}
    public static void releaseContext() {}
    public static boolean isCurrent() { return true; }
    public static void processMessages() {}
    public static void setVSyncEnabled(boolean v) { vsync = v; jsSetVSync(v); }
    public static void setSwapInterval(int i) { vsync = i != 0; jsSetVSync(vsync); }
    public static void setLocation(int x, int y) {}
    public static void setResizable(boolean b) {}
    public static void setIcon(byte[]... icons) {}
    public static boolean isActive() { return created; }
    public static boolean isVisible() { return created; }
    public static void setVisible(boolean b) {}

    public static void destroy() {
        created = false;
        closeRequested = true;
    }

    public static int getWidth() { return mode.getWidth(); }
    public static int getHeight() { return mode.getHeight(); }
    public static int getX() { return 0; }
    public static int getY() { return 0; }

    public static long getStartTime() { return startTime; }
    public static float getPixelScaleFactor() { return 1f; }

    // ================= v0.6 补齐 =================

    public static DisplayMode getDesktopDisplayMode() { return mode; }
    public static void setIcon(java.nio.ByteBuffer[] icons) {}
    public static void setIcon(java.nio.ByteBuffer icon) {}
    public static boolean wasResized() { return false; }
    public static boolean isInsideWindow() { return true; }
    public static void setLocation(int x, int y, int width, int height) {}
    public static boolean isFocused() { return true; }
    public static boolean isBorderless() { return false; }
    public static void setBorderless(boolean b) {}
    public static int getDesktopX() { return 0; }
    public static int getDesktopY() { return 0; }
    public static int getDesktopWidth() { return mode.getWidth(); }
    public static int getDesktopHeight() { return mode.getHeight(); }
}
