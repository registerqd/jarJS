package jarjs.lwjgl.opengl;

/** org.lwjgl.opengl.ARBVertexShader 兼容层。 */
public final class ARBVertexShader {

    private ARBVertexShader() {}

    public static final int GL_VERTEX_SHADER_ARB = 0x8B31;
    public static final int GL_MAX_VERTEX_UNIFORM_COMPONENTS_ARB = 0x8B4A;

    public static int glCreateShaderObjectARB(int type) { return GL20.glCreateShader(type); }
    public static void glShaderSourceARB(int shader, CharSequence source) { GL20.glShaderSource(shader, source); }
    public static void glCompileShaderARB(int shader) { GL20.glCompileShader(shader); }
    public static void glBindAttribLocationARB(int program, int index, CharSequence name) {
        GL20.glBindAttribLocation(program, index, name);
    }
    public static int glGetAttribLocationARB(int program, CharSequence name) {
        return GL20.glGetAttribLocation(program, name);
    }
}
