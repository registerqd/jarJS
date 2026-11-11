package jarjs.awt.event;

public class KeyEvent {
    public static final int VK_LEFT = 37;
    public static final int VK_UP = 38;
    public static final int VK_RIGHT = 39;
    public static final int VK_DOWN = 40;
    public static final int VK_SPACE = 32;
    public static final int VK_ENTER = 13;
    public static final int VK_ESCAPE = 27;
    public static final int VK_A = 65;
    public static final int VK_D = 68;
    public static final int VK_P = 80;
    public static final int VK_R = 82;
    public static final int VK_S = 83;
    public static final int VK_W = 87;

    private final int code;
    private final String key;

    public KeyEvent(int code, String key) {
        this.code = code;
        this.key = key;
    }

    public int getKeyCode() { return code; }
    public String getKeyChar() { return key; }
}
