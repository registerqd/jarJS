package jarjs.font;

import jarjs.lwjgl.opengl.GL;
import jarjs.lwjgl.opengl.GL11;
import jarjs.lwjgl.opengl.GLBackend;
import jarjs.media.MediaBackend;

/**
 * FreeType 的浏览器替代实现 —— 用 Canvas2D 把文字光栅化成位图。
 *
 * 典型用法：
 * <pre>
 *   CanvasFont.Glyph g = CanvasFont.render("Hello", "sans-serif", 32, false, false);
 *   int tex = CanvasFont.uploadTexture(g);
 *   // 然后用 tex 画一个带纹理的四边形
 * </pre>
 */
public final class CanvasFont {

    private CanvasFont() {}

    /** 一段文字的光栅化结果（同时保留原始参数，便于直接上传纹理）。 */
    public static final class Glyph {
        public final int width;
        public final int height;
        public final int advance;
        public final int baseline;
        public final int[] pixels;   // ARGB，白色字形 + alpha

        public final String text;
        public final String family;
        public final float size;
        public final boolean bold;
        public final boolean italic;

        public Glyph(int width, int height, int advance, int baseline, int[] pixels,
                     String text, String family, float size, boolean bold, boolean italic) {
            this.width = width;
            this.height = height;
            this.advance = advance;
            this.baseline = baseline;
            this.pixels = pixels;
            this.text = text;
            this.family = family;
            this.size = size;
            this.bold = bold;
            this.italic = italic;
        }
    }

    /** 把文字光栅化成位图。family 支持 "sans-serif" / "serif" / "monospace" 或系统字体名。 */
    public static Glyph render(String text, String family, float size, boolean bold, boolean italic) {
        int[] meta = MediaBackend.textMetrics(text, family, size, bold, italic);
        if (meta == null || meta.length < 4) {
            return new Glyph(1, 1, 1, 1, new int[]{0}, text, family, size, bold, italic);
        }
        int[] rgba = MediaBackend.textPixels(text, family, size, bold, italic);
        int n = meta[0] * meta[1];
        int[] argb = new int[n];
        if (rgba != null && rgba.length >= n * 4) {
            for (int i = 0; i < n; i++) {
                int a = rgba[i * 4 + 3] & 0xFF;
                argb[i] = (a << 24) | 0xFFFFFF;   // 白色字形，靠 alpha 成形
            }
        }
        return new Glyph(meta[0], meta[1], meta[2], meta[3], argb, text, family, size, bold, italic);
    }

    /** 只测量宽度（像素）。 */
    public static int measure(String text, String family, float size, boolean bold, boolean italic) {
        return MediaBackend.measureText(text, family, size, bold, italic);
    }

    /**
     * 把字形上传成 OpenGL 纹理，返回纹理 ID。
     * 走 Canvas 直接上传（texImage2D 接受 canvas），不经过大数组跨语言传输。
     */
    public static int uploadTexture(Glyph g) {
        int tex = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex);

        int[] wh = GLBackend.texImageText(tex, g.text, g.family, g.size, g.bold, g.italic);
        int w = (wh != null && wh.length >= 2) ? wh[0] : g.width;
        int h = (wh != null && wh.length >= 2) ? wh[1] : g.height;

        GL.registerTexture(tex, w, h);
        return tex;
    }
}
