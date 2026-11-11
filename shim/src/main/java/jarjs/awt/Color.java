package jarjs.awt;

public class Color {
    public static final Color BLACK = new Color(0, 0, 0);
    public static final Color WHITE = new Color(255, 255, 255);
    public static final Color RED = new Color(255, 0, 0);
    public static final Color GREEN = new Color(0, 200, 0);
    public static final Color BLUE = new Color(0, 90, 255);
    public static final Color YELLOW = new Color(255, 230, 0);
    public static final Color CYAN = new Color(0, 220, 220);
    public static final Color MAGENTA = new Color(230, 0, 230);
    public static final Color ORANGE = new Color(255, 140, 0);
    public static final Color GRAY = new Color(128, 128, 128);
    public static final Color DARK_GRAY = new Color(64, 64, 64);
    public static final Color LIGHT_GRAY = new Color(200, 200, 200);
    public static final Color PINK = new Color(255, 175, 175);

    private final int r, g, b, a;

    public Color(int r, int g, int b) { this(r, g, b, 255); }

    public Color(int r, int g, int b, int a) {
        this.r = clamp(r); this.g = clamp(g); this.b = clamp(b); this.a = clamp(a);
    }

    public Color(int rgb) {
        this((rgb >> 16) & 255, (rgb >> 8) & 255, rgb & 255);
    }

    private static int clamp(int v) { return v < 0 ? 0 : (v > 255 ? 255 : v); }

    public int getRed() { return r; }
    public int getGreen() { return g; }
    public int getBlue() { return b; }
    public int getAlpha() { return a; }
    public int getRGB() { return (a << 24) | (r << 16) | (g << 8) | b; }

    public String css() {
        return a == 255 ? "rgb(" + r + "," + g + "," + b + ")"
                        : "rgba(" + r + "," + g + "," + b + "," + (a / 255.0) + ")";
    }
}
