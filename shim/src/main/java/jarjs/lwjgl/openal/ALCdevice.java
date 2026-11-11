package jarjs.lwjgl.openal;

/** org.lwjgl.openal.ALCdevice 兼容层。 */
public class ALCdevice {
    private final long handle;
    public ALCdevice(long handle) { this.handle = handle; }
    public long getHandle() { return handle; }
}
