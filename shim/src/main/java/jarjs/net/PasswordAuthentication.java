package jarjs.net;

/** java.net.PasswordAuthentication 兼容层。 */
public final class PasswordAuthentication {
    private final String userName;
    private final char[] password;

    public PasswordAuthentication(String userName, char[] password) {
        this.userName = userName;
        this.password = password == null ? new char[0] : password.clone();
    }

    public String getUserName() { return userName; }
    public char[] getPassword() { return password.clone(); }
}
