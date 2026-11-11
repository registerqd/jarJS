package jarjs.swing;

import jarjs.awt.Graphics;
import jarjs.awt.event.KeyEvent;
import jarjs.awt.event.KeyListener;
import org.teavm.jso.browser.Window;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.events.KeyboardEvent;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLDocument;

import java.util.ArrayList;
import java.util.List;

public class JFrame extends JPanel {
    private static JFrame active;

    private String title = "";
    private final List<JComponent> children = new ArrayList<>();
    private final List<KeyListener> keyListeners = new ArrayList<>();
    private HTMLCanvasElement canvas;
    private CanvasRenderingContext2D ctx;
    private boolean started = false;

    public JFrame() { active = this; }

    public void setTitle(String t) { title = t == null ? "" : t; }
    public String getTitle() { return title; }

    public void setSize(int w, int h) {
        super.setSize(w, h);
    }

    public void setDefaultCloseOperation(int op) {}
    public void setLocationRelativeTo(Object c) {}
    public void setResizable(boolean b) {}
    public void pack() {}
    public JComponent getContentPane() { return this; }
    public void setLayout(Object layout) {}

    public void add(JComponent c) { if (c != null) children.add(c); }
    public jarjs.awt.Component add(jarjs.awt.Component c) {
        if (c instanceof JComponent) children.add((JComponent) c);
        return c;
    }
    public void add(JComponent c, Object constraints) { if (c != null) children.add(c); }
    public void add(String name, JComponent c) { if (c != null) children.add(c); }

    @Override
    public void addKeyListener(KeyListener l) { if (l != null) keyListeners.add(l); }

    public void setVisible(boolean v) {
        if (v) show();
    }

    private void show() {
        if (started) return;
        started = true;
        HTMLDocument doc = HTMLDocument.current();
        org.teavm.jso.dom.html.HTMLElement el =
                (org.teavm.jso.dom.html.HTMLElement) doc.getElementById("game");
        if (el == null) {
            System.err.println("[JARJS] 页面里找不到 id=game 的 canvas");
            return;
        }
        canvas = (HTMLCanvasElement) el;
        double dpr = Math.min(Window.current().getDevicePixelRatio(), 2);
        if (dpr <= 0) dpr = 1;
        canvas.setWidth((int) (width * dpr));
        canvas.setHeight((int) (height * dpr));
        canvas.getStyle().setProperty("width", width + "px");
        canvas.getStyle().setProperty("height", height + "px");
        ctx = (CanvasRenderingContext2D) canvas.getContext("2d");
        ctx.setTransform(dpr, 0, 0, dpr, 0, 0);

        if (!title.isEmpty()) doc.setTitle(title);

        Window.current().addEventListener("keydown", (EventListener<KeyboardEvent>) e -> {
            int code = e.getKeyCode();
            if (code == 32 || (code >= 37 && code <= 40)) e.preventDefault();
            dispatch(new KeyEvent(code, e.getKey()), true);
        });
        Window.current().addEventListener("keyup", (EventListener<KeyboardEvent>) e -> {
            dispatch(new KeyEvent(e.getKeyCode(), e.getKey()), false);
        });

        Window.setInterval(JFrame::renderAll, 16);
        renderAll();
    }

    public static void requestRepaint() { renderAll(); }

    private void dispatch(KeyEvent ke, boolean pressed) {
        for (KeyListener l : keyListeners) {
            try { if (pressed) l.keyPressed(ke); else l.keyReleased(ke); }
            catch (Throwable t) { t.printStackTrace(); }
        }
        for (JComponent c : children) {
            for (KeyListener l : c.getKeyListeners()) {
                try { if (pressed) l.keyPressed(ke); else l.keyReleased(ke); }
                catch (Throwable t) { t.printStackTrace(); }
            }
        }
    }

    private static void renderAll() {
        JFrame f = active;
        if (f == null || f.ctx == null) return;
        f.ctx.setFillStyle("#0b0f14");
        f.ctx.fillRect(0, 0, f.getWidth(), f.getHeight());
        Graphics g = new Graphics(f.ctx);
        try { f.paint(g); } catch (Throwable t) { t.printStackTrace(); }
        for (JComponent c : f.children) {
            if (c == null || !c.isVisible()) continue;
            try { c.paint(g); } catch (Throwable t) { t.printStackTrace(); }
        }
    }
}
