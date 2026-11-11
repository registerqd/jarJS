package jarjs.lwjgl.util.glu;

import jarjs.lwjgl.opengl.GL;

/** org.lwjgl.util.glu.GLU 的兼容层。 */
public final class GLU {

    public static final int GLU_INVALID_ENUM = 100900;
    public static final int GLU_INVALID_VALUE = 100901;
    public static final int GLU_OUT_OF_MEMORY = 100902;
    public static final int GLU_INCOMPATIBLE_GL_VERSION = 100903;

    public static final int GLU_SMOOTH = 100000;
    public static final int GLU_FLAT = 100001;
    public static final int GLU_NONE = 100002;

    public static final int GLU_POINT = 100010;
    public static final int GLU_LINE = 100011;
    public static final int GLU_FILL = 100012;
    public static final int GLU_SILHOUETTE = 100013;

    private GLU() {}

    public static void gluPerspective(float fovy, float aspect, float zNear, float zFar) {
        GL.perspective(fovy, aspect, zNear, zFar);
    }

    public static void gluLookAt(float eyeX, float eyeY, float eyeZ,
                                 float centerX, float centerY, float centerZ,
                                 float upX, float upY, float upZ) {
        GL.lookAt(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
    }

    public static void gluOrtho2D(float left, float right, float bottom, float top) {
        GL.ortho(left, right, bottom, top, -1f, 1f);
    }

    public static String gluErrorString(int error) {
        switch (error) {
            case GLU_INVALID_ENUM: return "invalid enumerant";
            case GLU_INVALID_VALUE: return "invalid value";
            case GLU_OUT_OF_MEMORY: return "out of memory";
            case GLU_INCOMPATIBLE_GL_VERSION: return "incompatible gl version";
            default: return "unknown error";
        }
    }
}
