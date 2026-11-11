package jarjs.image;

import jarjs.media.MediaBackend;

/**
 * 图像解码：把 base64 内嵌的 PNG/JPG 解码成 ARGB 像素，可直接上传纹理。
 *
 * 典型用法：
 * <pre>
 *   ImageLoader.load("assets/logo.png");
 *   while (!ImageLoader.isReady("assets/logo.png")) { Display.update(); }
 *   int tex = ImageLoader.uploadTexture("assets/logo.png");
 * </pre>
 */
public final class ImageLoader {

    private ImageLoader() {}

    /** 触发异步解码（资源必须已在构建时内嵌）。 */
    public static void load(String path) {
        String dataUrl = MediaBackend.assetDataUrl(path);
        if (dataUrl == null) {
            MediaBackend.warn("资源未内嵌: " + path);
            return;
        }
        MediaBackend.loadImage(path, dataUrl);
    }

    public static boolean exists(String path) {
        return MediaBackend.assetExists(path);
    }

    public static boolean isReady(String path) {
        return MediaBackend.isImageReady(path) != 0;
    }

    public static int getWidth(String path) { return MediaBackend.getImageWidth(path); }
    public static int getHeight(String path) { return MediaBackend.getImageHeight(path); }
    public static int[] getPixels(String path) { return MediaBackend.getImagePixels(path); }

    /** 把已解码的图像上传成纹理，返回纹理 ID。 */
    public static int uploadTexture(String path) {
        int w = getWidth(path);
        int h = getHeight(path);
        int[] px = getPixels(path);
        if (w <= 0 || h <= 0 || px == null) return 0;

        int tex = jarjs.lwjgl.opengl.GL11.glGenTextures();
        jarjs.lwjgl.opengl.GL11.glBindTexture(jarjs.lwjgl.opengl.GL11.GL_TEXTURE_2D, tex);
        jarjs.lwjgl.opengl.GL11.glTexParameteri(jarjs.lwjgl.opengl.GL11.GL_TEXTURE_2D,
                jarjs.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER, jarjs.lwjgl.opengl.GL11.GL_LINEAR);
        jarjs.lwjgl.opengl.GL11.glTexParameteri(jarjs.lwjgl.opengl.GL11.GL_TEXTURE_2D,
                jarjs.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER, jarjs.lwjgl.opengl.GL11.GL_LINEAR);
        jarjs.lwjgl.opengl.GL11.glTexImage2D(jarjs.lwjgl.opengl.GL11.GL_TEXTURE_2D, 0,
                jarjs.lwjgl.opengl.GL11.GL_RGBA, w, h, 0,
                jarjs.lwjgl.opengl.GL11.GL_RGBA, jarjs.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE, px);
        return tex;
    }
}
