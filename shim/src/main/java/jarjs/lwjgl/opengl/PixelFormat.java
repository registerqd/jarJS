package jarjs.lwjgl.opengl;

/** org.lwjgl.opengl.PixelFormat 的兼容层（参数仅记录，WebGL 侧固定 RGBA + 深度）。 */
public class PixelFormat {
    public int bpp = 32;
    public int alpha = 8;
    public int depth = 24;
    public int stencil = 0;
    public int samples = 0;
    public boolean stereo = false;

    public PixelFormat() {}

    public PixelFormat(int bpp) { this.bpp = bpp; }

    public PixelFormat(int bpp, int alpha, int depth, int stencil) {
        this.bpp = bpp; this.alpha = alpha; this.depth = depth; this.stencil = stencil;
    }

    public PixelFormat(int bpp, int alpha, int depth, int stencil, int samples) {
        this(bpp, alpha, depth, stencil);
        this.samples = samples;
    }

    public PixelFormat withDepthBits(int d) { this.depth = d; return this; }
    public PixelFormat withStencilBits(int s) { this.stencil = s; return this; }
    public PixelFormat withSamples(int s) { this.samples = s; return this; }
    public PixelFormat withAlphaBits(int a) { this.alpha = a; return this; }
}
