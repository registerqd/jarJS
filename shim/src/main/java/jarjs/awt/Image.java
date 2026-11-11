package jarjs.awt;

import jarjs.awt.image.BufferedImage;

/**
 * java.awt.Image 兼容层。
 * v0.5 的 Graphics.drawImage 引用了它却没有实体类，导致 shim 编译不闭合，这里补齐。
 */
public abstract class Image {

    public static final int SCALE_DEFAULT = 1;
    public static final int SCALE_FAST = 2;
    public static final int SCALE_SMOOTH = 4;

    protected int width = 0;
    protected int height = 0;

    public int getWidth(Object observer) { return width; }
    public int getHeight(Object observer) { return height; }
    public Object getSource() { return null; }
    public void flush() {}
    public Image getScaledInstance(int w, int h, int hints) { return this; }

    /** 由 BufferedImage 转成 Image。 */
    public static Image fromBufferedImage(BufferedImage img) {
        return new BufferedImageImage(img);
    }

    static final class BufferedImageImage extends Image {
        final BufferedImage image;

        BufferedImageImage(BufferedImage image) {
            this.image = image;
            this.width = image == null ? 0 : image.getWidth();
            this.height = image == null ? 0 : image.getHeight();
        }
    }
}
