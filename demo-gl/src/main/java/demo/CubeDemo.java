package demo;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * LWJGL 2 固定管线示例：光照立方体 + 棋盘纹理地面 + 键盘交互。
 * 用来验证 JARJS 的 OpenGL → WebGL 层。
 */
public class CubeDemo {

    private static float angle = 0f;
    private static float camY = 1.8f;
    private static float camZ = 6.2f;
    private static float cubeY = 0.9f;
    private static int textureId;

    public static void main(String[] args) throws LWJGLException {
        Display.setDisplayMode(new DisplayMode(760, 560));
        Display.setTitle("JARJS · OpenGL 固定管线立方体");
        Display.create();

        setupGL();
        textureId = makeCheckerTexture();

        while (!Display.isCloseRequested()) {
            handleInput();
            render();
            Display.update();
        }
        Display.destroy();
    }

    private static void setupGL() {
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
        GL11.glShadeModel(GL11.GL_SMOOTH);

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_LIGHT0);
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glColorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE);

        GL11.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, fb(3.5f, 5.0f, 4.0f, 1.0f));
        GL11.glLight(GL11.GL_LIGHT0, GL11.GL_AMBIENT, fb(0.28f, 0.30f, 0.36f, 1f));
        GL11.glLight(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, fb(1.0f, 0.96f, 0.90f, 1f));
        GL11.glLight(GL11.GL_LIGHT0, GL11.GL_SPECULAR, fb(1.0f, 1.0f, 1.0f, 1f));

        GL11.glMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_SPECULAR, fb(0.9f, 0.9f, 0.9f, 1f));
        GL11.glMaterialf(GL11.GL_FRONT_AND_BACK, GL11.GL_SHININESS, 28f);
        GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, fb(0.20f, 0.22f, 0.26f, 1f));

        GL11.glClearColor(0.055f, 0.07f, 0.10f, 1f);
    }

    private static FloatBuffer fb(float... v) {
        FloatBuffer b = BufferUtils.createFloatBuffer(v.length);
        b.put(v);
        b.flip();
        return b;
    }

    private static int makeCheckerTexture() {
        int size = 64;
        int[] pixels = new int[size * size];
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                boolean dark = ((x / 8) + (y / 8)) % 2 == 0;
                int r = dark ? 28 : 210;
                int g = dark ? 34 : 214;
                int b = dark ? 46 : 200;
                pixels[y * size + x] = 0xFF000000 | (r << 16) | (g << 8) | b;
            }
        }
        int id = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        IntBuffer buf = BufferUtils.createIntBuffer(pixels.length);
        buf.put(pixels);
        buf.flip();
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, size, size, 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
        return id;
    }

    private static void handleInput() {
        if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
            Display.destroy();
            return;
        }
        float speed = 0.08f;
        if (Keyboard.isKeyDown(Keyboard.KEY_LEFT) || Keyboard.isKeyDown(Keyboard.KEY_A)) angle -= 1.6f;
        if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT) || Keyboard.isKeyDown(Keyboard.KEY_D)) angle += 1.6f;
        if (Keyboard.isKeyDown(Keyboard.KEY_UP) || Keyboard.isKeyDown(Keyboard.KEY_W)) camZ -= speed;
        if (Keyboard.isKeyDown(Keyboard.KEY_DOWN) || Keyboard.isKeyDown(Keyboard.KEY_S)) camZ += speed;
        if (Keyboard.isKeyDown(Keyboard.KEY_Q)) camY += speed;
        if (Keyboard.isKeyDown(Keyboard.KEY_E)) camY -= speed;
        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) cubeY += 0.04f;
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) cubeY -= 0.04f;

        if (camZ < 2.2f) camZ = 2.2f;
        if (camZ > 14f) camZ = 14f;
        if (camY < 0.4f) camY = 0.4f;
        if (camY > 8f) camY = 8f;
        if (cubeY < 0.9f) cubeY = 0.9f;
        if (cubeY > 4f) cubeY = 4f;

        angle += 0.55f;
    }

    private static void render() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        float aspect = (float) Display.getWidth() / Math.max(1, Display.getHeight());
        GLU.gluPerspective(52f, aspect, 0.1f, 120f);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        GLU.gluLookAt(0f, camY, camZ, 0f, 0.8f, 0f, 0f, 1f, 0f);

        drawGround();
        drawCube();
        drawTexturedPlane();
    }

    private static void drawGround() {
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glColor3f(1f, 1f, 1f);

        float s = 6f;
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glNormal3f(0, 1, 0);
        GL11.glTexCoord2f(0, 0); GL11.glVertex3f(-s, 0, -s);
        GL11.glTexCoord2f(6, 0); GL11.glVertex3f(s, 0, -s);
        GL11.glTexCoord2f(6, 6); GL11.glVertex3f(s, 0, s);
        GL11.glTexCoord2f(0, 6); GL11.glVertex3f(-s, 0, s);
        GL11.glEnd();

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_CULL_FACE);
    }

    private static void drawCube() {
        GL11.glPushMatrix();
        GL11.glTranslatef(0f, cubeY, 0f);
        GL11.glRotatef(angle, 0.35f, 1f, 0.15f);
        GL11.glScalef(1.15f, 1.15f, 1.15f);

        float h = 0.5f;

        // 前面
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glNormal3f(0, 0, 1);
        GL11.glColor3f(0.92f, 0.26f, 0.30f);
        GL11.glVertex3f(-h, -h, h);
        GL11.glVertex3f(h, -h, h);
        GL11.glVertex3f(h, h, h);
        GL11.glVertex3f(-h, h, h);
        GL11.glEnd();

        // 后面
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glNormal3f(0, 0, -1);
        GL11.glColor3f(0.95f, 0.62f, 0.18f);
        GL11.glVertex3f(h, -h, -h);
        GL11.glVertex3f(-h, -h, -h);
        GL11.glVertex3f(-h, h, -h);
        GL11.glVertex3f(h, h, -h);
        GL11.glEnd();

        // 左面
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glNormal3f(-1, 0, 0);
        GL11.glColor3f(0.22f, 0.62f, 0.86f);
        GL11.glVertex3f(-h, -h, -h);
        GL11.glVertex3f(-h, -h, h);
        GL11.glVertex3f(-h, h, h);
        GL11.glVertex3f(-h, h, -h);
        GL11.glEnd();

        // 右面
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glNormal3f(1, 0, 0);
        GL11.glColor3f(0.30f, 0.78f, 0.52f);
        GL11.glVertex3f(h, -h, h);
        GL11.glVertex3f(h, -h, -h);
        GL11.glVertex3f(h, h, -h);
        GL11.glVertex3f(h, h, h);
        GL11.glEnd();

        // 顶面
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glNormal3f(0, 1, 0);
        GL11.glColor3f(0.86f, 0.86f, 0.90f);
        GL11.glVertex3f(-h, h, h);
        GL11.glVertex3f(h, h, h);
        GL11.glVertex3f(h, h, -h);
        GL11.glVertex3f(-h, h, -h);
        GL11.glEnd();

        // 底面
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glNormal3f(0, -1, 0);
        GL11.glColor3f(0.55f, 0.35f, 0.70f);
        GL11.glVertex3f(-h, -h, -h);
        GL11.glVertex3f(h, -h, -h);
        GL11.glVertex3f(h, -h, h);
        GL11.glVertex3f(-h, -h, h);
        GL11.glEnd();

        GL11.glPopMatrix();
    }

    private static void drawTexturedPlane() {
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glColor3f(1f, 1f, 1f);

        GL11.glPushMatrix();
        GL11.glTranslatef(-2.6f, 1.6f, -1.2f);
        GL11.glRotatef(angle * 0.8f, 0f, 1f, 0f);

        float s = 0.9f;
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0, 0); GL11.glVertex3f(-s, -s, 0);
        GL11.glTexCoord2f(2, 0); GL11.glVertex3f(s, -s, 0);
        GL11.glTexCoord2f(2, 2); GL11.glVertex3f(s, s, 0);
        GL11.glTexCoord2f(0, 2); GL11.glVertex3f(-s, s, 0);
        GL11.glEnd();

        GL11.glPopMatrix();

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_CULL_FACE);
    }
}
