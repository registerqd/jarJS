package jarjs.lwjgl.opengl;

/**
 * 固定管线核心：矩阵栈、立即模式收集、逐顶点光照、纹理对象、图元展开。
 * 不依赖 JDK 的 java.awt / javax，所有输出通过 GLBackend 交给 WebGL。
 */
public final class GL {

    // ---- 图元 ----
    public static final int POINTS = 0x0000;
    public static final int LINES = 0x0001;
    public static final int LINE_LOOP = 0x0002;
    public static final int LINE_STRIP = 0x0003;
    public static final int TRIANGLES = 0x0004;
    public static final int TRIANGLE_STRIP = 0x0005;
    public static final int TRIANGLE_FAN = 0x0006;
    public static final int QUADS = 0x0007;
    public static final int QUAD_STRIP = 0x0008;
    public static final int POLYGON = 0x0009;

    // ---- 矩阵模式 ----
    public static final int MODELVIEW = 0x1700;
    public static final int PROJECTION = 0x1701;
    public static final int TEXTURE = 0x1702;

    // ---- 清除位 ----
    public static final int DEPTH_BUFFER_BIT = 0x0100;
    public static final int STENCIL_BUFFER_BIT = 0x0400;
    public static final int COLOR_BUFFER_BIT = 0x4000;

    // ---- 能力 ----
    public static final int DEPTH_TEST = 0x0B71;
    public static final int BLEND = 0x0BE2;
    public static final int CULL_FACE = 0x0B44;
    public static final int LIGHTING = 0x0B50;
    public static final int COLOR_MATERIAL = 0x0B57;
    public static final int NORMALIZE = 0x0BA1;
    public static final int TEXTURE_2D = 0x0DE1;
    public static final int ALPHA_TEST = 0x0BC0;
    public static final int LINE_SMOOTH = 0x0B20;
    public static final int POLYGON_SMOOTH = 0x0B41;
    public static final int SCISSOR_TEST = 0x0C11;
    public static final int FOG = 0x0B60;

    // ---- 光照 ----
    public static final int LIGHT0 = 0x4000;
    public static final int LIGHT1 = 0x4001;
    public static final int LIGHT2 = 0x4002;
    public static final int LIGHT3 = 0x4003;
    public static final int LIGHT4 = 0x4004;
    public static final int LIGHT5 = 0x4005;
    public static final int LIGHT6 = 0x4006;
    public static final int LIGHT7 = 0x4007;

    public static final int AMBIENT = 0x1200;
    public static final int DIFFUSE = 0x1201;
    public static final int SPECULAR = 0x1202;
    public static final int POSITION = 0x1203;
    public static final int SPOT_DIRECTION = 0x1204;
    public static final int SPOT_EXPONENT = 0x1205;
    public static final int SPOT_CUTOFF = 0x1206;
    public static final int CONSTANT_ATTENUATION = 0x1207;
    public static final int LINEAR_ATTENUATION = 0x1208;
    public static final int QUADRATIC_ATTENUATION = 0x1209;
    public static final int EMISSION = 0x1600;
    public static final int SHININESS = 0x1601;
    public static final int AMBIENT_AND_DIFFUSE = 0x1602;
    public static final int LIGHT_MODEL_AMBIENT = 0x0B53;
    public static final int LIGHT_MODEL_TWO_SIDE = 0x0B52;
    public static final int FRONT = 0x0404;
    public static final int BACK = 0x0405;
    public static final int FRONT_AND_BACK = 0x0408;

    // ---- 纹理 ----
    public static final int TEXTURE_MAG_FILTER = 0x2800;
    public static final int TEXTURE_MIN_FILTER = 0x2801;
    public static final int TEXTURE_WRAP_S = 0x2802;
    public static final int TEXTURE_WRAP_T = 0x2803;
    public static final int NEAREST = 0x2600;
    public static final int LINEAR = 0x2601;
    public static final int NEAREST_MIPMAP_NEAREST = 0x2700;
    public static final int LINEAR_MIPMAP_NEAREST = 0x2701;
    public static final int NEAREST_MIPMAP_LINEAR = 0x2702;
    public static final int LINEAR_MIPMAP_LINEAR = 0x2703;
    public static final int REPEAT = 0x2901;
    public static final int CLAMP = 0x2900;
    public static final int CLAMP_TO_EDGE = 0x812F;
    public static final int RGBA = 0x1908;
    public static final int RGB = 0x1907;
    public static final int BGRA = 0x80E1;
    public static final int BGR = 0x80E0;
    public static final int LUMINANCE = 0x1909;
    public static final int LUMINANCE_ALPHA = 0x190A;
    public static final int ALPHA = 0x1906;
    public static final int UNSIGNED_BYTE = 0x1401;
    public static final int UNSIGNED_SHORT = 0x1403;
    public static final int FLOAT = 0x1406;
    public static final int TEXTURE_ENV = 0x2300;
    public static final int TEXTURE_ENV_MODE = 0x2200;
    public static final int MODULATE = 0x2100;
    public static final int DECAL = 0x2101;
    public static final int REPLACE = 0x1E01;
    public static final int COMBINE = 0x8570;

    // ---- 混合 ----
    public static final int ZERO = 0;
    public static final int ONE = 1;
    public static final int SRC_COLOR = 0x0300;
    public static final int ONE_MINUS_SRC_COLOR = 0x0301;
    public static final int SRC_ALPHA = 0x0302;
    public static final int ONE_MINUS_SRC_ALPHA = 0x0303;
    public static final int DST_ALPHA = 0x0304;
    public static final int ONE_MINUS_DST_ALPHA = 0x0305;
    public static final int DST_COLOR = 0x0306;
    public static final int ONE_MINUS_DST_COLOR = 0x0307;

    // ---- 深度/面/着色 ----
    public static final int NEVER = 0x0200;
    public static final int LESS = 0x0201;
    public static final int EQUAL = 0x0202;
    public static final int LEQUAL = 0x0203;
    public static final int GREATER = 0x0204;
    public static final int NOTEQUAL = 0x0205;
    public static final int GEQUAL = 0x0206;
    public static final int ALWAYS = 0x0207;
    public static final int CW = 0x0900;
    public static final int CCW = 0x0901;
    public static final int FRONT_FACE = 0x0B46;
    public static final int CULL_FACE_MODE = 0x0B45;
    public static final int SMOOTH = 0x1D01;
    public static final int FLAT = 0x1D00;
    public static final int SHADE_MODEL = 0x0B54;
    public static final int FILL = 0x1B02;
    public static final int LINE = 0x1B01;
    public static final int POINT = 0x1B00;
    public static final int POLYGON_MODE = 0x0B40;

    // ---- 字符串 ----
    public static final int VENDOR = 0x1F00;
    public static final int RENDERER = 0x1F01;
    public static final int VERSION = 0x1F02;
    public static final int EXTENSIONS = 0x1F03;

    // 提交标志位
    public static final int F_DEPTH = 1;
    public static final int F_BLEND = 2;
    public static final int F_CULL = 4;
    public static final int F_TEX = 8;
    public static final int F_POINTS = 16;

    // ---- 状态 ----
    private static boolean depthTest = false;
    private static boolean blend = false;
    private static boolean cullFace = false;
    private static boolean lighting = false;
    private static boolean colorMaterial = false;
    private static boolean normalize = false;
    private static boolean texture2D = false;
    private static boolean smooth = true;
    private static int polygonMode = FILL;
    private static int blendSrc = ONE, blendDst = ZERO;
    private static int depthFunc = LESS;
    private static boolean depthMask = true;
    private static int cullMode = BACK;
    private static int frontFace = CCW;
    private static float lineWidth = 1f;
    private static float pointSize = 1f;
    private static int alphaFunc = ALWAYS;
    private static float alphaRef = 0f;

    private static float clearR = 0, clearG = 0, clearB = 0, clearA = 1;

    // 当前颜色 / 法线 / 纹理坐标
    private static float cr = 1, cg = 1, cb = 1, ca = 1;
    private static float nx = 0, ny = 0, nz = 1;
    private static float tu = 0, tv = 0;

    // 光照参数（视图空间）
    private static final float[][] lightAmbient = new float[8][4];
    private static final float[][] lightDiffuse = new float[8][4];
    private static final float[][] lightSpecular = new float[8][4];
    private static final float[][] lightPosition = new float[8][4];
    private static final boolean[] lightOn = new boolean[8];
    private static final float[] lightAtten = {1, 0, 0};
    private static final float[] globalAmbient = {0.2f, 0.2f, 0.2f, 1f};

    private static final float[] matAmbient = {0.2f, 0.2f, 0.2f, 1f};
    private static final float[] matDiffuse = {0.8f, 0.8f, 0.8f, 1f};
    private static final float[] matSpecular = {0f, 0f, 0f, 1f};
    private static final float[] matEmission = {0f, 0f, 0f, 1f};
    private static float matShininess = 0f;

    // 矩阵
    private static final float[] modelview = new float[16];
    private static final float[] projection = new float[16];
    private static final float[] texMatrix = new float[16];
    private static int matrixMode = MODELVIEW;

    private static final float[][] mvStack = new float[64][];
    private static final float[][] prStack = new float[32][];
    private static final float[][] txStack = new float[32][];
    private static int mvTop = 0, prTop = 0, txTop = 0;

    // 立即模式
    private static int beginMode = -1;
    private static float[] vb = new float[9 * 4096];
    private static int vCount = 0;

    // 纹理对象
    private static int nextTexId = 1;
    private static final java.util.HashMap<Integer, Texture> textures = new java.util.HashMap<>();
    private static int boundTex = 0;

    private static boolean initialized = false;
    private static int viewportW = 480, viewportH = 480;

    static {
        identity(modelview);
        identity(projection);
        identity(texMatrix);
        for (int i = 0; i < 8; i++) {
            lightAmbient[i][3] = 1; lightDiffuse[i][3] = 1; lightSpecular[i][3] = 1;
            lightPosition[i][3] = 0;
        }
        lightAmbient[0][0] = lightAmbient[0][1] = lightAmbient[0][2] = 0f;
        lightDiffuse[0][0] = lightDiffuse[0][1] = lightDiffuse[0][2] = 1f;
        lightSpecular[0][0] = lightSpecular[0][1] = lightSpecular[0][2] = 1f;
        lightPosition[0][2] = 1f;
        lightPosition[0][3] = 0f;
    }

    private GL() {}

    static final class Texture {
        int w, h;
        int[] argb;
        boolean dirty = true;
        int minFilter = LINEAR, magFilter = LINEAR, wrapS = REPEAT, wrapT = REPEAT;
    }

    // ================= 初始化 =================

    public static boolean init(int w, int h) {
        if (initialized) return true;
        viewportW = w; viewportH = h;
        double dpr = 1;
        int ok = GLBackend.init(w, h, dpr);
        initialized = ok != 0;
        if (!initialized) GLBackend.warn("WebGL 初始化失败，检查 canvas#game 是否已存在或上下文被 2D 占用");
        return initialized;
    }

    public static boolean isInitialized() { return initialized; }

    // ================= 矩阵 =================

    public static void matrixMode(int mode) { matrixMode = mode; }

    private static float[] currentMatrix() {
        switch (matrixMode) {
            case PROJECTION: return projection;
            case TEXTURE: return texMatrix;
            default: return modelview;
        }
    }

    private static void pushTo(float[][] stack, int[] top, float[] src) {
        if (top[0] >= stack.length) { GLBackend.warn("矩阵栈溢出"); return; }
        stack[top[0]++] = src.clone();
    }

    public static void pushMatrix() {
        switch (matrixMode) {
            case PROJECTION:
                if (prTop < prStack.length) prStack[prTop++] = projection.clone();
                else GLBackend.warn("投影矩阵栈溢出");
                break;
            case TEXTURE:
                if (txTop < txStack.length) txStack[txTop++] = texMatrix.clone();
                else GLBackend.warn("纹理矩阵栈溢出");
                break;
            default:
                if (mvTop < mvStack.length) mvStack[mvTop++] = modelview.clone();
                else GLBackend.warn("模型视图矩阵栈溢出");
                break;
        }
    }

    /** 返回当前指定矩阵的副本，供 glGetFloatv 使用。 */
    public static float[] matrix(int pname) {
        switch (pname) {
            case 0x0BA6: return modelview.clone();
            case 0x0BA7: return projection.clone();
            case 0x1702: return texMatrix.clone();
            default: return null;
        }
    }

    public static void popMatrix() {
        switch (matrixMode) {
            case PROJECTION:
                if (prTop > 0) copy(prStack[--prTop], projection); break;
            case TEXTURE:
                if (txTop > 0) copy(txStack[--txTop], texMatrix); break;
            default:
                if (mvTop > 0) copy(mvStack[--mvTop], modelview); break;
        }
    }

    public static void loadIdentity() { identity(currentMatrix()); }

    public static void loadMatrix(float[] m) { copy(m, currentMatrix()); }

    public static void multMatrix(float[] m) {
        float[] out = new float[16];
        multiply(out, currentMatrix(), m);
        copy(out, currentMatrix());
    }

    public static void translate(float x, float y, float z) {
        float[] t = new float[16];
        identity(t);
        t[12] = x; t[13] = y; t[14] = z;
        multMatrix(t);
    }

    public static void scale(float x, float y, float z) {
        float[] t = new float[16];
        identity(t);
        t[0] = x; t[5] = y; t[10] = z;
        multMatrix(t);
    }

    public static void rotate(float angleDeg, float x, float y, float z) {
        float len = (float) Math.sqrt(x * x + y * y + z * z);
        if (len < 1e-6f) return;
        x /= len; y /= len; z /= len;
        double rad = Math.toRadians(angleDeg);
        float c = (float) Math.cos(rad), s = (float) Math.sin(rad), ic = 1 - c;
        float[] m = new float[16];
        m[0] = x * x * ic + c;
        m[1] = y * x * ic + z * s;
        m[2] = x * z * ic - y * s;
        m[4] = x * y * ic - z * s;
        m[5] = y * y * ic + c;
        m[6] = y * z * ic + x * s;
        m[8] = x * z * ic + y * s;
        m[9] = y * z * ic - x * s;
        m[10] = z * z * ic + c;
        m[15] = 1;
        multMatrix(m);
    }

    public static void ortho(float l, float r, float b, float t, float n, float f) {
        float[] m = new float[16];
        m[0] = 2f / (r - l);
        m[5] = 2f / (t - b);
        m[10] = -2f / (f - n);
        m[12] = -(r + l) / (r - l);
        m[13] = -(t + b) / (t - b);
        m[14] = -(f + n) / (f - n);
        m[15] = 1;
        multMatrix(m);
    }

    public static void frustum(float l, float r, float b, float t, float n, float f) {
        float[] m = new float[16];
        m[0] = 2 * n / (r - l);
        m[5] = 2 * n / (t - b);
        m[8] = (r + l) / (r - l);
        m[9] = (t + b) / (t - b);
        m[10] = -(f + n) / (f - n);
        m[11] = -1;
        m[14] = -2 * f * n / (f - n);
        multMatrix(m);
    }

    public static void perspective(float fovy, float aspect, float near, float far) {
        double rad = Math.toRadians(fovy) / 2.0;
        float f = (float) (1.0 / Math.tan(rad));
        float[] m = new float[16];
        m[0] = f / aspect;
        m[5] = f;
        m[10] = (far + near) / (near - far);
        m[11] = -1;
        m[14] = 2 * far * near / (near - far);
        multMatrix(m);
    }

    public static void lookAt(float ex, float ey, float ez, float cx, float cy, float cz, float ux, float uy, float uz) {
        float fx = cx - ex, fy = cy - ey, fz = cz - ez;
        float fl = (float) Math.sqrt(fx * fx + fy * fy + fz * fz);
        if (fl < 1e-6f) return;
        fx /= fl; fy /= fl; fz /= fl;
        float sx = fy * uz - fz * uy, sy = fz * ux - fx * uz, sz = fx * uy - fy * ux;
        float sl = (float) Math.sqrt(sx * sx + sy * sy + sz * sz);
        if (sl < 1e-6f) return;
        sx /= sl; sy /= sl; sz /= sl;
        float ux2 = sy * fz - sz * fy, uy2 = sz * fx - sx * fz, uz2 = sx * fy - sy * fx;
        float[] m = new float[16];
        m[0] = sx; m[4] = sy; m[8] = sz;
        m[1] = ux2; m[5] = uy2; m[9] = uz2;
        m[2] = -fx; m[6] = -fy; m[10] = -fz;
        m[12] = -(sx * ex + sy * ey + sz * ez);
        m[13] = -(ux2 * ex + uy2 * ey + uz2 * ez);
        m[14] = fx * ex + fy * ey + fz * ez;
        m[15] = 1;
        multMatrix(m);
    }

    // ================= 状态 =================

    public static void enable(int cap) {
        if (cap >= LIGHT0 && cap <= LIGHT7) { lightOn[cap - LIGHT0] = true; return; }
        switch (cap) {
            case DEPTH_TEST: depthTest = true; break;
            case BLEND: blend = true; break;
            case CULL_FACE: cullFace = true; break;
            case LIGHTING: lighting = true; break;
            case COLOR_MATERIAL: colorMaterial = true; break;
            case NORMALIZE: normalize = true; break;
            case TEXTURE_2D: texture2D = true; break;
            default: break;
        }
    }

    public static void disable(int cap) {
        if (cap >= LIGHT0 && cap <= LIGHT7) { lightOn[cap - LIGHT0] = false; return; }
        switch (cap) {
            case DEPTH_TEST: depthTest = false; break;
            case BLEND: blend = false; break;
            case CULL_FACE: cullFace = false; break;
            case LIGHTING: lighting = false; break;
            case COLOR_MATERIAL: colorMaterial = false; break;
            case NORMALIZE: normalize = false; break;
            case TEXTURE_2D: texture2D = false; break;
            default: break;
        }
    }

    public static void lightEnable(int light, boolean on) {
        int idx = light - LIGHT0;
        if (idx >= 0 && idx < 8) lightOn[idx] = on;
    }

    public static void clearColor(float r, float g, float b, float a) {
        clearR = r; clearG = g; clearB = b; clearA = a;
    }

    public static void clear(int mask) {
        if (!initialized) return;
        GLBackend.clear(clearR, clearG, clearB, clearA, mask);
    }

    public static void viewport(int x, int y, int w, int h) { viewportW = w; viewportH = h; }

    public static void blendFunc(int s, int d) { blendSrc = s; blendDst = d; }
    public static void depthFunc(int f) { depthFunc = f; }
    public static void depthMask(boolean m) { depthMask = m; }
    public static void cullFace(int m) { cullMode = m; }
    public static void frontFace(int m) { frontFace = m; }
    public static void shadeModel(int m) { smooth = (m == SMOOTH); }
    public static void polygonMode(int face, int mode) { polygonMode = mode; }
    public static void lineWidthSet(float w) { lineWidth = w; }
    public static void pointSizeSet(float s) { pointSize = s; }
    public static void alphaFunc(int f, float ref) { alphaFunc = f; alphaRef = ref; }

    public static void color(float r, float g, float b, float a) { cr = r; cg = g; cb = b; ca = a; }
    public static void normal(float x, float y, float z) {
        float l = (float) Math.sqrt(x * x + y * y + z * z);
        if (l < 1e-6f) { nx = 0; ny = 0; nz = 1; return; }
        nx = x / l; ny = y / l; nz = z / l;
    }
    public static void texCoord(float u, float v) { tu = u; tv = v; }

    // ================= 光照参数 =================

    public static void light(int light, int pname, float[] v) {
        int idx = light - LIGHT0;
        if (idx < 0 || idx >= 8) return;
        switch (pname) {
            case AMBIENT: copy4(v, lightAmbient[idx]); break;
            case DIFFUSE: copy4(v, lightDiffuse[idx]); break;
            case SPECULAR: copy4(v, lightSpecular[idx]); break;
            case POSITION: copy4(v, lightPosition[idx]); break;
            case CONSTANT_ATTENUATION: lightAtten[0] = v[0]; break;
            case LINEAR_ATTENUATION: lightAtten[1] = v[0]; break;
            case QUADRATIC_ATTENUATION: lightAtten[2] = v[0]; break;
            default: break;
        }
    }

    public static void lightModel(int pname, float[] v) {
        if (pname == LIGHT_MODEL_AMBIENT) copy4(v, globalAmbient);
    }

    public static void material(int face, int pname, float[] v) {
        switch (pname) {
            case AMBIENT: copy4(v, matAmbient); break;
            case DIFFUSE: copy4(v, matDiffuse); break;
            case SPECULAR: copy4(v, matSpecular); break;
            case EMISSION: copy4(v, matEmission); break;
            case AMBIENT_AND_DIFFUSE:
                copy4(v, matAmbient); copy4(v, matDiffuse); break;
            case SHININESS: matShininess = v[0]; break;
            default: break;
        }
    }

    // ================= 纹理 =================

    public static int genTexture() { return nextTexId++; }

    /** 登记一个由 JS 侧直接上传的纹理尺寸，让固定管线知道它可以被采样。 */
    public static void registerTexture(int id, int w, int h) {
        Texture t = textures.get(id);
        if (t == null) { t = new Texture(); textures.put(id, t); }
        t.w = w; t.h = h; t.argb = null; t.dirty = false;
    }

    public static void bindTexture(int id) { boundTex = id; }

    public static void deleteTexture(int id) {
        textures.remove(id);
        if (initialized) GLBackend.deleteTexture(id);
        if (boundTex == id) boundTex = 0;
    }

    public static void texParameter(int target, int pname, int value) {
        Texture t = textures.get(boundTex);
        if (t == null) {
            t = new Texture();
            textures.put(boundTex, t);
        }
        switch (pname) {
            case TEXTURE_MIN_FILTER: t.minFilter = value; break;
            case TEXTURE_MAG_FILTER: t.magFilter = value; break;
            case TEXTURE_WRAP_S: t.wrapS = value; break;
            case TEXTURE_WRAP_T: t.wrapT = value; break;
            default: break;
        }
    }

    /** 从 ARGB 像素创建/更新纹理。 */
    public static void texImage2D(int target, int level, int internalFormat, int w, int h,
                                  int border, int format, int type, int[] argb) {
        if (boundTex == 0) boundTex = genTexture();
        Texture t = textures.get(boundTex);
        if (t == null) { t = new Texture(); textures.put(boundTex, t); }
        t.w = w; t.h = h; t.argb = argb; t.dirty = true;
        uploadIfNeeded(boundTex, t);
    }

    /** 从字节缓冲创建纹理，支持 RGBA / RGB / BGRA / BGR / LUMINANCE / LUMINANCE_ALPHA / ALPHA。 */
    public static void texImage2D(int target, int level, int internalFormat, int w, int h,
                                  int border, int format, int type, java.nio.ByteBuffer data) {
        int n = w * h;
        int[] argb = new int[n];
        int bpp = bytesPerPixel(format);
        if (data == null) {
            for (int i = 0; i < n; i++) argb[i] = 0xFFFFFFFF;
        } else {
            int base = data.position();
            for (int i = 0; i < n; i++) {
                int o = base + i * bpp;
                int a = 255, r, g, b;
                switch (format) {
                    case RGBA: r = u8(data, o); g = u8(data, o + 1); b = u8(data, o + 2); a = u8(data, o + 3); break;
                    case BGRA: b = u8(data, o); g = u8(data, o + 1); r = u8(data, o + 2); a = u8(data, o + 3); break;
                    case RGB: r = u8(data, o); g = u8(data, o + 1); b = u8(data, o + 2); break;
                    case BGR: b = u8(data, o); g = u8(data, o + 1); r = u8(data, o + 2); break;
                    case LUMINANCE: r = g = b = u8(data, o); break;
                    case LUMINANCE_ALPHA: r = g = b = u8(data, o); a = u8(data, o + 1); break;
                    case ALPHA: r = g = b = 255; a = u8(data, o); break;
                    default: r = g = b = 255; break;
                }
                argb[i] = (a << 24) | (r << 16) | (g << 8) | b;
            }
        }
        texImage2D(target, level, internalFormat, w, h, border, format, type, argb);
    }

    private static int bytesPerPixel(int format) {
        switch (format) {
            case RGBA: case BGRA: return 4;
            case RGB: case BGR: return 3;
            case LUMINANCE_ALPHA: return 2;
            default: return 1;
        }
    }

    private static int u8(java.nio.ByteBuffer b, int i) { return b.get(i) & 0xFF; }

    private static void uploadIfNeeded(int id, Texture t) {
        if (!t.dirty || !initialized) return;
        t.dirty = false;
        GLBackend.texImage(id, t.w, t.h, t.argb, t.magFilter, t.wrapS);
    }

    // ================= 立即模式 =================

    public static void begin(int mode) {
        beginMode = mode;
        vCount = 0;
    }

    public static void vertex(float x, float y, float z) {
        if (beginMode < 0) return;
        ensure(9);
        int o = vCount * 9;
        vb[o] = x; vb[o + 1] = y; vb[o + 2] = z;
        float r = cr, g = cg, b = cb;
        if (lighting) {
            float[] lit = shade(x, y, z);
            r = lit[0]; g = lit[1]; b = lit[2];
        }
        vb[o + 3] = r; vb[o + 4] = g; vb[o + 5] = b; vb[o + 6] = ca;
        float u = tu, v = tv;
        if (texture2D) {
            float ux = texMatrix[0] * u + texMatrix[4] * v + texMatrix[12];
            float uy = texMatrix[1] * u + texMatrix[5] * v + texMatrix[13];
            u = ux; v = uy;
        }
        vb[o + 7] = u; vb[o + 8] = v;
        vCount++;
    }

    public static void end() {
        if (beginMode < 0) return;
        int mode = beginMode;
        beginMode = -1;
        if (vCount == 0) return;

        int outMode = mode;
        float[] out;
        int outCount;

        if (mode == QUADS) {
            int quads = vCount / 4;
            out = new float[quads * 6 * 9];
            int k = 0;
            for (int q = 0; q < quads; q++) {
                int a = q * 4, b = a + 1, c = a + 2, d = a + 3;
                k = emitTri(out, k, a, b, c);
                k = emitTri(out, k, a, c, d);
            }
            outMode = TRIANGLES;
            outCount = quads * 6;
        } else if (mode == QUAD_STRIP) {
            int quads = (vCount - 2) / 2;
            out = new float[quads * 6 * 9];
            int k = 0;
            for (int q = 0; q < quads; q++) {
                int a = q * 2, b = a + 1, c = a + 2, d = a + 3;
                k = emitTri(out, k, a, b, c);
                k = emitTri(out, k, b, d, c);
            }
            outMode = TRIANGLES;
            outCount = quads * 6;
        } else if (mode == POLYGON) {
            int tris = Math.max(0, vCount - 2);
            out = new float[tris * 3 * 9];
            int k = 0;
            for (int i = 1; i + 1 < vCount; i++) {
                k = emitTri(out, k, 0, i, i + 1);
            }
            outMode = TRIANGLES;
            outCount = tris * 3;
        } else {
            out = new float[vCount * 9];
            System.arraycopy(vb, 0, out, 0, vCount * 9);
            outCount = vCount;
        }

        if (polygonMode == LINE && (outMode == TRIANGLES || outMode == TRIANGLE_STRIP || outMode == TRIANGLE_FAN)) {
            float[] lines = new float[(outCount / 3) * 6 * 9];
            int k = 0;
            for (int t = 0; t + 2 < outCount; t += 3) {
                k = emitLine(lines, k, out, t, t + 1);
                k = emitLine(lines, k, out, t + 1, t + 2);
                k = emitLine(lines, k, out, t + 2, t);
            }
            out = lines;
            outCount = (outCount / 3) * 6;
            outMode = LINES;
        }

        if (smooth == false && (outMode == TRIANGLES || outMode == TRIANGLE_STRIP || outMode == TRIANGLE_FAN)) {
            flatten(out, outCount);
        }

        submit(outMode, out, outCount);
        vCount = 0;
    }

    private static int emitTri(float[] dst, int k, int a, int b, int c) {
        k = copyVert(dst, k, a);
        k = copyVert(dst, k, b);
        return copyVert(dst, k, c);
    }

    private static int emitLine(float[] dst, int k, float[] src, int a, int b) {
        System.arraycopy(src, a * 9, dst, k, 9); k += 9;
        System.arraycopy(src, b * 9, dst, k, 9); k += 9;
        return k;
    }

    private static int copyVert(float[] dst, int k, int idx) {
        System.arraycopy(vb, idx * 9, dst, k, 9);
        return k + 9;
    }

    private static void flatten(float[] v, int count) {
        for (int i = 0; i + 2 < count; i += 3) {
            float r = v[(i + 2) * 9 + 3], g = v[(i + 2) * 9 + 4], b = v[(i + 2) * 9 + 5], a = v[(i + 2) * 9 + 6];
            for (int j = 0; j < 3; j++) {
                int o = (i + j) * 9;
                v[o + 3] = r; v[o + 4] = g; v[o + 5] = b; v[o + 6] = a;
            }
        }
    }

    private static void submit(int mode, float[] verts, int count) {
        if (count == 0) return;
        if (recording != null) {
            float[] mvp = new float[16];
            multiply(mvp, projection, modelview);
            int texId = texture2D ? boundTex : 0;
            float[] rec = new float[4 + count * 9 + 16];
            rec[0] = mode; rec[1] = count; rec[2] = texId; rec[3] = 0;
            System.arraycopy(verts, 0, rec, 4, count * 9);
            System.arraycopy(mvp, 0, rec, 4 + count * 9, 16);
            recording.add(rec);
            return;
        }
        if (!initialized) return;
        int texId = texture2D ? boundTex : 0;
        Texture t = texId != 0 ? textures.get(texId) : null;
        if (t != null) uploadIfNeeded(texId, t);

        float[] mvp = new float[16];
        multiply(mvp, projection, modelview);

        int flags = 0;
        if (depthTest) flags |= F_DEPTH;
        if (blend) flags |= F_BLEND;
        if (cullFace) flags |= F_CULL;
        if (texId != 0 && t != null && t.w > 0) flags |= F_TEX;
        if (mode == POINTS) flags |= F_POINTS;

        GLBackend.setState(flags);
        GLBackend.blendFunc(blendSrc, blendDst);
        GLBackend.lineWidth(lineWidth);
        GLBackend.pointSize(pointSize);
        GLBackend.draw(mode, verts, count, mvp, texId, flags);
    }

    private static void ensure(int n) {
        if ((vCount + 1) * 9 <= vb.length) return;
        float[] nv = new float[vb.length * 2];
        System.arraycopy(vb, 0, nv, 0, vb.length);
        vb = nv;
    }

    // ================= 光照计算 =================

    private static final float[] litOut = new float[3];

    private static float[] shade(float x, float y, float z) {
        float ex = modelview[0] * x + modelview[4] * y + modelview[8] * z + modelview[12];
        float ey = modelview[1] * x + modelview[5] * y + modelview[9] * z + modelview[13];
        float ez = modelview[2] * x + modelview[6] * y + modelview[10] * z + modelview[14];

        float tnx = modelview[0] * nx + modelview[4] * ny + modelview[8] * nz;
        float tny = modelview[1] * nx + modelview[5] * ny + modelview[9] * nz;
        float tnz = modelview[2] * nx + modelview[6] * ny + modelview[10] * nz;
        float nl = (float) Math.sqrt(tnx * tnx + tny * tny + tnz * tnz);
        if (nl < 1e-6f) { tnx = 0; tny = 0; tnz = 1; nl = 1; }
        tnx /= nl; tny /= nl; tnz /= nl;

        float ar = globalAmbient[0] * matAmbient[0] + matEmission[0];
        float ag = globalAmbient[1] * matAmbient[1] + matEmission[1];
        float ab = globalAmbient[2] * matAmbient[2] + matEmission[2];

        float vx = -ex, vy = -ey, vz = -ez;
        float vl = (float) Math.sqrt(vx * vx + vy * vy + vz * vz);
        if (vl < 1e-6f) { vx = 0; vy = 0; vz = 1; vl = 1; }
        vx /= vl; vy /= vl; vz /= vl;

        for (int i = 0; i < 8; i++) {
            if (!lightOn[i]) continue;
            float lx, ly, lz;
            if (lightPosition[i][3] == 0f) {
                lx = -lightPosition[i][0]; ly = -lightPosition[i][1]; lz = -lightPosition[i][2];
            } else {
                lx = lightPosition[i][0] - ex; ly = lightPosition[i][1] - ey; lz = lightPosition[i][2] - ez;
            }
            float ll = (float) Math.sqrt(lx * lx + ly * ly + lz * lz);
            if (ll < 1e-6f) continue;
            lx /= ll; ly /= ll; lz /= ll;

            float ndl = tnx * lx + tny * ly + tnz * lz;
            if (ndl < 0) ndl = 0;

            float att = 1f;
            if (lightPosition[i][3] != 0f) {
                float d2 = (lightPosition[i][0] - ex) * (lightPosition[i][0] - ex)
                        + (lightPosition[i][1] - ey) * (lightPosition[i][1] - ey)
                        + (lightPosition[i][2] - ez) * (lightPosition[i][2] - ez);
                float d = (float) Math.sqrt(d2);
                att = 1f / (lightAtten[0] + lightAtten[1] * d + lightAtten[2] * d2);
            }

            ar += att * lightAmbient[i][0] * matAmbient[0];
            ag += att * lightAmbient[i][1] * matAmbient[1];
            ab += att * lightAmbient[i][2] * matAmbient[2];

            ar += att * ndl * lightDiffuse[i][0] * matDiffuse[0];
            ag += att * ndl * lightDiffuse[i][1] * matDiffuse[1];
            ab += att * ndl * lightDiffuse[i][2] * matDiffuse[2];

            if (matShininess > 0) {
                float hx = lx + vx, hy = ly + vy, hz = lz + vz;
                float hl = (float) Math.sqrt(hx * hx + hy * hy + hz * hz);
                if (hl > 1e-6f) {
                    hx /= hl; hy /= hl; hz /= hl;
                    float ndh = tnx * hx + tny * hy + tnz * hz;
                    if (ndh > 0) {
                        float sp = (float) Math.pow(ndh, matShininess);
                        ar += att * sp * lightSpecular[i][0] * matSpecular[0];
                        ag += att * sp * lightSpecular[i][1] * matSpecular[1];
                        ab += att * sp * lightSpecular[i][2] * matSpecular[2];
                    }
                }
            }
        }

        litOut[0] = clamp01(ar * (colorMaterial ? cr : 1f));
        litOut[1] = clamp01(ag * (colorMaterial ? cg : 1f));
        litOut[2] = clamp01(ab * (colorMaterial ? cb : 1f));
        return litOut;
    }

    private static float clamp01(float v) { return v < 0 ? 0 : (v > 1 ? 1 : v); }

    // ================= 数学工具 =================

    static void identity(float[] m) {
        for (int i = 0; i < 16; i++) m[i] = 0;
        m[0] = m[5] = m[10] = m[15] = 1;
    }

    static void copy(float[] src, float[] dst) { System.arraycopy(src, 0, dst, 0, 16); }

    static void copy4(float[] src, float[] dst) {
        int n = Math.min(4, src.length);
        for (int i = 0; i < n; i++) dst[i] = src[i];
    }

    /** out = a * b，列主序。 */
    static void multiply(float[] out, float[] a, float[] b) {
        float[] r = new float[16];
        for (int c = 0; c < 4; c++) {
            for (int row = 0; row < 4; row++) {
                float s = 0;
                for (int k = 0; k < 4; k++) {
                    s += a[k * 4 + row] * b[c * 4 + k];
                }
                r[c * 4 + row] = s;
            }
        }
        System.arraycopy(r, 0, out, 0, 16);
    }

    public static String getString(int name) {
        if (!initialized) return "JARJS-GL (not initialized)";
        String s = GLBackend.getString(name);
        return s == null ? "" : s;
    }

    // 顶点数组（client state）
    public static int currentProgram = 0;
    private static java.nio.FloatBuffer vpBuf, cpBuf, tpBuf;
    private static int vpSize = 3, vpStride = 0, cpSize = 4, cpStride = 0, tpSize = 2, tpStride = 0;
    private static boolean vpOn, cpOn, tpOn;

    public static final int VERTEX_ARRAY = 0x8074;
    public static final int NORMAL_ARRAY = 0x8075;
    public static final int COLOR_ARRAY = 0x8076;
    public static final int TEXTURE_COORD_ARRAY = 0x8078;

    public static void clientState(int cap, boolean on) {
        switch (cap) {
            case VERTEX_ARRAY: vpOn = on; break;
            case COLOR_ARRAY: cpOn = on; break;
            case TEXTURE_COORD_ARRAY: tpOn = on; break;
            default: break;
        }
    }

    public static void vertexPointer(int size, int stride, java.nio.FloatBuffer b) {
        vpSize = size; vpStride = stride; vpBuf = b;
    }

    public static void colorPointer(int size, int stride, java.nio.FloatBuffer b) {
        cpSize = size; cpStride = stride; cpBuf = b;
    }

    public static void texCoordPointer(int size, int stride, java.nio.FloatBuffer b) {
        tpSize = size; tpStride = stride; tpBuf = b;
    }

    public static void normalPointer(int stride, java.nio.FloatBuffer b) { /* 法线由逐顶点光照即时使用 */ }

    public static void drawArrays(int mode, int first, int count) {
        if (count <= 0) return;
        if (currentProgram != 0 || !vpOn) {
            GLBackend.drawArraysNative(mode, first, count);
            return;
        }
        float[] out = new float[count * 9];
        for (int i = 0; i < count; i++) fillVertex(out, i * 9, first + i);
        submitExpanded(mode, out, count);
    }

    public static void drawElements(int mode, int count, int[] idx) {
        if (count <= 0) return;
        float[] out = new float[count * 9];
        for (int i = 0; i < count; i++) fillVertex(out, i * 9, idx[i]);
        submitExpanded(mode, out, count);
    }

    private static void fillVertex(float[] out, int o, int idx) {
        float x = 0, y = 0, z = 0;
        if (vpOn && vpBuf != null) {
            int step = vpStride > 0 ? vpStride / 4 : vpSize;
            int base = vpBuf.position() + idx * step;
            x = vpBuf.get(base);
            if (vpSize > 1) y = vpBuf.get(base + 1);
            if (vpSize > 2) z = vpBuf.get(base + 2);
        }
        float r = cr, g = cg, b = cb, a = ca;
        if (cpOn && cpBuf != null) {
            int step = cpStride > 0 ? cpStride / 4 : cpSize;
            int base = cpBuf.position() + idx * step;
            r = cpBuf.get(base);
            g = cpBuf.get(base + 1);
            b = cpBuf.get(base + 2);
            if (cpSize > 3) a = cpBuf.get(base + 3);
        }
        if (lighting) {
            float[] lit = shade(x, y, z);
            r = lit[0]; g = lit[1]; b = lit[2];
        }
        float u = tu, v = tv;
        if (tpOn && tpBuf != null) {
            int step = tpStride > 0 ? tpStride / 4 : tpSize;
            int base = tpBuf.position() + idx * step;
            u = tpBuf.get(base);
            if (tpSize > 1) v = tpBuf.get(base + 1);
        }
        out[o] = x; out[o + 1] = y; out[o + 2] = z;
        out[o + 3] = r; out[o + 4] = g; out[o + 5] = b; out[o + 6] = a;
        out[o + 7] = u; out[o + 8] = v;
    }

    /** 顶点数组入口：把 QUADS 等展开成 WebGL 认识的基本图元。 */
    private static void submitExpanded(int mode, float[] v, int count) {
        if (mode == QUADS) {
            int quads = count / 4;
            float[] out = new float[quads * 6 * 9];
            int k = 0;
            for (int q = 0; q < quads; q++) {
                int a = q * 4;
                System.arraycopy(v, a * 9, out, k, 9); k += 9;
                System.arraycopy(v, (a + 1) * 9, out, k, 9); k += 9;
                System.arraycopy(v, (a + 2) * 9, out, k, 9); k += 9;
                System.arraycopy(v, a * 9, out, k, 9); k += 9;
                System.arraycopy(v, (a + 2) * 9, out, k, 9); k += 9;
                System.arraycopy(v, (a + 3) * 9, out, k, 9); k += 9;
            }
            submit(TRIANGLES, out, quads * 6);
        } else if (mode == QUAD_STRIP) {
            int quads = (count - 2) / 2;
            float[] out = new float[quads * 6 * 9];
            int k = 0;
            for (int q = 0; q < quads; q++) {
                int a = q * 2;
                int[] tri1 = {a, a + 1, a + 2};
                int[] tri2 = {a + 1, a + 3, a + 2};
                for (int i = 0; i < 3; i++) { System.arraycopy(v, tri1[i] * 9, out, k, 9); k += 9; }
                for (int i = 0; i < 3; i++) { System.arraycopy(v, tri2[i] * 9, out, k, 9); k += 9; }
            }
            submit(TRIANGLES, out, quads * 6);
        } else if (mode == POLYGON) {
            int tris = Math.max(0, count - 2);
            float[] out = new float[tris * 3 * 9];
            int k = 0;
            for (int i = 1; i + 1 < count; i++) {
                System.arraycopy(v, 0, out, k, 9); k += 9;
                System.arraycopy(v, i * 9, out, k, 9); k += 9;
                System.arraycopy(v, (i + 1) * 9, out, k, 9); k += 9;
            }
            submit(TRIANGLES, out, tris * 3);
        } else {
            submit(mode, v, count);
        }
    }

    // ================= 显示列表 =================

    private static int listMode = -1;
    private static int listId = 0;
    private static final java.util.HashMap<Integer, java.util.ArrayList<float[]>> displayLists = new java.util.HashMap<>();
    private static java.util.ArrayList<float[]> recording = null;

    public static void newList(int id, int mode) {
        listId = id;
        listMode = mode;
        recording = new java.util.ArrayList<>();
    }

    public static void endList() {
        if (recording != null) {
            displayLists.put(listId, recording);
            recording = null;
        }
        listMode = -1;
        listId = 0;
    }

    public static void callList(int id) {
        java.util.ArrayList<float[]> l = displayLists.get(id);
        if (l == null) return;
        for (float[] rec : l) {
            int mode = (int) rec[0];
            int count = (int) rec[1];
            int texId = (int) rec[2];
            int len = count * 9;
            float[] verts = new float[len];
            System.arraycopy(rec, 4, verts, 0, len);
            float[] mvp = new float[16];
            System.arraycopy(rec, 4 + len, mvp, 0, 16);
            int flags = 0;
            if (depthTest) flags |= F_DEPTH;
            if (blend) flags |= F_BLEND;
            if (cullFace) flags |= F_CULL;
            if (texId != 0 && textures.get(texId) != null) flags |= F_TEX;
            if (mode == POINTS) flags |= F_POINTS;
            GLBackend.setState(flags);
            GLBackend.draw(mode, verts, count, mvp, texId, flags);
        }
    }

    public static void deleteList(int id) { displayLists.remove(id); }

    public static boolean isRecording() { return recording != null; }

    // ================= 属性栈 =================

    private static final java.util.ArrayList<float[]> attribStack = new java.util.ArrayList<>();

    public static void pushAttrib() {
        attribStack.add(new float[]{
                depthTest ? 1 : 0, blend ? 1 : 0, cullFace ? 1 : 0, lighting ? 1 : 0,
                colorMaterial ? 1 : 0, normalize ? 1 : 0, texture2D ? 1 : 0, smooth ? 1 : 0,
                polygonMode, blendSrc, blendDst, depthFunc, cullMode, frontFace,
                lineWidth, pointSize, boundTex,
                cr, cg, cb, ca
        });
    }

    public static void popAttrib() {
        if (attribStack.isEmpty()) return;
        float[] a = attribStack.remove(attribStack.size() - 1);
        depthTest = a[0] != 0; blend = a[1] != 0; cullFace = a[2] != 0; lighting = a[3] != 0;
        colorMaterial = a[4] != 0; normalize = a[5] != 0; texture2D = a[6] != 0; smooth = a[7] != 0;
        polygonMode = (int) a[8]; blendSrc = (int) a[9]; blendDst = (int) a[10];
        depthFunc = (int) a[11]; cullMode = (int) a[12]; frontFace = (int) a[13];
        lineWidth = a[14]; pointSize = a[15]; boundTex = (int) a[16];
        cr = a[17]; cg = a[18]; cb = a[19]; ca = a[20];
    }

    public static int getError() { return initialized ? GLBackend.getError() : 0; }
}
