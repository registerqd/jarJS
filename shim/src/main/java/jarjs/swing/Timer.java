package jarjs.swing;

import jarjs.awt.Graphics;
import jarjs.awt.event.ActionEvent;
import jarjs.awt.event.ActionListener;
import org.teavm.jso.browser.Window;

public class Timer {
    private int delay;
    private ActionListener listener;
    private int handle = -1;
    private boolean running = false;

    public Timer(int delay, ActionListener listener) {
        this.delay = delay;
        this.listener = listener;
    }

    public void setDelay(int d) { delay = d; }
    public int getDelay() { return delay; }
    public boolean isRunning() { return running; }

    public void start() {
        if (running) return;
        running = true;
        handle = Window.setInterval(() -> {
            if (listener != null) listener.actionPerformed(new ActionEvent(this));
        }, delay);
    }

    public void stop() {
        if (!running) return;
        running = false;
        if (handle >= 0) Window.clearInterval(handle);
        handle = -1;
    }

    public void restart() { stop(); start(); }
}
