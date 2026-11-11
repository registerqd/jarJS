package demo;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;

/**
 * LWJGL 2.9.3 现代可编程管线演示：自定义 GLSL 着色器 + VBO 顶点缓冲 + 动态 Uniform 交互。
 * 验证 JARJS 的现代着色器管线（GL20 / GL15）在浏览器的原生直通运行。
 */
public class ShaderDemo {

    // 顶点着色器（标准全屏 Quad，附带 UV 坐标传递）
    private static final String VERTEX_SHADER_SRC =
            "#version 120\n" +
            "attribute vec2 aPosition;\n" +
            "attribute vec2 aTexCoord;\n" +
            "varying vec2 vUV;\n" +
            "void main() {\n" +
            "    vUV = aTexCoord;\n" +
            "    gl_Position = vec4(aPosition, 0.0, 1.0);\n" +
            "}\n";

    // 片段着色器（动态数学彩虹等离子体波纹与发光脉动环）
    private static final String FRAGMENT_SHADER_SRC =
            "#version 120\n" +
            "varying vec2 vUV;\n" +
            "uniform float uTime;\n" +
            "uniform vec2 uResolution;\n" +
            "void main() {\n" +
            "    vec2 p = (vUV - 0.5) * 2.0;\n" +
            "    p.x *= (uResolution.x / uResolution.y);\n" +
            "    float dist = length(p);\n" +
            "    \n" +
            "    // 等离子体色彩波动方程\n" +
            "    float v = sin(p.x * 6.0 + uTime * 1.5) + sin(p.y * 6.0 - uTime * 1.2);\n" +
            "    v += sin(dist * 10.0 - uTime * 2.0);\n" +
            "    \n" +
            "    // 脉动光环\n" +
            "    float ring = 0.03 / abs(dist - 0.55 - 0.08 * sin(uTime * 3.0));\n" +
            "    \n" +
            "    // 彩虹配色映射\n" +
            "    float r = sin(v + 0.0) * 0.5 + 0.5;\n" +
            "    float g = sin(v + 2.094) * 0.5 + 0.5;\n" +
            "    float b = sin(v + 4.188) * 0.5 + 0.5;\n" +
            "    \n" +
            "    vec3 col = vec3(r, g, b) * 0.7 + vec3(ring * 0.35, ring * 0.65, ring * 0.95);\n" +
            "    gl_FragColor = vec4(col, 1.0);\n" +
            "}\n";

    private static int programId;
    private static int vboId;
    private static int uTimeLoc;
    private static int uResolutionLoc;

    private static float time = 0.0f;
    private static float timeSpeed = 1.0f;
    private static boolean paused = false;

    public static void main(String[] args) throws LWJGLException {
        final int width = 760;
        final int height = 560;

        Display.setDisplayMode(new DisplayMode(width, height));
        Display.setTitle("JARJS · 现代 GL20 自定义着色器演示");
        Display.create();

        initShaders();
        initVBO();

        long lastNanos = System.nanoTime();

        while (!Display.isCloseRequested()) {
            long now = System.nanoTime();
            float dt = (now - lastNanos) / 1.0e9f;
            lastNanos = now;

            handleInput(dt);

            if (!paused) {
                time += dt * timeSpeed;
            }

            render(width, height);
            Display.update();
        }

        cleanup();
        Display.destroy();
    }

    private static void initShaders() {
        int vs = compileShader(GL20.GL_VERTEX_SHADER, VERTEX_SHADER_SRC);
        int fs = compileShader(GL20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_SRC);

        programId = GL20.glCreateProgram();
        GL20.glAttachShader(programId, vs);
        GL20.glAttachShader(programId, fs);

        // 显式绑定顶点属性索引
        GL20.glBindAttribLocation(programId, 0, "aPosition");
        GL20.glBindAttribLocation(programId, 1, "aTexCoord");

        GL20.glLinkProgram(programId);
        int linkStatus = GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS);
        if (linkStatus == 0) {
            String log = GL20.glGetProgramInfoLog(programId, 1024);
            throw new RuntimeException("着色器程序链接失败: " + log);
        }

        // 获取 Uniform 槽位
        uTimeLoc = GL20.glGetUniformLocation(programId, "uTime");
        uResolutionLoc = GL20.glGetUniformLocation(programId, "uResolution");
    }

    private static int compileShader(int type, String source) {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);
        int status = GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS);
        if (status == 0) {
            String log = GL20.glGetShaderInfoLog(shader, 1024);
            throw new RuntimeException("Shader 编译失败 (type=" + type + "): " + log);
        }
        return shader;
    }

    private static void initVBO() {
        // 全屏 Quad (两个三角形，包含 X, Y, U, V 交错排布)
        float[] quadVertices = {
                //  Position    // UV
                -1.0f, -1.0f,   0.0f, 0.0f,
                 1.0f, -1.0f,   1.0f, 0.0f,
                 1.0f,  1.0f,   1.0f, 1.0f,

                -1.0f, -1.0f,   0.0f, 0.0f,
                 1.0f,  1.0f,   1.0f, 1.0f,
                -1.0f,  1.0f,   0.0f, 1.0f
        };

        FloatBuffer buffer = BufferUtils.createFloatBuffer(quadVertices.length);
        buffer.put(quadVertices);
        buffer.flip();

        vboId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    private static void handleInput(float dt) {
        if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
            Display.destroy();
            return;
        }

        // 单次事件处理
        while (Keyboard.next()) {
            if (Keyboard.getEventKeyState()) {
                int key = Keyboard.getEventKey();
                if (key == Keyboard.KEY_SPACE) {
                    paused = !paused;
                } else if (key == Keyboard.KEY_R) {
                    time = 0.0f;
                }
            }
        }

        // 持续按键处理
        if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT) || Keyboard.isKeyDown(Keyboard.KEY_D)) {
            timeSpeed = Math.min(timeSpeed + 1.2f * dt, 4.0f);
        } else if (Keyboard.isKeyDown(Keyboard.KEY_LEFT) || Keyboard.isKeyDown(Keyboard.KEY_A)) {
            timeSpeed = Math.max(timeSpeed - 1.2f * dt, 0.1f);
        }
    }

    private static void render(int w, int h) {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glViewport(0, 0, w, h);

        // 激活着色器程序
        GL20.glUseProgram(programId);

        // 更新 Uniform
        GL20.glUniform1f(uTimeLoc, time);
        GL20.glUniform2f(uResolutionLoc, (float) w, (float) h);

        // 绑定顶点缓冲并配置属性指针
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);

        final int stride = 4 * 4; // 4 个 float (x, y, u, v)
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, stride, 0);

        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, stride, 2 * 4);

        // 原生绘制调用
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);

        // 清理绑定
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL20.glUseProgram(0);
    }

    private static void cleanup() {
        GL15.glDeleteBuffers(vboId);
        GL20.glDeleteProgram(programId);
    }
}
