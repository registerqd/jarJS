package jarjs.net;

/** java.net.InetSocketAddress / SocketAddress 兼容层。 */
public class InetSocketAddress {

    private final InetAddress address;
    private final String hostname;
    private final int port;

    public InetSocketAddress(int port) {
        this((InetAddress) null, port);
    }

    public InetSocketAddress(String hostname, int port) {
        this.hostname = hostname;
        this.address = InetAddress.getByName(hostname);
        this.port = port;
    }

    public InetSocketAddress(InetAddress addr, int port) {
        this.address = addr;
        this.hostname = addr == null ? "" : addr.getHostName();
        this.port = port;
    }

    public static InetSocketAddress createUnresolved(String host, int port) {
        return new InetSocketAddress(host, port);
    }

    public int getPort() { return port; }
    public InetAddress getAddress() { return address; }
    public String getHostName() { return hostname; }
    public String getHostString() { return hostname; }
    public boolean isUnresolved() { return address == null; }

    @Override
    public String toString() { return hostname + ":" + port; }
}
