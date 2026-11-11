package jarjs.net;

/**
 * java.net.Authenticator 兼容层。
 */
public abstract class Authenticator {

    public static void setDefault(Authenticator a) {}

    public static Authenticator getDefault() { return null; }

    protected PasswordAuthentication getPasswordAuthentication() { return null; }

    protected final String getRequestingHost() { return ""; }
    protected final int getRequestingPort() { return 0; }
    protected final String getRequestingPrompt() { return ""; }
    protected final String getRequestingProtocol() { return ""; }
    protected final String getRequestingScheme() { return ""; }
}
