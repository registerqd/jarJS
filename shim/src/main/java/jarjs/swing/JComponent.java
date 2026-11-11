package jarjs.swing;

import jarjs.awt.Color;
import jarjs.awt.Component;
import jarjs.awt.Font;
import jarjs.awt.Graphics;
import jarjs.awt.event.KeyListener;

public class JComponent extends Component {
    protected Color background;
    protected Color foreground = Color.BLACK;
    protected Font font = new Font("sans-serif", Font.PLAIN, 14);
    protected boolean focusable = false;

    public void setBackground(Color c) { background = c; }
    public Color getBackground() { return background; }
    public void setForeground(Color c) { foreground = c; }
    public Color getForeground() { return foreground; }
    public void setFont(Font f) { font = f; }
    public Font getFont() { return font; }

    @Override
    public void setFocusable(boolean b) { focusable = b; }
    @Override
    public boolean isFocusable() { return focusable; }

    protected final java.util.List<KeyListener> keyListeners = new java.util.ArrayList<>();

    public void addKeyListener(KeyListener l) { if (l != null) keyListeners.add(l); }

    public java.util.List<KeyListener> getKeyListeners() { return keyListeners; }

    public void paint(Graphics g) {}

    @Override
    public void repaint() { JFrame.requestRepaint(); }
}
