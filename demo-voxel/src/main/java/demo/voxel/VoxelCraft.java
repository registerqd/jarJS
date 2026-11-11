package demo.voxel;

import jarjs.assets.Assets;
import jarjs.font.CanvasFont;
import jarjs.image.ImageLoader;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import java.nio.ByteBuffer;

/**
 * VoxelCraft 3D 体素微型沙盒：纯 Java LWJGL 2.9.3 编写。
 * 包含第一人称漫游、体素地形、挖方块、放方块与 OpenAL 音效。
 */
public class VoxelCraft {

    private static final int W = 760;
    private static final int H = 560;

    private static World world;
    private static Player player;
    private static int atlasTex = 0;
    private static int tipTex = 0;
    private static int tipW = 0, tipH = 0;

    private static int hitSound = 0, hitSrc = 0;
    private static int placeSound = 0, placeSrc = 0;
    private static boolean audioReady = false;

    public static void main(String[] args) throws LWJGLException {
        Display.setDisplayMode(new DisplayMode(W, H));
        Display.setTitle("VoxelCraft 3D · 体素沙盒 (jarJS Showcase)");
        Display.create();

        initGL();
        initAssets();
        initAudio();

        world = new World();
        player = new Player();

        Mouse.setGrabbed(true);

        long lastNanos = System.nanoTime();

        while (!Display.isCloseRequested()) {
            long now = System.nanoTime();
            float dt = (now - lastNanos) / 1.0e9f;
            lastNanos = now;
            if (dt > 0.1f) dt = 0.1f; // 限制大步长

            handleInput();
            player.update(dt, world);

            render();
            Display.update();
        }

        cleanup();
        Display.destroy();
    }

    private static void initGL() {
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glDisable(GL11.GL_CULL_FACE); // 禁用背面剔除，确保所有表面均正确呈现
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glClearColor(0.48f, 0.72f, 0.96f, 1.0f); // 明亮的天空蔚蓝底色
    }

    private static void initAssets() {
        ImageLoader.load("assets/atlas.png");
        int guard = 0;
        while (!ImageLoader.isReady("assets/atlas.png") && guard++ < 300) {
            Display.update();
        }
        if (ImageLoader.isReady("assets/atlas.png")) {
            atlasTex = ImageLoader.uploadTexture("assets/atlas.png");
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, atlasTex);
            // 像素风格使用 NEAREST 滤波
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        }

        CanvasFont.Glyph g = CanvasFont.render("VoxelCraft 3D · WASD Move | Space Jump | L/R-Click Dig/Place | 1-6 Blocks", "sans-serif", 16, true, false);
        tipTex = CanvasFont.uploadTexture(g);
        tipW = g.width;
        tipH = g.height;
    }

    private static void initAudio() {
        try {
            AL.create();
            hitSound = loadWavSound("assets/hit.wav");
            hitSrc = AL10.alGenSources();
            AL10.alSourcei(hitSrc, AL10.AL_BUFFER, hitSound);
            AL10.alSourcef(hitSrc, AL10.AL_GAIN, 0.65f);

            placeSound = loadWavSound("assets/place.wav");
            placeSrc = AL10.alGenSources();
            AL10.alSourcei(placeSrc, AL10.AL_BUFFER, placeSound);
            AL10.alSourcef(placeSrc, AL10.AL_GAIN, 0.65f);

            audioReady = true;
        } catch (Throwable t) {
            System.out.println("[VoxelCraft] 音频初始化跳过: " + t.getMessage());
        }
    }

    private static int loadWavSound(String path) {
        byte[] wav = Assets.getBytes(path);
        if (wav == null) return 0;
        int rate = 22050, bits = 16, dataOff = -1, dataLen = 0;
        int p = 12;
        while (p + 8 <= wav.length) {
            String id = "" + (char) wav[p] + (char) wav[p + 1] + (char) wav[p + 2] + (char) wav[p + 3];
            int size = (wav[p + 4] & 0xFF) | ((wav[p + 5] & 0xFF) << 8)
                     | ((wav[p + 6] & 0xFF) << 16) | ((wav[p + 7] & 0xFF) << 24);
            if ("fmt ".equals(id)) {
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
        if (dataOff < 0 || dataLen <= 0) return 0;

        int format = (bits == 8) ? AL10.AL_FORMAT_MONO8 : AL10.AL_FORMAT_MONO16;
        ByteBuffer pcm = BufferUtils.createByteBuffer(dataLen);
        pcm.put(wav, dataOff, dataLen);
        pcm.flip();

        int buf = AL10.alGenBuffers();
        AL10.alBufferData(buf, format, pcm, rate);
        return buf;
    }

    private static void handleInput() {
        if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
            Display.destroy();
            return;
        }

        // 鼠标点击挖掘与放置方块
        while (Mouse.next()) {
            if (Mouse.getEventButtonState()) {
                int btn = Mouse.getEventButton();
                if (btn == 0 && player.hasTarget) {
                    // 左键破坏方块
                    world.setBlock(player.targetX, player.targetY, player.targetZ, Block.AIR);
                    if (audioReady) AL10.alSourcePlay(hitSrc);
                } else if (btn == 1 && player.hasTarget) {
                    // 右键放置方块（不能卡在自己身体格内）
                    int px = player.placeX, py = player.placeY, pz = player.placeZ;
                    int plX = (int) Math.floor(player.x);
                    int plY0 = (int) Math.floor(player.y);
                    int plY1 = (int) Math.floor(player.y + 1.5f);
                    int plZ = (int) Math.floor(player.z);
                    boolean insidePlayer = (px == plX && pz == plZ && (py == plY0 || py == plY1));

                    if (!insidePlayer && py >= 0 && py < World.H) {
                        world.setBlock(px, py, pz, player.selectedBlock);
                        if (audioReady) AL10.alSourcePlay(placeSrc);
                    }
                }
            }
        }
    }

    private static void render() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        // ---------- 3D 场景 ----------
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        float aspect = (float) W / Math.max(1, H);
        GLU.gluPerspective(68.0f, aspect, 0.1f, 150.0f);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        // 第一人称相机 View 矩阵：逆旋转 + 逆平移
        GL11.glRotatef(-player.pitch, 1.0f, 0.0f, 0.0f);
        GL11.glRotatef(-player.yaw,   0.0f, 1.0f, 0.0f);
        GL11.glTranslatef(-player.x, -(player.y + 1.6f), -player.z);

        // 渲染世界
        if (atlasTex != 0) {
            world.render(atlasTex);
        }

        // 渲染准星选中的方块高亮边框
        if (player.hasTarget) {
            renderTargetWireframe(player.targetX, player.targetY, player.targetZ);
        }

        // ---------- 2D HUD 覆盖层 ----------
        renderHUD();
    }

    private static void renderTargetWireframe(int bx, int by, int bz) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glLineWidth(2.0f);
        GL11.glColor4f(0.1f, 0.1f, 0.1f, 0.6f);

        float x0 = bx - 0.005f, y0 = by - 0.005f, z0 = bz - 0.005f;
        float x1 = bx + 1.005f, y1 = by + 1.005f, z1 = bz + 1.005f;

        GL11.glBegin(GL11.GL_LINES);
        // 底框
        GL11.glVertex3f(x0, y0, z0); GL11.glVertex3f(x1, y0, z0);
        GL11.glVertex3f(x1, y0, z0); GL11.glVertex3f(x1, y0, z1);
        GL11.glVertex3f(x1, y0, z1); GL11.glVertex3f(x0, y0, z1);
        GL11.glVertex3f(x0, y0, z1); GL11.glVertex3f(x0, y0, z0);
        // 顶框
        GL11.glVertex3f(x0, y1, z0); GL11.glVertex3f(x1, y1, z0);
        GL11.glVertex3f(x1, y1, z0); GL11.glVertex3f(x1, y1, z1);
        GL11.glVertex3f(x1, y1, z1); GL11.glVertex3f(x0, y1, z1);
        GL11.glVertex3f(x0, y1, z1); GL11.glVertex3f(x0, y1, z0);
        // 4 根竖线
        GL11.glVertex3f(x0, y0, z0); GL11.glVertex3f(x0, y1, z0);
        GL11.glVertex3f(x1, y0, z0); GL11.glVertex3f(x1, y1, z0);
        GL11.glVertex3f(x1, y0, z1); GL11.glVertex3f(x1, y1, z1);
        GL11.glVertex3f(x0, y0, z1); GL11.glVertex3f(x0, y1, z1);
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    private static void renderHUD() {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0, W, 0, H, -1, 1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();

        // 准星 Crosshair (中央亮白十字准星，四方延伸各 9px)
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        float cx = W / 2.0f, cy = H / 2.0f;
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.95f);
        GL11.glBegin(GL11.GL_QUADS);
        // 水平段
        GL11.glVertex2f(cx - 10, cy - 1); GL11.glVertex2f(cx - 2, cy - 1);
        GL11.glVertex2f(cx - 2, cy + 1); GL11.glVertex2f(cx - 10, cy + 1);
        GL11.glVertex2f(cx + 2, cy - 1); GL11.glVertex2f(cx + 10, cy - 1);
        GL11.glVertex2f(cx + 10, cy + 1); GL11.glVertex2f(cx + 2, cy + 1);
        // 垂直段
        GL11.glVertex2f(cx - 1, cy + 2); GL11.glVertex2f(cx + 1, cy + 2);
        GL11.glVertex2f(cx + 1, cy + 10); GL11.glVertex2f(cx - 1, cy + 10);
        GL11.glVertex2f(cx - 1, cy - 10); GL11.glVertex2f(cx + 1, cy - 10);
        GL11.glVertex2f(cx + 1, cy - 2); GL11.glVertex2f(cx - 1, cy - 2);
        GL11.glEnd();

        // 顶部操作提示文字
        if (tipTex != 0) {
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, tipTex);
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.95f);
            float tx = 14, ty = H - tipH - 12;
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glTexCoord2f(0, 1); GL11.glVertex2f(tx, ty);
            GL11.glTexCoord2f(1, 1); GL11.glVertex2f(tx + tipW, ty);
            GL11.glTexCoord2f(1, 0); GL11.glVertex2f(tx + tipW, ty + tipH);
            GL11.glTexCoord2f(0, 0); GL11.glVertex2f(tx, ty + tipH);
            GL11.glEnd();
        }

        // 底部快捷槽指示当前方块
        float barW = 200, barH = 28;
        float barX = (W - barW) / 2.0f, barY = 16;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.55f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(barX, barY);
        GL11.glVertex2f(barX + barW, barY);
        GL11.glVertex2f(barX + barW, barY + barH);
        GL11.glVertex2f(barX, barY + barH);
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    private static void cleanup() {
        if (hitSrc != 0) AL10.alDeleteSources(hitSrc);
        if (hitSound != 0) AL10.alDeleteBuffers(hitSound);
        if (placeSrc != 0) AL10.alDeleteSources(placeSrc);
        if (placeSound != 0) AL10.alDeleteBuffers(placeSound);
        AL.destroy();
    }
}
