package demo;

import jarjs.assets.Assets;
import jarjs.font.CanvasFont;
import jarjs.image.ImageLoader;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.util.WaveData;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

/**
 * 多媒体能力演示：base64 内嵌资源 + 图像解码 + OpenAL 音频 + Canvas 字体光栅化。
 * 全部走浏览器原生 API，不需要任何 native 库。
 */
public class MediaDemo {

    private static final int W = 700;
    private static final int H = 520;

    private static int logoTex = 0;
    private static int textTex = 0;
    private static int textW = 0, textH = 0;
    private static int soundBuf = 0;
    private static int soundSrc = 0;
    private static boolean soundReady = false;
    private static int playCount = 0;
    private static long lastBeep = 0;

    public static void main(String[] args) throws LWJGLException {
        Display.setDisplayMode(new DisplayMode(W, H));
        Display.setTitle("JARJS · 多媒体演示（图像 / 音频 / 字体）");
        Display.create();

        setupGL();
        loadAssets();
        setupAudio();
        buildText();

        while (!Display.isCloseRequested()) {
            render();
            Display.update();
        }
        Display.destroy();
    }

    private static void setupGL() {
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glClearColor(0.06f, 0.08f, 0.12f, 1f);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0, W, H, 0, -1, 1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
    }

    private static void loadAssets() {
        System.out.println("[MediaDemo] 资源表: " + Assets.list().length + " 个文件");
        ImageLoader.load("assets/logo.png");

        // 等待浏览器异步解码完成
        int guard = 0;
        while (!ImageLoader.isReady("assets/logo.png") && guard++ < 300) {
            Display.update();
        }
        if (ImageLoader.isReady("assets/logo.png")) {
            logoTex = ImageLoader.uploadTexture("assets/logo.png");
            System.out.println("[MediaDemo] 图像已解码: "
                    + ImageLoader.getWidth("assets/logo.png") + "x"
                    + ImageLoader.getHeight("assets/logo.png"));
        } else {
            System.out.println("[MediaDemo] 图像解码超时");
        }
    }

    private static void setupAudio() {
        try {
            AL.create();
        } catch (Exception e) {
            System.out.println("[MediaDemo] AL.create 失败: " + e);
        }

        byte[] wav = Assets.getBytes("assets/beep.wav");
        if (wav == null) {
            System.out.println("[MediaDemo] 找不到 assets/beep.wav");
            return;
        }

        // 纯 Java 解析 RIFF/WAVE 头，不依赖任何 native 或流实现
        int channels = 1, rate = 22050, bits = 16, dataOff = -1, dataLen = 0;
        int p = 12;
        while (p + 8 <= wav.length) {
            String id = "" + (char) wav[p] + (char) wav[p + 1] + (char) wav[p + 2] + (char) wav[p + 3];
            int size = (wav[p + 4] & 0xFF) | ((wav[p + 5] & 0xFF) << 8)
                     | ((wav[p + 6] & 0xFF) << 16) | ((wav[p + 7] & 0xFF) << 24);
            if ("fmt ".equals(id)) {
                channels = (wav[p + 10] & 0xFF) | ((wav[p + 11] & 0xFF) << 8);
                rate = (wav[p + 12] & 0xFF) | ((wav[p + 13] & 0xFF) << 8)
                     | ((wav[p + 14] & 0xFF) << 16) | ((wav[p + 15] & 0xFF) << 24);
                bits = (wav[p + 22] & 0xFF) | ((wav[p + 23] & 0xFF) << 8);
            } else if ("data".equals(id)) {
                dataOff = p + 8;
                dataLen = Math.min(size, wav.length - dataOff);
                break;
            }
            p += 8 + size + (size & 1);
        }
        if (dataOff < 0 || dataLen <= 0) {
            System.out.println("[MediaDemo] WAV 数据段缺失");
            return;
        }

        int format;
        if (channels == 1) format = (bits == 8) ? AL10.AL_FORMAT_MONO8 : AL10.AL_FORMAT_MONO16;
        else format = (bits == 8) ? AL10.AL_FORMAT_STEREO8 : AL10.AL_FORMAT_STEREO16;

        java.nio.ByteBuffer pcm = org.lwjgl.BufferUtils.createByteBuffer(dataLen);
        pcm.put(wav, dataOff, dataLen);
        pcm.flip();

        soundBuf = AL10.alGenBuffers();
        AL10.alBufferData(soundBuf, format, pcm, rate);
        soundSrc = AL10.alGenSources();
        AL10.alSourcei(soundSrc, AL10.AL_BUFFER, soundBuf);
        AL10.alSourcef(soundSrc, AL10.AL_GAIN, 0.55f);
        soundReady = true;
        System.out.println("[MediaDemo] 音频就绪: " + rate + "Hz, format=" + format + ", pcm=" + dataLen + "B");
    }

    private static void buildText() {
        CanvasFont.Glyph g = CanvasFont.render("JARJS Media OK", "sans-serif", 34, true, false);
        textTex = CanvasFont.uploadTexture(g);
        textW = g.width;
        textH = g.height;
    }

    private static void render() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

        // 首次进入自动播放一次提示音
        long now = System.currentTimeMillis();
        if (soundReady && playCount == 0 && now - lastBeep > 600) {
            AL10.alSourcePlay(soundSrc);
            playCount++;
            lastBeep = now;
        }

        // 背景网格
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(0.10f, 0.13f, 0.18f, 1f);
        for (int i = 0; i < 12; i++) {
            float y = 40 + i * 38;
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex2f(0, y);
            GL11.glVertex2f(W, y);
            GL11.glVertex2f(W, y + 1);
            GL11.glVertex2f(0, y + 1);
            GL11.glEnd();
        }

        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // 图片
        if (logoTex != 0) {
            float iw = 192, ih = 192;
            float x = (W - iw) / 2f, y = 90;
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, logoTex);
            GL11.glColor4f(1f, 1f, 1f, 1f);
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glTexCoord2f(0, 0); GL11.glVertex2f(x, y);
            GL11.glTexCoord2f(1, 0); GL11.glVertex2f(x + iw, y);
            GL11.glTexCoord2f(1, 1); GL11.glVertex2f(x + iw, y + ih);
            GL11.glTexCoord2f(0, 1); GL11.glVertex2f(x, y + ih);
            GL11.glEnd();
        }

        // 文字
        if (textTex != 0) {
            float scale = 1.0f;
            float tw = textW * scale, th = textH * scale;
            float x = (W - tw) / 2f, y = 330;
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textTex);
            GL11.glColor4f(0.45f, 0.95f, 0.85f, 1f);
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glTexCoord2f(0, 1); GL11.glVertex2f(x, y);
            GL11.glTexCoord2f(1, 1); GL11.glVertex2f(x + tw, y);
            GL11.glTexCoord2f(1, 0); GL11.glVertex2f(x + tw, y + th);
            GL11.glTexCoord2f(0, 0); GL11.glVertex2f(x, y + th);
            GL11.glEnd();
        }

        // 状态条
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(0.85f, 0.55f, 0.25f, 1f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(0, H - 6);
        GL11.glVertex2f(W, H - 6);
        GL11.glVertex2f(W, H);
        GL11.glVertex2f(0, H);
        GL11.glEnd();
    }
}
