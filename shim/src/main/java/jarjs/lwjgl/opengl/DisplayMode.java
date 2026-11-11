package jarjs.lwjgl.opengl;

/** org.lwjgl.opengl.DisplayMode 的兼容层。 */
public class DisplayMode {
    private final int width;
    private final int height;
    private final int frequency;
    private final int bpp;

    public DisplayMode(int width, int height) {
        this(width, height, 60, 32);
    }

    public DisplayMode(int width, int height, int frequency, int bpp) {
        this.width = width;
        this.height = height;
        this.frequency = frequency;
        this.bpp = bpp;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getFrequency() { return frequency; }
    public int getBitsPerPixel() { return bpp; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DisplayMode)) return false;
        DisplayMode d = (DisplayMode) o;
        return width == d.width && height == d.height && frequency == d.frequency && bpp == d.bpp;
    }

    @Override
    public int hashCode() { return width * 31 + height; }

    @Override
    public String toString() { return width + "x" + height + "x" + bpp + "@" + frequency + "Hz"; }
}
