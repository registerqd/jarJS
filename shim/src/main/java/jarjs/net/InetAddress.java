package jarjs.net;

/**
 * java.net.InetAddress 兼容层。
 * 浏览器没有原生 IP 栈，这里提供可解析的占位地址，保证 Socket/Netty 初始化不崩。
 */
public class InetAddress {

    private final String hostName;
    private final byte[] address;

    InetAddress(String hostName, byte[] address) {
        this.hostName = hostName == null ? "" : hostName;
        this.address = address == null ? new byte[]{127, 0, 0, 1} : address;
    }

    public static InetAddress getLocalHost() {
        return new InetAddress("localhost", new byte[]{127, 0, 0, 1});
    }

    public static InetAddress getLoopbackAddress() {
        return new InetAddress("127.0.0.1", new byte[]{127, 0, 0, 1});
    }

    public static InetAddress getByName(String host) {
        return new InetAddress(host, new byte[]{127, 0, 0, 1});
    }

    public static InetAddress[] getAllByName(String host) {
        return new InetAddress[]{getByName(host)};
    }

    public static InetAddress getByAddress(byte[] addr) {
        return new InetAddress("", addr);
    }

    public static InetAddress getByAddress(String host, byte[] addr) {
        return new InetAddress(host, addr);
    }

    public String getHostName() { return hostName; }
    public String getCanonicalHostName() { return hostName; }
    public String getHostAddress() { return "127.0.0.1"; }
    public byte[] getAddress() { return address.clone(); }
    public boolean isLoopbackAddress() { return true; }
    public boolean isAnyLocalAddress() { return false; }
    public boolean isReachable(int timeout) { return true; }

    @Override
    public String toString() { return hostName + "/127.0.0.1"; }
}
