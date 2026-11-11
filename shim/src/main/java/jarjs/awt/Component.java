package jarjs.awt;

public class Component {
    protected int width = 100;
    protected int height = 100;
    protected int x = 0;
    protected int y = 0;
    protected boolean visible = true;

    public void setSize(int w, int h) { width = w; height = h; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public void setLocation(int x, int y) { this.x = x; this.y = y; }
    public int getX() { return x; }
    public int getY() { return y; }
    public void setVisible(boolean b) { visible = b; }
    public boolean isVisible() { return visible; }
    public void setFocusable(boolean b) {}
    public boolean isFocusable() { return false; }
    public void requestFocus() {}
    public void repaint() {}
}
