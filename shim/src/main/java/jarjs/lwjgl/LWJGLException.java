package jarjs.lwjgl;

/** org.lwjgl.LWJGLException 的兼容层。 */
public class LWJGLException extends Exception {
    public LWJGLException() { super(); }
    public LWJGLException(String msg) { super(msg); }
    public LWJGLException(String msg, Throwable cause) { super(msg, cause); }
    public LWJGLException(Throwable cause) { super(cause); }
}
