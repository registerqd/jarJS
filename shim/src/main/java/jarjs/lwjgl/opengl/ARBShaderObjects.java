package jarjs.lwjgl.opengl;

/** org.lwjgl.opengl.ARBShaderObjects 兼容层。 */
public final class ARBShaderObjects {

    private ARBShaderObjects() {}

    public static final int GL_PROGRAM_OBJECT_ARB = 0x8B40;
    public static final int GL_OBJECT_COMPILE_STATUS_ARB = 0x8B81;
    public static final int GL_OBJECT_LINK_STATUS_ARB = 0x8B82;

    public static int glCreateShaderObjectARB(int type) { return GL20.glCreateShader(type); }
    public static void glShaderSourceARB(int shader, CharSequence source) { GL20.glShaderSource(shader, source); }
    public static void glCompileShaderARB(int shader) { GL20.glCompileShader(shader); }
    public static int glGetObjectParameteriARB(int obj, int pname) { return GL20.glGetShaderi(obj, pname); }
    public static String glGetInfoLogARB(int obj, int maxLength) { return GL20.glGetShaderInfoLog(obj, maxLength); }
    public static void glDeleteObjectARB(int obj) { GL20.glDeleteShader(obj); }

    public static int glCreateProgramObjectARB() { return GL20.glCreateProgram(); }
    public static void glAttachObjectARB(int program, int shader) { GL20.glAttachShader(program, shader); }
    public static void glLinkProgramARB(int program) { GL20.glLinkProgram(program); }
    public static void glUseProgramObjectARB(int program) { GL20.glUseProgram(program); }
    public static void glDeleteProgramARB(int program) { GL20.glDeleteProgram(program); }
    public static int glGetUniformLocationARB(int program, CharSequence name) {
        return GL20.glGetUniformLocation(program, name);
    }
}
