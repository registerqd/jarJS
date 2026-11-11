package jarjs.awt.image;

/**
 * java.awt.image.BufferedImage 兼容层。
 * 支持游戏常用的贴图像素读取、切片与类型定义。
 */
public class BufferedImage {

    public static final int TYPE_CUSTOM = 0;
    public static final int TYPE_INT_RGB = 1;
    public static final int TYPE_INT_ARGB = 2;
    public static final int TYPE_INT_ARGB_PRE = 3;
    public static final int TYPE_INT_BGR = 4;
    public static final int TYPE_3BYTE_BGR = 5;
    public static final int TYPE_4BYTE_ABGR = 6;
    public static final int TYPE_BYTE_GRAY = 10;

    private final int width;
    private final int height;
    private final int imageType;
    private final int[] pixels;

    public BufferedImage(int width, int height, int imageType) {
        this.width = width;
        this.height = height;
        this.imageType = imageType;
        this.pixels = new int[width * height];
    }

    public BufferedImage(int width, int height, int imageType, int[] pixels) {
        this.width = width;
        this.height = height;
        this.imageType = imageType;
        this.pixels = (pixels != null && pixels.length == width * height) ? pixels : new int[width * height];
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getType() { return imageType; }
    public int[] getRawPixels() { return pixels; }

    public int getRGB(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return 0;
        return pixels[y * width + x];
    }

    public void setRGB(int x, int y, int rgb) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            pixels[y * width + x] = rgb;
        }
    }

    public int[] getRGB(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize) {
        if (rgbArray == null) rgbArray = new int[offset + h * scansize];
        int yOff = offset;
        for (int y = startY; y < startY + h; y++) {
            int xOff = yOff;
            for (int x = startX; x < startX + w; x++) {
                if (x >= 0 && x < width && y >= 0 && y < height) {
                    rgbArray[xOff] = pixels[y * width + x];
                } else {
                    rgbArray[xOff] = 0;
                }
                xOff++;
            }
            yOff += scansize;
        }
        return rgbArray;
    }

    public void setRGB(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize) {
        int yOff = offset;
        for (int y = startY; y < startY + h; y++) {
            int xOff = yOff;
            for (int x = startX; x < startX + w; x++) {
                if (x >= 0 && x < width && y >= 0 && y < height) {
                    pixels[y * width + x] = rgbArray[xOff];
                }
                xOff++;
            }
            yOff += scansize;
        }
    }

    public BufferedImage getSubimage(int x, int y, int w, int h) {
        BufferedImage sub = new BufferedImage(w, h, imageType);
        for (int sy = 0; sy < h; sy++) {
            for (int sx = 0; sx < w; sx++) {
                sub.setRGB(sx, sy, getRGB(x + sx, y + sy));
            }
        }
        return sub;
    }
}
