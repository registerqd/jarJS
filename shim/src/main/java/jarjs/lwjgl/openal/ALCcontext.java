package jarjs.lwjgl.openal;

/** org.lwjgl.openal.ALCcontext 兼容层。 */
public class ALCcontext {
    private final long handle;
    public ALCcontext(long handle) { this.handle = handle; }
    public long getHandle() { return handle; }
}
