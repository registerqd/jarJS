package jarjs.lwjgl.opengl;

import java.nio.FloatBuffer;

/**
 * org.lwjgl.opengl.GL11 的兼容层。常量与 OpenGL 1.1 一致，方法全部转发到固定管线核心。
 */
public final class GL11 {

    private GL11() {}

    // 图元
    public static final int GL_POINTS = 0x0000;
    public static final int GL_LINES = 0x0001;
    public static final int GL_LINE_LOOP = 0x0002;
    public static final int GL_LINE_STRIP = 0x0003;
    public static final int GL_TRIANGLES = 0x0004;
    public static final int GL_TRIANGLE_STRIP = 0x0005;
    public static final int GL_TRIANGLE_FAN = 0x0006;
    public static final int GL_QUADS = 0x0007;
    public static final int GL_QUAD_STRIP = 0x0008;
    public static final int GL_POLYGON = 0x0009;

    // 矩阵
    public static final int GL_MATRIX_MODE = 0x0BA0;
    public static final int GL_MODELVIEW = 0x1700;
    public static final int GL_PROJECTION = 0x1701;
    public static final int GL_TEXTURE = 0x1702;
    public static final int GL_MODELVIEW_MATRIX = 0x0BA6;
    public static final int GL_PROJECTION_MATRIX = 0x0BA7;

    // 清除
    public static final int GL_COLOR_BUFFER_BIT = 0x4000;
    public static final int GL_DEPTH_BUFFER_BIT = 0x0100;
    public static final int GL_STENCIL_BUFFER_BIT = 0x0400;

    // 能力
    public static final int GL_DEPTH_TEST = 0x0B71;
    public static final int GL_BLEND = 0x0BE2;
    public static final int GL_CULL_FACE = 0x0B44;
    public static final int GL_LIGHTING = 0x0B50;
    public static final int GL_COLOR_MATERIAL = 0x0B57;
    public static final int GL_NORMALIZE = 0x0BA1;
    public static final int GL_TEXTURE_2D = 0x0DE1;
    public static final int GL_ALPHA_TEST = 0x0BC0;
    public static final int GL_LINE_SMOOTH = 0x0B20;
    public static final int GL_POLYGON_SMOOTH = 0x0B41;
    public static final int GL_SCISSOR_TEST = 0x0C11;
    public static final int GL_FOG = 0x0B60;
    public static final int GL_DITHER = 0x0BD0;
    public static final int GL_AUTO_NORMAL = 0x0D80;

    // 光照
    public static final int GL_LIGHT0 = 0x4000;
    public static final int GL_LIGHT1 = 0x4001;
    public static final int GL_LIGHT2 = 0x4002;
    public static final int GL_LIGHT3 = 0x4003;
    public static final int GL_LIGHT4 = 0x4004;
    public static final int GL_LIGHT5 = 0x4005;
    public static final int GL_LIGHT6 = 0x4006;
    public static final int GL_LIGHT7 = 0x4007;
    public static final int GL_AMBIENT = 0x1200;
    public static final int GL_DIFFUSE = 0x1201;
    public static final int GL_SPECULAR = 0x1202;
    public static final int GL_POSITION = 0x1203;
    public static final int GL_SPOT_DIRECTION = 0x1204;
    public static final int GL_SPOT_EXPONENT = 0x1205;
    public static final int GL_SPOT_CUTOFF = 0x1206;
    public static final int GL_CONSTANT_ATTENUATION = 0x1207;
    public static final int GL_LINEAR_ATTENUATION = 0x1208;
    public static final int GL_QUADRATIC_ATTENUATION = 0x1209;
    public static final int GL_EMISSION = 0x1600;
    public static final int GL_SHININESS = 0x1601;
    public static final int GL_AMBIENT_AND_DIFFUSE = 0x1602;
    public static final int GL_LIGHT_MODEL_AMBIENT = 0x0B53;
    public static final int GL_LIGHT_MODEL_TWO_SIDE = 0x0B52;
    public static final int GL_FRONT = 0x0404;
    public static final int GL_BACK = 0x0405;
    public static final int GL_FRONT_AND_BACK = 0x0408;

    // 纹理
    public static final int GL_TEXTURE_MAG_FILTER = 0x2800;
    public static final int GL_TEXTURE_MIN_FILTER = 0x2801;
    public static final int GL_TEXTURE_WRAP_S = 0x2802;
    public static final int GL_TEXTURE_WRAP_T = 0x2803;
    public static final int GL_NEAREST = 0x2600;
    public static final int GL_LINEAR = 0x2601;
    public static final int GL_NEAREST_MIPMAP_NEAREST = 0x2700;
    public static final int GL_LINEAR_MIPMAP_NEAREST = 0x2701;
    public static final int GL_NEAREST_MIPMAP_LINEAR = 0x2702;
    public static final int GL_LINEAR_MIPMAP_LINEAR = 0x2703;
    public static final int GL_REPEAT = 0x2901;
    public static final int GL_CLAMP = 0x2900;
    public static final int GL_CLAMP_TO_EDGE = 0x812F;
    public static final int GL_RGBA = 0x1908;
    public static final int GL_RGB = 0x1907;
    public static final int GL_BGRA = 0x80E1;
    public static final int GL_BGR = 0x80E0;
    public static final int GL_LUMINANCE = 0x1909;
    public static final int GL_LUMINANCE_ALPHA = 0x190A;
    public static final int GL_ALPHA = 0x1906;
    public static final int GL_UNSIGNED_BYTE = 0x1401;
    public static final int GL_UNSIGNED_SHORT = 0x1403;
    public static final int GL_FLOAT = 0x1406;
    public static final int GL_TEXTURE_ENV = 0x2300;
    public static final int GL_TEXTURE_ENV_MODE = 0x2200;
    public static final int GL_MODULATE = 0x2100;
    public static final int GL_DECAL = 0x2101;
    public static final int GL_REPLACE = 0x1E01;
    public static final int GL_COMBINE = 0x8570;
    public static final int GL_TEXTURE_WIDTH = 0x1000;
    public static final int GL_TEXTURE_HEIGHT = 0x1001;

    // 混合
    public static final int GL_ZERO = 0;
    public static final int GL_ONE = 1;
    public static final int GL_SRC_COLOR = 0x0300;
    public static final int GL_ONE_MINUS_SRC_COLOR = 0x0301;
    public static final int GL_SRC_ALPHA = 0x0302;
    public static final int GL_ONE_MINUS_SRC_ALPHA = 0x0303;
    public static final int GL_DST_ALPHA = 0x0304;
    public static final int GL_ONE_MINUS_DST_ALPHA = 0x0305;
    public static final int GL_DST_COLOR = 0x0306;
    public static final int GL_ONE_MINUS_DST_COLOR = 0x0307;

    // 深度 / 面 / 着色
    public static final int GL_NEVER = 0x0200;
    public static final int GL_LESS = 0x0201;
    public static final int GL_EQUAL = 0x0202;
    public static final int GL_LEQUAL = 0x0203;
    public static final int GL_GREATER = 0x0204;
    public static final int GL_NOTEQUAL = 0x0205;
    public static final int GL_GEQUAL = 0x0206;
    public static final int GL_ALWAYS = 0x0207;
    public static final int GL_CW = 0x0900;
    public static final int GL_CCW = 0x0901;
    public static final int GL_FRONT_FACE = 0x0B46;
    public static final int GL_CULL_FACE_MODE = 0x0B45;
    public static final int GL_SMOOTH = 0x1D01;
    public static final int GL_FLAT = 0x1D00;
    public static final int GL_SHADE_MODEL = 0x0B54;
    public static final int GL_FILL = 0x1B02;
    public static final int GL_LINE = 0x1B01;
    public static final int GL_POINT = 0x1B00;
    public static final int GL_POLYGON_MODE = 0x0B40;

    // 字符串 / 错误
    public static final int GL_VENDOR = 0x1F00;
    public static final int GL_RENDERER = 0x1F01;
    public static final int GL_VERSION = 0x1F02;
    public static final int GL_EXTENSIONS = 0x1F03;
    public static final int GL_NO_ERROR = 0;
    public static final int GL_INVALID_ENUM = 0x0500;
    public static final int GL_INVALID_VALUE = 0x0501;
    public static final int GL_INVALID_OPERATION = 0x0502;
    public static final int GL_OUT_OF_MEMORY = 0x0505;

    // 提示
    public static final int GL_PERSPECTIVE_CORRECTION_HINT = 0x0C50;
    public static final int GL_POINT_SMOOTH_HINT = 0x0C51;
    public static final int GL_LINE_SMOOTH_HINT = 0x0C52;
    public static final int GL_POLYGON_SMOOTH_HINT = 0x0C53;
    public static final int GL_FOG_HINT = 0x0C54;
    public static final int GL_DONT_CARE = 0x1100;
    public static final int GL_FASTEST = 0x1101;
    public static final int GL_NICEST = 0x1102;

    public static final int GL_UNPACK_ALIGNMENT = 0x0CF5;
    public static final int GL_PACK_ALIGNMENT = 0x0D05;

    // ================= 立即模式 =================

    public static void glBegin(int mode) { GL.begin(mode); }
    public static void glEnd() { GL.end(); }

    public static void glVertex2f(float x, float y) { GL.vertex(x, y, 0); }
    public static void glVertex2d(double x, double y) { GL.vertex((float) x, (float) y, 0); }
    public static void glVertex3f(float x, float y, float z) { GL.vertex(x, y, z); }
    public static void glVertex3d(double x, double y, double z) { GL.vertex((float) x, (float) y, (float) z); }
    public static void glVertex4f(float x, float y, float z, float w) { GL.vertex(x / w, y / w, z / w); }
    public static void glVertex3fv(float[] v) { GL.vertex(v[0], v[1], v[2]); }
    public static void glVertex3fv(FloatBuffer v) { GL.vertex(v.get(v.position()), v.get(v.position() + 1), v.get(v.position() + 2)); }

    public static void glColor3f(float r, float g, float b) { GL.color(r, g, b, 1); }
    public static void glColor4f(float r, float g, float b, float a) { GL.color(r, g, b, a); }
    public static void glColor3d(double r, double g, double b) { GL.color((float) r, (float) g, (float) b, 1); }
    public static void glColor4d(double r, double g, double b, double a) { GL.color((float) r, (float) g, (float) b, (float) a); }
    public static void glColor3ub(int r, int g, int b) { GL.color((r & 255) / 255f, (g & 255) / 255f, (b & 255) / 255f, 1); }
    public static void glColor4ub(int r, int g, int b, int a) { GL.color((r & 255) / 255f, (g & 255) / 255f, (b & 255) / 255f, (a & 255) / 255f); }
    public static void glColor3fv(float[] v) { GL.color(v[0], v[1], v[2], 1); }
    public static void glColor4fv(float[] v) { GL.color(v[0], v[1], v[2], v[3]); }
    public static void glColor3fv(FloatBuffer v) { int p = v.position(); GL.color(v.get(p), v.get(p + 1), v.get(p + 2), 1); }
    public static void glColor4fv(FloatBuffer v) { int p = v.position(); GL.color(v.get(p), v.get(p + 1), v.get(p + 2), v.get(p + 3)); }

    public static void glNormal3f(float x, float y, float z) { GL.normal(x, y, z); }
    public static void glNormal3d(double x, double y, double z) { GL.normal((float) x, (float) y, (float) z); }
    public static void glNormal3fv(float[] v) { GL.normal(v[0], v[1], v[2]); }

    public static void glTexCoord2f(float u, float v) { GL.texCoord(u, v); }
    public static void glTexCoord2d(double u, double v) { GL.texCoord((float) u, (float) v); }
    public static void glTexCoord2f(float u, float v, float w) { GL.texCoord(u, v); }
    public static void glTexCoord2fv(float[] v) { GL.texCoord(v[0], v[1]); }

    // ================= 矩阵 =================

    public static void glMatrixMode(int mode) { GL.matrixMode(mode); }
    public static void glLoadIdentity() { GL.loadIdentity(); }
    public static void glPushMatrix() { GL.pushMatrix(); }
    public static void glPopMatrix() { GL.popMatrix(); }
    public static void glTranslatef(float x, float y, float z) { GL.translate(x, y, z); }
    public static void glTranslated(double x, double y, double z) { GL.translate((float) x, (float) y, (float) z); }
    public static void glScalef(float x, float y, float z) { GL.scale(x, y, z); }
    public static void glScaled(double x, double y, double z) { GL.scale((float) x, (float) y, (float) z); }
    public static void glRotatef(float a, float x, float y, float z) { GL.rotate(a, x, y, z); }
    public static void glRotated(double a, double x, double y, double z) { GL.rotate((float) a, (float) x, (float) y, (float) z); }
    public static void glMultMatrixf(float[] m) { GL.multMatrix(m); }
    public static void glMultMatrixf(FloatBuffer m) { GL.multMatrix(read16(m)); }
    public static void glLoadMatrixf(float[] m) { GL.loadMatrix(m); }
    public static void glLoadMatrixf(FloatBuffer m) { GL.loadMatrix(read16(m)); }
    public static void glOrtho(double l, double r, double b, double t, double n, double f) {
        GL.ortho((float) l, (float) r, (float) b, (float) t, (float) n, (float) f);
    }
    public static void glFrustum(double l, double r, double b, double t, double n, double f) {
        GL.frustum((float) l, (float) r, (float) b, (float) t, (float) n, (float) f);
    }

    private static float[] read16(FloatBuffer b) {
        float[] m = new float[16];
        int p = b.position();
        for (int i = 0; i < 16; i++) m[i] = b.get(p + i);
        return m;
    }

    public static void glGetFloatv(int pname, float[] out) {
        float[] m = GL.matrix(pname);
        if (m != null) System.arraycopy(m, 0, out, 0, Math.min(16, out.length));
    }

    public static void glGetFloatv(int pname, FloatBuffer out) {
        float[] m = GL.matrix(pname);
        if (m == null) return;
        int p = out.position();
        for (int i = 0; i < 16; i++) out.put(p + i, m[i]);
    }

    // ================= 状态 =================

    public static void glEnable(int cap) { GL.enable(cap); }
    public static void glDisable(int cap) { GL.disable(cap); }
    public static void glClear(int mask) { GL.clear(mask); }
    public static void glClearColor(float r, float g, float b, float a) { GL.clearColor(r, g, b, a); }
    public static void glClearDepth(double d) {}
    public static void glViewport(int x, int y, int w, int h) { GL.viewport(x, y, w, h); }
    public static void glBlendFunc(int s, int d) { GL.blendFunc(s, d); }
    public static void glDepthFunc(int f) { GL.depthFunc(f); }
    public static void glDepthMask(boolean m) { GL.depthMask(m); }
    public static void glCullFace(int m) { GL.cullFace(m); }
    public static void glFrontFace(int m) { GL.frontFace(m); }
    public static void glShadeModel(int m) { GL.shadeModel(m); }
    public static void glPolygonMode(int face, int mode) { GL.polygonMode(face, mode); }
    public static void glLineWidth(float w) { GL.lineWidthSet(w); }
    public static void glPointSize(float s) { GL.pointSizeSet(s); }
    public static void glAlphaFunc(int f, float ref) { GL.alphaFunc(f, ref); }
    public static void glHint(int target, int mode) {}
    public static void glPixelStorei(int pname, int param) {}
    public static void glScissor(int x, int y, int w, int h) {}
    public static void glFlush() {}
    public static void glFinish() {}
    public static void glClearStencil(int s) {}
    public static void glColorMask(boolean r, boolean g, boolean b, boolean a) {}

    // ================= 光照 / 材质 =================

    public static void glEnableLight(int light) { GL.lightEnable(light, true); }
    public static void glLight(int light, int pname, float[] v) { GL.light(light, pname, v); }
    public static void glLightfv(int light, int pname, float[] v) { GL.light(light, pname, v); }
    public static void glLightfv(int light, int pname, FloatBuffer v) { GL.light(light, pname, read4(v)); }
    public static void glLight(int light, int pname, FloatBuffer v) { GL.light(light, pname, read4(v)); }
    public static void glLightf(int light, int pname, float v) { GL.light(light, pname, new float[]{v}); }
    public static void glLighti(int light, int pname, int v) { GL.light(light, pname, new float[]{v}); }
    public static void glLightModelfv(int pname, float[] v) { GL.lightModel(pname, v); }
    public static void glLightModelfv(int pname, FloatBuffer v) { GL.lightModel(pname, read4(v)); }
    public static void glLightModel(int pname, float[] v) { GL.lightModel(pname, v); }
    public static void glLightModel(int pname, FloatBuffer v) { GL.lightModel(pname, read4(v)); }
    public static void glLightModeli(int pname, int v) { GL.lightModel(pname, new float[]{v}); }
    public static void glMaterial(int face, int pname, float[] v) { GL.material(face, pname, v); }
    public static void glMaterialfv(int face, int pname, float[] v) { GL.material(face, pname, v); }
    public static void glMaterialfv(int face, int pname, FloatBuffer v) { GL.material(face, pname, read4(v)); }
    public static void glMaterial(int face, int pname, FloatBuffer v) { GL.material(face, pname, read4(v)); }
    public static void glMaterialf(int face, int pname, float v) { GL.material(face, pname, new float[]{v}); }
    public static void glColorMaterial(int face, int mode) {}

    private static float[] read4(FloatBuffer b) {
        float[] v = new float[4];
        int p = b.position();
        for (int i = 0; i < 4; i++) v[i] = b.get(p + i);
        return v;
    }

    // ================= 纹理 =================

    public static int glGenTextures() { return GL.genTexture(); }
    public static void glGenTextures(java.nio.IntBuffer ids) {
        for (int i = ids.position(); i < ids.limit(); i++) ids.put(i, GL.genTexture());
    }
    public static void glBindTexture(int target, int id) { GL.bindTexture(id); }
    public static void glDeleteTextures(int id) { GL.deleteTexture(id); }
    public static void glDeleteTextures(java.nio.IntBuffer ids) {
        for (int i = ids.position(); i < ids.limit(); i++) GL.deleteTexture(ids.get(i));
    }
    public static void glTexParameteri(int target, int pname, int value) { GL.texParameter(target, pname, value); }
    public static void glTexParameterf(int target, int pname, float value) { GL.texParameter(target, pname, (int) value); }
    public static void glTexEnvi(int target, int pname, int value) {}
    public static void glTexEnvf(int target, int pname, float value) {}

    public static void glTexImage2D(int target, int level, int internalFormat, int w, int h,
                                    int border, int format, int type, int[] argb) {
        GL.texImage2D(target, level, internalFormat, w, h, border, format, type, argb);
    }

    public static void glTexImage2D(int target, int level, int internalFormat, int w, int h,
                                    int border, int format, int type, java.nio.ByteBuffer data) {
        GL.texImage2D(target, level, internalFormat, w, h, border, format, type, data);
    }

    public static void glTexImage2D(int target, int level, int internalFormat, int w, int h,
                                    int border, int format, int type, java.nio.IntBuffer data) {
        int n = w * h;
        int[] argb = new int[n];
        int p = data.position();
        for (int i = 0; i < n; i++) argb[i] = data.get(p + i);
        GL.texImage2D(target, level, internalFormat, w, h, border, format, type, argb);
    }

    // ================= 查询 =================

    public static String glGetString(int name) { return GL.getString(name); }
    public static int glGetError() { return GL.getError(); }
    public static void glGetIntegerv(int pname, int[] out) {}
    public static void glGetIntegerv(int pname, java.nio.IntBuffer out) {}

    // ================= 顶点数组 =================

    public static final int GL_VERTEX_ARRAY = 0x8074;
    public static final int GL_NORMAL_ARRAY = 0x8075;
    public static final int GL_COLOR_ARRAY = 0x8076;
    public static final int GL_TEXTURE_COORD_ARRAY = 0x8078;

    public static void glEnableClientState(int cap) { GL.clientState(cap, true); }
    public static void glDisableClientState(int cap) { GL.clientState(cap, false); }
    public static void glVertexPointer(int size, int stride, FloatBuffer pointer) { GL.vertexPointer(size, stride, pointer); }
    public static void glColorPointer(int size, int stride, FloatBuffer pointer) { GL.colorPointer(size, stride, pointer); }
    public static void glTexCoordPointer(int size, int stride, FloatBuffer pointer) { GL.texCoordPointer(size, stride, pointer); }
    public static void glNormalPointer(int stride, FloatBuffer pointer) { GL.normalPointer(stride, pointer); }
    public static void glClientActiveTexture(int texture) {}

    public static void glDrawArrays(int mode, int first, int count) { GL.drawArrays(mode, first, count); }

    public static void glDrawElements(int mode, java.nio.IntBuffer indices) {
        int n = indices.remaining();
        int[] idx = new int[n];
        int p = indices.position();
        for (int i = 0; i < n; i++) idx[i] = indices.get(p + i);
        GL.drawElements(mode, n, idx);
    }

    public static void glDrawElements(int mode, int count, int type, long indices) {}

    public static void glDrawElements(int mode, java.nio.ShortBuffer indices) {
        int n = indices.remaining();
        int[] idx = new int[n];
        int p = indices.position();
        for (int i = 0; i < n; i++) idx[i] = indices.get(p + i) & 0xFFFF;
        GL.drawElements(mode, n, idx);
    }

    // ================= 显示列表 =================

    public static final int GL_COMPILE = 0x1300;
    public static final int GL_COMPILE_AND_EXECUTE = 0x1301;

    public static int glGenLists(int range) { return 1; }
    public static void glNewList(int list, int mode) { GL.newList(list, mode); }
    public static void glEndList() { GL.endList(); }
    public static void glCallList(int list) { GL.callList(list); }
    public static void glDeleteLists(int list, int range) { GL.deleteList(list); }

    // ================= 属性栈 =================

    public static void glPushAttrib(int mask) { GL.pushAttrib(); }
    public static void glPopAttrib() { GL.popAttrib(); }
    public static void glPushClientAttrib(int mask) {}
    public static void glPopClientAttrib() {}

    // ================= v0.6 补齐：工业应用常用 GL11 接口 =================

    // 顶点数组指针（Buffer 通用重载，兼容老代码传 Buffer 的写法）
    public static void glVertexPointer(int size, int stride, java.nio.Buffer pointer) {
        if (pointer instanceof FloatBuffer) GL.vertexPointer(size, stride, (FloatBuffer) pointer);
    }

    public static void glColorPointer(int size, int stride, java.nio.Buffer pointer) {
        if (pointer instanceof FloatBuffer) GL.colorPointer(size, stride, (FloatBuffer) pointer);
    }

    public static void glTexCoordPointer(int size, int stride, java.nio.Buffer pointer) {
        if (pointer instanceof FloatBuffer) GL.texCoordPointer(size, stride, (FloatBuffer) pointer);
    }

    public static void glNormalPointer(java.nio.Buffer pointer) {}

    // 雾效（WebGL 无固定管线雾，接受参数不报错）
    public static void glFog(int pname, float[] params) {}
    public static void glFog(int pname, FloatBuffer params) {}
    public static void glFogf(int pname, float param) {}
    public static void glFogi(int pname, int param) {}
    public static void glFogfv(int pname, float[] params) {}
    public static void glFogfv(int pname, FloatBuffer params) {}

    // 状态查询
    public static int glGetInteger(int pname) { return GL.queryInt(pname); }
    public static float glGetFloat(int pname) { return GL.queryFloat(pname); }
    public static void glGetInteger(int pname, java.nio.IntBuffer params) {}
    public static void glGetFloat(int pname, FloatBuffer params) {}
    public static void glGetBoolean(int pname, java.nio.ByteBuffer params) {}

    // 像素读取
    public static void glReadPixels(int x, int y, int width, int height, int format, int type, java.nio.ByteBuffer pixels) {
        GL.readPixels(x, y, width, height, format, type, pixels);
    }

    public static void glGetTexImage(int target, int level, int format, int type, java.nio.ByteBuffer pixels) {}

    // 矩阵
    public static void glMultMatrix(java.nio.FloatBuffer m) { GL.multMatrix(read16(m)); }

    // 纹理坐标生成（自动纹理坐标，接受参数不报错）
    public static void glTexGen(int coord, int pname, float[] params) {}
    public static void glTexGen(int coord, int pname, FloatBuffer params) {}
    public static void glTexGeni(int coord, int pname, int param) {}
    public static void glTexGenf(int coord, int pname, float param) {}

    // 纹理环境
    public static void glTexEnv(int target, int pname, float[] params) {}
    public static void glTexEnv(int target, int pname, FloatBuffer params) {}
    public static void glTexEnvfv(int target, int pname, float[] params) {}
    public static void glTexEnvfv(int target, int pname, FloatBuffer params) {}

    // 多边形偏移 / 逻辑操作
    public static void glPolygonOffset(float factor, float units) {}
    public static void glLogicOp(int opcode) {}
    public static void glPolygonStipple(java.nio.ByteBuffer mask) {}

    // 纹理复制
    public static void glCopyTexSubImage2D(int target, int level, int xoffset, int yoffset,
                                           int x, int y, int width, int height) {}
    public static void glCopyTexImage2D(int target, int level, int internalFormat,
                                        int x, int y, int width, int height, int border) {}

    // 累积缓冲 / 模板（空实现，保证签名完整）
    public static void glClearAccum(float r, float g, float b, float a) {}
    public static void glAccum(int op, float value) {}
    public static void glStencilFunc(int func, int ref, int mask) {}
    public static void glStencilOp(int fail, int zfail, int zpass) {}
    public static void glStencilMask(int mask) {}
}
